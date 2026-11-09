import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import PeriodicidadeModal from './PeriodicidadeModal';
import { periodicidadeService } from '../services/periodicidadeService';

vi.mock('../services/periodicidadeService', () => ({
  periodicidadeService: {
    create: vi.fn(),
    update: vi.fn(),
  },
}));

describe('PeriodicidadeModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renderiza os campos obrigatórios quando aberto no modo de criação', () => {
    render(
      <PeriodicidadeModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    expect(screen.getByRole('heading', { name: /Nova Periodicidade/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/Nome da Periodicidade \*/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Intervalo em Dias/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Salvar/i })).toBeInTheDocument();
  });

  it('permite aplicar um preset rápido no modo de criação', () => {
    render(
      <PeriodicidadeModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    const presetSemanal = screen.getByRole('button', { name: /Semanal \(7d\)/i });
    fireEvent.click(presetSemanal);

    expect(screen.getByLabelText(/Nome da Periodicidade \*/i)).toHaveValue('Semanal');
    expect(screen.getByLabelText(/Intervalo em Dias/i)).toHaveValue(7);
  });

  it('chama periodicidadeService.create ao submeter dados válidos', async () => {
    const mockCreated = {
      idPeriodicidade: 'p1',
      nomePeriodicidade: 'Semanal',
      diasIntervalo: 7,
      descricaoPeriodicidade: 'Atendimento semanal',
    };
    vi.mocked(periodicidadeService.create).mockResolvedValueOnce(mockCreated);

    render(
      <PeriodicidadeModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    fireEvent.change(screen.getByLabelText(/Nome da Periodicidade \*/i), {
      target: { value: 'Semanal' },
    });
    fireEvent.change(screen.getByLabelText(/Intervalo em Dias/i), {
      target: { value: '7' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Salvar/i }));

    await waitFor(() => {
      expect(periodicidadeService.create).toHaveBeenCalledWith({
        nomePeriodicidade: 'Semanal',
        diasIntervalo: 7,
        descricaoPeriodicidade: undefined,
      });
      expect(mockOnSuccess).toHaveBeenCalledWith(mockCreated);
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it('preenche campos e chama update no modo de edição', async () => {
    const initial = {
      idPeriodicidade: 'p1',
      nomePeriodicidade: 'Quinzenal',
      diasIntervalo: 14,
      descricaoPeriodicidade: 'A cada 15 dias',
    };
    vi.mocked(periodicidadeService.update).mockResolvedValueOnce({
      ...initial,
      nomePeriodicidade: 'Quinzenal VIP',
    });

    render(
      <PeriodicidadeModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
        initialData={initial}
      />
    );

    expect(screen.getByRole('heading', { name: /Editar Periodicidade/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/Nome da Periodicidade \*/i)).toHaveValue('Quinzenal');
    expect(screen.getByLabelText(/Intervalo em Dias/i)).toHaveValue(14);

    fireEvent.change(screen.getByLabelText(/Nome da Periodicidade \*/i), {
      target: { value: 'Quinzenal VIP' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Atualizar/i }));

    await waitFor(() => {
      expect(periodicidadeService.update).toHaveBeenCalledWith('p1', {
        nomePeriodicidade: 'Quinzenal VIP',
        diasIntervalo: 14,
        descricaoPeriodicidade: 'A cada 15 dias',
      });
      expect(mockOnClose).toHaveBeenCalled();
    });
  });
});
