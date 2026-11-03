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

