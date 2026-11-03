import api, { TOKEN_STORAGE_KEY } from './api';
import { LoginCredentials, AuthResponse } from '../types/auth';
import { UserDTO } from '../types/user';

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  async getMe(): Promise<UserDTO> {
    const response = await api.get<UserDTO>('/users/me');
    return response.data;
  },

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  },

  getToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  },

  clearToken(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  },
};

