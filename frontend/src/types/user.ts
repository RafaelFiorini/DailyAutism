export interface UserDTO {
  idUser: string;
  nomeUser: string;
  emailUser: string;
  idPessoa?: string | null;
  perfis: string[];
  idResponsavel?: string | null;
  idProfissional?: string | null;
}

export interface User {
  idUser: string;
  nomeUser: string;
  emailUser: string;
  idPessoa?: string | null;
  nomePessoa?: string | null;
  cpf?: string | null;
  datanasc?: string | null;
  rg?: string | null;
  endereco?: string | null;
  telefone?: string | null;
  celular?: string | null;
  perfis: string[];
  isResponsavel: boolean;
  idResponsavel?: string | null;
  isProfissional: boolean;
  idProfissional?: string | null;
  conselhoProf?: string | null;
  isFuncionarioClinica?: boolean;
  isAdminClinica?: boolean;
  idClinica?: string | null;
  nomeClinica?: string | null;
  cargoClinica?: string | null;
  isAdmin: boolean;
  ativo?: boolean;
}

export interface SaveUserInput {
  nome: string;
  email: string;
  senha?: string;
  cpf: string;
  telefone?: string;
  celular?: string;
  rg?: string;
  datanasc?: string;
  endereco?: string;
  isResponsavel: boolean;
  isProfissional: boolean;
  conselhoProf?: string;
  isFuncionarioClinica?: boolean;
  isAdminClinica?: boolean;
  idClinica?: string;
  cargoClinica?: string;
  isAdmin: boolean;
}
