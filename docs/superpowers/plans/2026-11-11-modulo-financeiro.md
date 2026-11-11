# Módulo Financeiro — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar o Módulo Financeiro multi-clínica do DailyAutism em duas etapas testáveis: (Etapa 1) tabela de preços, pacotes/combos particulares e contratos pré-pagos com abatimento por especialidade; (Etapa 2) faturamento mensal de convênios por comparecimento, geração de guia em PDF e assinatura eletrônica pelo responsável.

**Architecture:** Modelo relacional único no Spring Boot + JPA/MySQL com isolamento explícito por `Clinica` e congelamento de preços no momento da contratação/fechamento. No frontend React, um novo item de menu **Financeiro** (com subvisões adaptadas para `ADMIN_CLINICA`/`FUNCIONARIO_CLINICA` e `RESPONSAVEL`) consome a API REST protegida por permissões em `DataSeeder`, além de avisos no Dashboard quando houver guias liberadas para assinatura.

**Tech Stack:** Java 21, Spring Boot 3.5.4, Spring Data JPA, Spring Security, MySQL 8, OpenPDF (`com.github.librepdf:openpdf`), React 18, TypeScript, Tailwind CSS, Vitest + React Testing Library.

**Spec:** [`docs/superpowers/specs/2026-11-11-modulo-financeiro-design.md`](../specs/2026-11-11-modulo-financeiro-design.md)

## Global Constraints

- Toda regra financeira, preço, pacote, contrato e guia pertence a uma `Clinica` específica; usuários de clínica só enxergam/operam dados das clínicas às quais estão vinculados.
- Alterar um preço em `TabelaPrecoClinica` ou pacote em `PacoteParticular` não pode recalcular contratos particulares já salvos nem guias já geradas.
- Um mesmo `Compromisso` marcado como `REALIZADO` pode gerar no máximo um abatimento ativo em `MovimentacaoContratoParticular` ou compor no máximo uma `GuiaFaturamentoConvenio` não cancelada.
- Marcar um compromisso como `REALIZADO` não é bloqueado por falta de contrato particular; se houver contrato ativo com saldo para aquela especialidade, o abatimento acontece automaticamente.
- A guia de convênio só pode ser marcada como `ENVIADA_CONVENIO` após estar com status `ASSINADA` (ou seja, após a assinatura eletrônica do responsável).
- A assinatura eletrônica exige confirmação de senha do responsável autenticado e grava carimbo verificador + hash SHA-256 no PDF, mantendo o provedor desacoplado (`INTERNO_DAILYAUTISM`, futuro `GOV_BR`).

## Review Focus

- **Imutabilidade de preços históricos:** Editar `TabelaPrecoClinica` ou `PacoteParticular` depois de fechado um contrato ou guia nunca pode mudar `valorCobrado`, `valorUnitarioReferencia` ou `valorUnitarioConvenio` existentes.
- **Idempotência e estorno em `CompromissoService`:** Atualizar um compromisso já `REALIZADO` (ex.: mudar observação ou sala) não pode abater duas vezes do contrato; reverter de `REALIZADO` para `AGENDADO` ou `CANCELADO` deve gerar `ESTORNO` apenas uma vez e reabrir o contrato se ele tinha passado para `CONCLUIDO`.
- **Bloqueio de dupla inclusão em guias de convênio:** Tentar gerar duas guias para o mesmo período não pode faturar duas vezes um `Compromisso` já vinculado a uma guia com status diferente de `CANCELADA`.
- **Isolamento entre clínicas e responsáveis:** Um `ADMIN_CLINICA` da Clínica A não pode consultar, faturar ou cancelar contratos/guias da Clínica B; um `RESPONSAVEL` não pode assinar guia de um paciente ao qual não está vinculado.
- **Proteção de fluxo da guia assinada:** Tentar enviar ao convênio (`ENVIADA_CONVENIO`) uma guia ainda em `RASCUNHO` ou `AGUARDANDO_ASSINATURA`, ou tentar alterar/reassinar uma guia já `ASSINADA`, deve falhar com erro HTTP claro (`400`/`409`).

---

## ETAPA 1 — Preços, Pacotes e Contratos Particulares (Pré-Pago)

### Task 1: Evolução de `ClinicaPlanoSaude`, `Compromisso` e Novas Entidades de Preços/Pacotes

**Files:**
- Modify: `src/main/java/com/clinica/dailyautism/domain/entity/ClinicaPlanoSaude.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/entity/Compromisso.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/CompromissoService.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/TabelaPrecoClinica.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/PacoteParticular.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/PacoteParticularItem.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/TabelaPrecoClinicaRepository.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/PacoteParticularRepository.java`
- Modify: `src/main/java/com/clinica/dailyautism/infrastructure/security/DataSeeder.java`

- [ ] **Step 1:** Atualizar `ClinicaPlanoSaude` com `diaCorteEnvio` (`Integer`) e `prazoPagamentoDias` (`Integer`, default `30`), e adicionar relacionamento `@ManyToOne @JoinColumn(name = "idClinica") private Clinica clinica;` opcional em `Compromisso` (preenchido na criação/atualização quando resolvido pela clínica do usuário/paciente/profissional).
- [ ] **Step 2:** Criar entidades `TabelaPrecoClinica` (com `clinica`, `tipoCompromisso`, `planoSaude` nullable, `valorSessao` `BigDecimal`), `PacoteParticular` e `PacoteParticularItem` estendendo `BaseEntity` com `@SQLRestriction("ativo = true")`.
- [ ] **Step 3:** Criar repositórios JPA com queries filtradas por `idClinica`, `idTipoCompromisso` e `idPlanoSaude` (`isNull` para particular).
- [ ] **Step 4:** Registrar em `DataSeeder` as novas ações (`VER_FINANCEIRO`, `GERENCIAR_PRECOS_CLINICA`, `GERENCIAR_CONTRATO_PARTICULAR`, `GERENCIAR_FATURAMENTO_CONVENIO`, `ASSINAR_GUIA_CONVENIO`) distribuídas nos perfis `ADMIN`, `ADMIN_CLINICA`, `FUNCIONARIO_CLINICA`, `CLINICA` e `RESPONSAVEL`.
- [ ] **Step 5:** Compilar backend (`mvn test-compile`) e verificar criação de schema.

---

### Task 2: API REST de Tabela de Preços, Prazos de Convênio e Pacotes Particulares

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/TabelaPrecoDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/SaveTabelaPrecoDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/PacoteParticularDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/SavePacoteParticularDTO.java`
- Modify: `src/main/java/com/clinica/dailyautism/infrastructure/controller/ClinicaPlanoSaudeRestResource.java` (endpoint para atualizar prazos `diaCorteEnvio`/`prazoPagamentoDias`)
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/TabelaPrecoService.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/PacoteParticularService.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/controller/FinanceiroConfigRestResource.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/aplicationservice/TabelaPrecoServiceTest.java`

- [ ] **Step 1:** Escrever testes unitários em `TabelaPrecoServiceTest` garantindo: (a) unicidade por `(clinica, tipoCompromisso, planoSaude)`, (b) fallback nulo de `planoSaude` representando Particular, (c) cálculo de `valorSemDescontoCalculado` no pacote particular, e (d) isolamento de clínica.
- [ ] **Step 2:** Rodar `mvn test -Dtest=TabelaPrecoServiceTest` e confirmar falha inicial (TDD).
- [ ] **Step 3:** Implementar `TabelaPrecoService`, `PacoteParticularService` e endpoints em `FinanceiroConfigRestResource` (`/financeiro/precos`, `/financeiro/pacotes`, `/financeiro/convenios-prazos`) com `@PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")` e leitura com `VER_FINANCEIRO`.
- [ ] **Step 4:** Rodar `mvn test -Dtest=TabelaPrecoServiceTest` e confirmar sucesso.

---

### Task 3: Entidades, Regras e Abatimento Automático de Contratos Particulares

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/ModalidadeContratoParticular.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/StatusPagamentoContrato.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/StatusContratoParticular.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/TipoMovimentacaoContrato.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/ContratoParticular.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/ContratoParticularItem.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/MovimentacaoContratoParticular.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/ContratoParticularRepository.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/MovimentacaoContratoParticularRepository.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/ContratoParticularService.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/CompromissoService.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/ContratoParticularDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/SaveContratoParticularDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/controller/ContratoParticularRestResource.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/aplicationservice/ContratoParticularServiceTest.java`

- [ ] **Step 1:** Escrever testes em `ContratoParticularServiceTest` cobrindo:
  - Contratação de combo multi-especialidade (ex.: 4 TO + 2 Psico × 2 meses = 8 TO + 4 Psico) congelando `valorUnitarioReferencia`.
  - Abatimento automático quando `Compromisso` muda para `REALIZADO` (abate somente o item daquela especialidade).
  - Idempotência (re-salvar `REALIZADO` não abate duas vezes).
  - Estorno automático ao sair de `REALIZADO` para `CANCELADO`/`AGENDADO` (reabre contrato se estava `CONCLUIDO`).
  - Fechamento automático como `CONCLUIDO` quando todos os itens atingem `quantidadeConsumida == quantidadeContratada`.
  - Não bloqueio caso o paciente não tenha contrato particular com saldo.
- [ ] **Step 2:** Implementar `ContratoParticularService` (`criarContrato`, `registrarPagamento`, `processarAlteracaoStatusCompromisso`, `listarPorClinica`, `listarParaResponsavel`, `cancelarContrato`) e integrar chamada transacional em `CompromissoService` quando o status do compromisso é alterado.
- [ ] **Step 3:** Expor REST controller `/financeiro/contratos-particulares` com filtros por clínica, paciente e visão restrita ao responsável logado (`/meus`).
- [ ] **Step 4:** Rodar testes de backend e validar fluxo ponta a ponta via Docker/API.

---

### Task 4: Frontend — Menu Financeiro, Cadastro de Preços/Pacotes e Contratos Particulares (Etapa 1)

**Files:**
- Create: `frontend/src/types/financeiro.ts`
- Create: `frontend/src/services/financeiroService.ts`
- Create: `frontend/src/pages/Financeiro.tsx`
- Create: `frontend/src/components/TabelaPrecosTab.tsx`
- Create: `frontend/src/components/ContratosParticularesTab.tsx`
- Create: `frontend/src/components/ContratoParticularModal.tsx`
- Modify: `frontend/src/components/AdminLayout.tsx`
- Modify: `frontend/src/App.tsx`
- Test: `frontend/src/pages/Financeiro.test.tsx`
- Test: `frontend/src/services/financeiroService.test.ts`

- [ ] **Step 1:** Criar tipos e `financeiroService.ts` com testes unitários (`financeiroService.test.ts`).
- [ ] **Step 2:** Criar `TabelaPrecosTab.tsx` permitindo ao `ADMIN_CLINICA` configurar valores particulares por especialidade, valores de repasse + prazos por convênio e combos/pacotes com desconto.
- [ ] **Step 3:** Criar `ContratosParticularesTab.tsx` e `ContratoParticularModal.tsx` (suportando escolha de pacote pronto ou montagem de itens por especialidade × meses, registro de pagamento e extrato de sessões abatidas; modo somente leitura para `RESPONSAVEL`).
- [ ] **Step 4:** Adicionar item **Financeiro** no menu lateral de `AdminLayout.tsx` (visível para `ADMIN`, `ADMIN_CLINICA`, `FUNCIONARIO_CLINICA`, `CLINICA` e `RESPONSAVEL`) e rota `/admin/financeiro` em `App.tsx`.
- [ ] **Step 5:** Rodar `pnpm --dir frontend test --run` e `pnpm --dir frontend build` (todos passando) + rebuild Docker backend e teste de integração da Etapa 1.

---

## ETAPA 2 — Faturamento de Convênios, Guia em PDF e Assinatura Eletrônica

### Task 5: Dependência OpenPDF, Entidades de Guia e Serviço de Geração de PDF

**Files:**
- Modify: `pom.xml` (adicionar `com.github.librepdf:openpdf`)
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/StatusGuiaFaturamento.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/ProvedorAssinatura.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/GuiaFaturamentoConvenio.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/GuiaFaturamentoItem.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/GuiaFaturamentoConvenioRepository.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/GuiaFaturamentoItemRepository.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/GuiaPdfService.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/aplicationservice/GuiaPdfServiceTest.java`

- [ ] **Step 1:** Adicionar `openpdf` ao `pom.xml` e criar entidades `GuiaFaturamentoConvenio` e `GuiaFaturamentoItem` com hashes SHA-256, dados da assinatura e campos de envio/previsão de pagamento.
- [ ] **Step 2:** Criar `GuiaPdfService` gerando PDF A4 legível contendo: cabeçalho da clínica, identificação do convênio e paciente (com nº carteirinha/guia), tabela cronológica de sessões (`data/hora`, `especialidade`, `profissional + conselho`, `valor`), valor total e bloco de assinatura (vazio em `RASCUNHO`/`AGUARDANDO_ASSINATURA`, ou preenchido com nome, CPF mascarado/completo conforme padrão interno, data/hora, IP e código verificador em `ASSINADA`).
- [ ] **Step 3:** Escrever teste em `GuiaPdfServiceTest` validando que o PDF gerado abre/contém os bytes válidos (`%PDF-`) e retorna hash SHA-256 distinto entre rascunho e versão assinada.

---

### Task 6: Fluxo de Fechamento, Liberação, Assinatura Eletrônica e Envio ao Convênio

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/AssinaturaEletronicaProvider.java` (interface desacoplada para `INTERNO_DAILYAUTISM` hoje e `GOV_BR` no futuro)
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/FaturamentoConvenioService.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/GuiaFaturamentoDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/GerarGuiaFaturamentoDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/AssinarGuiaDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/controller/FaturamentoConvenioRestResource.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/aplicationservice/FaturamentoConvenioServiceTest.java`

- [ ] **Step 1:** Escrever testes em `FaturamentoConvenioServiceTest` cobrindo:
  - Consolidação de `Compromisso` (`REALIZADO`) no período sem duplicar atendimentos já faturados.
  - Cálculo de valor pela `TabelaPrecoClinica` do convênio na clínica e congelamento em `GuiaFaturamentoItem`.
  - Transição `RASCUNHO` → `AGUARDANDO_ASSINATURA` → `ASSINADA` (validando senha e vínculo do `RESPONSAVEL`).
  - Bloqueio de envio ao convênio se ainda não assinada (`ASSINADA`).
  - Cálculo de `dataPrevisaoPagamento = dataEnvio + prazoPagamentoDias` ao marcar `ENVIADA_CONVENIO` e baixa `PAGA`.
- [ ] **Step 2:** Implementar `FaturamentoConvenioService` e endpoints REST em `/financeiro/guias-convenio` (gerar prévia/guia, listar pendentes de assinatura para o responsável, baixar PDF `/{id}/pdf`, liberar para assinatura, assinar `/{id}/assinar`, enviar ao convênio `/{id}/enviar`, marcar paga `/{id}/pagar`, cancelar `/{id}/cancelar`).
- [ ] **Step 3:** Rodar testes unitários no backend.

---

### Task 7: Frontend — Aba Faturamento de Convênios, Modal de Assinatura e Alerta no Dashboard

**Files:**
- Modify: `frontend/src/types/financeiro.ts`
- Modify: `frontend/src/services/financeiroService.ts`
- Create: `frontend/src/components/FaturamentoConveniosTab.tsx`
- Create: `frontend/src/components/AssinarGuiaModal.tsx`
- Modify: `frontend/src/pages/Financeiro.tsx`
- Modify: `frontend/src/pages/Dashboard.tsx` (card/alerta para `RESPONSAVEL` quando houver guias `AGUARDANDO_ASSINATURA`, com atalho para conferir/assinar)
- Test: `frontend/src/components/AssinarGuiaModal.test.tsx`
- Modify: `frontend/src/pages/Financeiro.test.tsx`

- [ ] **Step 1:** Implementar `FaturamentoConveniosTab.tsx` para a clínica gerar fechamentos por competência/convênio, baixar PDF, liberar para assinatura, registrar envio e dar baixa de recebimento.
- [ ] **Step 2:** Implementar `AssinarGuiaModal.tsx` exibindo itens da guia, botão para visualizar/baixar PDF e campo de confirmação de senha para concluir a assinatura eletrônica pelo responsável.
- [ ] **Step 3:** Adicionar banner/card de pendências no `Dashboard.tsx` do `RESPONSAVEL` listando guias aguardando assinatura.
- [ ] **Step 4:** Escrever testes unitários para `AssinarGuiaModal` e fluxos da aba de convênios em `Financeiro.test.tsx`.
- [ ] **Step 5:** Rodar `pnpm --dir frontend test --run`, `pnpm --dir frontend build`, recompilar `java_backend` no Docker e executar verificação E2E completa de Etapa 1 + Etapa 2.
