# Plano e Checklist de Revisão Arquitetural — Persistência de Arquivos (DailyAutism)

Este documento organiza a revisão da entidade `Arquivo` e do módulo de armazenamento do **DailyAutism** (Java / Spring Boot / MySQL 8), preparando o sistema para operar em modelo híbrido (MySQL `BLOB` + Object Storage compatível com S3) e suportar migração segura para **Oracle Cloud (OCI) Object Storage**, **Cloudflare R2** ou **AWS S3** sem acoplamento e sem perda de dados.

---

## 1. Diagnóstico do Estado Atual (O que verificar primeiro)

Antes de alterar código ou schema, levante o cenário real da tabela e dos fluxos atuais no repositório.

| Área | O que inspecionar | Risco comum a evitar |
| :--- | :--- | :--- |
| **Schema MySQL** | Tipo exato do `BLOB` (`BLOB` 64 KB, `MEDIUMBLOB` 16 MB, `LONGBLOB` 4 GB), tamanho do `VARCHAR`, nulabilidade e índices | `BLOB` padrão estoura com PDFs/imagens; `VARCHAR(255)` pode truncar chaves longas |
| **Entidade JPA** | Uso de `@Lob`, `FetchType.LAZY` vs `EAGER`, e se o `BLOB` está na mesma tabela dos metadados | `EAGER` carrega megabytes na memória em listagens simples de pacientes/guias |
| **Camada Web / Service** | Upload/download via `byte[]` em memória vs `InputStream` / `StreamingResponseBody` | `OutOfMemoryError` na JVM (especialmente em VPS com RAM limitada) |
| **Endereçamento atual** | O que está gravado hoje no `VARCHAR` (caminho local, URL completa ou vazio) | Salvar URL absoluta com domínio acopla o banco ao provedor atual |
| **Segurança e LGPD** | Como os arquivos de pacientes/guias são servidos e quem tem permissão de download | Expor URLs públicas permanentes de documentos sensíveis sem autenticação |

---

## 2. Decisão Arquitetural: Modelo Híbrido vs. Migração Definitiva

### Estratégia Recomendada
Adotar **arquitetura multi-provedor baseada na linha do banco** (*per-row provider*), usando o modo híbrido como **ponte segura** para migração definitiva ao Object Storage:

1. **Escrita configurável por ambiente:** Uma propriedade no `application.yml` (`app.storage.active-provider=s3` ou `database`) define onde **novos** arquivos serão gravados (útil para rodar `database` ou MinIO/LocalStack em desenvolvimento local e `s3` em produção).
2. **Leitura guiada pelo registro:** Cada linha da tabela `arquivo` informa em qual provedor ela reside. Assim, arquivos antigos no `BLOB` e arquivos novos no S3 convivem sem erro durante e após a transição.
3. **Chave relativa no `VARCHAR`:** Armazenar apenas a **object key** (ex.: `tenants/{tenantId}/pacientes/{pacienteId}/{uuid}.pdf`), nunca a URL completa (`https://...`). A URL ou stream é montado em tempo de execução pelo provedor ativo.

### Evolução Sugerida da Tabela `arquivo`

```sql
ALTER TABLE arquivo
  ADD COLUMN storage_provider VARCHAR(30) NOT NULL DEFAULT 'DATABASE',
  ADD COLUMN object_key       VARCHAR(512) NULL,
  ADD COLUMN content_type     VARCHAR(120) NULL,
  ADD COLUMN tamanho_bytes    BIGINT NULL,
  ADD COLUMN checksum_sha256  CHAR(64) NULL,
  MODIFY COLUMN dados         LONGBLOB NULL;
```

> **Observação:** Se o `VARCHAR` atual já cumpre o papel de `object_key`, apenas garanta que ele suporte pelo menos 512 caracteres e que registros legados não tenham valores inconsistentes antes de reutilizá-lo.

---

## 3. Desacoplamento no Spring Boot (Padrão Strategy / Port & Adapter)

Para evitar acoplamento direto do `ArquivoService` com o MySQL ou com o SDK do S3:

1. **Criar interface `FileStorageProvider`:**
   - `StorageProviderType getType()` (`DATABASE`, `S3`)
   - `StoredObjectInfo save(StorageSaveCommand command)` (recebe `InputStream`, tamanho, `contentType` e `objectKey` sugerida)
   - `InputStreamResource load(Arquivo arquivo)` (ou `StreamingResponseBody` / `InputStream`)
   - `void delete(Arquivo arquivo)`
   - `Optional<String> generatePresignedUrl(Arquivo arquivo, Duration ttl)` (opcional para download direto do S3/R2)
2. **Implementações isoladas:**
   - `DatabaseFileStorageProvider`: lê e grava na coluna `BLOB` (idealmente isolada em uma entidade/tabela filha `arquivo_conteudo` ou via query nativa/projeção para não carregar bytes em consultas de listagem).
   - `S3FileStorageProvider`: usa `S3Client` (AWS SDK v2) configurado com `endpointOverride`, `region`, `credentials` e `forcePathStyle` (necessário para compatibilidade uniforme entre **OCI Object Storage**, **Cloudflare R2**, **AWS S3** e **MinIO**).
3. **Orquestrador (`ArquivoService`):**
   - Injeta `Map<StorageProviderType, FileStorageProvider>` (Spring injeta a lista de beans e você indexa por `getType()`).
   - No **upload**: usa o provedor ativo configurado para o ambiente.
   - No **download/delete**: consulta `arquivo.getStorageProvider()` e roteia para a implementação correspondente.

### Cuidado Crítico com Transações (`@Transactional`)
Nunca execute chamadas de rede para o S3 dentro de uma transação `@Transactional` aberta no MySQL, pois lentidão na rede prenderá conexões do pool (HikariCP):
- **Fluxo seguro de upload:**
  1. Gere a `objectKey` e calcule o `checksum_sha256` (ou faça streaming).
  2. Envie o arquivo para o `S3FileStorageProvider` **fora** da transação de banco.
  3. Persista os metadados na tabela `arquivo` em transação curta.
  4. Se a persistência no MySQL falhar, dispare a exclusão compensatória do objeto no S3 (em bloco `try/catch` com log de erro para limpeza posterior).

---

## 4. Plano de Migração de Dados (Zero-Downtime / Backfill)

Execute a transição em 4 etapas para garantir zero perda de dados:

1. **Etapa 1 — Preparação e Deploy Compatível:**
   - Aplicar a migration (Flyway/Liquibase) adicionando `storage_provider` (default `'DATABASE'`), `checksum_sha256`, `tamanho_bytes`, `content_type` e tornando o `BLOB` anulável.
   - Subir o backend com suporte híbrido de leitura e novos uploads já apontando para `S3` (ou manter `DATABASE` até validar o bucket em produção).
2. **Etapa 2 — Job de Backfill em Lotes:**
   - Criar um `CommandLineRunner`, endpoint administrativo protegido ou `@Scheduled` temporário que processe lotes pequenos (ex.: 50 registros por vez com `storage_provider = 'DATABASE'` e `dados IS NOT NULL`).
   - Para cada registro: calcular SHA-256 do `BLOB` -> fazer upload no S3 com a `objectKey` -> validar sucesso (tamanho/ETag) -> atualizar `storage_provider = 'S3'`, `object_key` e `checksum_sha256` no MySQL.
   - **Regra de segurança:** Não apague o `BLOB` no mesmo passo da cópia; mantenha-o temporariamente como rede de segurança.
3. **Etapa 3 — Auditoria e Validação:**
   - Verificar que não restam registros ativos em `DATABASE` (`SELECT COUNT(*) FROM arquivo WHERE storage_provider = 'DATABASE';`).
   - Testar download de uma amostra aleatória de arquivos migrados (preferencialmente conferindo o SHA-256).
4. **Etapa 4 — Limpeza do Banco e Recuperação de Espaço:**
   - Realizar backup completo (`mysqldump`) antes da limpeza.
   - Zerar o `BLOB` em lotes pequenos para evitar lock na tabela (`UPDATE arquivo SET dados = NULL WHERE storage_provider = 'S3' AND dados IS NOT NULL LIMIT 500;`).
   - Avaliar `OPTIMIZE TABLE arquivo` (em janela de manutenção) para liberar espaço em disco no volume do MySQL.
   - Só remover a coluna `BLOB` definitivamente quando tiver certeza de que nenhum ambiente ou rollback precisa dela.

---

## 5. Checklist Prático de Revisão

### Banco de Dados (MySQL)
- [ ] Inspecionar `SHOW CREATE TABLE arquivo;` e anotar tipo do `BLOB`, tamanho do `VARCHAR` e chaves estrangeiras.
- [ ] Medir volume atual da tabela (`SELECT COUNT(*), ROUND(SUM(LENGTH(dados))/1024/1024, 2) AS mb FROM arquivo;`).
- [ ] Verificar o que está gravado hoje no campo `VARCHAR` (se há dados preenchidos, nulos ou URLs completas).
- [ ] Criar migration (Flyway/Liquibase) adicionando `storage_provider`, `content_type`, `tamanho_bytes`, `checksum_sha256` e tornando `dados` anulável.
- [ ] Garantir índice em `storage_provider` (ou índice composto se houver `tenant_id` / `paciente_id`) para acelerar o backfill.

### Backend (Java / Spring Boot)
- [ ] Auditar a entidade `Arquivo`: garantir que listagens/DTOs não carreguem o campo `BLOB` em memória (`@Lob` com `FetchType.LAZY` ou separação em projeção/tabela auxiliar).
- [ ] Verificar configuração de upload no `application.yml` (`spring.servlet.multipart.max-file-size` e `max-request-size`).
- [ ] Definir interface `FileStorageProvider` e enum `StorageProviderType`.
- [ ] Implementar `DatabaseFileStorageProvider` mantendo compatibilidade com registros legados.
- [ ] Implementar `S3FileStorageProvider` com AWS SDK v2 (`S3Client`), configurável via variáveis de ambiente (`STORAGE_ENDPOINT`, `STORAGE_BUCKET`, `STORAGE_REGION`, `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY`, `STORAGE_PATH_STYLE`).
- [ ] Atualizar `ArquivoService` para usar `active-provider` na escrita e `arquivo.getStorageProvider()` na leitura/exclusão.
- [ ] Garantir que o upload ao S3 ocorra fora de `@Transactional` do banco, com remoção compensatória se o commit falhar.
- [ ] Validar que exclusões (`DELETE`) removem o arquivo tanto do provedor (S3 ou `BLOB`) quanto do registro (ou aplicam *soft delete* consistente).

### Infraestrutura e Provedor S3 (OCI Object Storage / Cloudflare R2 / Local)
- [ ] Configurar bucket privado (bloqueio total de acesso público anônimo) para documentos do DailyAutism.
- [ ] Definir padrão de `object_key` sem dados sensíveis expostos no nome do arquivo (ex.: usar UUID em vez de CPF/nome do paciente na chave).
- [ ] Configurar lifecycle/versionamento ou rotina de backup do bucket, conforme criticidade dos documentos.
- [ ] Configurar MinIO ou LocalStack no `docker-compose.yml` de desenvolvimento para testar a implementação S3 localmente.

### Segurança e LGPD
- [ ] Garantir que o download passe por validação de autorização do usuário autenticado (nunca expor URL pública fixa do bucket).
- [ ] Se usar *Presigned URLs* do S3/R2, definir expiração curta (ex.: 1 a 5 minutos) e gerar a URL apenas após validar permissão no backend.
- [ ] Validar `contentType` e assinatura/extensão permitida no upload (evitar upload de executáveis ou scripts maliciosos).

### Migração e Testes
- [ ] Escrever testes de integração cobrindo upload, download e delete tanto em `DATABASE` quanto em `S3` (via Testcontainers + MinIO/LocalStack).
- [ ] Testar o job de backfill em ambiente de staging/homologação com cópia de dados reais.
- [ ] Executar backfill em produção, validar contagem e checksums antes de limpar os `BLOB`s antigos.
