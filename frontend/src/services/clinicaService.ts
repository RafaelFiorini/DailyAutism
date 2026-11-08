import api from './api';
import type { Clinica } from '../types/clinica';

export const clinicaService = {
  async list(): Promise<Clinica[]> {
    const { data } = await api.get<Clinica[]>('/clinicas');
    return data;
  },

  async getById(id: string): Promise<Clinica> {
    const { data } = await api.get<Clinica>(`/clinicas/${id}`);
    return data;
  },
};
