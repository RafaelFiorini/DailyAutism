export interface Profissional {
  idProf: string;
  idPessoa: string;
  nomeProfissional: string;
  cpf?: string;
  email?: string;
  telefone?: string;
  conselhoProf: string;
}

export interface SaveProfissionalInput {
  idPessoa?: string;
  nomePessoa?: string;
  CPFPessoa?: string;
  datanascPessoa?: string;
  RGPessoa?: string;
  enderecoPessoa?: string;
  telefonePessoa?: string;
  celularPessoa?: string;
  emailPessoa?: string;
  conselhoProf: string;
}

