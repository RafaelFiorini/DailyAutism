export type TipoBloqueioAgenda =
  | 'FERIADO'
  | 'FERIAS_COLETIVAS'
  | 'RECESSO'
  | 'PONTO_FACULTATIVO'
  | 'OUTRO';

export interface BloqueioAgendaClinica {
  idBloqueio: string;
  idClinica?: string;
  nomeClinica?: string;
  titulo: string;
  tipo: TipoBloqueioAgenda;
  dataInicio: string;
  dataFim: string;
  horaInicio?: string | null;
  horaFim?: string | null;
  observacao?: string | null;
  criadoPorNome?: string | null;
  agendamentosSuspensos?: number | null;
}

export interface SaveBloqueioAgendaClinicaInput {
  idClinica?: string;
  titulo: string;
  tipo: TipoBloqueioAgenda;
  dataInicio: string;
  dataFim: string;
  horaInicio?: string | null;
  horaFim?: string | null;
  observacao?: string;
  suspenderAgendamentosExistentes?: boolean;
}

export const TIPO_BLOQUEIO_LABELS: Record<TipoBloqueioAgenda, { label: string; badgeClass: string }> = {
  FERIADO: { label: 'Feriado', badgeClass: 'bg-rose-50 text-rose-700 border-rose-200' },
  FERIAS_COLETIVAS: { label: 'Férias Coletivas', badgeClass: 'bg-indigo-50 text-indigo-700 border-indigo-200' },
  RECESSO: { label: 'Recesso', badgeClass: 'bg-amber-50 text-amber-700 border-amber-200' },
  PONTO_FACULTATIVO: { label: 'Ponto Facultativo', badgeClass: 'bg-sky-50 text-sky-700 border-sky-200' },
  OUTRO: { label: 'Outro', badgeClass: 'bg-gray-100 text-gray-700 border-gray-200' },
};
