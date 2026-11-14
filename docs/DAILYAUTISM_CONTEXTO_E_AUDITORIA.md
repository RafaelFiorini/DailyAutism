# DailyAutism — Documento de Contexto, Arquitetura, Auditoria e Roadmap

> **Objetivo deste documento:** Servir como base completa para discussões de planejamento, regras de negócio, arquitetura e auditorias de código (ex.: uso em modo voz/chat no celular longe do PC). Não contém código de implementação — foca em **como o sistema está estruturado hoje**, **o que já foi entregue** e **quais decisões/auditorias precisam ser tomadas**.

---

## Como usar este documento com um agente no celular (Prompt sugerido)

> *"Leia este documento sobre o projeto DailyAutism. Quero que você atue como meu Arquiteto de Software e Product Manager. Hoje não vamos escrever código; quero debater regras de negócio, priorizar auditorias técnicas e planejar os próximos passos (incluindo publicação em produção). Me faça perguntas objetivas uma por vez ou sugira melhorias práticas."*

---

## 1. Visão Geral do Produto

O **DailyAutism** é uma plataforma multi-clínica para gestão de clínicas de atendimento multidisciplinar (especialmente voltadas a terapias para TEA — Terapia Ocupacional, Psicologia, Fonoaudiologia, Fisioterapia, Musicoterapia, etc.).

### Perfis de Acesso (RBAC)
1. **`ADMIN` (Global):** Acesso total ao sistema, gerencia clínicas, usuários, papéis e todos os cadastros globais.
2. **`ADMIN_CLINICA`:** Administrador de uma clínica vinculada; gerencia agenda, pacientes, equipe, tabela de preços, contratos particulares, faturamento e remessas de convênios da sua clínica.
3. **`FUNCIONARIO_CLINICA` / `CLINICA`:** Recepção/operação da clínica vinculada (agenda do dia, presença, pacientes, responsáveis, documentos e faturamento).
4. **`PROFISSIONAL`:** Terapeuta/médico; visualiza sua agenda diária ordenada por horário, pacientes vinculados e documentos compartilhados.
5. **`RESPONSAVEL`:** Pai, mãe ou tutor legal; acompanha seus dependentes, aprova/rejeita agendamentos pendentes, acompanha saldo de pacotes particulares, confere e assina eletronicamente guias de convênio em PDF e acessa documentos.

### Usuários de Teste / Referência Configurados
- **Admin Global:** `admin@dailyautism.com` (Senha: `admin123`)
- **Admin Attenzione Clínica:** `admin@attenzione.com.br` (Senha: `admin123`) — Perfil `ADMIN_CLINICA` vinculado à *Attenzione Clínica Integrada*.
- **Admin Clinica Secure:** `admin@securebv.com.br` (Senha: `admin123`) — Perfil `ADMIN_CLINICA` vinculado à *Clinica Secure*.
- **Recepção Attenzione:** `ana@attenzione.com.br` (Senha: `admin123`) — Perfil `FUNCIONARIO_CLINICA`.
- **Terapeuta:** `camila.ribeiro@attenzione.teste.com` (Dra. Camila Ribeiro - Psicologia / ABA).
- **Responsáveis:** `carla.mendes@teste.com`, `patricia.rocha@teste.com`, `fernando.lima@teste.com`, etc.

---

## 2. Stack Técnica & Infraestrutura Atual

- **Backend:** Java 21, Spring Boot 3.5.4, Spring Data JPA / Hibernate, Spring Security + JWT (`jjwt` 0.12.3), Bean Validation, `springdoc-openapi-starter-webmvc-ui` 2.8.3, **OpenPDF** (`com.github.librepdf:openpdf:2.0.3`) para geração de guias em PDF.
- **Banco de Dados:** MySQL 8.0 (`daily_autism_db`), `hibernate.ddl-auto: update` em `src/main/resources/application.yml`.
- **Frontend Web:** React 18.3.1, TypeScript (~5.7.2), Vite 6.1.0, React Router DOM 6.28.2, Tailwind CSS 3.4.17, Axios 1.7.9 (`VITE_API_URL`), Lucide React, testes com Vitest 3.2.7 + Testing Library (119 testes unitários automatizados passando).
- **Mobile (em paralelo no repo):** pasta `DApp/` (projeto Android/Gradle isolado).
- **Containerização local atual:**
  - `Dockerfile` multi-stage para o backend (`maven:3.9.6-eclipse-temurin-21` -> `eclipse-temurin:21-jre`, porta `8080`).
  - `docker-compose.yml` com `database` (`mysql:8.0`, porta `3306`, volume `mysql_data`) e `app` (`java_backend`, porta `8080`).
  - Frontend roda localmente via Vite (`localhost:5173`).
- **Controle de Versão Git:**
  - Repositório sincronizado via SSH com autenticação isolada no host `github.com-pessoal` para o perfil pessoal `rafaelfiorini`.

---

## 3. Mapa do Domínio e Módulos Implementados

### 3.1 Núcleo de Cadastros e Soft Delete
- **Todas as 30 entidades JPA** herdam `BaseEntity` (`ativo = true`) e usam `@SQLRestriction("ativo = true")`.
- **Exclusões (`DELETE`)** apenas desativam (`ativo = false`) sem apagar registros fisicamente do banco de dados.
- **Proteção contra `orphanRemoval`:** `PacoteParticular`, `ContratoParticular` e `GuiaFaturamentoConvenio` preservam histórico desativando itens antigos (`ativo = false`).
- **Cadastros com índice `UNIQUE` no MySQL e fluxo de reativação:**
  - **5 entidades críticas:** `Clinica` (`cnpj_clinica`), `User` (`email_user`, `id_pessoa`), `Paciente` (`pessoa_paciente`), `Profissional` (`pessoa_prof`) e `Responsavel` (`pessoa_responsavel`).
  - Ao tentar criar registro cujo identificador `UNIQUE` pertence a um cadastro desativado, a API retorna **HTTP `409 Conflict`** (`INACTIVE_RESOURCE_EXISTS` com `resourceId` e `resourceName`) e os modais no frontend oferecem o botão **"Reativar cadastro existente"**.
  - As telas possuem filtro **`Ativos | Inativos`** (`GET .../inativos` via queries nativas) e ação **`Reativar`** (`PATCH .../{id}/reativar`).

### 3.2 Agenda, Timeline da Clínica e Prevenção de Conflitos
- **Timeline de Horários no Dashboard da Clínica (`AgendaClinicaTimeline`):**
  - No dashboard de `ADMIN_CLINICA` e `FUNCIONARIO_CLINICA`, a agenda do dia é renderizada agrupada em sequência cronológica de horas (`08:30`, `09:30`, etc.).
  - Caixinhas visuais organizadas lado a lado para cada atendimento simultâneo (mostrando Paciente, Profissional, Especialidade e Sala) com botões de ação rápida de presença (**Realizado** e **Falta**).
- **Validação Estrita de Conflito de Horário por Profissional:**
  - Ao agendar (`POST /compromissos`) ou remarcar (`PUT /compromissos/{id}`), o sistema verifica se o profissional já possui compromisso ativo (`AGENDADO`, `AGUARDANDO_APROVACAO`, `REALIZADO`) no mesmo dia e horário.
  - **Privacidade e Sigilo Multi-Clínica:** Se o profissional já estiver ocupado em outra clínica, o sistema bloqueia com erro `400 Bad Request` emitindo mensagem neutra:
    > *"O profissional já possui um compromisso agendado neste mesmo dia e horário."*  
    Sem revelar em hipótese alguma o nome da outra clínica ou os dados do paciente de terceiros (garantia de sigilo médico e LGPD).
- **Regras de Aprovação e Periodicidade:**
  - Aprovação com data anterior a hoje ou guia vencida retorna erro orientando o responsável.
  - Replicação de sessões recorrentes respeitando limites da guia do convênio ou contrato.

### 3.3 Pacientes, Planos de Saúde e Documentos
- **Vínculos multi-clínica:** `PacienteClinica`, `ProfissionalClinica`, `ResponsavelClinica`, `FuncionarioClinica`, `ClinicaPlanoSaude`, `PacientePlanoSaude`, `ProfissionalPlanoSaude`, `ProfissionalPaciente`.
- **Controle de guias do paciente:** liberação por período (`dataInicio`/`vencimento`) ou por quantidade de sessões (`quantidadeSessoesLiberadas`).
- **Massa de Dados Pronta:** Pacientes com sessões agendadas até janeiro de 2027 com distribuição equilibrada por especialidade, profissional e modalidade (particular vs convênios).

### 3.4 Módulo Financeiro & Gestão de Convênios
- **Particular Pré-Pago & Combos:**
  - `TabelaPrecoClinica`: valores por especialidade (`TipoCompromisso`) por clínica (particular ou repasse de convênio).
  - `PacoteParticular`: combos com desconto (ex.: 10 sessões de TO ou combo multidisciplinar).
  - `ContratoParticular`: contratação pré-paga congelando valores, com **abatimento automático** de sessão ao marcar `Compromisso` como `REALIZADO` (e estorno ao cancelar).
- **Faturamento de Convênios, PDF e Assinatura Eletrônica:**
  - `GuiaFaturamentoConvenio` & `GuiaFaturamentoItem`: consolidação de atendimentos realizados por paciente/plano/competência.
  - PDF gerado via OpenPDF com hash SHA-256 e assinatura eletrônica do responsável no portal/web.
- **Envio de Remessa em Lote por Convênio (`POST /financeiro/guias-convenio/lote/enviar`):**
  - Permite fechar a remessa mensal/quinzenal e despachar todas as guias com status `ASSINADA` de um convênio de uma só vez.
  - Atualiza todas para `ENVIADA_CONVENIO` e calcula a `dataPrevisaoPagamento` em lote.
- **Gestão de Prazos de Faturamento Descentralizada:**
  - Configuração do **Dia de Corte** (envio do lote) e **Prazo de Pagamento** (dias para crédito) disponível tanto no Financeiro quanto diretamente na listagem de **Planos de Saúde** (`/planos-saude`) via `ConfigurarPrazoModal`.
  - Ação de **"Enviar Lote"** disponível diretamente nos cards dos convênios com guias assinadas prontas.

---

## 4. Mapa de Rotas Frontend & Endpoints Backend

### Telas Web (`frontend/src/App.tsx`)
- `/login` — Autenticação JWT
- `/admin` — `Dashboard` (visão adaptada por perfil, com timeline de horas da clínica, KPIs e alertas de aprovações/assinaturas)
- `/admin/agenda` — `Agenda` (filtros por busca, profissional, status, período, aprovar/rejeitar/status)
- `/admin/pacientes` — `Pacientes` (com filtro Ativos/Inativos)
- `/admin/responsaveis` — `Responsaveis` (com filtro Ativos/Inativos)
- `/admin/profissionais` — `Profissionais` (com filtro Ativos/Inativos)
- `/admin/usuarios` — `Usuarios` (Admin global, filtro Ativos/Inativos)
- `/admin/clinicas` — `Clinicas` (Admin global, filtro Ativos/Inativos)
- `/admin/planos` — `PlanosSaude` (Gestão de convênios, configuração de prazos de corte/pagamento e despacho de lotes de guias assinadas)
- `/admin/periodicidades` — `Periodicidades`
- `/admin/arquivos` — `Arquivos`
- `/admin/financeiro` — `Financeiro` (Tabela de preços/pacotes/prazos, Contratos particulares, Guias de convênio e Lotes prontos)

### Principais Controllers REST
- `AuthRestResource` (`/auth/login`, `/auth/register`)
- `ClinicaRestResource` (`/clinicas`, `/clinicas/inativos`, `/clinicas/{id}/reativar`)
- `UserRestResource` (`/users`, `/users/me`, `/users/inativos`, `/users/{id}/reativar`)
- `PacienteRestResource` (`/pacientes`, `/pacientes/completo`, `/pacientes/inativos`, `/pacientes/{id}/reativar`)
- `ProfissionalRestResource` (`/profissionais`, `/profissionais/me`, `/profissionais/inativos`, `/profissionais/{id}/reativar`)
- `ResponsavelRestResource` (`/responsaveis`, `/responsaveis/me`, `/responsaveis/inativos`, `/responsaveis/{id}/reativar`)
- `CompromissoRestResource` (`/compromissos`, `/{id}/aprovar`, `/{id}/rejeitar`, `/{id}/status`) & `AgendaRestResource`
- `FaturamentoConvenioRestResource` (`/financeiro/guias-convenio`, `/{id}/assinar`, `/{id}/enviar`, `/lote/enviar`, `/{id}/pagar`, `/{id}/pdf`)
- `ClinicaPlanoSaudeRestResource` (`/clinica-plano-saude`, `/{idVinculo}/prazos`)
- `FinanceiroConfigRestResource` & `ContratoParticularRestResource`
- `ArquivoRestResource`, `TipoArquivoRestResource`, `TipoCompromissoRestResource`, `PeriodicidadeRestResource`, `PlanoSaudeRestResource`

---

## 5. Roadmap Futuro (Ideias & Próximos Módulos)

1. **Refinamento Financeiro (V2):**
   - **Repasse aos profissionais:** regra de comissão/valor pago por atendimento ao terapeuta.
   - **Regra de falta por antecedência:** cancelamentos com X horas de antecedência não abatem saldo; faltas sem aviso abatem sessão.
   - **Assinatura gov.br:** avaliar após validação com operadoras se exigem integração gov.br além do hash eletrônico interno com senha.
   - **NFSe / Cobrança automática (PIX/Boleto):** integração futura com gateway (Asaas/Iugu/MercadoPago).
2. **Notificações ativas:** WhatsApp / E-mail / Push para lembrar pais sobre aprovações de agenda e guias liberadas para assinatura antes do dia de corte do convênio.
3. **App Móvel (`DApp/`):** sincronização de contratos de API com o fluxo dos responsáveis e profissionais.
4. **Publicação em Produção (CI/CD + Infra):** pipeline automatizada para build, testes e deploy de backend, banco e frontend.

---

## 6. Guia de Auditorias de Código & Pontos para Debater Longe do PC

Use esta lista para discutir melhorias antes de mexer no código:

1. **Auditoria de Configuração de Produção (`application.yml` vs `application-prod.yml`):**
   - Hoje `application.yml` tem `hibernate.ddl-auto: update` e credenciais locais de banco (`rafael`/`teste123`).
   - *Debate:* Migrar para **Flyway ou Liquibase** com `ddl-auto: validate`. Externalizar variáveis (`SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`).

2. **Auditoria de CORS e Segurança (`SecurityConfig`):**
   - Hoje `setAllowedOrigins` aceita `http://localhost:5173`.
   - *Debate:* Parametrizar origens permitidas via variável de ambiente para o domínio web de produção; expiração do JWT e fechamento de endpoints do Swagger em produção.

3. **Auditoria de Persistência de Arquivos (`Arquivo` e PDFs):**
   - *Status da Fase 1 (Concluída):*
     - Entidade `Arquivo` preparada com colunas para nuvem: `tipo_armazenamento` (`BANCO_BASE64` vs `OCI_OBJECT_STORAGE`), `object_key`, `tamanho_bytes`, `url_arquivo`.
     - Backend mantém gravação local no MySQL até validação do bucket na Oracle Cloud.
   - *Fase 2 (Antes de subir para Produção na OCI):*
     - Ativação do bucket no OCI Object Storage via SDK S3-compatible com URLs pré-assinadas (PAR) temporárias.

4. **Auditoria de Soft Delete Residual (`@ManyToMany` e Auditoria de Alterações):**
   - 30 entidades possuem `ativo`, mas tabelas de junção puras ainda têm remoção física de relacionamentos.
   - *Debate:* Adicionar Spring Data JPA Auditing (`created_at`, `updated_at`, `created_by`, `updated_by`) nas entidades críticas para rastreabilidade de ações clínicas e financeiras.

5. **Auditoria de Performance & Escalabilidade (Paginação):**
   - `Agenda` e `Financeiro` crescem com rapidez no primeiro ano de operação.
   - *Debate:* Implementar `Pageable` e filtros por mês/competência diretamente nos endpoints de listagem do Spring Data JPA.

6. **Auditoria de LGPD & Backups:**
   - Sistema manipula prontuários e dados de saúde de menores de idade.
   - *Debate:* Rotina automatizada de backup diário do MySQL, termo de consentimento LGPD no portal dos pais e mascaramento de logs.

7. **Pipeline de Deploy (Back + Front):**
   - *Checklist Pré-Deploy:*
     1. Bucket OCI Object Storage configurado e credenciais IAM geradas.
     2. Build e testes do backend (`mvn test`) e frontend (`pnpm build`).
     3. Containerização e orquestração via Docker Compose com Nginx e certificado Let's Encrypt (HTTPS).
     4. Healthcheck e verificação de conectividade com banco de dados de produção.
