export type ModalidadeContratoParticular = 'POR_SESSOES' | 'POR_PERIODO_MENSAL';
export type StatusPagamentoContrato = 'PENDENTE' | 'PAGO' | 'CANCELADO';
export type StatusContratoParticular = 'ATIVO' | 'CONCLUIDO' | 'CANCELADO';
export type TipoMovimentacaoContrato = 'ABATIMENTO' | 'ESTORNO' | 'AJUSTE_MANUAL';

export interface TabelaPreco {
  idTabelaPreco: string;
  idClinica: string;
  nomeClinica?: string | null;
  idTipoCompromisso: string;
  nomeTipoCompromisso?: string | null;
  idPlanoSaude?: string | null;
  nomePlanoSaude?: string | null;
  valorSessao: number;
  particular: boolean;
}

export interface SaveTabelaPrecoInput {
  idClinica?: string;
  idTipoCompromisso: string;
  idPlanoSaude?: string | null;
  valorSessao: number;
}

export interface PacoteParticularItem {
  idPacoteItem?: string;
  idTipoCompromisso: string;
  nomeTipoCompromisso?: string | null;
  quantidadeSessoes: number;
}

export interface PacoteParticular {
  idPacote: string;
  idClinica: string;
  nomeClinica?: string | null;
  nomePacote: string;
  descricao?: string | null;
  valorTotalPacote: number;
  valorSemDescontoCalculado?: number | null;
  validadeMesesSugerida?: number | null;
  totalSessoes: number;
  itens: PacoteParticularItem[];
}

export interface SavePacoteParticularInput {
  idClinica?: string;
  nomePacote: string;
  descricao?: string;
  valorTotalPacote: number;
  validadeMesesSugerida?: number;
  itens: {
    idTipoCompromisso: string;
    quantidadeSessoes: number;
  }[];
}

export interface ClinicaPlanoPrazo {
  idClinicaPlanoSaude: string;
  idClinica: string;
  nomeClinica?: string | null;
  idPlanoSaude: string;
  nomePlano?: string | null;
  operadora?: string | null;
  diaCorteEnvio?: number | null;
  prazoPagamentoDias: number;
}

export interface SaveClinicaPlanoPrazoInput {
  idClinica: string;
  idPlanoSaude: string;
  diaCorteEnvio?: number;
  prazoPagamentoDias?: number;
}

export interface MovimentacaoContrato {
  idMovimentacao: string;
  idCompromisso?: string | null;
  tituloCompromisso?: string | null;
  tipoMovimentacao: TipoMovimentacaoContrato;
  quantidade: number;
  dataMovimentacao: string;
  observacao?: string | null;
}

export interface ContratoItem {
  idContratoItem: string;
  idTipoCompromisso: string;
  nomeTipoCompromisso?: string | null;
  quantidadeContratada: number;
  quantidadeConsumida: number;
  quantidadeRestante: number;
  valorUnitarioReferencia?: number | null;
  movimentacoes: MovimentacaoContrato[];
}

export interface ContratoParticular {
  idContratoParticular: string;
  idClinica: string;
  nomeClinica?: string | null;
  idPaciente: string;
  nomePaciente?: string | null;
  idResponsavel?: string | null;
  nomeResponsavel?: string | null;
  idPacoteOrigem?: string | null;
  nomePacoteOrigem?: string | null;
  descricao: string;
  modalidade: ModalidadeContratoParticular;
  dataInicio: string;
  dataFimPrevista?: string | null;
  valorCobrado: number;
  statusPagamento: StatusPagamentoContrato;
  dataPagamento?: string | null;
  formaPagamento?: string | null;
  statusContrato: StatusContratoParticular;
  totalSessoesContratadas: number;
  totalSessoesConsumidas: number;
  totalSessoesRestantes: number;
  itens: ContratoItem[];
}

export interface SaveContratoParticularInput {
  idClinica?: string;
  idPaciente: string;
  idResponsavel?: string;
  idPacote?: string;
  multiplicadorMeses?: number;
  descricao?: string;
  modalidade?: ModalidadeContratoParticular;
  dataInicio?: string;
  dataFimPrevista?: string;
  valorCobrado?: number;
  pagoNaContratacao?: boolean;
  dataPagamento?: string;
  formaPagamento?: string;
  itens?: {
    idTipoCompromisso: string;
    quantidadeContratada: number;
  }[];
}
