import api from './api';
import type {
  ClinicaPlanoPrazo,
  ContratoParticular,
  PacoteParticular,
  SaveClinicaPlanoPrazoInput,
  SaveContratoParticularInput,
  SavePacoteParticularInput,
  SaveTabelaPrecoInput,
  TabelaPreco,
} from '../types/financeiro';

export const financeiroService = {
  // Tabela de preços
  async listPrecos(clinicaId?: string): Promise<TabelaPreco[]> {
    const { data } = await api.get<TabelaPreco[]>('/financeiro/precos', {
      params: clinicaId ? { clinicaId } : undefined,
    });
    return data;
  },

  async savePreco(input: SaveTabelaPrecoInput): Promise<TabelaPreco> {
    const { data } = await api.post<TabelaPreco>('/financeiro/precos', input);
    return data;
  },

  async deletePreco(id: string): Promise<void> {
    await api.delete(`/financeiro/precos/${id}`);
  },

  // Pacotes particulares
  async listPacotes(clinicaId?: string): Promise<PacoteParticular[]> {
    const { data } = await api.get<PacoteParticular[]>('/financeiro/pacotes', {
      params: clinicaId ? { clinicaId } : undefined,
    });
    return data;
  },

  async createPacote(input: SavePacoteParticularInput): Promise<PacoteParticular> {
    const { data } = await api.post<PacoteParticular>('/financeiro/pacotes', input);
    return data;
  },

  async updatePacote(id: string, input: SavePacoteParticularInput): Promise<PacoteParticular> {
    const { data } = await api.put<PacoteParticular>(`/financeiro/pacotes/${id}`, input);
    return data;
  },

  async deletePacote(id: string): Promise<void> {
    await api.delete(`/financeiro/pacotes/${id}`);
  },

  // Prazos de convênios por clínica
  async listPrazosConvenios(clinicaId?: string): Promise<ClinicaPlanoPrazo[]> {
    const { data } = await api.get<ClinicaPlanoPrazo[]>('/financeiro/convenios-prazos', {
      params: clinicaId ? { clinicaId } : undefined,
    });
    return data;
  },

  async savePrazoConvenio(input: SaveClinicaPlanoPrazoInput): Promise<ClinicaPlanoPrazo> {
    const { data } = await api.post<ClinicaPlanoPrazo>('/financeiro/convenios-prazos', input);
    return data;
  },

  // Contratos particulares
  async listContratosParticulares(params?: {
    clinicaId?: string;
    pacienteId?: string;
  }): Promise<ContratoParticular[]> {
    const { data } = await api.get<ContratoParticular[]>('/financeiro/contratos-particulares', {
      params,
    });
    return data;
  },

  async createContratoParticular(input: SaveContratoParticularInput): Promise<ContratoParticular> {
    const { data } = await api.post<ContratoParticular>('/financeiro/contratos-particulares', input);
    return data;
  },

  async registrarPagamentoContrato(
    id: string,
    payload?: { dataPagamento?: string; formaPagamento?: string }
  ): Promise<ContratoParticular> {
    const { data } = await api.patch<ContratoParticular>(
      `/financeiro/contratos-particulares/${id}/pagar`,
      payload || {}
    );
    return data;
  },

  async cancelarContratoParticular(id: string): Promise<ContratoParticular> {
    const { data } = await api.patch<ContratoParticular>(
      `/financeiro/contratos-particulares/${id}/cancelar`
    );
    return data;
  },
};
