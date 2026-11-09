import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import PlanosSaude from './PlanosSaude';
import { planoSaudeService } from '../services/planoSaudeService';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../services/planoSaudeService', () => ({
  planoSaudeService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../hooks/useAuth');

const mockPlanos = [
  {
    idPlanoSaude: 'p1',
    nomePlano: 'Amil Fácil S60',
    operadora: 'Amil',
    ansReg: '417505',
  },
  {
    idPlanoSaude: 'p2',
    nomePlano: 'Unimed Pleno Nacional',
    operadora: 'Unimed',
    ansReg: '305541',
  },
];

describe('PlanosSaude Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Admin User', emailUser: 'admin@test.com', perfis: ['ADMIN'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: true,
      isAdminClinica: false,
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });
  });

  it('renderiza a lista de planos de saúde para qualquer usuário autenticado', async () => {
    vi.mocked(planoSaudeService.list).mockResolvedValueOnce(mockPlanos);

    render(<PlanosSaude />);

    await waitFor(() => {
      expect(screen.getByText('Amil Fácil S60')).toBeInTheDocument();
      expect(screen.getByText('Unimed Pleno Nacional')).toBeInTheDocument();
      expect(screen.getByText('Amil')).toBeInTheDocument();
      expect(screen.getByText('Unimed')).toBeInTheDocument();
      expect(screen.getByText('ANS: 417505')).toBeInTheDocument();
      expect(screen.getByText('ANS: 305541')).toBeInTheDocument();
    });
  });

  it('filtra a lista de planos pelo campo de busca', async () => {
    vi.mocked(planoSaudeService.list).mockResolvedValueOnce(mockPlanos);

    render(<PlanosSaude />);

    await waitFor(() => {
      expect(screen.getByText('Amil Fácil S60')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar por nome do plano/i);
    fireEvent.change(searchInput, { target: { value: 'Unimed' } });

    expect(screen.queryByText('Amil Fácil S60')).not.toBeInTheDocument();
    expect(screen.getByText('Unimed Pleno Nacional')).toBeInTheDocument();
  });

  it('abre o modal ao clicar no botão Novo Plano de Saúde para ADMIN ou ADMIN_CLINICA', async () => {
    vi.mocked(planoSaudeService.list).mockResolvedValueOnce(mockPlanos);

    render(<PlanosSaude />);

    await waitFor(() => {
      expect(screen.getByText('Amil Fácil S60')).toBeInTheDocument();
    });

    const newBtn = screen.getByRole('button', { name: /Novo Plano de Saúde/i });
    fireEvent.click(newBtn);

    expect(screen.getByRole('heading', { name: /Novo Plano de Saúde/i })).toBeInTheDocument();
  });

  it('exibe badge de modo somente leitura e esconde ações para perfis não administradores', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '2', nomeUser: 'Recepcionista', emailUser: 'recep@test.com', perfis: ['FUNCIONARIO_CLINICA'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: false,
      isAdminClinica: false,
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: true,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });
    vi.mocked(planoSaudeService.list).mockResolvedValueOnce(mockPlanos);

    render(<PlanosSaude />);

    await waitFor(() => {
      expect(screen.getByText('Amil Fácil S60')).toBeInTheDocument();
    });

    expect(screen.getByText(/Modo Somente Leitura/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Novo Plano de Saúde/i })).not.toBeInTheDocument();
    expect(screen.queryByTitle(/Editar plano de saúde/i)).not.toBeInTheDocument();
    expect(screen.queryByTitle(/Desativar plano de saúde/i)).not.toBeInTheDocument();
  });
});
