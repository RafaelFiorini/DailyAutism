import api from './api';

export interface Notificacao {
  idNotificacao: string;
  titulo: string;
  mensagem: string;
  tipo: 'CANCELAMENTO_SESSAO' | 'GRADE_TERAPEUTICA' | 'SISTEMA' | string;
  idReferencia?: string | null;
  lida: boolean;
  lidaEm?: string | null;
  criadoEm: string;
}

export const notificacaoService = {
  async list(): Promise<Notificacao[]> {
    const { data } = await api.get<Notificacao[]>('/notificacoes');
    return data;
  },

  async countNaoLidas(): Promise<number> {
    const { data } = await api.get<{ count: number }>('/notificacoes/nao-lidas/count');
    return data?.count ?? 0;
  },

  async marcarLida(id: string): Promise<Notificacao> {
    const { data } = await api.patch<Notificacao>(`/notificacoes/${id}/lida`);
    return data;
  },

  async marcarTodasLidas(): Promise<void> {
    await api.patch('/notificacoes/marcar-todas-lidas');
  },
};
