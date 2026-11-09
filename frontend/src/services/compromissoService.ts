import api from './api';
import type {
  Compromisso,
  SaveCompromissoInput,
  StatusCompromisso,
  TipoCompromisso,
  AprovarCompromissoInput,
} from '../types/compromisso';

export const compromissoService = {
  async list(params?: { pacienteId?: string; status?: StatusCompromisso }): Promise<Compromisso[]> {
    const { data } = await api.get<Compromisso[]>('/compromissos', { params });
    return data;
  },

  async getById(id: string): Promise<Compromisso> {
    const { data } = await api.get<Compromisso>(`/compromissos/${id}`);
    return data;
  },

  async create(input: SaveCompromissoInput): Promise<Compromisso> {
    const { data } = await api.post<Compromisso>('/compromissos', input);
    return data;
  },

  async update(id: string, input: SaveCompromissoInput): Promise<Compromisso> {
    const { data } = await api.put<Compromisso>(`/compromissos/${id}`, input);
    return data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/compromissos/${id}`);
  },

  async aprovar(id: string, input?: AprovarCompromissoInput): Promise<Compromisso[]> {
    const { data } = await api.patch<Compromisso[]>(`/compromissos/${id}/aprovar`, input || {});
    return data;
  },

  async rejeitar(id: string): Promise<Compromisso> {
    const { data } = await api.patch<Compromisso>(`/compromissos/${id}/rejeitar`);
    return data;
  },

  async updateStatus(id: string, status: StatusCompromisso): Promise<Compromisso> {
    const { data } = await api.patch<Compromisso>(`/compromissos/${id}/status`, { status });
    return data;
  },

  async listTipos(): Promise<TipoCompromisso[]> {
    const { data } = await api.get<TipoCompromisso[]>('/tipos-compromisso');
    return data;
  },
};
