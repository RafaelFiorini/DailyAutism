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
  responsavel?: boolean;
  idResponsavel?: string | null;
  isProfissional: boolean;
  profissional?: boolean;
  idProfissional?: string | null;
  conselhoProf?: string | null;
  isFuncionarioClinica?: boolean;
  funcionarioClinica?: boolean;
  isAdminClinica?: boolean;
  adminClinica?: boolean;
  idClinica?: string | null;
  nomeClinica?: string | null;
  cargoClinica?: string | null;
  isAdmin: boolean;
  admin?: boolean;
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
  responsavel?: boolean;
  isProfissional: boolean;
  profissional?: boolean;
  conselhoProf?: string;
  isFuncionarioClinica?: boolean;
  funcionarioClinica?: boolean;
  isAdminClinica?: boolean;
  adminClinica?: boolean;
  idClinica?: string;
  cargoClinica?: string;
  isAdmin: boolean;
  admin?: boolean;
}
