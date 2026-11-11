import api from './api';
import type { User, SaveUserInput } from '../types/user';

export const userService = {
  async list(): Promise<User[]> {
    const { data } = await api.get<User[]>('/users');
    return data;
  },

  async listInativos(): Promise<User[]> {
    const { data } = await api.get<User[]>('/users/inativos');
    return data;
  },

  async reativar(id: string): Promise<User> {
    const { data } = await api.patch<User>(`/users/${id}/reativar`);
    return data;
  },

  async getById(id: string): Promise<User> {
    const { data } = await api.get<User>(`/users/${id}`);
    return data;
  },

  async create(payload: SaveUserInput): Promise<User> {
    const { data } = await api.post<User>('/users', payload);
    return data;
  },

  async update(id: string, payload: SaveUserInput): Promise<User> {
    const { data } = await api.put<User>(`/users/${id}`, payload);
    return data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/users/${id}`);
  },
};
