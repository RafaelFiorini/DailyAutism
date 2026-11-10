import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CompromissoModal from './CompromissoModal';
import { compromissoService } from '../services/compromissoService';
import { pacienteService } from '../services/pacienteService';
import { periodicidadeService } from '../services/periodicidadeService';
import { profissionalService } from '../services/profissionalService';

vi.mock('../services/compromissoService', () => ({
  compromissoService: {
    create: vi.fn(),
    update: vi.fn(),
    listTipos: vi.fn(),
  },
}));

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn(),
  },
}));

vi.mock('../services/periodicidadeService', () => ({
  periodicidadeService: {
    list: vi.fn(),
  },
}));

vi.mock('../services/profissionalService', () => ({
  profissionalService: {
    list: vi.fn(),
  },
}));

describe('CompromissoModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(pacienteService.list).mockResolvedValue([
      { idPaciente: 'pac-1', nomePessoa: 'Lucas Silva', CPFPessoa: '123' } as any,
    ]);
    vi.mocked(compromissoService.listTipos).mockResolvedValue([
      { idTipoCompromisso: 'tipo-1', nomeTipo: 'Fonoaudiologia' },
    ]);
    vi.mocked(periodicidadeService.list).mockResolvedValue([
      { idPeriodicidade: 'p-1', nomePeriodicidade: 'Semanal', diasIntervalo: 7 },
    ]);
    vi.mocked(profissionalService.list).mockResolvedValue([
      { idProf: 'prof-1', nomeProfissional: 'Dra. Camila Santos', conselhoProf: 'CRFa 1234' } as any,
    ]);
  });

  it('renderiza o formulário de novo agendamento e preenche os selects', async () => {
    render(
      <CompromissoModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Novo Agendamento')).toBeInTheDocument();
      expect(screen.getByText('Lucas Silva')).toBeInTheDocument();
      expect(screen.getByText('Fonoaudiologia')).toBeInTheDocument();
      expect(screen.getByText('Semanal (7d)')).toBeInTheDocument();
      expect(screen.getByText(/Dra. Camila Santos/)).toBeInTheDocument();
    });
  });

  it('permite criar um novo agendamento com sucesso associando o profissional', async () => {
    vi.mocked(compromissoService.create).mockResolvedValue({
      idCompromisso: 'c-new',
      tituloCompromisso: 'Sessão Fono',
      status: 'AGUARDANDO_APROVACAO',
      idProfissional: 'prof-1',
    } as any);

    render(
      <CompromissoModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Lucas Silva')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/paciente/i), { target: { value: 'pac-1' } });
    fireEvent.change(screen.getByLabelText(/tipo de atendimento/i), { target: { value: 'tipo-1' } });
    fireEvent.change(screen.getByLabelText(/profissional \/ terapeuta/i), { target: { value: 'prof-1' } });
    fireEvent.change(screen.getByPlaceholderText(/sessão semanal/i), { target: { value: 'Sessão Fono' } });

    const btnSubmit = screen.getByRole('button', { name: /criar agendamento/i });
    fireEvent.click(btnSubmit);

    await waitFor(() => {
      expect(compromissoService.create).toHaveBeenCalledWith(
        expect.objectContaining({
          idPaciente: 'pac-1',
          idTipoCompromisso: 'tipo-1',
          idProfissional: 'prof-1',
          tituloCompromisso: 'Sessão Fono',
        })
      );
      expect(mockOnSuccess).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });
});
