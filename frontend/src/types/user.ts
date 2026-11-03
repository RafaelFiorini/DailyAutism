export interface UserDTO {
  idUser: string;
  nomeUser: string;
  emailUser: string;
  idPessoa?: string | null;
  perfis: string[];
  idResponsavel?: string | null;
  idProfissional?: string | null;
}

