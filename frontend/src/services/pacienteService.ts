import api from './api';
import type { Paciente, SavePacienteInput, ClinicaOption, PlanoSaudeOption, ResponsavelOption } from '../types/paciente';

export const pacienteService = {
  async list(): Promise<Paciente[]> {
    const response = await api.get<Paciente[]>('/pacientes');
    return response.data;
  },

  async getById(id: string): Promise<Paciente> {
    const response = await api.get<Paciente>(`/pacientes/${id}`);
    return response.data;
  },

  async create(payload: SavePacienteInput): Promise<Paciente> {
    const response = await api.post<Paciente>('/pacientes', payload);
    return response.data;
  },

  async update(id: string, payload: SavePacienteInput): Promise<Paciente> {
    const response = await api.put<Paciente>(`/pacientes/${id}`, payload);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/pacientes/${id}`);
  },

  async listPlanos(): Promise<PlanoSaudeOption[]> {
    try {
      const response = await api.get<PlanoSaudeOption[]>('/planos-saude');
      return response.data;
    } catch {
      return [];
    }
  },

  async listClinicas(): Promise<ClinicaOption[]> {
    try {
      const response = await api.get<ClinicaOption[]>('/clinicas');
      return response.data;
    } catch {
      return [];
    }
  },

  async listResponsaveis(): Promise<ResponsavelOption[]> {
    try {
      const response = await api.get<any[]>('/responsaveis');
      return response.data.map((r) => ({
        idResponsavel: r.idResponsavel,
        nome:
          r.nomeResponsavel ||
          r.nomePessoa ||
          r.nome ||
          r.pessoaResponsavel?.nomePessoa ||
          'Responsável sem nome',
        cpf: r.cpf || r.CPFPessoa || r.pessoaResponsavel?.cpfpessoa,
      }));
    } catch {
      return [];
    }
  },
};
