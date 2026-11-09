import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import PlanoSaudeModal from './PlanoSaudeModal';
import { planoSaudeService } from '../services/planoSaudeService';

vi.mock('../services/planoSaudeService', () => ({
  planoSaudeService: {
    create: vi.fn(),
    update: vi.fn(),
    list: vi.fn(),
  },
}));

describe('PlanoSaudeModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('não renderiza quando isOpen é false', () => {
    render(<PlanoSaudeModal isOpen={false} onClose={vi.fn()} onSuccess={vi.fn()} />);
    expect(screen.queryByText(/Novo Plano de Saúde/i)).not.toBeInTheDocument();
  });

  it('renderiza os campos obrigatórios quando aberto no modo de criação', () => {
    render(<PlanoSaudeModal isOpen={true} onClose={vi.fn()} onSuccess={vi.fn()} />);

    expect(screen.getByRole('heading', { name: /Novo Plano de Saúde/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Amil Fácil S60/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Amil, Unimed, Bradesco/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/417505/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Cadastrar Plano/i })).toBeInTheDocument();
  });

  it('submete dados válidos para planoSaudeService.create', async () => {
    const mockSuccess = vi.fn();
    const mockClose = vi.fn();
    vi.mocked(planoSaudeService.create).mockResolvedValueOnce({
      idPlanoSaude: 'new-p1',
      nomePlano: 'Bradesco Top Saúde',
      operadora: 'Bradesco Saúde',
      ansReg: '005711',
    });

    render(<PlanoSaudeModal isOpen={true} onClose={mockClose} onSuccess={mockSuccess} />);

    fireEvent.change(screen.getByPlaceholderText(/Amil Fácil S60/i), {
      target: { value: 'Bradesco Top Saúde' },
    });
    fireEvent.change(screen.getByPlaceholderText(/Amil, Unimed, Bradesco/i), {
      target: { value: 'Bradesco Saúde' },
    });
    fireEvent.change(screen.getByPlaceholderText(/417505/i), {
      target: { value: '005711' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Cadastrar Plano/i }));

    await waitFor(() => {
      expect(planoSaudeService.create).toHaveBeenCalledWith(
        expect.objectContaining({
          nomePlano: 'Bradesco Top Saúde',
          operadora: 'Bradesco Saúde',
          ansReg: '005711',
        })
      );
      expect(mockSuccess).toHaveBeenCalled();
      expect(mockClose).toHaveBeenCalled();
    });
  });
});
