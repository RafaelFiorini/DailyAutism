export interface LoginCredentials {
  emailUser: string;
  passwordUser: string;
}

export interface AuthResponse {
  token: string;
}

export interface ApiErrorResponse {
  errorCode?: string;
  errorMessage?: string;
  details?: string[];
  status?: number;
  path?: string;
}

export interface PacienteInicialInput {
  nome: string;
  cpf: string;
  apelido?: string;
  datanasc?: string;
}

export interface RegisterInput {
  tipo: 'RESPONSAVEL' | 'PROFISSIONAL';
  nome: string;
  email: string;
  senha: string;
  cpf: string;
  telefone?: string;
  celular?: string;
  rg?: string;
  datanasc?: string;
  endereco?: string;
  conselhoProf?: string;
  isPacienteTambem?: boolean;
  pacientes?: PacienteInicialInput[];
}
