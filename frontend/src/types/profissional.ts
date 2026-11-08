export interface Profissional {
  idProf: string;
  idPessoa: string;
  nomeProfissional: string;
  cpf?: string;
  email?: string;
  telefone?: string;
  conselhoProf: string;
  rg?: string;
  endereco?: string;
  datanasc?: string;
}

export interface SaveProfissionalInput {
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
  conselhoProf: string;
}

