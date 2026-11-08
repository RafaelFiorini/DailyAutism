import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Usuarios from './Usuarios';
import { userService } from '../services/userService';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../services/userService', () => ({
  userService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../hooks/useAuth');

const mockUsers = [
  {
    idUser: '1',
    nomeUser: 'Dra. Ana Paula',
    emailUser: 'ana@clinica.com',
    cpf: '12345678901',
    celular: '11999998888',
    perfis: ['PROFISSIONAL', 'RESPONSAVEL'],
    isResponsavel: true,
    isProfissional: true,
    conselhoProf: 'CRP 06/1234',
    isAdmin: false,
  },
  {
    idUser: '2',
    nomeUser: 'Carlos Silva',
    emailUser: 'carlos@email.com',
    cpf: '98765432100',
    celular: '11988887777',
    perfis: ['RESPONSAVEL'],
    isResponsavel: true,
    isProfissional: false,
    isAdmin: false,
  },
];

describe('Usuarios Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('exibe tela de acesso restrito se o usuário não for ADMIN', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Pai', emailUser: 'pai@test.com', perfis: ['RESPONSAVEL'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: false,
      isResponsavel: true,
      isProfissional: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    render(<Usuarios />);
    expect(screen.getByText('Acesso Restrito')).toBeInTheDocument();
  });

  it('renderiza lista de usuários com papéis Responsável e Profissional para ADMIN', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: 'admin', nomeUser: 'Admin', emailUser: 'admin@dailyautism.com', perfis: ['ADMIN'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: true,
      isResponsavel: false,
      isProfissional: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    vi.mocked(userService.list).mockResolvedValueOnce(mockUsers);

    render(<Usuarios />);

    await waitFor(() => {
      expect(screen.getByText('Dra. Ana Paula')).toBeInTheDocument();
      expect(screen.getByText('Carlos Silva')).toBeInTheDocument();
      // Dra. Ana Paula tem ambos os perfis:
      expect(screen.getAllByText(/Responsável/).length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByText(/Profissional/).length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('(CRP 06/1234)')).toBeInTheDocument();
    });
  });

  it('filtra a lista de usuários pelo campo de busca', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: 'admin', nomeUser: 'Admin', emailUser: 'admin@dailyautism.com', perfis: ['ADMIN'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: true,
      isResponsavel: false,
      isProfissional: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    vi.mocked(userService.list).mockResolvedValueOnce(mockUsers);

    render(<Usuarios />);

    await waitFor(() => {
      expect(screen.getByText('Dra. Ana Paula')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar por nome, e-mail/i);
    fireEvent.change(searchInput, { target: { value: 'Carlos' } });

    expect(screen.queryByText('Dra. Ana Paula')).not.toBeInTheDocument();
    expect(screen.getByText('Carlos Silva')).toBeInTheDocument();
  });
});
