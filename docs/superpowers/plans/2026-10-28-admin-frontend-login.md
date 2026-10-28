# Painel Administrativo DailyAutism - Frontend React com Autenticação de Administrador

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar o frontend administrativo do DailyAutism em React (Vite + TypeScript + Tailwind CSS), focado inicialmente no fluxo completo de autenticação de administrador (login, persistência de token JWT, validação de perfil ADMIN, rotas protegidas e dashboard administrativo inicial).

**Architecture:** Aplicação Single-Page (SPA) com React 19, TypeScript e React Router 7. Utiliza um `AuthContext` centralizado para gerenciamento do token JWT e do perfil do usuário obtido via `GET /users/me`, cliente Axios com interceptors para injeção automática de `Bearer <token>` e tratamento de erro 401, Tailwind CSS para estilização moderna e acessível, e suíte de testes unitários/integração com Vitest e React Testing Library.

**Tech Stack:** React 19, Vite, TypeScript, React Router 7, Axios, Tailwind CSS, Lucide React, Vitest, React Testing Library, jsdom.

**Spec:** Contexto extraído do backend Spring Boot em `DailyAutism`:
- `POST /auth/login` aceita `{ emailUser, passwordUser }` e retorna `{ token }`
- `GET /users/me` (com Bearer token) retorna `UserDTO` com `{ idUser, nomeUser, emailUser, idPessoa, perfis: string[] }`
- Apenas usuários com `"ADMIN"` em `perfis` têm autorização para acessar os recursos de administração.

## Global Constraints

- O projeto frontend deve residir no subdiretório `frontend/` na raiz do repositório DailyAutism.
- O servidor de desenvolvimento Vite deve rodar na porta 5173 (já configurada no CORS do backend em `SecurityConfig.java`).
- Todas as requisições para a API devem apontar para `http://localhost:8080` (configurável via variável de ambiente `VITE_API_URL`).
- Comunicação de dados tipada com interfaces estritas em TypeScript.
- Nenhuma dependência externa pesada de UI (manter código limpo e modular usando Tailwind CSS e Lucide React).
- Cobertura de testes unitários para serviços, contexto de autenticação, páginas de login e rotas protegidas.

## Review Focus

1. **Credenciais inválidas ou erro 401 na API:** A tela de login deve exibir mensagem amigável e acessível ("E-mail ou senha incorretos") sem travar ou deixar o botão em estado de loading perpétuo.
2. **Usuário autenticado sem perfil ADMIN:** Se um usuário com perfil `RESPONSAVEL` ou `PROFISSIONAL` fizer login no painel administrativo, o sistema deve barrar o acesso e exibir mensagem de permissão negada, impedindo acesso indevido ao dashboard.
3. **Token expirado ou inválido durante a navegação:** O interceptor Axios deve detectar resposta HTTP 401, limpar o armazenamento local e redirecionar imediatamente o usuário para `/login` preservando feedback visual.
4. **Persistência de sessão e carregamento:** Ao recarregar a página (F5) em uma rota protegida, a aplicação deve mostrar estado de carregamento enquanto valida o token com `/users/me` antes de redirecionar precipitadamente para o login.
5. **Validação de campos vazios ou formato de e-mail inválido:** O formulário de login deve validar os campos antes de disparar requisição desnecessária ao backend.

---

### Task 1: Scaffolding e Configuração do Projeto React com Vite, TypeScript e Tailwind CSS

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.app.json`
- Create: `frontend/tsconfig.node.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/App.tsx`
- Create: `frontend/src/index.css`
- Create: `frontend/vitest.config.ts`
- Create: `frontend/src/test/setup.ts`
- Test: `frontend/src/App.test.tsx`

**Interfaces:**
- Consumes: N/A
- Produces: Estrutura base do projeto executável e ambiente de testes Vitest configurado com `@testing-library/react`.

- [ ] **Step 1: Write the failing test for App component mount**

```tsx
// frontend/src/App.test.tsx
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('App Component', () => {
  it('renders initial application title or entry point', () => {
    render(<App />);
    expect(screen.getByText(/DailyAutism/i)).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm --prefix frontend test` (ou `cd frontend && npm test`)
Expected: FAIL with missing modules or files not found.

- [ ] **Step 3: Setup Vite project configuration and minimal App**

Criar os arquivos de configuração do Vite, Tailwind CSS, TypeScript e Vitest:
- `package.json` com scripts `dev`, `build`, `test`.
- Instalar dependências essenciais: `react`, `react-dom`, `react-router-dom`, `axios`, `lucide-react`, `tailwindcss`, `@tailwindcss/vite` ou `postcss`/`autoprefixer`.
- DevDependencies de teste: `vitest`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, `jsdom`.
- Configurar `vite.config.ts` com proxy para `http://localhost:8080` e suporte a Vitest.
- Configurar `src/index.css` com as diretivas do Tailwind.
- Implementar `src/App.tsx` exibindo título básico `DailyAutism Administração`.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm --prefix frontend test`
Expected: PASS (1 test passed)

- [ ] **Step 5: Commit scaffolding**

```bash
git add frontend/
git commit -m "feat(frontend): initialize React Vite project with TypeScript, Tailwind and Vitest"
```

---

### Task 2: Camada de Tipagem e Cliente HTTP Axios com Interceptor de Token

**Files:**
- Create: `frontend/src/types/auth.ts`
- Create: `frontend/src/types/user.ts`
- Create: `frontend/src/services/api.ts`
- Create: `frontend/src/services/authService.ts`
- Test: `frontend/src/services/authService.test.ts`

**Interfaces:**
- Consumes: Backend endpoints `POST /auth/login` e `GET /users/me`.
- Produces:
  - `LoginCredentials`: `{ emailUser: string; passwordUser: string }`
  - `AuthResponse`: `{ token: string }`
  - `UserDTO`: `{ idUser: string; nomeUser: string; emailUser: string; idPessoa?: string | null; perfis: string[]; idResponsavel?: string | null; idProfissional?: string | null }`
  - `authService.login(credentials: LoginCredentials): Promise<AuthResponse>`
  - `authService.getMe(): Promise<UserDTO>`
  - `api`: Instância Axios configurada com interceptors de Request (Bearer token) e Response (401 handler).

- [ ] **Step 1: Write the failing tests for authService**

```typescript
// frontend/src/services/authService.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authService } from './authService';
import api from './api';

vi.mock('./api');

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('login sends credentials and returns token', async () => {
    const mockResponse = { data: { token: 'mock-jwt-token' } };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const result = await authService.login({
      emailUser: 'admin@clinica.com',
      passwordUser: 'admin123',
    });

    expect(api.post).toHaveBeenCalledWith('/auth/login', {
      emailUser: 'admin@clinica.com',
      passwordUser: 'admin123',
    });
    expect(result).toEqual({ token: 'mock-jwt-token' });
  });

  it('getMe requests authenticated user profile', async () => {
    const mockUser = {
      idUser: '123',
      nomeUser: 'Administrador Geral',
      emailUser: 'admin@clinica.com',
      perfis: ['ADMIN'],
    };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockUser });

    const result = await authService.getMe();

    expect(api.get).toHaveBeenCalledWith('/users/me');
    expect(result).toEqual(mockUser);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm --prefix frontend test -- authService.test.ts`
Expected: FAIL with "Cannot find module './authService'".

- [ ] **Step 3: Implement types, api client and authService**

- Criar `frontend/src/types/auth.ts` e `frontend/src/types/user.ts`.
- Criar `frontend/src/services/api.ts` com:
  - `baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080'`
  - Interceptor de Request que lê `localStorage.getItem('dailyautism_token')` e injeta `Authorization: Bearer <token>`.
  - Interceptor de Response que se `status === 401`, dispara evento de logout / remove token.
- Criar `frontend/src/services/authService.ts` implementando `login` e `getMe`.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm --prefix frontend test -- authService.test.ts`
Expected: PASS (2 tests passed).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/types/ frontend/src/services/
git commit -m "feat(frontend): implement API client, auth service and TypeScript types"
```

---

### Task 3: Gerenciamento de Estado de Autenticação (`AuthContext` e `useAuth`)

**Files:**
- Create: `frontend/src/context/AuthContext.tsx`
- Create: `frontend/src/hooks/useAuth.ts`
- Test: `frontend/src/context/AuthContext.test.tsx`

**Interfaces:**
- Consumes: `authService.login`, `authService.getMe`, tipos `UserDTO`, `LoginCredentials`.
- Produces:
  - `AuthContextType`:
    - `user: UserDTO | null`
    - `token: string | null`
    - `isAuthenticated: boolean`
    - `isAdmin: boolean`
    - `loading: boolean`
    - `login: (credentials: LoginCredentials) => Promise<void>`
    - `logout: () => void`
  - `useAuth()` hook para acesso ao contexto.

- [ ] **Step 1: Write the failing tests for AuthContext**

```tsx
// frontend/src/context/AuthContext.test.tsx
import { render, screen, act, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuthProvider } from './AuthContext';
import { useAuth } from '../hooks/useAuth';
import { authService } from '../services/authService';

vi.mock('../services/authService');

const TestConsumer = () => {
  const { user, isAuthenticated, isAdmin, loading, login, logout } = useAuth();
  if (loading) return <div>Carregando...</div>;
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'Autenticado' : 'Nao Autenticado'}</span>
      <span data-testid="admin-status">{isAdmin ? 'Admin' : 'Nao Admin'}</span>
      <span data-testid="user-name">{user?.nomeUser || 'Nenhum'}</span>
      <button onClick={() => login({ emailUser: 'admin@test.com', passwordUser: '123456' })}>
        Entrar
      </button>
      <button onClick={logout}>Sair</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('initializes as unauthenticated when no token in localStorage', () => {
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );
    expect(screen.getByTestId('auth-status')).toHaveTextContent('Nao Autenticado');
    expect(screen.getByTestId('admin-status')).toHaveTextContent('Nao Admin');
  });

  it('performs login, saves token, fetches profile and sets isAdmin true', async () => {
    vi.mocked(authService.login).mockResolvedValueOnce({ token: 'test-jwt' });
    vi.mocked(authService.getMe).mockResolvedValueOnce({
      idUser: '1',
      nomeUser: 'Rafael Admin',
      emailUser: 'admin@test.com',
      perfis: ['ADMIN'],
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Entrar').click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('Autenticado');
      expect(screen.getByTestId('admin-status')).toHaveTextContent('Admin');
      expect(screen.getByTestId('user-name')).toHaveTextContent('Rafael Admin');
    });

    expect(localStorage.getItem('dailyautism_token')).toBe('test-jwt');
  });

  it('clears state and storage on logout', async () => {
    localStorage.setItem('dailyautism_token', 'existing-token');
    vi.mocked(authService.getMe).mockResolvedValueOnce({
      idUser: '1',
      nomeUser: 'Rafael Admin',
      emailUser: 'admin@test.com',
      perfis: ['ADMIN'],
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('Autenticado');
    });

    act(() => {
      screen.getByText('Sair').click();
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('Nao Autenticado');
    expect(localStorage.getItem('dailyautism_token')).toBeNull();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm --prefix frontend test -- AuthContext.test.tsx`
Expected: FAIL with missing modules.

- [ ] **Step 3: Implement AuthProvider and useAuth hook**

- Em `frontend/src/context/AuthContext.tsx`:
  - Definir `AuthContext` com tipos completos.
  - Implementar verificação de token no `localStorage` ao montar o componente com `authService.getMe()`.
  - Calcular `isAdmin = !!user?.perfis?.includes('ADMIN')`.
  - Salvar/remover token em `localStorage`.
- Em `frontend/src/hooks/useAuth.ts`:
  - Hook facilitador que retorna o contexto ou lança erro se usado fora de `AuthProvider`.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm --prefix frontend test -- AuthContext.test.tsx`
Expected: PASS (3 tests passed).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/context/ frontend/src/hooks/
git commit -m "feat(frontend): implement AuthContext and useAuth hook with role validation"
```

---

### Task 4: Tela de Login do Administrador

**Files:**
- Create: `frontend/src/pages/Login.tsx`
- Test: `frontend/src/pages/Login.test.tsx`

**Interfaces:**
- Consumes: `useAuth()`, componentes visuais Tailwind, Lucide React (`Lock`, `Mail`, `AlertCircle`, `Loader2`).
- Produces: Página de Login (`/login`) acessível, responsiva, com validação de formulário em tempo real e exibição de erros do backend.

- [ ] **Step 1: Write the failing tests for Login page**

```tsx
// frontend/src/pages/Login.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import Login from './Login';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../hooks/useAuth');

const renderWithRouter = (ui: React.ReactElement) => {
  return render(<BrowserRouter>{ui}</BrowserRouter>);
};

describe('Login Page', () => {
  it('renders login form elements with title, fields and button', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      loading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });

    renderWithRouter(<Login />);

    expect(screen.getByRole('heading', { name: /painel administrativo/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/senha/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument();
  });

  it('validates empty inputs and does not submit', async () => {
    const mockLogin = vi.fn();
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      loading: false,
      login: mockLogin,
      logout: vi.fn(),
    });

    renderWithRouter(<Login />);
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }));

    expect(await screen.findByText(/informe o e-mail/i)).toBeInTheDocument();
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('submits form with valid data and calls login', async () => {
    const mockLogin = vi.fn().mockResolvedValue(undefined);
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      loading: false,
      login: mockLogin,
      logout: vi.fn(),
    });

    renderWithRouter(<Login />);
    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: 'admin@clinica.com' } });
    fireEvent.change(screen.getByLabelText(/senha/i), { target: { value: 'senha123' } });
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        emailUser: 'admin@clinica.com',
        passwordUser: 'senha123',
      });
    });
  });

  it('displays error message when login fails', async () => {
    const mockLogin = vi.fn().mockRejectedValue({
      response: { data: { errorMessage: 'Credenciais inválidas' }, status: 401 },
    });
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      loading: false,
      login: mockLogin,
      logout: vi.fn(),
    });

    renderWithRouter(<Login />);
    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: 'admin@clinica.com' } });
    fireEvent.change(screen.getByLabelText(/senha/i), { target: { value: 'errada' } });
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }));

    expect(await screen.findByText(/e-mail ou senha incorretos/i)).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm --prefix frontend test -- Login.test.tsx`
Expected: FAIL with "Cannot find module './Login'".

- [ ] **Step 3: Implement Login page component**

- Criar `frontend/src/pages/Login.tsx`:
  - Design limpo com identidade visual do DailyAutism (tons azuis/índigo acolhedores).
  - Ícone da marca, cabeçalho ("DailyAutism", "Painel Administrativo").
  - Inputs com ícones (`Mail`, `Lock`), botão de alternar visualização da senha (`Eye`/`EyeOff`).
  - Tratamento de loading com `Loader2` girando no botão.
  - Alerta de erro estilizado em caso de falha de autenticação.
  - Redirecionamento automático caso `isAuthenticated && isAdmin` já seja verdadeiro.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm --prefix frontend test -- Login.test.tsx`
Expected: PASS (4 tests passed).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/Login.tsx frontend/src/pages/Login.test.tsx
git commit -m "feat(frontend): implement administrator login page with validation and error handling"
```

---

### Task 5: Roteamento Protegido e Layout Administrativo

**Files:**
- Create: `frontend/src/components/ProtectedRoute.tsx`
- Create: `frontend/src/components/AdminLayout.tsx`
- Create: `frontend/src/pages/Dashboard.tsx`
- Test: `frontend/src/components/ProtectedRoute.test.tsx`

**Interfaces:**
- Consumes: `useAuth()`, `Outlet`, `Navigate`, `useLocation` do React Router.
- Produces:
  - `<ProtectedRoute>`: Bloqueia usuários não autenticados e usuários sem role `ADMIN`.
  - `<AdminLayout>`: Barra lateral (Sidebar) com links de navegação, cabeçalho com usuário ativo e botão de logout.
  - `<Dashboard>`: Página inicial com métricas de sistema e boas-vindas.

- [ ] **Step 1: Write the failing tests for ProtectedRoute**

```tsx
// frontend/src/components/ProtectedRoute.test.tsx
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../hooks/useAuth');

describe('ProtectedRoute', () => {
  it('shows loading spinner when auth is verifying', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      loading: true,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/admin" element={<div>Conteúdo Privado</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.queryByText('Conteúdo Privado')).not.toBeInTheDocument();
  });

  it('redirects to /login when user is not authenticated', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      loading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route path="/login" element={<div>Tela de Login</div>} />
          <Route element={<ProtectedRoute />}>
            <Route path="/admin" element={<div>Conteúdo Privado</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Tela de Login')).toBeInTheDocument();
    expect(screen.queryByText('Conteúdo Privado')).not.toBeInTheDocument();
  });

  it('displays access denied when authenticated but not ADMIN', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Pai Responsavel', emailUser: 'pai@test.com', perfis: ['RESPONSAVEL'] },
      token: 'valid-token',
      isAuthenticated: true,
      isAdmin: false,
      loading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/admin" element={<div>Conteúdo Privado</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/acesso negado/i)).toBeInTheDocument();
    expect(screen.queryByText('Conteúdo Privado')).not.toBeInTheDocument();
  });

  it('renders child routes when user is authenticated and ADMIN', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Admin Geral', emailUser: 'admin@test.com', perfis: ['ADMIN'] },
      token: 'valid-token',
      isAuthenticated: true,
      isAdmin: true,
      loading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/admin" element={<div>Conteúdo Privado</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Conteúdo Privado')).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm --prefix frontend test -- ProtectedRoute.test.tsx`
Expected: FAIL with missing ProtectedRoute module.

- [ ] **Step 3: Implement ProtectedRoute, AdminLayout and Dashboard**

- Em `frontend/src/components/ProtectedRoute.tsx`:
  - Se `loading === true`, exibir tela de transição com spinner.
  - Se `!isAuthenticated`, `<Navigate to="/login" replace state={{ from: location }} />`.
  - Se `!isAdmin`, exibir card de Acesso Negado ("Seu usuário não possui permissão de Administrador para acessar este módulo").
  - Caso contrário, renderizar `<Outlet />`.
- Em `frontend/src/components/AdminLayout.tsx`:
  - Sidebar com navegação para:
    - Início (`/admin`)
    - Usuários (`/admin/usuarios`)
    - Clínicas (`/admin/clinicas`)
    - Pacientes (`/admin/pacientes`)
    - Profissionais (`/admin/profissionais`)
    - Planos de Saúde (`/admin/planos`)
  - Topbar com avatar/iniciais, nome do usuário, badge `ADMIN` e botão Sair (`LogOut`).
- Em `frontend/src/pages/Dashboard.tsx`:
  - Boas-vindas personalizadas com `user?.nomeUser`.
  - Cards de acesso rápido para as principais entidades da clínica.
  - Indicador de status do backend.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm --prefix frontend test -- ProtectedRoute.test.tsx`
Expected: PASS (4 tests passed).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/ frontend/src/pages/Dashboard.tsx
git commit -m "feat(frontend): implement ProtectedRoute guard, AdminLayout and Dashboard"
```

---

### Task 6: Integração Final das Rotas em `App.tsx` e Testes End-to-End de Integração

**Files:**
- Modify: `frontend/src/App.tsx`
- Test: `frontend/src/App.test.tsx`

**Interfaces:**
- Consumes: `AuthProvider`, `BrowserRouter`, `Routes`, `Route`, `Login`, `ProtectedRoute`, `AdminLayout`, `Dashboard`.
- Produces: Árvore completa de rotas da aplicação frontend.

- [ ] **Step 1: Write integration test for App routing**

```tsx
// frontend/src/App.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import App from './App';
import { authService } from './services/authService';

vi.mock('./services/authService');

describe('App Routing Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('renders Login page by default when unauthenticated', async () => {
    render(<App />);
    expect(await screen.findByRole('heading', { name: /painel administrativo/i })).toBeInTheDocument();
  });

  it('redirects to /admin dashboard when valid admin token exists', async () => {
    localStorage.setItem('dailyautism_token', 'valid-admin-token');
    vi.mocked(authService.getMe).mockResolvedValueOnce({
      idUser: 'admin-1',
      nomeUser: 'Dra. Ana Paula (Admin)',
      emailUser: 'admin@clinica.com',
      perfis: ['ADMIN'],
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText(/Dra\. Ana Paula/i)).toBeInTheDocument();
      expect(screen.getByText(/visão geral/i)).toBeInTheDocument();
    });
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm --prefix frontend test -- App.test.tsx`
Expected: FAIL due to previous basic App implementation.

- [ ] **Step 3: Update App.tsx with full routing tree**

Configurar `frontend/src/App.tsx`:
```tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './components/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<Dashboard />} />
              {/* Rotas futuras: /admin/usuarios, /admin/clinicas, etc. */}
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/admin" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
```

- [ ] **Step 4: Run full test suite to verify all passes**

Run: `npm --prefix frontend test -- --run`
Expected: PASS (All test files passing).

- [ ] **Step 5: Commit and finalize**

```bash
git add frontend/src/App.tsx frontend/src/App.test.tsx
git commit -m "feat(frontend): integrate complete admin routing and authentication flow"
```

---

### Task 7: Ajuste de Seed no Backend (Criação de Usuário Admin Inicial para Testes Reais)

**Files:**
- Modify: `src/main/resources/application.yml` (garantir segredo JWT válido com 256 bits)
- Modify: `src/main/java/com/clinica/dailyautism/infrastructure/security/DataSeeder.java`

**Interfaces:**
- Consumes: `UserRepository`, `PerfilRepository`, `PasswordEncoder`.
- Produces: Usuário administrador padrão persistido no banco ao inicializar a aplicação para permitir o login no frontend (`admin@dailyautism.com` / `admin123`).

- [ ] **Step 1: Write verification check for DataSeeder admin creation**

Verificar se o `DataSeeder.java` já checa e insere o usuário padrão `admin@dailyautism.com` caso a tabela de usuários esteja vazia.

- [ ] **Step 2: Update DataSeeder.java and application.yml**

1. Em `src/main/resources/application.yml`:
Substituir o segredo fraco por uma chave de 256 bits ou variável de ambiente com fallback seguro:
```yaml
api:
  security:
    token:
      secret: ${JWT_SECRET:DailyAutismSecretKeyForJwtTokenGenerationMustBe256BitsLong2026!}
```
2. Em `DataSeeder.java`:
Adicionar injeção de `UserRepository` e `PasswordEncoder`. No método `run`, chamar `seedAdminUser()`:
```java
private void seedAdminUser() {
    if (userRepository.findByEmailUser("admin@dailyautism.com").isEmpty()) {
        Perfil adminPerfil = perfilRepository.findByNome("ADMIN").orElseThrow();
        User admin = User.builder()
                .nomeUser("Administrador DailyAutism")
                .emailUser("admin@dailyautism.com")
                .passwordUser(passwordEncoder.encode("admin123"))
                .perfis(Set.of(adminPerfil))
                .build();
        userRepository.save(admin);
    }
}
```

- [ ] **Step 3: Verify build and backend readiness**

Confirmar que os arquivos foram salvos e compilam sem erros de sintaxe.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/application.yml src/main/java/com/clinica/dailyautism/infrastructure/security/DataSeeder.java
git commit -m "fix(backend): set compliant 256-bit JWT secret and seed default admin user"
```

