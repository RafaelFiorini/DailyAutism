import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Pacientes from './Pacientes';
import { pacienteService } from '../services/pacienteService';

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
  });

  it('renderiza o estado de carregamento inicialmente', () => {
    vi.mocked(pacienteService.list).mockReturnValueOnce(new Promise(() => {}));
    render(<Pacientes />);
    expect(screen.getByText('Carregando pacientes...')).toBeInTheDocument();
  });

  it('renderiza a lista de pacientes com sucesso', async () => {
    vi.mocked(pacienteService.list).mockResolvedValueOnce(mockPacientes);
    render(<Pacientes />);

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
    render(<Pacientes />);

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
    render(<Pacientes />);

    await waitFor(() => {
      expect(screen.getByText('Nenhum paciente encontrado')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Novo Paciente/i }));

    expect(screen.getByText('Cadastrar Novo Paciente')).toBeInTheDocument();
  });
});
