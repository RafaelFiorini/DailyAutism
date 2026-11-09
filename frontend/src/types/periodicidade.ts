export interface Periodicidade {
  idPeriodicidade: string;
  nomePeriodicidade: string;
  diasIntervalo?: number | null;
  descricaoPeriodicidade?: string | null;
}

export interface SavePeriodicidadeInput {
  nomePeriodicidade: string;
  diasIntervalo?: number | null;
  descricaoPeriodicidade?: string;
}
