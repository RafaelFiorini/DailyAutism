# Plano de Implementação — Cadastro de Responsáveis e Vínculo com Pacientes

**Data:** 08/11/2026  
**Objetivo:** Criar o módulo completo de cadastro e gerenciamento de Responsáveis no painel administrativo (`/admin/responsaveis`), suportando cadastro unificado/atômico com `Pessoa`, inclusão do próprio responsável como paciente (`isPacienteTambem`) e vínculo com pacientes existentes.

---

## 1. Arquitetura e Modelagem

### Backend
- **`SaveResponsavelDTO`**:
  - Aceita `idPessoa` (para vincular pessoa existente) OU dados completos da pessoa (`nomePessoa`, `CPFPessoa`, `datanascPessoa`, `RGPessoa`, `enderecoPessoa`, `telefonePessoa`, `celularPessoa`, `emailPessoa`).
  - Anotações Jackson `@JsonProperty` e `@JsonAlias` para compatibilidade total.
  - `isPacienteTambem` (booleano): se verdadeiro, cria/vincula automaticamente um `Paciente` para a mesma `Pessoa` com `isResponsavel = true` e o adiciona à lista `responsavel.getPacientes()`.
  - `pacienteIds` (`List<String>`): lista opcional de IDs de pacientes existentes para vincular ao responsável.
- **`ResponsavelDTO`**:
  - Exposição de `idResponsavel`, `idPessoa`, `nomeResponsavel`, `cpf`, `email`, `telefone`, `isPaciente` e `pacientes` (lista com `idPaciente` e `nomePaciente`).
- **`ResponsavelService`**:
  - `createResponsavel`: método transacional `@Transactional` que:
    1. Higieniza o CPF (remove pontuação).
    2. Busca `Pessoa` existente por `idPessoa` ou `CPFPessoa`, ou cria uma nova `Pessoa`.
    3. Verifica se a pessoa já possui cadastro de `Responsavel`.
    4. Trata `isPacienteTambem`: se ativo, garante a existência do `Paciente` correspondente à pessoa e o adiciona aos pacientes do responsável.
    5. Vincula pacientes informados em `pacienteIds`.
    6. Salva e retorna o `Responsavel`.
- **`ResponsavelRepository` & `PacienteRepository`**:
  - Adição de métodos de consulta convenientes (`existsByPessoaResponsavel`, `findByPessoaPaciente`).

### Frontend
- **Tipos TypeScript** (`frontend/src/types/responsavel.ts`):
  - Interfaces `Responsavel`, `SaveResponsavelInput` e `PacienteOption`.
- **Serviço de API** (`frontend/src/services/responsavelService.ts`):
  - Métodos `list()`, `create()`, `delete()`.
- **Modal de Cadastro** (`frontend/src/components/ResponsavelModal.tsx`):
  - Formulário com dados pessoais (Nome, CPF, E-mail, Celular, Data Nasc, RG, Endereço).
  - Checkbox destacado: "O responsável também é paciente em acompanhamento".
  - Seletor de pacientes existentes (busca via `GET /pacientes`).
  - Validação de campos obrigatórios e exibição do erro real retornado pela API.
- **Página de Listagem** (`frontend/src/pages/Responsaveis.tsx`):
  - Tabela moderna com busca por nome, CPF e nome dos pacientes vinculados.
  - Badges informando os pacientes vinculados e tag especial caso o responsável seja paciente.
  - Ação de exclusão com confirmação.
- **Navegação e Rotas**:
  - Adição de `Responsáveis` no menu lateral de [`AdminLayout.tsx`](file:///c:/Users/rafael.fiorini/DailyAutism/frontend/src/components/AdminLayout.tsx).
  - Rota `<Route path="responsaveis" element={<Responsaveis />} />` em [`App.tsx`](file:///c:/Users/rafael.fiorini/DailyAutism/frontend/src/App.tsx).

---

## 2. Tarefas e Critérios de Aceite

- [ ] **Tarefa 1: Backend — DTOs, Repositories e ResponsavelService**
  - Payload unificado com dados de `Pessoa`.
  - Suporte a `isPacienteTambem` e `pacienteIds`.
  - Resposta enriquecida em `ResponsavelDTO`.
- [ ] **Tarefa 2: Frontend — Tipos e Serviço de API**
  - Criação de `responsavel.ts` e `responsavelService.ts`.
  - Testes unitários para o serviço (`responsavelService.test.ts`).
- [ ] **Tarefa 3: Frontend — Componente ResponsavelModal**
  - Modal com formulário completo, checkbox de paciente e seleção de pacientes existentes.
  - Testes unitários (`ResponsavelModal.test.tsx`).
- [ ] **Tarefa 4: Frontend — Página Responsaveis, Menu e Rota**
  - Implementação da página `Responsaveis.tsx` com busca e tabela.
  - Testes unitários (`Responsaveis.test.tsx`).
  - Inclusão no menu lateral e em `App.tsx`.
- [ ] **Tarefa 5: Verificação, Testes Totais e Rebuild Docker**
  - Rodar suíte completa de testes no Vitest (todos passando).
  - Executar `pnpm build`.
  - Recompilar container `java_backend` via Docker Compose e verificar inicialização sem tocar no MySQL.
