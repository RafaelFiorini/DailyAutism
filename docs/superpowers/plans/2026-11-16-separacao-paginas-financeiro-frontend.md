# Separação de Páginas: Financeiro da Clínica vs Meu Financeiro (Profissional) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Separar a tela compartilhada de financeiro em duas páginas web independentes: `Financeiro.tsx` (restrita à gestão da clínica e admin global: faturamento, convênios, contratos particulares e tabela de preços) e `FinanceiroProfissionalPage.tsx` (rota `/admin/meu-financeiro`, focada exclusivamente no extrato, métricas e conciliação de repasses do profissional).

**Architecture:** Aplicação do Princípio de Responsabilidade Única (SRP) no frontend. Cada persona tem sua rota dedicada, eliminando condicionais complexas (`isProfissionalOnly`), eliminando tentativas de chamadas a endpoints com permissões restritas (HTTP 403) e simplificando o menu de navegação lateral.

**Tech Stack:** React 18, TypeScript, React Router 6, Tailwind CSS, Lucide Icons, Vitest, React Testing Library.

**Spec:** Discussão de arquitetura e modelo conceitual aprovado em 16/11/2026.

## Global Constraints

- Rota de gestão da clínica / admin: `/admin/financeiro` (mantida para não quebrar referências existentes).
- Nova rota do profissional: `/admin/meu-financeiro`.
- Se um usuário com perfil exclusivo `PROFISSIONAL` tentar acessar `/admin/financeiro`, deve ser redirecionado automaticamente para `/admin/meu-financeiro`.
- Se um usuário sem perfil de profissional tentar acessar `/admin/meu-financeiro`, deve ser redirecionado para `/admin/financeiro` ou `/admin`.
- Todos os testes unitários do Vitest (120+ testes) devem continuar 100% passando.
- `npm run build` deve compilar sem nenhum aviso de tipo TypeScript.

## Review Focus

1. Usuário apenas `PROFISSIONAL` logado: menu exibe "Meu Financeiro" apontando para `/admin/meu-financeiro`, sem exibir abas de convênios/preços/contratos da clínica.
2. Usuário `ADMIN` ou `ADMIN_CLINICA`: acessa `/admin/financeiro` com todas as abas de gestão intactas (Particulares, Tabela de Preços, Guias de Convênio).
3. Redirecionamento de salvaguarda: navegação direta na barra de endereços para `/admin/financeiro` por profissional redireciona para `/admin/meu-financeiro`.
4. Usuário híbrido (ex: médico que também é gestor da clínica): consegue acessar ambas as páginas conforme as permissões de sua conta.
5. Zero chamadas a endpoints administrativos (`listContratosParticulares`) na rota do profissional.

---

### Task 1: Criar a Página Dedicada `FinanceiroProfissionalPage.tsx` e Testes

**Files:**
- Create: `frontend/src/pages/FinanceiroProfissionalPage.tsx`
- Create: `frontend/src/pages/FinanceiroProfissionalPage.test.tsx`

- [x] **Step 1:** Escrever o teste unitário inicial `FinanceiroProfissionalPage.test.tsx` validando que a página renderiza o cabeçalho "Meu Financeiro & Repasses" e o componente `FinanceiroProfissionalTab`.
- [x] **Step 2:** Executar o teste com vitest e validar a falha inicial esperada (arquivo ainda não existe).
- [x] **Step 3:** Implementar `frontend/src/pages/FinanceiroProfissionalPage.tsx` contendo o cabeçalho informativo limpo e renderizando `<FinanceiroProfissionalTab />`.
- [x] **Step 4:** Executar novamente o teste unitário e verificar 100% de sucesso.
- [x] **Step 5:** Commit isolado da nova página.

---

### Task 2: Registrar Rotas e Atualizar Navegação na Sidebar

**Files:**
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/components/AdminLayout.tsx`
- Modify: `frontend/src/components/AdminLayout.test.tsx` (ou teste correspondente se houver)

- [x] **Step 1:** Em `frontend/src/App.tsx`, importar `FinanceiroProfissionalPage` e adicionar a rota:
  `<Route path="meu-financeiro" element={<FinanceiroProfissionalPage />} />` dentro de `<Route path="/admin" element={<AdminLayout />}>`.
- [x] **Step 2:** Em `frontend/src/components/AdminLayout.tsx`:
  - No menu do `isProfissional`, alterar o item de Financeiro para:
    `{ name: 'Meu Financeiro', href: '/admin/meu-financeiro', icon: DollarSign, end: false }`.
  - Garantir que `isAdmin` ou `isFuncionarioClinica` continuem apontando para `/admin/financeiro`.
- [x] **Step 3:** Executar testes do frontend para verificar integridade da navegação.
- [x] **Step 4:** Commit das configurações de rota e navegação.

---

### Task 3: Limpar e Simplificar `Financeiro.tsx`

**Files:**
- Modify: `frontend/src/pages/Financeiro.tsx`
- Modify: `frontend/src/pages/Financeiro.test.tsx`

- [ ] **Step 1:** Atualizar os testes em `Financeiro.test.tsx` para refletir que a página agora é estritamente o módulo de gestão financeira da clínica.
- [ ] **Step 2:** Em `frontend/src/pages/Financeiro.tsx`:
  - Se `isProfissionalOnly`, renderizar `<Navigate to="/admin/meu-financeiro" replace />`.
  - Remover a aba `'meu-financeiro'` do tipo `TabKey` e do estado inicial.
  - Remover o botão da aba "Meu Extrato & Repasses" e a importação de `FinanceiroProfissionalTab`.
  - Limpar as condicionais residuais `!isProfissionalOnly` dos botões das outras abas (já que profissionais exclusivos são redirecionados).
- [ ] **Step 3:** Executar `npm test` em `Financeiro.test.tsx` e validar 100% de aprovação.
- [ ] **Step 4:** Commit da limpeza do módulo financeiro administrativo.

---

### Task 4: Validação Geral da Suite de Testes e Build de Produção

**Files:**
- Todos os arquivos modificados.

- [ ] **Step 1:** Executar `npm --prefix frontend run build` para garantir zero erros de TypeScript e bundle limpo.
- [ ] **Step 2:** Executar `npm --prefix frontend test -- --run` para garantir que toda a suíte de testes unitários passe com sucesso.
- [ ] **Step 3:** Commit final e push para o repositório remoto (`origin/master`).
