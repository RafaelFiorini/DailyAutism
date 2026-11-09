import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Periodicidades from './Periodicidades';
import { periodicidadeService } from '../services/periodicidadeService';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../services/periodicidadeService', () => ({
  periodicidadeService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../hooks/useAuth');

const mockPeriodicidades = [
  {
    idPeriodicidade: 'p1',
    nomePeriodicidade: 'Semanal',
    diasIntervalo: 7,
    descricaoPeriodicidade: 'Atendimento 1 vez por semana',
  },
  {
    idPeriodicidade: 'p2',
    nomePeriodicidade: 'Quinzenal',
    diasIntervalo: 14,
    descricaoPeriodicidade: 'Atendimento a cada duas semanas',
  },
];

describe('Periodicidades Page', () => {
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

  it('exibe tela de acesso restrito para perfis comuns (ex: RESPONSAVEL)', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '2', nomeUser: 'Pai', emailUser: 'pai@test.com', perfis: ['RESPONSAVEL'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: false,
      isAdminClinica: false,
      isResponsavel: true,
      isProfissional: false,
      isFuncionarioClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    render(<Periodicidades />);

    expect(screen.getByText('Acesso Restrito')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Nova Periodicidade/i })).not.toBeInTheDocument();
  });

  it('permite acesso e renderiza a lista para ADMIN', async () => {
    vi.mocked(periodicidadeService.list).mockResolvedValueOnce(mockPeriodicidades);

    render(<Periodicidades />);

    await waitFor(() => {
      expect(screen.getByText('Semanal')).toBeInTheDocument();
      expect(screen.getByText('Quinzenal')).toBeInTheDocument();
      expect(screen.getByText('7 dias')).toBeInTheDocument();
      expect(screen.getByText('14 dias')).toBeInTheDocument();
      expect(screen.getByText('Atendimento 1 vez por semana')).toBeInTheDocument();
    });
  });

  it('permite acesso e renderiza a lista para ADMIN_CLINICA', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '3', nomeUser: 'Gestor Clinica', emailUser: 'gestor@clinica.com', perfis: ['ADMIN_CLINICA'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: false,
      isAdminClinica: true,
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: true,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });
    vi.mocked(periodicidadeService.list).mockResolvedValueOnce(mockPeriodicidades);

    render(<Periodicidades />);

    await waitFor(() => {
      expect(screen.getByText('Semanal')).toBeInTheDocument();
      expect(screen.getByText('Quinzenal')).toBeInTheDocument();
    });
  });

  it('filtra a lista pelo campo de busca', async () => {
    vi.mocked(periodicidadeService.list).mockResolvedValueOnce(mockPeriodicidades);

    render(<Periodicidades />);

    await waitFor(() => {
      expect(screen.getByText('Semanal')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar por nome da periodicidade/i);
    fireEvent.change(searchInput, { target: { value: 'Quinzenal' } });

    expect(screen.queryByText('Semanal')).not.toBeInTheDocument();
    expect(screen.getByText('Quinzenal')).toBeInTheDocument();
  });

  it('abre o modal ao clicar no botão Nova Periodicidade', async () => {
    vi.mocked(periodicidadeService.list).mockResolvedValueOnce(mockPeriodicidades);

    render(<Periodicidades />);

    await waitFor(() => {
      expect(screen.getByText('Semanal')).toBeInTheDocument();
    });

    const newBtn = screen.getByRole('button', { name: /Nova Periodicidade/i });
    fireEvent.click(newBtn);

    expect(screen.getByRole('heading', { name: /Nova Periodicidade/i })).toBeInTheDocument();
  });
});
