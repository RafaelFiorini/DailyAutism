import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Pacientes from './Pacientes';
import { pacienteService } from '../services/pacienteService';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../hooks/useAuth');

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn(),
    delete: vi.fn(),
    listPlanos: vi.fn().mockResolvedValue([]),
    listClinicas: vi.fn().mockResolvedValue([]),
  },
}));

const mockPacientes = [
  {
    idPaciente: '1',
    nomePessoa: 'Lucas Silva',
    apelidoPaciente: 'Luquinhas',
    cpf: '12345678901',
    isResponsavel: true,
    planos: ['Unimed'],
    clinicas: ['Clínica Kids'],
    responsaveis: ['Carlos Silva'],
  },
  {
    idPaciente: '2',
    nomePessoa: 'Beatriz Costa',
    apelidoPaciente: 'Bia',
    cpf: '98765432100',
    isResponsavel: false,
    planos: [],
    clinicas: [],
    responsaveis: ['Ana Costa'],
  },
];

describe('Pacientes Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Admin', perfis: ['ADMIN'] },
      isAdmin: true,
      isAdminClinica: false,
      isFuncionarioClinica: false,
      isResponsavel: false,
      isProfissional: false,
      isAuthenticated: true,
      loading: false,
    } as any);
  });

  it('renderiza o estado de carregamento inicialmente', () => {
    vi.mocked(pacienteService.list).mockReturnValueOnce(new Promise(() => {}));
    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );
    expect(screen.getByText('Carregando pacientes...')).toBeInTheDocument();
  });

  it('renderiza a lista de pacientes com sucesso', async () => {
    vi.mocked(pacienteService.list).mockResolvedValueOnce(mockPacientes);
    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Lucas Silva')).toBeInTheDocument();
      expect(screen.getByText('Beatriz Costa')).toBeInTheDocument();
      expect(screen.getByText('É o Responsável')).toBeInTheDocument();
      expect(screen.getByText('Unimed')).toBeInTheDocument();
      expect(screen.getByText('Clínica Kids')).toBeInTheDocument();
    });
  });

  it('filtra a lista de pacientes pela barra de busca', async () => {
    vi.mocked(pacienteService.list).mockResolvedValueOnce(mockPacientes);
    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Lucas Silva')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar por nome, apelido, CPF/i);
    fireEvent.change(searchInput, { target: { value: 'Bia' } });

    expect(screen.queryByText('Lucas Silva')).not.toBeInTheDocument();
    expect(screen.getByText('Beatriz Costa')).toBeInTheDocument();
  });

  it('abre modal ao clicar em Novo Paciente', async () => {
    vi.mocked(pacienteService.list).mockResolvedValueOnce([]);
    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Nenhum paciente encontrado')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Novo Paciente/i }));

    expect(screen.getByText('Cadastrar Novo Paciente')).toBeInTheDocument();
  });

  it('exibe limite de terapias e status de vencimento para paciente particular', async () => {
    vi.mocked(pacienteService.list).mockResolvedValueOnce([
      {
        idPaciente: '3',
        nomePessoa: 'Pedro Particular',
        cpf: '22233344455',
        isResponsavel: false,
        planos: [],
        clinicas: [],
        responsaveis: ['Mariana'],
        quantidadeSessoesLiberadas: 10,
        vencimentoLiberacao: '2026-12-15',
        diasParaVencimento: 5,
        statusGuia: 'VENCENDO_EM_BREVE',
      },
    ]);

    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Pedro Particular')).toBeInTheDocument();
      expect(screen.getByText('Particular')).toBeInTheDocument();
      expect(screen.getByText('Vence em 5d')).toBeInTheDocument();
      expect(screen.getByText('10 sessões')).toBeInTheDocument();
      expect(screen.getByText('até 15/12/2026')).toBeInTheDocument();
    });
  });

  it('exibe título "Pacientes da Clínica" e botão "Novo Paciente" para usuário admin de clínica que também é profissional', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '2', nomeUser: 'Alexandra', perfis: ['ADMIN_CLINICA', 'PROFISSIONAL'] },
      isAdmin: false,
      isAdminClinica: true,
      isFuncionarioClinica: false,
      isResponsavel: false,
      isProfissional: true,
      isAuthenticated: true,
      loading: false,
    } as any);

    vi.mocked(pacienteService.list).mockResolvedValueOnce(mockPacientes);

    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1, name: /Pacientes da Clínica/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Novo Paciente/i })).toBeInTheDocument();
      expect(screen.queryByText('Pacientes em Atendimento')).not.toBeInTheDocument();
    });
  });

  it('exibe título "Pacientes em Atendimento" quando usuário for apenas profissional', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '3', nomeUser: 'Dr. Roberto', perfis: ['PROFISSIONAL'] },
      isAdmin: false,
      isAdminClinica: false,
      isFuncionarioClinica: false,
      isResponsavel: false,
      isProfissional: true,
      isAuthenticated: true,
      loading: false,
    } as any);

    vi.mocked(pacienteService.list).mockResolvedValueOnce(mockPacientes);

    render(
      <MemoryRouter>
        <Pacientes />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1, name: /Pacientes em Atendimento/i })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Novo Paciente/i })).not.toBeInTheDocument();
    });
  });
});
