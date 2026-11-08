import api from './api';
import type { Responsavel, SaveResponsavelInput, PacienteOption } from '../types/responsavel';

export const responsavelService = {
  async list(): Promise<Responsavel[]> {
    const response = await api.get<Responsavel[]>('/responsaveis');
    return response.data;
  },

  async create(payload: SaveResponsavelInput): Promise<Responsavel> {
    const response = await api.post<Responsavel>('/responsaveis', payload);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/responsaveis/${id}`);
  },

  async listPacientes(): Promise<PacienteOption[]> {
    const response = await api.get<PacienteOption[]>('/pacientes');
    return response.data;
  },
};
