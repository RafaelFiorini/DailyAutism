import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Agenda from './Agenda';
import { compromissoService } from '../services/compromissoService';
import type { Compromisso } from '../types/compromisso';

vi.mock('../services/compromissoService', () => ({
  compromissoService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    aprovar: vi.fn(),
    rejeitar: vi.fn(),
    updateStatus: vi.fn(),
    listTipos: vi.fn(),
  },
}));

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn().mockReturnValue({
    isResponsavel: false,
    isProfissional: false,
    isAdmin: true,
  }),
}));

vi.mock('../services/profissionalService', () => ({
  profissionalService: {
    list: vi.fn().mockResolvedValue([
      { idProf: 'prof-1', nomeProfissional: 'Dra. Camila Santos', conselhoProf: 'CRFa 1234' },
    ]),
  },
}));

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn().mockResolvedValue([]),
  },
}));

vi.mock('../services/periodicidadeService', () => ({
  periodicidadeService: {
    list: vi.fn().mockResolvedValue([]),
  },
}));

const mockCompromissos: Compromisso[] = [
  {
    idCompromisso: 'c1',
    tituloCompromisso: 'Sessão Semanal Fono',
    descricaoCompromisso: 'Foco em articulação',
    dataHoraCompromisso: '2026-11-10T14:00:00',
    localCompromisso: 'Sala 02',
    status: 'AGENDADO',
    idPaciente: 'pac-1',
    nomePaciente: 'Lucas Silva',
    idTipoCompromisso: 'tipo-1',
    nomeTipoCompromisso: 'Fonoaudiologia',
    idPeriodicidade: 'p-1',
    nomePeriodicidade: 'Semanal',
    idProfissional: 'prof-1',
    nomeProfissional: 'Dra. Camila Santos',
    conselhoProfissional: 'CRFa 1234',
    idUser: 'u-1',
  },
  {
    idCompromisso: 'c2',
    tituloCompromisso: 'Avaliação Inicial ABA',
    descricaoCompromisso: 'Entrevista',
    dataHoraCompromisso: '2026-11-12T10:00:00',
    localCompromisso: 'Consultório 01',
    status: 'AGUARDANDO_APROVACAO',
    idPaciente: 'pac-2',
    nomePaciente: 'Beatriz Costa',
    idTipoCompromisso: 'tipo-2',
    nomeTipoCompromisso: 'Psicologia / ABA',
    idPeriodicidade: 'p-2',
    nomePeriodicidade: 'Avulsa',
    idProfissional: 'prof-2',
    nomeProfissional: 'Dr. Roberto Lima',
    idUser: 'u-1',
  },
];

describe('Agenda Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renderiza os compromissos corretamente após o carregamento incluindo profissional', async () => {
    vi.mocked(compromissoService.list).mockResolvedValue(mockCompromissos);

    render(<Agenda />);

    expect(screen.getByText(/carregando agendamentos/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Sessão Semanal Fono')).toBeInTheDocument();
      expect(screen.getByText('Lucas Silva')).toBeInTheDocument();
      expect(screen.getByText('Fonoaudiologia')).toBeInTheDocument();
      expect(screen.getAllByText('Dra. Camila Santos').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('CRFa 1234')).toBeInTheDocument();
      expect(screen.getByText('Avaliação Inicial ABA')).toBeInTheDocument();
      expect(screen.getByText('Beatriz Costa')).toBeInTheDocument();
      expect(screen.getByText('Psicologia / ABA')).toBeInTheDocument();
      expect(screen.getByText('Dr. Roberto Lima')).toBeInTheDocument();
    });
  });

  it('permite filtrar por busca textual (nome do paciente ou profissional)', async () => {
    vi.mocked(compromissoService.list).mockResolvedValue(mockCompromissos);

    render(<Agenda />);

    await waitFor(() => {
      expect(screen.getByText('Lucas Silva')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/buscar por paciente/i);
    fireEvent.change(searchInput, { target: { value: 'Roberto' } });

    expect(screen.queryByText('Lucas Silva')).not.toBeInTheDocument();
    expect(screen.getByText('Beatriz Costa')).toBeInTheDocument();
    expect(screen.getByText('Dr. Roberto Lima')).toBeInTheDocument();
  });

  it('permite aprovar um compromisso pendente', async () => {
    vi.mocked(compromissoService.list).mockResolvedValue(mockCompromissos);
    vi.mocked(compromissoService.aprovar).mockResolvedValue([]);
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<Agenda />);

    await waitFor(() => {
      expect(screen.getByText('Avaliação Inicial ABA')).toBeInTheDocument();
    });

    const btnAprovar = screen.getByTitle('Aprovar Agendamento');
    fireEvent.click(btnAprovar);

    await waitFor(() => {
      expect(compromissoService.aprovar).toHaveBeenCalledWith('c2');
    });
  });
});
