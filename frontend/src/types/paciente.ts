export type TipoLiberacaoGuia = 'PERIODO' | 'QUANTIDADE';

export interface PacienteGuiaPlano {
  idPacientePlanoSaude: string;
  idPaciente: string;
  nomePaciente?: string;
  idPlanoSaude: string;
  nomePlano?: string;
  operadora?: string;
  numeroCarteirinha?: string;
  validadeCarteirinha?: string;
  numeroGuia?: string;
  tipoLiberacao: TipoLiberacaoGuia;
  dataInicioLiberacao?: string;
  vencimentoLiberacao?: string;
  quantidadeSessoesLiberadas?: number;
  diasParaVencimento?: number | null;
  statusGuia?: 'ATIVA' | 'VENCENDO_EM_BREVE' | 'VENCIDA' | 'SEM_GUIA' | string;
}

export interface SavePacienteGuiaPlanoInput {
  idPaciente: string;
  idPlanoSaude: string;
  numeroCarteirinha?: string;
  validadeCarteirinha?: string;
  numeroGuia?: string;
  tipoLiberacao?: TipoLiberacaoGuia;
  dataInicioLiberacao?: string;
  vencimentoLiberacao?: string;
  quantidadeSessoesLiberadas?: number;
  idPeriodicidadeReferencia?: string;
}

export interface Paciente {
  idPaciente: string;
  idPessoa?: string;
  nomePessoa: string;
  cpf?: string;
  apelidoPaciente?: string;
  isResponsavel?: boolean;
  hasOutroResponsavel?: boolean;
  rg?: string;
  endereco?: string;
  telefone?: string;
  celular?: string;
  email?: string;
  datanasc?: string;
  planos?: string[];
  clinicas?: string[];
  responsaveis?: string[];
  idPlanoSaude?: string;
  idPacientePlanoSaude?: string;
  numeroCarteirinha?: string;
  validadeCarteirinha?: string;
  numeroGuia?: string;
  tipoLiberacao?: TipoLiberacaoGuia;
  dataInicioLiberacao?: string;
  vencimentoLiberacao?: string;
  quantidadeSessoesLiberadas?: number;
  diasParaVencimento?: number | null;
  statusGuia?: string;
  guias?: PacienteGuiaPlano[];
  idClinica?: string;
  idResponsavel?: string;
}

export interface SavePacienteInput {
  idPessoa?: string;
  nomePessoa?: string;
  CPFPessoa?: string;
  cpf?: string;
  datanascPessoa?: string;
  RGPessoa?: string;
  rg?: string;
  enderecoPessoa?: string;
  telefonePessoa?: string;
  celularPessoa?: string;
  emailPessoa?: string;
  apelidoPaciente?: string;
  isResponsavel?: boolean;
  idPlanoSaude?: string;
  numeroCarteirinha?: string;
  validadeCarteirinha?: string;
  numeroGuia?: string;
  tipoLiberacao?: TipoLiberacaoGuia;
  dataInicioLiberacao?: string;
  vencimentoLiberacao?: string;
  quantidadeSessoesLiberadas?: number;
  idPeriodicidadeReferencia?: string;
  idClinica?: string;
  idResponsavel?: string;
  vincularAoUsuarioLogado?: boolean;
}

export interface ClinicaOption {
  idClinica: string;
  nomeClinica: string;
  cnpjClinica?: string;
}

export interface PlanoSaudeOption {
  idPlanoSaude: string;
  nomePlano: string;
  operadora?: string;
}

export interface ResponsavelOption {
  idResponsavel: string;
  nome: string;
  cpf?: string;
}
