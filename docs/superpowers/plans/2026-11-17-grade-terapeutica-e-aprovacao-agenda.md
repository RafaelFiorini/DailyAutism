# Grade Terapêutica Semanal, Aprovação pelo Responsável e Replicação de Agenda Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar o fluxo completo de proposta de grade semanal terapêutica para o paciente, aprovação/contraproposta pelo responsável no DApp e na Web, e replicação automática dos compromissos na agenda até o fim da vigência da guia ou contrato, com suporte a versionamento e tratamento de choques de horário.

**Architecture:** Módulo orientado a domínio (DDD) com entidade agregada `GradeTerapeutica` e `GradeTerapeuticaItem`. O motor de replicação expande semana a semana os agendamentos, associando cada `Compromisso` à grade de origem e marcando conflitos pontuais como `PENDENTE_REAGENDAMENTO`. Suporte a substituição/versionamento com data de corte para cancelamento automático de agendamentos futuros.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate, PostgreSQL, Kotlin, Jetpack Compose, Material 3, React 18, TypeScript, Tailwind CSS, Vite.

**Spec:** [grade-terapeutica-e-aprovacao-agenda.md](file:///C:/Users/rafael.fiorini/.gemini/antigravity/brain/a70f712d-f7c6-47e0-a5dc-b1c38f5a6e10/grade-terapeutica-e-aprovacao-agenda.md)

## Global Constraints

- O cálculo de replicação deve respeitar as datas `dataInicioVigencia` e `dataFimVigencia`.
- Formato de hora padrão: `HH:mm` no formato local (`LocalTime`).
- Status da Grade: `RASCUNHO`, `PENDENTE_APROVACAO`, `SUGESTAO_RESPONSAVEL`, `APROVADA`, `REJEITADA`, `VIGENTE`, `SUBSTITUIDA`.
- Na substituição de grade (mudança no meio da vigência), atendimentos passados (`< dataInicioNovaGrade`) NUNCA podem ser alterados ou apagados.
- Compromissos conflitantes durante a replicação devem receber o status `PENDENTE_REAGENDAMENTO`.

## Review Focus

1. Paciente com Guia autorizada para mais terapias do que a clínica oferece: a grade permite salvar apenas as especialidades com profissionais disponíveis.
2. Responsável que clica em "Sugerir Alterações": salva o texto da justificativa e muda status para `SUGESTAO_RESPONSAVEL` sem replicar agenda.
3. Replicação em feriados/choques: gera o atendimento com status `PENDENTE_REAGENDAMENTO` e não cancela a geração do restante do período.
4. Troca de terapeuta no meio da vigência: cancela compromissos futuros da grade antiga (`>= dataInicioNovaGrade`) e gera os novos até a mesma data final da guia.
5. Exclusão definitiva de um horário da grade: exclui os compromissos futuros daquele horário específico preservando o histórico passado.

---

### Task 1: Domain Entities, Enums & Repositories (Backend)

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/StatusGradeTerapeutica.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/DiaSemana.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/GradeTerapeutica.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/entity/GradeTerapeuticaItem.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/GradeTerapeuticaRepository.java`
- Create: `src/main/java/com/clinica/dailyautism/domain/repository/GradeTerapeuticaItemRepository.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/entity/StatusCompromisso.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/entity/Compromisso.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/repository/GradeTerapeuticaRepositoryTest.java`

- [x] **Step 1:** Criar enum `StatusGradeTerapeutica` e `DiaSemana`.
- [x] **Step 2:** Atualizar `StatusCompromisso.java` adicionando `PENDENTE_REAGENDAMENTO`.
- [x] **Step 3:** Adicionar `@ManyToOne @JoinColumn(name = "idGradeTerapeutica") private GradeTerapeutica gradeTerapeutica` em `Compromisso.java`.
- [x] **Step 4:** Criar entidade `GradeTerapeutica` herdando de `BaseEntity` com mapeamento JPA, versionamento (`numeroVersao`), vínculo com `Paciente`, `Clinica`, `idGradeAnterior` e campos de vigência e auditoria.
- [x] **Step 5:** Criar entidade `GradeTerapeuticaItem` com dia da semana, horários, tipo de compromisso e profissional.
- [x] **Step 6:** Criar repositórios `GradeTerapeuticaRepository` e `GradeTerapeuticaItemRepository`.
- [x] **Step 7:** Criar teste de repositório `GradeTerapeuticaRepositoryTest` validando persistência e consultas.
- [x] **Step 8:** Commit das entidades e repositórios.

---

### Task 2: Motor de Replicação e Regras de Negócio (Backend Service)

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/GradeTerapeuticaService.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/CompromissoService.java`
- Test: `src/test/java/com/clinica/dailyautism/domain/aplicationservice/GradeTerapeuticaServiceTest.java`

- [x] **Step 1:** Criar teste unitário `GradeTerapeuticaServiceTest` cobrindo:
  - Criação de grade e envio para aprovação.
  - Aprovação do responsável e replicação semana a semana.
  - Detecção de choques de horário gerando `PENDENTE_REAGENDAMENTO`.
  - Substituição de grade com corte de data e cancelamento de compromissos futuros.
  - Registro de sugestão de alteração pelo responsável.
- [x] **Step 2:** Executar testes para verificar falha inicial (TDD).
- [x] **Step 3:** Implementar `GradeTerapeuticaService` com os métodos:
  - `criarPropostaGrade(...)`
  - `enviarParaAprovacao(idGrade)`
  - `registrarSugestaoResponsavel(idGrade, justificativa)`
  - `aprovarEReplicar(idGrade)`
  - `rejeitar(idGrade, motivo)`
  - `proporAlteracao(idGradeVigente, dataCorte, novaGradeDTO)`
  - `listarConflitosPendentes(idClinica, idPaciente)`
- [x] **Step 4:** Executar testes unitários e garantir 100% aprovados.
- [x] **Step 5:** Commit do serviço de aplicação.

---

### Task 3: REST API & DTOs (Backend)

**Files:**
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/GradeTerapeuticaDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/SaveGradeTerapeuticaDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/SugestaoGradeDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/dto/RelatorioConflitoGradeDTO.java`
- Create: `src/main/java/com/clinica/dailyautism/infrastructure/controller/GradeTerapeuticaRestResource.java`
- Test: `src/test/java/com/clinica/dailyautism/infrastructure/controller/GradeTerapeuticaRestResourceTest.java`

- [x] **Step 1:** Criar DTOs para entrada e saída de grades e itens semanais.
- [x] **Step 2:** Implementar controller `GradeTerapeuticaRestResource`:
  - `POST /grades-terapeuticas`: Cria proposta.
  - `GET /grades-terapeuticas/paciente/{idPaciente}`: Lista histórico e grade vigente do paciente.
  - `POST /grades-terapeuticas/{id}/enviar`: Envia proposta para o responsável.
  - `POST /grades-terapeuticas/{id}/aprovar`: Aprova e dispara replicação.
  - `POST /grades-terapeuticas/{id}/sugerir`: Registra contraproposta da família.
  - `POST /grades-terapeuticas/{id}/rejeitar`: Rejeita proposta.
  - `POST /grades-terapeuticas/{id}/alterar`: Proposta de nova versão com data de corte.
  - `GET /grades-terapeuticas/{id}/conflitos`: Lista compromissos que colidiram na geração.
- [x] **Step 3:** Criar testes de integração do controller validando permissões de acesso por perfil.
- [x] **Step 4:** Commit dos controllers e DTOs.

---

### Task 4: DApp Models, Retrofit Service & ViewModel (Mobile)

**Files:**
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/model/GradeTerapeuticaModels.kt`
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/network/GradeTerapeuticaService.kt`
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/home/GradeTerapeuticaViewModel.kt`
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/network/RetrofitClient.kt`

- [x] **Step 1:** Criar data classes Kotlin para grade, itens e payload de sugestão/aprovação.
- [x] **Step 2:** Criar interface `GradeTerapeuticaService` no Retrofit e registrar no `RetrofitClient`.
- [x] **Step 3:** Criar `GradeTerapeuticaViewModel` com estados de carregamento de proposta pendente, aprovação e envio de contraproposta com texto.
- [x] **Step 4:** Commit da camada de dados no DApp.

---

### Task 5: DApp UI de Aprovação da Grade pelo Responsável (Mobile)

**Files:**
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/home/PropostaGradeCard.kt`
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/home/DetalhesGradeDialog.kt`
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/home/HomeResponsavelScreen.kt`

- [x] **Step 1:** Construir `PropostaGradeCard` exibido na Home do Responsável quando houver proposta `PENDENTE_APROVACAO`.
- [x] **Step 2:** Construir `DetalhesGradeDialog`:
  - Visualização da tabela de horários semanais agrupada por dia (Segunda a Sexta).
  - Exibição de terapeuta, especialidade e vigência.
  - Botão "Aprovar Horários".
  - Botão "Sugerir Alterações" com campo de texto livre.
  - Botão "Rejeitar".
- [x] **Step 3:** Integrar o card e o dialog na `HomeResponsavelScreen.kt`.
- [x] **Step 4:** Commit dos componentes de tela no DApp.

---

### Task 6: Painel da Grade Terapêutica na Web (Clínica / Secretaria)

**Files:**
- Create: `frontend/src/types/gradeTerapeutica.ts`
- Create: `frontend/src/services/gradeTerapeuticaService.ts`
- Create: `frontend/src/components/GradeTerapeuticaMontadorModal.tsx`
- Create: `frontend/src/components/GradeTerapeuticaConflitosModal.tsx`
- Modify: `frontend/src/pages/Agenda.tsx` (ou aba dedicada de Grade no perfil do paciente)
- Test: `frontend/src/components/GradeTerapeuticaMontadorModal.test.tsx`

- [x] **Step 1:** Criar tipos TypeScript e serviço frontend `gradeTerapeuticaService.ts`.
- [x] **Step 2:** Criar modal `GradeTerapeuticaMontadorModal` com grade horária visual (dias da semana x horários), seletores de especialidade e profissional, e seleção da guia/contrato de vigência.
- [x] **Step 3:** Criar visualização de status da proposta: se `SUGESTAO_RESPONSAVEL`, exibir o texto enviado pela família e botão para ajustar.
- [x] **Step 4:** Criar modal/card `GradeTerapeuticaConflitosModal` listando atendimentos com status `PENDENTE_REAGENDAMENTO` para resolução rápida.
- [x] **Step 5:** Escrever testes unitários e integrar na tela de Agenda/Pacientes da Web.
- [x] **Step 6:** Commit do painel Web da secretaria.

---

### Task 7: Validação Integrada, Build de Produção & Deploy

**Files:**
- Todos os arquivos modificados.

- [x] **Step 1:** Executar suite de testes do Backend Spring Boot (`./mvnw test`).
- [x] **Step 2:** Executar build e testes do Frontend Web (`npm run build`, `npm test`).
- [x] **Step 3:** Fazer push do repositório `DailyAutism` para acionar deploy automático na OCI.
- [x] **Step 4:** Fazer push do repositório `DApp` para acionar compilação automática do APK no GitHub Actions.
- [x] **Step 5:** Validar funcionamento de ponta a ponta.
