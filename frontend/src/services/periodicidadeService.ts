import api from './api';
import type { Periodicidade, SavePeriodicidadeInput } from '../types/periodicidade';

export const periodicidadeService = {
  async list(): Promise<Periodicidade[]> {
    const { data } = await api.get<Periodicidade[]>('/periodicidades');
    return data;
  },

  async getById(id: string): Promise<Periodicidade> {
    const { data } = await api.get<Periodicidade>(`/periodicidades/${id}`);
    return data;
  },

  async create(input: SavePeriodicidadeInput): Promise<Periodicidade> {
    const { data } = await api.post<Periodicidade>('/periodicidades', input);
    return data;
  },

  async update(id: string, input: SavePeriodicidadeInput): Promise<Periodicidade> {
    const { data } = await api.put<Periodicidade>(`/periodicidades/${id}`, input);
    return data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/periodicidades/${id}`);
  },
};
