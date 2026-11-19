export type CategoriaAuditoria =
  | 'AUTENTICACAO'
  | 'CLINICO'
  | 'GRADE_TERAPEUTICA'
  | 'FINANCEIRO'
  | 'CADASTRO'
  | 'SEGURANCA';

export type AcaoAuditoria =
  | 'LOGIN_SUCESSO'
  | 'LOGIN_FALHA'
  | 'LOGOUT'
  | 'CRIACAO'
  | 'EDICAO'
  | 'EXCLUSAO'
  | 'APROVACAO'
  | 'REJEICAO'
  | 'ASSINATURA_DIGITAL'
  | 'CANCELAMENTO'
  | 'EXPORTACAO';

export interface AuditoriaLog {
  idAuditoriaLog: string;
  dataHora: string;
  idUsuario?: string;
  nomeUsuario?: string;
  loginUsuario?: string;
  perfilUsuario?: string;
  idClinica?: string;
  categoria: CategoriaAuditoria;
  categoriaDescricao?: string;
  acao: AcaoAuditoria;
  acaoDescricao?: string;
  entidade?: string;
  idEntidade?: string;
  descricao?: string;
  dadosAnteriores?: string;
  dadosNovos?: string;
  ipOrigem?: string;
  userAgent?: string;
}

export interface FiltroAuditoria {
  dataInicio?: string;
  dataFim?: string;
  categoria?: CategoriaAuditoria | '';
  acao?: AcaoAuditoria | '';
  buscaTexto?: string;
  idUsuario?: string;
  idClinica?: string;
  page?: number;
  size?: number;
}

export interface PageAuditoria {
  content: AuditoriaLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const CATEGORIAS_CONFIG: Record<
  CategoriaAuditoria,
  { label: string; bg: string; text: string; border: string }
> = {
  AUTENTICACAO: {
    label: 'Autenticação e Acesso',
    bg: 'bg-blue-50 dark:bg-blue-900/20',
    text: 'text-blue-700 dark:text-blue-300',
    border: 'border-blue-200 dark:border-blue-800',
  },
  CLINICO: {
    label: 'Atendimento Clínico',
    bg: 'bg-emerald-50 dark:bg-emerald-900/20',
    text: 'text-emerald-700 dark:text-emerald-300',
    border: 'border-emerald-200 dark:border-emerald-800',
  },
  GRADE_TERAPEUTICA: {
    label: 'Grade Terapêutica',
    bg: 'bg-purple-50 dark:bg-purple-900/20',
    text: 'text-purple-700 dark:text-purple-300',
    border: 'border-purple-200 dark:border-purple-800',
  },
  FINANCEIRO: {
    label: 'Financeiro e Repasse',
    bg: 'bg-amber-50 dark:bg-amber-900/20',
    text: 'text-amber-700 dark:text-amber-300',
    border: 'border-amber-200 dark:border-amber-800',
  },
  CADASTRO: {
    label: 'Cadastro e Pacientes',
    bg: 'bg-indigo-50 dark:bg-indigo-900/20',
    text: 'text-indigo-700 dark:text-indigo-300',
    border: 'border-indigo-200 dark:border-indigo-800',
  },
  SEGURANCA: {
    label: 'Segurança e Alertas',
    bg: 'bg-rose-50 dark:bg-rose-900/20',
    text: 'text-rose-700 dark:text-rose-300',
    border: 'border-rose-200 dark:border-rose-800',
  },
};

export const ACOES_CONFIG: Record<
  AcaoAuditoria,
  { label: string; badgeBg: string; badgeText: string }
> = {
  LOGIN_SUCESSO: { label: 'Login Sucesso', badgeBg: 'bg-emerald-100 dark:bg-emerald-900/40', badgeText: 'text-emerald-800 dark:text-emerald-300' },
  LOGIN_FALHA: { label: 'Falha de Login', badgeBg: 'bg-rose-100 dark:bg-rose-900/40', badgeText: 'text-rose-800 dark:text-rose-300' },
  LOGOUT: { label: 'Logout', badgeBg: 'bg-gray-100 dark:bg-gray-700', badgeText: 'text-gray-700 dark:text-gray-300' },
  CRIACAO: { label: 'Criação', badgeBg: 'bg-sky-100 dark:bg-sky-900/40', badgeText: 'text-sky-800 dark:text-sky-300' },
  EDICAO: { label: 'Edição', badgeBg: 'bg-indigo-100 dark:bg-indigo-900/40', badgeText: 'text-indigo-800 dark:text-indigo-300' },
  EXCLUSAO: { label: 'Exclusão', badgeBg: 'bg-red-100 dark:bg-red-900/40', badgeText: 'text-red-800 dark:text-red-300' },
  APROVACAO: { label: 'Aprovação', badgeBg: 'bg-teal-100 dark:bg-teal-900/40', badgeText: 'text-teal-800 dark:text-teal-300' },
  REJEICAO: { label: 'Rejeição', badgeBg: 'bg-orange-100 dark:bg-orange-900/40', badgeText: 'text-orange-800 dark:text-orange-300' },
  ASSINATURA_DIGITAL: { label: 'Assinatura', badgeBg: 'bg-violet-100 dark:bg-violet-900/40', badgeText: 'text-violet-800 dark:text-violet-300' },
  CANCELAMENTO: { label: 'Cancelamento', badgeBg: 'bg-amber-100 dark:bg-amber-900/40', badgeText: 'text-amber-800 dark:text-amber-300' },
  EXPORTACAO: { label: 'Exportação', badgeBg: 'bg-cyan-100 dark:bg-cyan-900/40', badgeText: 'text-cyan-800 dark:text-cyan-300' },
};
