import api from './api';
import type { Profissional, SaveProfissionalInput } from '../types/profissional';

export const profissionalService = {
  async list(): Promise<Profissional[]> {
    const response = await api.get<Profissional[]>('/profissionais');
    return response.data;
  },

  async create(input: SaveProfissionalInput): Promise<Profissional> {
    const response = await api.post<Profissional>('/profissionais', input);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/profissionais/${id}`);
  },
};

