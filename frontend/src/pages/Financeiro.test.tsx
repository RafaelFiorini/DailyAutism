import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Financeiro from './Financeiro';
import { financeiroService } from '../services/financeiroService';

const mockUseAuth = vi.fn();

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('../services/financeiroService', () => ({
  financeiroService: {
    listContratosParticulares: vi.fn(),
    listGuiasConvenio: vi.fn(),
    listPrecos: vi.fn(),
    listPacotes: vi.fn(),
    listPrazosConvenios: vi.fn(),
    savePreco: vi.fn(),
    deletePreco: vi.fn(),
    createPacote: vi.fn(),
    deletePacote: vi.fn(),
    savePrazoConvenio: vi.fn(),
    createContratoParticular: vi.fn(),
    registrarPagamentoContrato: vi.fn(),
    cancelarContratoParticular: vi.fn(),
    listSessoesElegiveis: vi.fn(),
    gerarGuiaConvenio: vi.fn(),
    liberarGuiaAssinatura: vi.fn(),
    assinarGuia: vi.fn(),
    enviarGuiaConvenio: vi.fn(),
    registrarPagamentoGuia: vi.fn(),
    cancelarGuiaConvenio: vi.fn(),
    downloadGuiaPdf: vi.fn(),
  },
}));

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn().mockResolvedValue([]),
    listClinicas: vi.fn().mockResolvedValue([{ idClinica: 'cli-1', nomeClinica: 'Attenzione' }]),
    listPlanos: vi.fn().mockResolvedValue([{ idPlanoSaude: 'pl-1', nomePlano: 'Unimed' }]),
  },
}));

vi.mock('../services/compromissoService', () => ({
  compromissoService: {
    listTipos: vi.fn().mockResolvedValue([{ idTipoCompromisso: 'tp-1', nomeTipo: 'Terapia Ocupacional' }]),
  },
}));

describe('Financeiro Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(financeiroService.listPrecos).mockResolvedValue([
      {
        idTabelaPreco: 'pr-1',
        idClinica: 'cli-1',
        idTipoCompromisso: 'tp-1',
        nomeTipoCompromisso: 'Terapia Ocupacional',
        valorSessao: 100,
        particular: true,
      },
    ]);
    vi.mocked(financeiroService.listPacotes).mockResolvedValue([]);
    vi.mocked(financeiroService.listPrazosConvenios).mockResolvedValue([]);
    vi.mocked(financeiroService.listGuiasConvenio).mockResolvedValue([]);
  });

  it('renderiza contratos particulares e tabela de preços para ADMIN_CLINICA', async () => {
    mockUseAuth.mockReturnValue({
      user: { idUser: '1', nomeUser: 'Gestor', idClinica: 'cli-1', perfis: ['ADMIN_CLINICA'] },
      isAdmin: false,
      isAdminClinica: true,
      isFuncionarioClinica: true,
      isResponsavel: false,
    });

    vi.mocked(financeiroService.listContratosParticulares).mockResolvedValueOnce([
      {
        idContratoParticular: 'ctr-1',
        idClinica: 'cli-1',
        idPaciente: 'pac-1',
        nomePaciente: 'Sofia Mendes',
        descricao: 'Combo 2 meses',
        modalidade: 'POR_PERIODO_MENSAL',
        dataInicio: '2026-11-10',
        valorCobrado: 850,
        statusPagamento: 'PAGO',
        statusContrato: 'ATIVO',
        totalSessoesContratadas: 10,
        totalSessoesConsumidas: 2,
        totalSessoesRestantes: 8,
        itens: [
          {
            idContratoItem: 'it-1',
            idTipoCompromisso: 'tp-1',
            nomeTipoCompromisso: 'Terapia Ocupacional',
            quantidadeContratada: 10,
            quantidadeConsumida: 2,
            quantidadeRestante: 8,
            movimentacoes: [],
          },
        ],
      },
    ]);

    render(
      <MemoryRouter>
        <Financeiro />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Sofia Mendes')).toBeInTheDocument();
      expect(screen.getByText(/Combo 2 meses/i)).toBeInTheDocument();
      expect(screen.getByText(/8 restantes/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Tabela de Preços & Combos/i }));

    await waitFor(() => {
      expect(screen.getByText('Tabela de Preços por Especialidade')).toBeInTheDocument();
      expect(screen.getAllByText('Terapia Ocupacional').length).toBeGreaterThanOrEqual(1);
    });
  });

  it('exibe visão somente leitura de acompanhamento para RESPONSAVEL', async () => {
    mockUseAuth.mockReturnValue({
      user: { idUser: '2', nomeUser: 'Carla', perfis: ['RESPONSAVEL'] },
      isAdmin: false,
      isAdminClinica: false,
      isFuncionarioClinica: false,
      isResponsavel: true,
    });

    vi.mocked(financeiroService.listContratosParticulares).mockResolvedValueOnce([]);

    render(
      <MemoryRouter>
        <Financeiro />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Financeiro & Pacotes')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Novo Fechamento Particular/i })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Tabela de Preços & Combos/i })).not.toBeInTheDocument();
    });
  });

  it('exibe aba de guias de convênio com opção de conferência e assinatura', async () => {
    mockUseAuth.mockReturnValue({
      user: { idUser: '2', nomeUser: 'Carla', perfis: ['RESPONSAVEL'] },
      isAdmin: false,
      isAdminClinica: false,
      isFuncionarioClinica: false,
      isResponsavel: true,
    });

    vi.mocked(financeiroService.listContratosParticulares).mockResolvedValueOnce([]);
    vi.mocked(financeiroService.listGuiasConvenio).mockResolvedValueOnce([
      {
        idGuiaFaturamento: 'g-1',
        idClinica: 'cli-1',
        idPaciente: 'pac-1',
        nomePaciente: 'Matheus Mendes',
        idPlanoSaude: 'pl-1',
        nomePlanoSaude: 'Unimed Curitiba',
        numeroLoteGuia: 'FAT-202611-0001',
        competenciaInicio: '2026-11-01',
        competenciaFim: '2026-11-30',
        valorTotal: 210,
        status: 'AGUARDANDO_ASSINATURA',
        itens: [
          {
            idGuiaItem: 'i1',
            idCompromisso: 'c1',
            dataHoraAtendimento: '2026-11-05T09:00:00',
            tipoCompromissoNome: 'Terapia Ocupacional',
            profissionalNome: 'Dra. Camila',
            valorUnitarioConvenio: 70,
          },
        ],
      },
    ]);

    render(
      <MemoryRouter>
        <Financeiro />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Guias de Convênio/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Guias de Convênio/i }));

    await waitFor(() => {
      expect(screen.getByText('FAT-202611-0001')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Conferir e Assinar/i })).toBeInTheDocument();
    });
  });
});
