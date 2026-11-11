import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from './api';
import { financeiroService } from './financeiroService';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('financeiroService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('lista e salva preços da clínica', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: [{ idTabelaPreco: 'p1', valorSessao: 100 }] });
    const precos = await financeiroService.listPrecos('cli-1');
    expect(api.get).toHaveBeenCalledWith('/financeiro/precos', { params: { clinicaId: 'cli-1' } });
    expect(precos).toHaveLength(1);

    vi.mocked(api.post).mockResolvedValueOnce({ data: { idTabelaPreco: 'p1', valorSessao: 120 } });
    const saved = await financeiroService.savePreco({
      idClinica: 'cli-1',
      idTipoCompromisso: 'tp-1',
      valorSessao: 120,
    });
    expect(api.post).toHaveBeenCalledWith('/financeiro/precos', {
      idClinica: 'cli-1',
      idTipoCompromisso: 'tp-1',
      valorSessao: 120,
    });
    expect(saved.valorSessao).toBe(120);
  });

  it('cria e registra pagamento de contrato particular', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({ data: { idContratoParticular: 'ctr-1' } });
    await financeiroService.createContratoParticular({
      idClinica: 'cli-1',
      idPaciente: 'pac-1',
      idPacote: 'pkg-1',
      multiplicadorMeses: 2,
      pagoNaContratacao: true,
    });
    expect(api.post).toHaveBeenCalledWith('/financeiro/contratos-particulares', {
      idClinica: 'cli-1',
      idPaciente: 'pac-1',
      idPacote: 'pkg-1',
      multiplicadorMeses: 2,
      pagoNaContratacao: true,
    });

    vi.mocked(api.patch).mockResolvedValueOnce({ data: { idContratoParticular: 'ctr-1', statusPagamento: 'PAGO' } });
    const paid = await financeiroService.registrarPagamentoContrato('ctr-1', { formaPagamento: 'PIX' });
    expect(api.patch).toHaveBeenCalledWith('/financeiro/contratos-particulares/ctr-1/pagar', {
      formaPagamento: 'PIX',
    });
    expect(paid.statusPagamento).toBe('PAGO');
  });
});
