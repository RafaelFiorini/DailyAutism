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

