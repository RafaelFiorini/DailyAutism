import api from './api';
import type { PlanoSaude, SavePlanoSaudeInput } from '../types/planoSaude';

export const planoSaudeService = {
  async list(): Promise<PlanoSaude[]> {
    const { data } = await api.get<PlanoSaude[]>('/planos-saude');
    return data;
  },

  async getById(id: string): Promise<PlanoSaude> {
    const { data } = await api.get<PlanoSaude>(`/planos-saude/${id}`);
    return data;
  },

  async create(input: SavePlanoSaudeInput): Promise<PlanoSaude> {
    const { data } = await api.post<PlanoSaude>('/planos-saude', input);
    return data;
  },

  async update(id: string, input: SavePlanoSaudeInput): Promise<PlanoSaude> {
    const { data } = await api.put<PlanoSaude>(`/planos-saude/${id}`, input);
    return data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/planos-saude/${id}`);
  },
};
