# Cadastro e Gestão de Profissionais Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar o fluxo completo de gestão e cadastro de profissionais de saúde (médicos e terapeutas) no DailyAutism, com backend atômico para criação/reaproveitamento de `Pessoa` por CPF e interface frontend em React com listagem, busca e modal de cadastro.

**Architecture:** O backend Spring Boot evolui `SaveProfissionalDTO` e `ProfissionalService` para criar ou vincular a `Pessoa` atomicamente na persistência do `Profissional`. O frontend React SPA (Vite + TypeScript + Tailwind) consome esses endpoints via `profissionalService`, exibindo uma tela `/admin/profissionais` com tabela responsiva, filtro em tempo real e modal de cadastro com validações de CPF e campos obrigatórios.

**Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, React 19, TypeScript, Tailwind CSS, Lucide React, Vitest, React Testing Library.

**Spec:** `docs/superpowers/specs/2026-11-05-cadastro-profissionais-design.md`

## Global Constraints

- O backend deve reaproveitar a `Pessoa` existente se o CPF informado já existir no banco de dados.
- A criação de `Pessoa` e `Profissional` deve ocorrer de forma estritamente transacional (`@Transactional`).
- O frontend deve validar formato de CPF e campos obrigatórios antes do envio.
- Todas as rotas frontend permanecem sob a guarda de `<ProtectedRoute />` exigindo role `ADMIN`.
- Todos os componentes novos do frontend devem possuir cobertura de testes com Vitest.

## Review Focus

1. **CPF já cadastrado como Responsável:** Ao cadastrar um profissional com CPF de um responsável já existente, o sistema deve vincular o profissional a essa pessoa sem erro de duplicidade e sem criar um segundo registro de Pessoa.
2. **Campos obrigatórios ausentes no formulário:** Se o administrador tentar submeter o modal sem Nome, CPF ou Conselho, o formulário deve exibir mensagens de validação sem disparar requisições à API.
3. **Falha na API ao listar ou cadastrar:** A tela e o modal devem exibir alertas amigáveis em caso de erro 400/500, sem travar o estado da interface.
4. **Busca e filtro em tempo real:** A barra de busca na listagem deve filtrar simultaneamente por Nome do profissional, Conselho (CRM/CRP/etc.) e CPF.
5. **Atualização imediata da listagem:** Após cadastrar um profissional com sucesso no modal, a tabela deve ser atualizada imediatamente refletindo o novo registro.

---

### Task 1: Backend - Payload e Serviço Atômico de Profissional

**Files:**
- Modify: `src/main/java/com/clinica/dailyautism/infrastructure/dto/SaveProfissionalDTO.java`
- Modify: `src/main/java/com/clinica/dailyautism/infrastructure/dto/ProfissionalDTO.java`
- Modify: `src/main/java/com/clinica/dailyautism/domain/aplicationservice/ProfissionalService.java`

**Interfaces:**
- Consumes: `PessoaRepository.findByCPFPessoa(String)`, `ProfissionalRepository.save(Profissional)`.
- Produces: `POST /profissionais` com payload unificado e retorno completo em `ProfissionalDTO`.

- [ ] **Step 1: Atualizar `SaveProfissionalDTO.java` com campos opcionais de Pessoa e Conselho obrigatório**

Adicionar campos de dados pessoais: `nomePessoa`, `CPFPessoa`, `datanascPessoa`, `RGPessoa`, `enderecoPessoa`, `telefonePessoa`, `celularPessoa`, `emailPessoa`, mantendo `idPessoa` opcional e `conselhoProf` obrigatório.

- [ ] **Step 2: Atualizar `ProfissionalDTO.java` para expor dados consolidados da Pessoa**

Incluir no `ProfissionalDTO` os campos `cpf`, `email` e `telefone` obtidos de `profissional.getPessoaProf()`.

- [ ] **Step 3: Implementar lógica de busca e criação atômica em `ProfissionalService.java`**

Se `idPessoa` estiver presente, buscar por ID. Caso contrário, se `CPFPessoa` estiver presente, buscar `findByCPFPessoa`. Se existir, reaproveitar; se não existir, instanciar e salvar nova `Pessoa`. Em seguida, associar a `Profissional` e salvar.

- [ ] **Step 4: Compilar e verificar integridade do código backend**

Run: `git diff src/`
Expected: Alterações corretas e sem quebras de dependências.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/clinica/dailyautism/infrastructure/dto/ src/main/java/com/clinica/dailyautism/domain/aplicationservice/ProfissionalService.java
git commit -m "feat(backend): support atomic person creation and lookup by CPF in professional registration"
```

---

### Task 2: Frontend - Camada de Tipos e Serviço de Profissionais

**Files:**
- Create: `frontend/src/types/profissional.ts`
- Create: `frontend/src/services/profissionalService.ts`
- Test: `frontend/src/services/profissionalService.test.ts`

**Interfaces:**
- Consumes: `api` (Axios client).
- Produces: `profissionalService.list()`, `profissionalService.create(input)`, `profissionalService.delete(id)`.

- [ ] **Step 1: Write failing tests for `profissionalService`**

Testar requisições GET `/profissionais`, POST `/profissionais` com dados unificados e DELETE `/profissionais/{id}`.

- [ ] **Step 2: Run test to verify it fails**

Run: `pnpm --prefix frontend test -- --run src/services/profissionalService.test.ts`
Expected: FAIL (módulo inexistente).

- [ ] **Step 3: Implement types and `profissionalService.ts`**

Criar interfaces `Profissional`, `SaveProfissionalInput` e métodos no serviço usando a instância `api`.

- [ ] **Step 4: Run test to verify it passes**

Run: `pnpm --prefix frontend test -- --run src/services/profissionalService.test.ts`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/types/profissional.ts frontend/src/services/profissionalService.ts frontend/src/services/profissionalService.test.ts
git commit -m "feat(frontend): implement professional service and TypeScript types"
```

---

### Task 3: Frontend - Componente Modal de Cadastro Unificado (`ProfissionalModal.tsx`)

**Files:**
- Create: `frontend/src/components/ProfissionalModal.tsx`
- Test: `frontend/src/components/ProfissionalModal.test.tsx`

**Interfaces:**
- Consumes: `SaveProfissionalInput`, ícones Lucide (`X`, `UserPlus`, `Loader2`, `AlertCircle`).
- Produces: `<ProfissionalModal isOpen={boolean} onClose={() => void} onSuccess={(novo) => void} />`.

- [ ] **Step 1: Write failing tests for `ProfissionalModal`**

Testar:
1. Renderização condicional quando `isOpen=true` e `isOpen=false`.
2. Validação de campos obrigatórios (Nome, CPF, Conselho).
3. Submissão com dados válidos chamando `profissionalService.create`.
4. Exibição de mensagem de erro caso a API falhe.

- [ ] **Step 2: Run test to verify it fails**

Run: `pnpm --prefix frontend test -- --run src/components/ProfissionalModal.test.tsx`
Expected: FAIL.

- [ ] **Step 3: Implement `ProfissionalModal.tsx`**

Modal responsivo e acessível estilizado com Tailwind CSS:
- Campos: Nome Completo, CPF, Data de Nascimento, E-mail, Celular, Conselho Profissional (ex.: CRM/SP 123456, CRP 06/1234).
- Indicador visual de loading no botão "Salvar Profissional".
- Mensagens de erro com badge de alerta.

- [ ] **Step 4: Run test to verify it passes**

Run: `pnpm --prefix frontend test -- --run src/components/ProfissionalModal.test.tsx`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/ProfissionalModal.tsx frontend/src/components/ProfissionalModal.test.tsx
git commit -m "feat(frontend): implement unified professional registration modal with validation"
```

---

### Task 4: Frontend - Página de Listagem e Gestão (`Profissionais.tsx`)

**Files:**
- Create: `frontend/src/pages/Profissionais.tsx`
- Test: `frontend/src/pages/Profissionais.test.tsx`

**Interfaces:**
- Consumes: `profissionalService`, `ProfissionalModal`.
- Produces: Página `/admin/profissionais` completa.

- [ ] **Step 1: Write failing tests for `Profissionais` page**

Testar:
1. Carregamento e renderização da lista de profissionais retornados pela API.
2. Filtro de busca por nome ou conselho.
3. Exibição de mensagem quando a lista estiver vazia.
4. Abertura do modal ao clicar em "+ Novo Profissional".

- [ ] **Step 2: Run test to verify it fails**

Run: `pnpm --prefix frontend test -- --run src/pages/Profissionais.test.tsx`
Expected: FAIL.

- [ ] **Step 3: Implement `Profissionais.tsx`**

- Cabeçalho com título, totalizadores e botão de ação.
- Input de busca com ícone de lupa e limpeza rápida.
- Tabela de dados com avatar de iniciais, nome, conselho em badge indigo, contato e botões de ação.
- Integração com `ProfissionalModal` e reload automático pós-cadastro.

- [ ] **Step 4: Run test to verify it passes**

Run: `pnpm --prefix frontend test -- --run src/pages/Profissionais.test.tsx`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/Profissionais.tsx frontend/src/pages/Profissionais.test.tsx
git commit -m "feat(frontend): implement professionals management page with search and listing"
```

---

### Task 5: Integração de Rota, Build e Deploy no Docker

**Files:**
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/App.test.tsx`

**Interfaces:**
- Consumes: `<Profissionais />` na rota `/admin/profissionais`.

- [ ] **Step 1: Adicionar rota `/admin/profissionais` em `App.tsx`**
- [ ] **Step 2: Atualizar testes de rota em `App.test.tsx`**
- [ ] **Step 3: Rodar suíte completa de testes do frontend**

Run: `pnpm --prefix frontend test -- --run`
Expected: Todos os testes passando.

- [ ] **Step 4: Executar build de produção do frontend**

Run: `pnpm --prefix frontend build`
Expected: Compilação sem erros (`dist/` gerado).

- [ ] **Step 5: Reconstruir container do backend no Docker**

Run: `docker compose up -d --build app`
Expected: Container `java_backend` atualizado e ativo.

- [ ] **Step 6: Commit final**

```bash
git add frontend/src/App.tsx frontend/src/App.test.tsx
git commit -m "feat(frontend): integrate professionals route in admin layout"
```

