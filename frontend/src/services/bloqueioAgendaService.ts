import api from './api';
import type { BloqueioAgendaClinica, SaveBloqueioAgendaClinicaInput } from '../types/bloqueioAgenda';

export const bloqueioAgendaService = {
  async list(idClinica?: string): Promise<BloqueioAgendaClinica[]> {
    const params = idClinica ? { idClinica } : undefined;
    const { data } = await api.get<BloqueioAgendaClinica[]>('/bloqueios-agenda', { params });
    return data;
  },

  async create(input: SaveBloqueioAgendaClinicaInput): Promise<BloqueioAgendaClinica> {
    const { data } = await api.post<BloqueioAgendaClinica>('/bloqueios-agenda', input);
    return data;
  },

  async importarEmLote(itens: SaveBloqueioAgendaClinicaInput[]): Promise<BloqueioAgendaClinica[]> {
    const { data } = await api.post<BloqueioAgendaClinica[]>('/bloqueios-agenda/importar-lote', itens);
    return data;
  },

  async remove(id: string): Promise<void> {
    await api.delete(`/bloqueios-agenda/${id}`);
  },
};
