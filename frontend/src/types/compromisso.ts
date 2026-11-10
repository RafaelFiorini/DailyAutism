export type StatusCompromisso =
  | 'AGUARDANDO_APROVACAO'
  | 'AGENDADO'
  | 'REJEITADO'
  | 'REALIZADO'
  | 'CANCELADO'
  | 'FALTOU';

export interface Compromisso {
  idCompromisso: string;
  tituloCompromisso: string;
  descricaoCompromisso?: string;
  dataHoraCompromisso: string;
  localCompromisso?: string;
  status: StatusCompromisso;
  idPaciente: string;
  nomePaciente: string;
  idTipoCompromisso: string;
  nomeTipoCompromisso: string;
  idPeriodicidade: string;
  nomePeriodicidade: string;
  idProfissional?: string;
  nomeProfissional?: string;
  conselhoProfissional?: string;
  idUser: string;
}

export interface SaveCompromissoInput {
  tituloCompromisso: string;
  descricaoCompromisso?: string;
  dataHoraCompromisso: string;
  localCompromisso?: string;
  idPaciente: string;
  idTipoCompromisso: string;
  idPeriodicidade: string;
  idProfissional?: string;
}

export interface TipoCompromisso {
  idTipoCompromisso: string;
  nomeTipo: string;
  descricaoTipo?: string;
}

export interface AprovarCompromissoInput {
  dataFim?: string;
}
