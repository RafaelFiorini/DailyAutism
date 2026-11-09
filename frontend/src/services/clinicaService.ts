import api from './api';
import type { Clinica, SaveClinicaInput } from '../types/clinica';

export const clinicaService = {
  async list(): Promise<Clinica[]> {
    const { data } = await api.get<Clinica[]>('/clinicas');
    return data;
  },

  async getById(id: string): Promise<Clinica> {
    const { data } = await api.get<Clinica>(`/clinicas/${id}`);
    return data;
  },

  async create(input: SaveClinicaInput): Promise<Clinica> {
    const { data } = await api.post<Clinica>('/clinicas', input);
    return data;
  },

  async update(id: string, input: SaveClinicaInput): Promise<Clinica> {
    const { data } = await api.put<Clinica>(`/clinicas/${id}`, input);
    return data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/clinicas/${id}`);
  },
};
