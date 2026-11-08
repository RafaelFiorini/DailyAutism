import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Responsaveis from './Responsaveis';
import { responsavelService } from '../services/responsavelService';

vi.mock('../services/responsavelService');

const mockResponsaveis = [
  {
    idResponsavel: '1',
    idPessoa: 'p-1',
    nomeResponsavel: 'Carlos Eduardo',
    cpf: '11122233344',
    email: 'carlos@email.com',
    telefone: '11999991111',
    isPaciente: false,
    pacientes: [{ idPaciente: 'pac-1', nomePaciente: 'Gabriel Eduardo' }],
  },
  {
    idResponsavel: '2',
    idPessoa: 'p-2',
    nomeResponsavel: 'Fernanda Rocha',
    cpf: '55566677788',
    email: 'fernanda@email.com',
    telefone: '11988882222',
    isPaciente: true,
    pacientes: [{ idPaciente: 'pac-2', nomePaciente: 'Fernanda Rocha' }],
  },
];

describe('Responsaveis Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(responsavelService.listPacientes).mockResolvedValue([]);
  });

  it('renders loading state and then lists responsaveis', async () => {
    vi.mocked(responsavelService.list).mockResolvedValueOnce(mockResponsaveis);

    render(<Responsaveis />);

    expect(screen.getByText(/carregando responsáveis/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Carlos Eduardo')).toBeInTheDocument();
      expect(screen.getAllByText('Fernanda Rocha').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('Gabriel Eduardo')).toBeInTheDocument();
      expect(screen.getByText(/também é paciente/i)).toBeInTheDocument();
    });
  });

  it('filters responsaveis by name, CPF or patient name', async () => {
    vi.mocked(responsavelService.list).mockResolvedValueOnce(mockResponsaveis);

    render(<Responsaveis />);

    await waitFor(() => {
      expect(screen.getByText('Carlos Eduardo')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/buscar por nome, cpf ou paciente/i);
    fireEvent.change(searchInput, { target: { value: 'Fernanda' } });

    expect(screen.queryByText('Carlos Eduardo')).toBeNull();
    expect(screen.getAllByText('Fernanda Rocha').length).toBeGreaterThanOrEqual(1);

    // Search by linked patient
    fireEvent.change(searchInput, { target: { value: 'Gabriel' } });
    expect(screen.getByText('Carlos Eduardo')).toBeInTheDocument();
    expect(screen.queryByText('Fernanda Rocha')).toBeNull();
  });

  it('shows empty state when no results match search', async () => {
    vi.mocked(responsavelService.list).mockResolvedValueOnce(mockResponsaveis);

    render(<Responsaveis />);

    await waitFor(() => {
      expect(screen.getByText('Carlos Eduardo')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/buscar por nome, cpf ou paciente/i);
    fireEvent.change(searchInput, { target: { value: 'Inexistente' } });

    expect(screen.getByText(/nenhum responsável encontrado/i)).toBeInTheDocument();
  });

  it('opens registration modal when clicking Novo Responsável button', async () => {
    vi.mocked(responsavelService.list).mockResolvedValueOnce([]);

    render(<Responsaveis />);

    await waitFor(() => {
      expect(screen.getByText(/novo responsável/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /novo responsável/i }));

    expect(screen.getByText(/cadastrar novo responsável/i)).toBeInTheDocument();
  });
});
