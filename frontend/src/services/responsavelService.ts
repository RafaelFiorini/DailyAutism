import api from './api';
import type { Responsavel, SaveResponsavelInput, PacienteOption } from '../types/responsavel';

export const responsavelService = {
  async list(): Promise<Responsavel[]> {
    const response = await api.get<Responsavel[]>('/responsaveis');
    return response.data;
  },

  async listInativos(): Promise<Responsavel[]> {
    const response = await api.get<Responsavel[]>('/responsaveis/inativos');
    return response.data;
  },

  async reativar(id: string): Promise<Responsavel> {
    const response = await api.patch<Responsavel>(`/responsaveis/${id}/reativar`);
    return response.data;
  },

  async create(payload: SaveResponsavelInput): Promise<Responsavel> {
    const response = await api.post<Responsavel>('/responsaveis', payload);
    return response.data;
  },

  async update(id: string, payload: SaveResponsavelInput): Promise<Responsavel> {
    const response = await api.put<Responsavel>(`/responsaveis/${id}`, payload);
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
