# DailyAutism — Documento de Contexto, Arquitetura, Auditoria e Roadmap

> **Objetivo deste documento:** Servir como base completa para discussões de planejamento, regras de negócio, arquitetura e auditorias de código (ex.: uso em modo voz/chat no celular longe do PC). Não contém código de implementação — foca em **como o sistema está estruturado hoje**, **o que já foi entregue** e **quais decisões/auditorias precisam ser tomadas**.

---

## Como usar este documento com um agente no celular (Prompt sugerido)

> *"Leia este documento sobre o projeto DailyAutism. Quero que você atue como meu Arquiteto de Software e Product Manager. Hoje não vamos escrever código; quero debater regras de negócio, priorizar auditorias técnicas e planejar os próximos passos (incluindo publicação em produção). Me faça perguntas objetivas uma por vez ou sugira melhorias práticas."*

---

## 1. Visão Geral do Produto

O **DailyAutism** é uma plataforma multi-clínica para gestão de clínicas de atendimento multidisciplinar (especialmente voltadas a terapias para TEA — Terapia Ocupacional, Psicologia, Fonoaudiologia, etc.).

### Perfis de Acesso (RBAC)
1. **`ADMIN` (Global):** Acesso total ao sistema, gerencia clínicas, usuários, papéis e todos os cadastros.
2. **`ADMIN_CLINICA`:** Administrador de uma ou mais clínicas vinculadas; gerencia agenda, pacientes, equipe, tabela de preços, contratos particulares e faturamento de convênios apenas das suas clínicas.
3. **`FUNCIONARIO_CLINICA` / `CLINICA`:** Recepção/operação da clínica vinculada (agenda, pacientes, responsáveis, documentos, operações autorizadas por permissões).
4. **`PROFISSIONAL`:** Terapeuta/médico; visualiza sua agenda, pacientes vinculados/em atendimento e documentos compartilhados.
5. **`RESPONSAVEL`:** Pai, mãe ou tutor legal; acompanha seus dependentes, aprova/rejeita agendamentos pendentes, acompanha saldo de pacotes particulares, confere e assina eletronicamente guias de convênio em PDF e acessa documentos.

---

## 2. Stack Técnica & Infraestrutura Atual

- **Backend:** Java 21, Spring Boot 3.5.4, Spring Data JPA / Hibernate, Spring Security + JWT (`jjwt` 0.12.3), Bean Validation, `springdoc-openapi-starter-webmvc-ui` 2.8.3, **OpenPDF** (`com.github.librepdf:openpdf:2.0.3`) para geração de guias em PDF.
- **Banco de Dados:** MySQL 8.0 (`daily_autism_db`), `hibernate.ddl-auto: update` em `src/main/resources/application.yml`.
- **Frontend Web:** React 18.3.1, TypeScript (~5.7.2), Vite 6.1.0, React Router DOM 6.28.2, Tailwind CSS 3.4.17, Axios 1.7.9 (`VITE_API_URL`), Lucide React, testes com Vitest 3.0.5 + Testing Library.
- **Mobile (em paralelo no repo):** pasta `DApp/` (projeto Android/Gradle).
- **Containerização local atual:**
  - `Dockerfile` multi-stage para o backend (`maven:3.9.6-eclipse-temurin-21` -> `eclipse-temurin:21-jre`, porta `8080`).
  - `docker-compose.yml` com `database` (`mysql:8.0`, porta `3306`, volume `mysql_data`) e `app` (`java_backend`, porta `8080`).
  - Frontend roda localmente via Vite (`localhost:5173`), ainda sem `Dockerfile`/Nginx próprio nem pipeline CI/CD configurada no `.github/workflows`.

---

## 3. Mapa do Domínio e Módulos Implementados

### 3.1 Núcleo de Cadastros e Soft Delete
- **Todas as 30 entidades JPA** herdam `BaseEntity` (`ativo = true`) e usam `@SQLRestriction("ativo = true")`.
- **Exclusões (`DELETE`)** apenas desativam (`ativo = false`) sem apagar registros do banco.
- **Proteção contra `orphanRemoval`:** `PacoteParticular`, `ContratoParticular` e `GuiaFaturamentoConvenio` não usam `orphanRemoval = true`; na edição de pacotes, itens antigos são desativados (`ativo = false`) preservando histórico.
- **Cadastros com índice `UNIQUE` no MySQL e fluxo de reativação:**
  - **5 entidades:** `Clinica` (`cnpj_clinica`), `User` (`email_user`, `id_pessoa`), `Paciente` (`pessoa_paciente`), `Profissional` (`pessoa_prof`) e `Responsavel` (`pessoa_responsavel`).
  - Ao tentar criar registro cujo identificador `UNIQUE` pertence a um registro desativado (`ativo = false`), a API retorna **HTTP `409 Conflict`** (`INACTIVE_RESOURCE_EXISTS` com `resourceId` e `resourceName`) e os modais no frontend oferecem o botão **"Reativar cadastro existente"**.
  - As 5 telas possuem filtro **`Ativos | Inativos`** (`GET .../inativos` via queries nativas) e ação **`Reativar`** (`PATCH .../{id}/reativar`).

### 3.2 Agenda e Aprovação de Sessões
- **Entidades:** `Compromisso`, `TipoCompromisso` (especialidade), `Periodicidade`.
- **Fluxo:** Compromissos podem nascer como `AGUARDANDO_APROVACAO` (ex.: para conferência do `RESPONSAVEL`) ou `AGENDADO`, passando depois para `REALIZADO`, `FALTOU`, `CANCELADO` ou `REJEITADO`.
- **Regras recentes:**
  - Aprovar agendamento com data anterior a hoje ou com guia/limite vencido retorna erro `400` claro orientando o responsável a avisar a clínica.
  - Suporte completo a `PATCH` no CORS (`SecurityConfig`).
  - Replicação recorrente respeitando periodicidade e limite da guia (`PERIODO` ou `QUANTIDADE`).

### 3.3 Pacientes, Planos de Saúde e Documentos
- **Vínculos multi-clínica:** `PacienteClinica`, `ProfissionalClinica`, `ResponsavelClinica`, `FuncionarioClinica`, `ClinicaPlanoSaude`, `PacientePlanoSaude`, `ProfissionalPlanoSaude`, `ProfissionalPaciente`.
- **Controle de guias do paciente:** liberação por período (`dataInicio`/`vencimento`) ou por quantidade de sessões (`quantidadeSessoesLiberadas`), com cálculo de previsão de término no modal.
- **Documentos (`Arquivo` / `TipoArquivo`):** upload multipart (até 20MB/25MB) e compartilhamento com profissionais.

### 3.4 Módulo Financeiro (Etapas 1 e 2 implementadas)
Baseado na spec `docs/superpowers/specs/2026-11-11-modulo-financeiro-design.md`:
- **Etapa 1 — Preços, Pacotes e Particular Pré-Pago:**
  - `TabelaPrecoClinica`: valor por especialidade (`TipoCompromisso`) por clínica — particular (`planoSaude == null`) ou repasse negociado por convênio.
  - `PacoteParticular` & `PacoteParticularItem`: pacotes/combos com desconto (ex.: 10 sessões ou combo mensal `4 TO + 2 Psico`).
  - `ContratoParticular`, `ContratoParticularItem`, `MovimentacaoContratoParticular`: contratação pré-paga por sessões ou período, congelando valores na contratação, com **abatimento automático** ao marcar `Compromisso` como `REALIZADO` (e estorno ao reverter) e extrato visível ao responsável.
- **Etapa 2 — Faturamento de Convênios, PDF e Assinatura Eletrônica:**
  - `ClinicaPlanoSaude` com `diaCorteEnvio` e `prazoPagamentoDias`.
  - `GuiaFaturamentoConvenio` & `GuiaFaturamentoItem`: consolidação de sessões `REALIZADO` não faturadas por paciente/convênio/competência.
  - Geração de PDF no backend com **OpenPDF** (`GuiaPdfService`), com hash verificador e carimbo de assinatura.
  - Fluxo de liberação para o `RESPONSAVEL`, alerta no `Dashboard`, conferência e **assinatura eletrônica própria no app/web** (validando senha e registrando data/hora, usuário, CPF, IP e hash), além de controle de envio ao convênio, previsão de recebimento e baixa de pagamento.

---

## 4. Mapa de Rotas Frontend & Endpoints Backend

### Telas Web (`frontend/src/App.tsx`)
- `/login` — Autenticação JWT
- `/admin` — `Dashboard` (visão adaptada por perfil, alertas de aprovações e guias pendentes)
- `/admin/agenda` — `Agenda` (filtros por busca, profissional, status, período, aprovar/rejeitar/status)
- `/admin/pacientes` — `Pacientes` (com filtro Ativos/Inativos)
- `/admin/responsaveis` — `Responsaveis` (com filtro Ativos/Inativos)
- `/admin/profissionais` — `Profissionais` (com filtro Ativos/Inativos)
- `/admin/usuarios` — `Usuarios` (Admin global, filtro Ativos/Inativos)
- `/admin/clinicas` — `Clinicas` (Admin global, filtro Ativos/Inativos)
- `/admin/planos` — `PlanosSaude`
- `/admin/periodicidades` — `Periodicidades`
- `/admin/arquivos` — `Arquivos`
- `/admin/financeiro` — `Financeiro` (Tabela de preços/pacotes/prazos, Contratos particulares, Guias de convênio)

### Controllers REST (`src/main/java/.../infrastructure/controller`)
- `AuthRestResource` (`/auth/login`, `/auth/register`)
- `ClinicaRestResource` (`/clinicas`, `/clinicas/inativos`, `/clinicas/{id}/reativar`)
- `UserRestResource` (`/users`, `/users/me`, `/users/inativos`, `/users/{id}/reativar`)
- `PacienteRestResource` (`/pacientes`, `/pacientes/completo`, `/pacientes/inativos`, `/pacientes/{id}/reativar`)
- `ProfissionalRestResource` (`/profissionais`, `/profissionais/me`, `/profissionais/inativos`, `/profissionais/{id}/reativar`)
- `ResponsavelRestResource` (`/responsaveis`, `/responsaveis/me`, `/responsaveis/inativos`, `/responsaveis/{id}/reativar`)
- `CompromissoRestResource` (`/compromissos`, `/{id}/aprovar`, `/{id}/rejeitar`, `/{id}/status`) & `AgendaRestResource`
- `FinanceiroConfigRestResource`, `ContratoParticularRestResource`, `FaturamentoConvenioRestResource`
- `ArquivoRestResource`, `TipoArquivoRestResource`, `TipoCompromissoRestResource`, `PeriodicidadeRestResource`, `PlanoSaudeRestResource`, `PessoaRestResource`
- Vínculos: `ClinicaPlanoSaudeRestResource`, `PacienteClinicaRestResource`, `PacientePlanoSaudeRestResource`, `ProfissionalClinicaRestResource`, `ProfissionalPacienteRestResource`, `ProfissionalPlanoSaudeRestResource`

---

## 5. Roadmap Futuro (Ideias & Próximos Módulos)

1. **Refinamento Financeiro (V2):**
   - **Repasse aos profissionais:** regra de comissão/valor pago por atendimento ao terapeuta.
   - **Regra de falta por antecedência:** se avisar com X horas/dias de antecedência não cobra/não abate; em cima da hora cobra integral ou parcial.
   - **Assinatura gov.br:** avaliar após validação com os primeiros convênios se exigem assinatura ICP-Brasil/gov.br além da assinatura eletrônica própria com hash + senha.
   - **NFSe / Cobrança automática (PIX/Boleto):** integração futura com gateway.
2. **Notificações ativas:** WhatsApp / E-mail / Push (no `DApp` Android) para lembrar aprovações de agenda e guias liberadas para assinatura.
3. **App Móvel (`DApp/`):** alinhamento de contratos de API com o fluxo de responsáveis/profissionais.
4. **Publicação em Produção (CI/CD + Infra):** pipeline automatizada para build, testes e deploy de backend, banco e frontend.

---

## 6. Guia de Auditorias de Código & Pontos para Debater Longe do PC

Use esta lista para discutir melhorias antes de mexer no código:

1. **Auditoria de Configuração de Produção (`application.yml` vs `application-prod.yml`):**
   - Hoje `application.yml` tem `hibernate.ddl-auto: update` e URL/usuário/senha de banco locais (`jdbc:mysql://database:3306/daily_autism_db`, `rafael`/`teste123`).
   - *Debate:* Quando migrar para **Flyway ou Liquibase** com `ddl-auto: validate`? Quais variáveis de ambiente precisam ser externalizadas (`SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`)?

2. **Auditoria de CORS e Segurança (`SecurityConfig`):**
   - Hoje `setAllowedOrigins` aceita apenas `http://localhost:5173` e `http://127.0.0.1:5173`.
   - *Debate:* Parametrizar origens permitidas via variável de ambiente para o domínio de produção; revisar tempo de expiração do JWT, política de troca de senha inicial (atualmente `Daily@...` ou prefixo de CPF em cadastros automáticos) e proteção de endpoints públicos (`/swagger-ui`, `/v3/api-docs` devem ficar fechados em produção?).

3. **Auditoria de Persistência de Arquivos (`Arquivo` e PDFs):**
   - *Status da Fase 1 (Concluída em 12/11/2026):*
     - Entidade `Arquivo` e tabela `arquivo` atualizadas com colunas para nuvem:
       - `tipo_armazenamento` (`BANCO_BASE64` vs `OCI_OBJECT_STORAGE`, padrão atual: `BANCO_BASE64`).
       - `object_key` (`VARCHAR(500)`) para identificar o arquivo dentro do bucket da nuvem.
       - `tamanho_bytes` (`BIGINT`) calculado automaticamente no upload em Base64 ou enviado pelo client.
       - `url_arquivo` expandido para `VARCHAR(1000)` para suportar URLs assinadas longas.
     - Backend continua salvando no MySQL até que a infraestrutura da OCI esteja configurada e testada.
   - *Fase 2 (Antes de subir para Produção na OCI):*
     - Ativação do `StorageService` S3-compatible para OCI Object Storage (bucket privado, URLs pré-assinadas temporárias / PAR com expiração de 10-15min).

4. **Auditoria de Soft Delete Residual (`@ManyToMany` e Auditoria de Alterações):**
   - Todas as 30 entidades usam `ativo`, mas as 4 tabelas de junção puras (`acao_perfil`, `arquivo_profissional`, `responsavel_paciente`, `user_perfil`) ainda são atualizadas fisicamente ao editar vínculos.
   - *Debate:* Vale adicionar trilha de auditoria (`created_at`, `updated_at`, `created_by`, `updated_by` via Spring Data JPA Auditing) nas entidades críticas (Agenda, Financeiro, Prontuário/Arquivos) para saber *quem* alterou/desativou cada registro?

5. **Auditoria de Performance & Escalabilidade (Paginação e N+1):**
   - Hoje listagens como `Compromisso`, `Paciente`, `User` retornam listas completas (`findAll()`). Alguns filtros (ex.: inativos por clínica) iteram vínculos em memória.
   - *Debate:* Quais telas vão crescer mais rápido no primeiro ano de operação (provavelmente `Agenda` e `Financeiro`) e devem ganhar paginação no servidor (`Pageable`) e filtros por mês/intervalo de datas primeiro?

6. **Auditoria de LGPD & Backups:**
   - O sistema lida com dados sensíveis de saúde e de menores de idade (pacientes autistas).
   - *Debate:* Rotina automática de backup diário do MySQL com retenção e teste de restauração, HTTPS obrigatório, mascaramento de dados em logs e termo de aceite/consentimento no portal do responsável.

7. **Auditoria de Pipeline de Deploy (Back + Front):**
   - *Debate de Topologia:*
     - **Opção Oracle Cloud (OCI):** VM Compute Always Free ou Ampere A1 + MySQL Database Service/Docker + OCI Object Storage S3-compatible + Nginx/Traefik com SSL Let's Encrypt.
     - **Opção VPS clássica (Hetzner / DigitalOcean / AWS Lightsail):** Docker Compose simples com Caddy/Nginx com HTTPS automático.
   - *Checklist de Passos da Pipeline (Antes de subir para Produção):*
     1. **Step Pré-Deploy OCI Storage:**
        - Criar Bucket privado no OCI Object Storage (com retenção e versionamento opcionais).
        - Gerar *Customer Secret Key* no IAM da OCI para autenticação compatível com S3.
        - Definir variáveis de ambiente no deploy: `OCI_ENDPOINT`, `OCI_BUCKET_NAME`, `OCI_ACCESS_KEY`, `OCI_SECRET_KEY`.
        - Chavear `app.storage.type=OCI_OBJECT_STORAGE` e ativar a implementação S3 no backend.
        - *(Opcional)* Rodar script único de migração para enviar os PDFs antigos salvos em Base64 para o bucket e limpar o `arquivo_base64` no banco.
     2. **Step Build & Testes:**
        - Build do backend com Maven e testes automatizados (`mvn test`).
        - Build do frontend Vite (`pnpm build`).
     3. **Step Container & Deploy:**
        - Publicação de imagem Docker ou deploy direto via runner com reinício seguro (`docker compose up -d`).
        - Validação de healthcheck da API e cert SSL.
