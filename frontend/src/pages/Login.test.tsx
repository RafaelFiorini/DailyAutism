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

