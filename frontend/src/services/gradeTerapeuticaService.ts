import api from './api';
import type {
  GradeTerapeutica,
  SaveGradeTerapeutica,
  ProporAlteracaoGrade,
  CancelarProfissionalGrade,
} from '../types/gradeTerapeutica';
import type { Compromisso } from '../types/compromisso';

export const gradeTerapeuticaService = {
  async getById(id: string): Promise<GradeTerapeutica> {
    const { data } = await api.get<GradeTerapeutica>(`/grades-terapeuticas/${id}`);
    return data;
  },

  async listPorPaciente(idPaciente: string): Promise<GradeTerapeutica[]> {
    const { data } = await api.get<GradeTerapeutica[]>(`/grades-terapeuticas/paciente/${idPaciente}`);
    return data;
  },

  async listPorClinica(idClinica: string): Promise<GradeTerapeutica[]> {
    const { data } = await api.get<GradeTerapeutica[]>(`/grades-terapeuticas/clinica/${idClinica}`);
    return data;
  },

  async criarProposta(input: SaveGradeTerapeutica): Promise<GradeTerapeutica> {
    const { data } = await api.post<GradeTerapeutica>('/grades-terapeuticas', input);
    return data;
  },

  async enviarParaAprovacao(id: string): Promise<GradeTerapeutica> {
    const { data } = await api.post<GradeTerapeutica>(`/grades-terapeuticas/${id}/enviar`);
    return data;
  },

  async aprovarEReplicar(id: string): Promise<Compromisso[]> {
    const { data } = await api.post<Compromisso[]>(`/grades-terapeuticas/${id}/aprovar`);
    return data;
  },

  async sugerirAlteracao(id: string, justificativa: string): Promise<GradeTerapeutica> {
    const { data } = await api.post<GradeTerapeutica>(`/grades-terapeuticas/${id}/sugerir`, { justificativa });
    return data;
  },

  async rejeitar(id: string, motivo?: string): Promise<GradeTerapeutica> {
    const { data } = await api.post<GradeTerapeutica>(`/grades-terapeuticas/${id}/rejeitar`, { justificativa: motivo });
    return data;
  },

  async proporAlteracao(id: string, input: ProporAlteracaoGrade): Promise<GradeTerapeutica> {
    const { data } = await api.post<GradeTerapeutica>(`/grades-terapeuticas/${id}/alterar`, input);
    return data;
  },

  async cancelarProfissional(id: string, input: CancelarProfissionalGrade): Promise<number> {
    const { data } = await api.post<number>(`/grades-terapeuticas/${id}/cancelar-profissional`, input);
    return data;
  },

  async getConflitosPorGrade(id: string): Promise<Compromisso[]> {
    const { data } = await api.get<Compromisso[]>(`/grades-terapeuticas/${id}/conflitos`);
    return data;
  },

  async getConflitosPorPaciente(idPaciente: string): Promise<Compromisso[]> {
    const { data } = await api.get<Compromisso[]>(`/grades-terapeuticas/conflitos/paciente/${idPaciente}`);
    return data;
  },
};
