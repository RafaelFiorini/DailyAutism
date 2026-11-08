export interface Paciente {
  idPaciente: string;
  idPessoa?: string;
  nomePessoa: string;
  cpf?: string;
  apelidoPaciente?: string;
  isResponsavel?: boolean;
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
  numeroCarteirinha?: string;
  idClinica?: string;
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
  idClinica?: string;
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
