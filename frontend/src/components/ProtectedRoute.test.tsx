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
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: true,
      login: vi.fn(),
      register: vi.fn(),
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
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
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

  it('allows access when authenticated even if not ADMIN (e.g. RESPONSAVEL)', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Pai Responsavel', emailUser: 'pai@test.com', perfis: ['RESPONSAVEL'] },
      token: 'valid-token',
      isAuthenticated: true,
      isAdmin: false,
      isResponsavel: true,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
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

  it('renders child routes when user is authenticated and ADMIN', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Admin Geral', emailUser: 'admin@test.com', perfis: ['ADMIN'] },
      token: 'valid-token',
      isAuthenticated: true,
      isAdmin: true,
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
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

