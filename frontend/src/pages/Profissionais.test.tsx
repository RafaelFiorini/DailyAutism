import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Profissionais from './Profissionais';
import { profissionalService } from '../services/profissionalService';

vi.mock('../services/profissionalService');

const mockProfissionais = [
  {
    idProf: '1',
    idPessoa: 'p-1',
    nomeProfissional: 'Dr. Lucas Silveira',
    cpf: '11122233344',
    email: 'lucas@clinica.com',
    telefone: '11999991111',
    conselhoProf: 'CRM/SP 98765',
  },
  {
    idProf: '2',
    idPessoa: 'p-2',
    nomeProfissional: 'Dra. Beatriz Lima',
    cpf: '55566677788',
    email: 'beatriz@clinica.com',
    telefone: '11988882222',
    conselhoProf: 'CRP 06/54321',
  },
];

describe('Profissionais Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders loading state and then lists professionals', async () => {
    vi.mocked(profissionalService.list).mockResolvedValueOnce(mockProfissionais);

    render(<Profissionais />);

    expect(screen.getByText(/carregando profissionais/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Dr. Lucas Silveira')).toBeInTheDocument();
      expect(screen.getByText('Dra. Beatriz Lima')).toBeInTheDocument();
      expect(screen.getByText('CRM/SP 98765')).toBeInTheDocument();
      expect(screen.getByText('CRP 06/54321')).toBeInTheDocument();
    });
  });

  it('filters professionals by name or council', async () => {
    vi.mocked(profissionalService.list).mockResolvedValueOnce(mockProfissionais);

    render(<Profissionais />);

    await waitFor(() => {
      expect(screen.getByText('Dr. Lucas Silveira')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/buscar por nome, conselho ou cpf/i);
    fireEvent.change(searchInput, { target: { value: 'Beatriz' } });

    expect(screen.queryByText('Dr. Lucas Silveira')).not.toBeInTheDocument();
    expect(screen.getByText('Dra. Beatriz Lima')).toBeInTheDocument();
  });

  it('shows empty state when no professionals match search', async () => {
    vi.mocked(profissionalService.list).mockResolvedValueOnce(mockProfissionais);

    render(<Profissionais />);

    await waitFor(() => {
      expect(screen.getByText('Dr. Lucas Silveira')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/buscar por nome, conselho ou cpf/i);
    fireEvent.change(searchInput, { target: { value: 'Inexistente' } });

    expect(screen.getByText(/nenhum profissional encontrado/i)).toBeInTheDocument();
  });

  it('opens modal when clicking Novo Profissional button', async () => {
    vi.mocked(profissionalService.list).mockResolvedValueOnce([]);

    render(<Profissionais />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /novo profissional/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /novo profissional/i }));

    expect(screen.getByRole('heading', { name: /cadastrar novo profissional/i })).toBeInTheDocument();
  });
});

