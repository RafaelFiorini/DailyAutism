import api from './api';
import type {
  Arquivo,
  SaveArquivoInput,
  CompartilharArquivoInput,
  TipoArquivo,
  ArquivoDownload,
  OpcoesCompartilhamento,
} from '../types/arquivo';

const MIME_BY_EXT: Record<string, string> = {
  pdf: 'application/pdf',
  doc: 'application/msword',
  docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  txt: 'text/plain;charset=utf-8',
  odt: 'application/vnd.oasis.opendocument.text',
  rtf: 'application/rtf',
};

export const arquivoService = {
  async list(params?: { pacienteId?: string; idTipoArquivo?: string }): Promise<Arquivo[]> {
    const { data } = await api.get<Arquivo[]>('/arquivos', { params });
    return data;
  },

  async listByPaciente(pacienteId: string): Promise<Arquivo[]> {
    const { data } = await api.get<Arquivo[]>(`/arquivos/paciente/${pacienteId}`);
    return data;
  },

  async listTipos(): Promise<TipoArquivo[]> {
    const { data } = await api.get<TipoArquivo[]>('/tipos-arquivo');
    return data;
  },

  async opcoesCompartilhamento(pacienteId: string): Promise<OpcoesCompartilhamento> {
    const { data } = await api.get<OpcoesCompartilhamento>(
      `/arquivos/opcoes-compartilhamento/${pacienteId}`
    );
    return data;
  },

  async create(input: SaveArquivoInput): Promise<Arquivo> {
    const { data } = await api.post<Arquivo>('/arquivos', input);
    return data;
  },

  async compartilhar(id: string, input: CompartilharArquivoInput): Promise<Arquivo> {
    const { data } = await api.patch<Arquivo>(`/arquivos/${id}/compartilhar`, input);
    return data;
  },

  async download(id: string): Promise<ArquivoDownload> {
    const { data } = await api.get<ArquivoDownload>(`/arquivos/${id}/download`);
    return data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/arquivos/${id}`);
  },

  triggerBrowserDownload(file: ArquivoDownload): void {
    const rawBase64 = file.arquivoBase64.includes(',')
      ? file.arquivoBase64.split(',')[1]
      : file.arquivoBase64;
    const byteChars = atob(rawBase64);
    const byteNumbers = new Array(byteChars.length);
    for (let i = 0; i < byteChars.length; i++) {
      byteNumbers[i] = byteChars.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const ext = (file.extensaoArquivo || 'pdf').toLowerCase();
    const mime = MIME_BY_EXT[ext] || 'application/octet-stream';
    const blob = new Blob([byteArray], { type: mime });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = file.nomeArquivo || `documento.${ext}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  },
};
