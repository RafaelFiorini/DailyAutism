import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import PlanosSaude from './PlanosSaude';
import { planoSaudeService } from '../services/planoSaudeService';

vi.mock('../services/planoSaudeService', () => ({
  planoSaudeService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));

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

  it('abre o modal ao clicar no botão Novo Plano de Saúde', async () => {
    vi.mocked(planoSaudeService.list).mockResolvedValueOnce(mockPlanos);

    render(<PlanosSaude />);

    await waitFor(() => {
      expect(screen.getByText('Amil Fácil S60')).toBeInTheDocument();
    });

    const newBtn = screen.getByRole('button', { name: /Novo Plano de Saúde/i });
    fireEvent.click(newBtn);

    expect(screen.getByRole('heading', { name: /Novo Plano de Saúde/i })).toBeInTheDocument();
  });
});
