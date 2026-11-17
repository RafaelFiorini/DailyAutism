export type DiaSemana =
  | 'SEGUNDA'
  | 'TERCA'
  | 'QUARTA'
  | 'QUINTA'
  | 'SEXTA'
  | 'SABADO'
  | 'DOMINGO';

export type StatusGradeTerapeutica =
  | 'RASCUNHO'
  | 'PENDENTE_APROVACAO'
  | 'SUGESTAO_RESPONSAVEL'
  | 'APROVADA'
  | 'REJEITADA'
  | 'VIGENTE'
  | 'SUBSTITUIDA';

export interface GradeTerapeuticaItem {
  idGradeTerapeuticaItem?: string;
  diaSemana: DiaSemana;
  diaSemanaDescricao?: string;
  horaInicio: string; // "HH:mm"
  horaFim: string; // "HH:mm"
  idTipoCompromisso: string;
  nomeTipoCompromisso?: string;
  idProfissional?: string;
  nomeProfissional?: string;
  sala?: string;
}

export interface SaveGradeTerapeuticaItem {
  diaSemana: DiaSemana;
  horaInicio: string;
  horaFim: string;
  idTipoCompromisso: string;
  idProfissional?: string;
  sala?: string;
}

export interface GradeTerapeutica {
  idGradeTerapeutica: string;
  idPaciente: string;
  nomePaciente?: string;
  idClinica?: string;
  idGradeAnterior?: string;
  numeroVersao: number;
  tipoOrigemVigencia?: string;
  idOrigemVigencia?: string;
  dataInicioVigencia: string;
  dataFimVigencia: string;
  status: StatusGradeTerapeutica;
  justificativaSugestaoResponsavel?: string;
  motivoRejeicao?: string;
  dataEnvio?: string;
  dataResposta?: string;
  itens: GradeTerapeuticaItem[];
}

export interface SaveGradeTerapeutica {
  idPaciente: string;
  idClinica?: string;
  tipoOrigemVigencia?: string;
  idOrigemVigencia?: string;
  dataInicioVigencia: string;
  dataFimVigencia: string;
  enviarParaAprovacao?: boolean;
  itens: SaveGradeTerapeuticaItem[];
}

export interface ProporAlteracaoGrade {
  dataInicioNovaGrade: string;
  itens: SaveGradeTerapeuticaItem[];
}

export interface CancelarProfissionalGrade {
  idProfissional: string;
  dataCorte: string;
  motivo?: string;
}

export const DIAS_SEMANA_ORDENADOS: { key: DiaSemana; label: string; abrev: string }[] = [
  { key: 'SEGUNDA', label: 'Segunda-feira', abrev: 'SEG' },
  { key: 'TERCA', label: 'Terça-feira', abrev: 'TER' },
  { key: 'QUARTA', label: 'Quarta-feira', abrev: 'QUA' },
  { key: 'QUINTA', label: 'Quinta-feira', abrev: 'QUI' },
  { key: 'SEXTA', label: 'Sexta-feira', abrev: 'SEX' },
  { key: 'SABADO', label: 'Sábado', abrev: 'SÁB' },
  { key: 'DOMINGO', label: 'Domingo', abrev: 'DOM' },
];

export const STATUS_GRADE_LABELS: Record<StatusGradeTerapeutica, { label: string; bg: string; text: string }> = {
  RASCUNHO: { label: 'Rascunho', bg: 'bg-gray-100 dark:bg-gray-700', text: 'text-gray-700 dark:text-gray-300' },
  PENDENTE_APROVACAO: { label: 'Aguardando Responsável', bg: 'bg-amber-100 dark:bg-amber-900/40', text: 'text-amber-800 dark:text-amber-300' },
  SUGESTAO_RESPONSAVEL: { label: 'Contraproposta da Família', bg: 'bg-orange-100 dark:bg-orange-900/40', text: 'text-orange-800 dark:text-orange-300' },
  APROVADA: { label: 'Aprovada', bg: 'bg-blue-100 dark:bg-blue-900/40', text: 'text-blue-800 dark:text-blue-300' },
  REJEITADA: { label: 'Rejeitada', bg: 'bg-red-100 dark:bg-red-900/40', text: 'text-red-800 dark:text-red-300' },
  VIGENTE: { label: 'Grade Vigente', bg: 'bg-emerald-100 dark:bg-emerald-900/40', text: 'text-emerald-800 dark:text-emerald-300' },
  SUBSTITUIDA: { label: 'Substituída (Histórico)', bg: 'bg-slate-100 dark:bg-slate-700', text: 'text-slate-600 dark:text-slate-400' },
};
