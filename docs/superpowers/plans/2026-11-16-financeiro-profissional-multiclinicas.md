# Módulo Financeiro do Profissional & Multi-Clínicas Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar o módulo financeiro do profissional no DailyAutism (Backend Spring Boot e DApp Android), permitindo conciliação de repasses de múltiplas clínicas (valor fixo ou percentual) e gestão de atendimentos particulares diretos, com registro contábil congelado por sessão e controle de baixas.

**Architecture:** Abordagem 2 (Conta Corrente com Lançamentos Congelados). Toda sessão realizada na agenda gera/atualiza um `LancamentoRepasseProfissional` com valores congelados. O profissional visualiza no DApp seu faturamento consolidado por clínica e particular, podendo dar baixa individual ou em lote quando recebe o repasse.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate, PostgreSQL, JUnit 5, Mockito, Kotlin, Jetpack Compose, Material3, Retrofit 2, Coroutines/Flow.

**Spec:** [modelo-erd-e-plano-financeiro-profissional.md](file:///C:/Users/rafael.fiorini/.gemini/antigravity/brain/a70f712d-f7c6-47e0-a5dc-b1c38f5a6e10/modelo-erd-e-plano-financeiro-profissional.md)

## Global Constraints

- Padrão monetário em `BigDecimal` com escala 2 (`precision = 12, scale = 2`).
- Datas em padrão ISO (`yyyy-MM-dd`) no backend e formato brasileiro `DD/MM/AAAA` (`dd/MM/yyyy`) na apresentação do DApp.
- Respeitar o isolamento multi-tenant: o profissional só acessa seus próprios lançamentos financeiros e acordos.
- Regra de repasse: `VALOR_FIXO` (ex: R$ 70,00) ou `PERCENTUAL` (ex: 60.00% sobre o valor da sessão). Se atendimento particular direto (sem clínica), o valor é 100% do `valorSessaoParticular` do profissional.

## Review Focus

1. Sessão sem clínica vinculada (particular autônomo): deve calcular repasse igual a 100% de `valorSessaoParticular` com tipo `PARTICULAR_DIRETO`.
2. Sessão cancelada ou com falta após realização: lançamento deve ter status alterado para `CANCELADO` e não somar nos totais a receber.
3. Repasse percentual com valor zero ou nulo na clínica: não deve lançar NPE; deve registrar zero e alertar pendência de configuração.
4. Baixa em lote por clínica: só deve dar baixa nos lançamentos com status `PENDENTE` do mês/ano informado, preservando lançamentos já pagos.
5. Preservação de histórico: alteração futura do valor da sessão ou percentual na clínica não deve retroagir em lançamentos passados já gerados.

---

### Task 1: Domain Entities & Enums (Backend)

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/TipoRepasseProfissional.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/StatusLancamentoRepasse.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/TipoOrigemLancamento.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/LancamentoRepasseProfissional.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/LancamentoRepasseRepository.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/entity/Profissional.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/entity/ProfissionalClinica.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/repository/LancamentoRepasseRepositoryTest.java`

- [ ] **Step 1:** Criar enums `TipoRepasseProfissional` (`VALOR_FIXO`, `PERCENTUAL`), `StatusLancamentoRepasse` (`PENDENTE`, `PAGO`, `CANCELADO`) e `TipoOrigemLancamento` (`CLINICA_FIXO`, `CLINICA_PERCENTUAL`, `PARTICULAR_DIRETO`).
- [ ] **Step 2:** Adicionar `valorSessaoParticular: BigDecimal` em `Profissional.java`.
- [ ] **Step 3:** Adicionar `tipoRepasse: TipoRepasseProfissional` e `valorRepasse: BigDecimal` em `ProfissionalClinica.java`.
- [ ] **Step 4:** Criar entidade `LancamentoRepasseProfissional` herdando de `BaseEntity` com mapeamento JPA, FKs (`Profissional`, `Clinica`, `Paciente`, `Compromisso`), campos de valores e datas.
- [ ] **Step 5:** Criar `LancamentoRepasseRepository` com consultas por profissional, período (`dataCompetencia`), clínica e status.
- [ ] **Step 6:** Escrever teste de repositório `LancamentoRepasseRepositoryTest` validando persistência e consultas por mês/ano.
- [ ] **Step 7:** Executar teste para validar sucesso.
- [ ] **Step 8:** Commit das alterações de domínio e entidades.

---

### Task 2: Application Service & Regras de Repasse e Baixa (Backend)

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/LancamentoRepasseService.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/CompromissoService.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/aplicationservice/LancamentoRepasseServiceTest.java`

- [ ] **Step 1:** Criar classe de teste `LancamentoRepasseServiceTest` cobrindo:
  - Cálculo de repasse fixo por clínica.
  - Cálculo de repasse percentual sobre tabela da clínica.
  - Cálculo de atendimento particular direto do profissional.
  - Baixa individual de lançamento.
  - Baixa em lote de clínica para um determinado mês.
- [ ] **Step 2:** Executar testes para garantir falha inicial (TDD).
- [ ] **Step 3:** Implementar `LancamentoRepasseService` com métodos:
  - `gerarOuAtualizarLancamentoPorCompromisso(Compromisso compromisso)`
  - `cancelarLancamentoPorCompromisso(String idCompromisso)`
  - `darBaixaLancamento(String idLancamento, LocalDate dataPagamento)`
  - `darBaixaLoteClinica(String idProfissional, String idClinica, YearMonth competencia, LocalDate dataPagamento)`
  - `obterResumoMensal(String idProfissional, YearMonth competencia)`
- [ ] **Step 4:** Integrar hook no `CompromissoService`: ao atualizar status do compromisso para `REALIZADO`, chamar geração de lançamento; ao cancelar/faltar, chamar cancelamento.
- [ ] **Step 5:** Rodar testes `LancamentoRepasseServiceTest` e garantir 100% aprovados.
- [ ] **Step 6:** Commit do serviço de aplicação e regras de negócio.

---

### Task 3: REST API Endpoints & DTOs (Backend)

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/LancamentoRepasseDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/ResumoFinanceiroProfissionalDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/ConfigurarAcordoRepasseDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/ConfigurarValorParticularDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/controller/ProfissionalFinanceiroRestResource.java`
- Modify: `src/main/java/com/clinica/dailyautism/infrastructure/controller/ProfissionalRestResource.java`
- Test: `src/test/java/com/clinica/dailyautism/infrastructure/controller/ProfissionalFinanceiroRestResourceTest.java`

- [ ] **Step 1:** Criar DTOs para transferência de dados de lançamentos, resumo consolidado e configuração de repasse.
- [ ] **Step 2:** Criar teste de integração/controller `ProfissionalFinanceiroRestResourceTest`.
- [ ] **Step 3:** Implementar controller `ProfissionalFinanceiroRestResource` com endpoints:
  - `GET /financeiro/profissional/resumo?mesAno=YYYY-MM`
  - `GET /financeiro/profissional/lancamentos?mesAno=YYYY-MM&idClinica=&status=`
  - `POST /financeiro/profissional/lancamentos/{id}/baixar`
  - `POST /financeiro/profissional/clinicas/{idClinica}/baixar-mes`
  - `PUT /financeiro/profissional/meu-valor-particular`
  - `PUT /financeiro/profissional/clinicas/{idClinica}/acordo-repasse`
- [ ] **Step 4:** Executar testes do controller e validar respostas HTTP 200/400/403.
- [ ] **Step 5:** Commit dos endpoints e DTOs.

---

### Task 4: DApp Models & Retrofit Client (Android)

**Files:**
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/model/FinanceiroProfissionalModels.kt`
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/network/FinanceiroProfissionalService.kt`
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/network/RetrofitClient.kt`

- [ ] **Step 1:** Criar data classes Kotlin em `FinanceiroProfissionalModels.kt`:
  - `LancamentoRepasseItem`
  - `ResumoFinanceiroProfissional`
  - `TotalPorClinicaItem`
  - `BaixarLancamentoRequest`
  - `ConfigurarValorParticularRequest`
- [ ] **Step 2:** Criar interface `FinanceiroProfissionalService` com anotações Retrofit (`@GET`, `@POST`, `@PUT`).
- [ ] **Step 3:** Adicionar `financeiroProfissionalService` singleton em `RetrofitClient.kt`.
- [ ] **Step 4:** Commit das camadas de rede e modelo no DApp.

---

### Task 5: DApp ViewModel & Aba Financeiro na Home do Profissional (Android)

**Files:**
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/home/HomeProfissionalViewModel.kt`
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/home/HomeProfissionalScreen.kt`

- [ ] **Step 1:** Atualizar enum `ProfissionalTab` em `HomeProfissionalViewModel.kt` para incluir `FINANCEIRO`.
- [ ] **Step 2:** Adicionar estados de resumo financeiro, mês de competência selecionado, lista de lançamentos e flag de baixa no `HomeProfissionalState`.
- [ ] **Step 3:** Implementar métodos no ViewModel:
  - `carregarFinanceiro(context, mesAno)`
  - `mudarMesCompetencia(mesAno)`
  - `darBaixaLancamento(context, idLancamento)`
  - `darBaixaLoteClinica(context, idClinica, mesAno)`
  - `salvarValorSessaoParticular(context, novoValor)`
- [ ] **Step 4:** Em `HomeProfissionalScreen.kt`, adicionar o 4º item de navegação inferior: `Financeiro` (`Icons.Default.MonetizationOn`).
- [ ] **Step 5:** Construir composable `TabFinanceiroProfissional`:
  - Seletor de mês/ano no topo (navegação mês anterior / próximo).
  - Cards de métricas: Total Faturado, Recebido (Na Conta) e Pendente (A Receber).
  - Seção "Por Clínica & Particular": cards de cada clínica com total a receber e botão "Dar Baixa no Mês".
  - Extrato analítico de atendimentos com filtros (Todos, Pendentes, Pagos), badge de clínica e botão de toggle de quitação.
  - Dialog para editar valor da sessão particular do profissional.
- [ ] **Step 6:** Commit das telas e componentes do DApp.

---

### Task 6: Validação Integrada, Deploy & Build APK

**Files:**
- All modified backend & DApp files.

- [ ] **Step 1:** Executar suite de testes do backend Spring Boot (`mvn test` ou gradle).
- [ ] **Step 2:** Fazer push das alterações do repositório backend.
- [ ] **Step 3:** Fazer push do DApp para `origin/master`.
- [ ] **Step 4:** Monitorar e validar execução do workflow `Build Android APK (DApp)` no GitHub Actions.
- [ ] **Step 5:** Confirmar geração da nova release com o APK atualizado.
