export interface PacienteVinculado {
  idPaciente: string;
  nomePaciente: string;
  apelidoPaciente?: string;
}

export interface Responsavel {
  idResponsavel: string;
  idPessoa: string;
  nomeResponsavel: string;
  cpf?: string;
  email?: string;
  telefone?: string;
  isPaciente?: boolean;
  rg?: string;
  endereco?: string;
  datanasc?: string;
  pacientes?: PacienteVinculado[];
  idClinica?: string;
}

export interface SaveResponsavelInput {
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
  isPacienteTambem?: boolean;
  pacienteIds?: string[];
  idClinica?: string;
}

export interface PacienteOption {
  idPaciente: string;
  nomePessoa: string;
  apelidoPaciente?: string;
}
