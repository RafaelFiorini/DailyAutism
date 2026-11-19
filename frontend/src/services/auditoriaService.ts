import api from './api';
import type { AuditoriaLog, FiltroAuditoria, PageAuditoria } from '../types/auditoria';

export const auditoriaService = {
  async listar(filtro: FiltroAuditoria = {}): Promise<PageAuditoria> {
    const params = new URLSearchParams();

    if (filtro.dataInicio) params.append('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params.append('dataFim', filtro.dataFim);
    if (filtro.categoria) params.append('categoria', filtro.categoria);
    if (filtro.acao) params.append('acao', filtro.acao);
    if (filtro.buscaTexto) params.append('buscaTexto', filtro.buscaTexto);
    if (filtro.idUsuario) params.append('idUsuario', filtro.idUsuario);
    if (filtro.idClinica) params.append('idClinica', filtro.idClinica);

    params.append('page', String(filtro.page ?? 0));
    params.append('size', String(filtro.size ?? 20));

    const { data } = await api.get<PageAuditoria>(`/auditoria?${params.toString()}`);
    return data;
  },

  async getById(id: string): Promise<AuditoriaLog> {
    const { data } = await api.get<AuditoriaLog>(`/auditoria/${id}`);
    return data;
  },

  async exportarCsv(filtro: FiltroAuditoria = {}): Promise<Blob> {
    const params = new URLSearchParams();

    if (filtro.dataInicio) params.append('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params.append('dataFim', filtro.dataFim);
    if (filtro.categoria) params.append('categoria', filtro.categoria);
    if (filtro.acao) params.append('acao', filtro.acao);
    if (filtro.buscaTexto) params.append('buscaTexto', filtro.buscaTexto);

    const response = await api.get(`/auditoria/exportar?${params.toString()}`, {
      responseType: 'blob',
    });
    return response.data;
  },
};
