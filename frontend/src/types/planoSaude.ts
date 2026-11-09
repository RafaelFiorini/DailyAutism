export interface PlanoSaude {
  idPlanoSaude: string;
  nomePlano: string;
  operadora: string;
  ansReg?: string;
}

export interface SavePlanoSaudeInput {
  nomePlano: string;
  operadora: string;
  ansReg?: string;
}
