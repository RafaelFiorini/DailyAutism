# DApp Auto-Cadastro (Responsável & Profissional) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar o fluxo nativo de auto-cadastro no aplicativo Android (DApp) permitindo que novos Responsáveis e Profissionais criem suas contas diretamente pelo app, com login automático pós-cadastro.

**Architecture:** MVVM com Jetpack Compose, Material 3 e Coroutines/StateFlow. O app enviará o payload estruturado para o endpoint existente `POST /auth/register`, salvará o token retornado no `TokenDataStore` e redirecionará automaticamente para a Home correspondente (`HomeResponsavel` ou `HomeProfissional`).

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Retrofit 2, Coroutines, StateFlow, Jetpack DataStore.

**Spec:** Modelo de dados `RegisterDTO` do backend DailyAutism.

## Global Constraints

- Perfis permitidos no auto-cadastro do app: `RESPONSAVEL` ou `PROFISSIONAL`.
- Campos obrigatórios comuns: Nome, CPF (11 dígitos), E-mail válido, Senha (mínimo 6 caracteres).
- Campo obrigatório para Profissional: `conselhoProf` (registro profissional no conselho de classe).
- Pós-cadastro: salvar token JWT no `TokenDataStore` e redirecionar para a home do perfil sem necessidade de relogar.

## Review Focus

1. Alternância entre perfis (Responsável vs Profissional): alternar limpa ou reconfigura campos específicos sem quebrar o estado.
2. Validação de senha: se senhas não coincidirem ou forem menores que 6 caracteres, exibir feedback visual sem chamada de rede.
3. Tratamento de CPF e E-mail duplicados: erros da API (400/409) devem ser exibidos de forma amigável ao usuário.
4. Auto-login: após resposta 200/201 do backend, garantir que `userMe` seja carregado e a navegação redirecione para a home correta.
5. Botão "Voltar ao Login": permitir cancelar e retornar à tela de login a qualquer momento.

---

### Task 1: DTO Models & Retrofit Interface (DApp)

**Files:**
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/model/RegisterModels.kt`
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/network/AuthService.kt`

- [x] **Step 1:** Criar data class `RegisterRequest` em `RegisterModels.kt` com suporte a `tipo`, `nome`, `email`, `senha`, `cpf`, `celular`, `telefone`, `conselhoProf`.
- [x] **Step 2:** Adicionar endpoint `@POST("auth/register") suspend fun register(@Body request: RegisterRequest): LoginResponse` em `AuthService.kt`.
- [x] **Step 3:** Commit das alterações de rede e modelos.

---

### Task 2: RegisterViewModel & Gerenciamento de Estado (DApp)

**Files:**
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/cadastro/RegisterViewModel.kt`

- [x] **Step 1:** Definir `RegisterUiState` (`Idle`, `Loading`, `Success(token, userMe)`, `Error(message)`).
- [x] **Step 2:** Implementar método `cadastrar(request, context)` no `RegisterViewModel` que valida os campos, chama `AuthService.register`, salva o token no `TokenDataStore`, busca os dados com `UserService.getMe` e emite `Success`.
- [x] **Step 3:** Commit do ViewModel.

---

### Task 3: RegisterScreen Composable & Validações Visuais (DApp)

**Files:**
- Create: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/cadastro/RegisterScreen.kt`

- [x] **Step 1:** Construir a interface com seletor de perfil (Tabs ou SegmentedButtons: "Responsável" / "Profissional").
- [x] **Step 2:** Adicionar campos de formulário: Nome, CPF com máscara, E-mail, Celular, Senha, Confirmação de Senha e Conselho (se profissional).
- [x] **Step 3:** Implementar validações visuais de formulário e botão de submissão com estado de loading.
- [x] **Step 4:** Adicionar botão de navegação "Já tem uma conta? Faça Login".
- [x] **Step 5:** Commit da tela de registro.

---

### Task 4: Integração na Navegação & LoginScreen (DApp)

**Files:**
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/navigation/AppNavigation.kt`
- Modify: `DApp/app/src/main/java/com/dailyautism/dapp/ui/screens/login/LoginScreen.kt`

- [x] **Step 1:** Adicionar `Screen.Register : Screen("register")` em `AppNavigation.kt`.
- [x] **Step 2:** Adicionar botão "Não tem uma conta? Cadastre-se" em `LoginScreen.kt` com callback `onNavigateToRegister`.
- [x] **Step 3:** Configurar a rota `Screen.Register.route` em `AppNavigation.kt` direcionando pós-sucesso para a home do perfil.
- [x] **Step 4:** Commit e push das alterações no DApp.
