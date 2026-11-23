import api, { TOKEN_STORAGE_KEY } from './api';
import {
  LoginCredentials,
  AuthResponse,
  RegisterInput,
  ConsultaCadastroInput,
  DadosUsuarioRecuperacao,
  AtualizarCadastroInput,
  UpdateMeInput,
} from '../types/auth';
import { UserDTO } from '../types/user';

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  async loginWithGoogle(idToken: string): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/google', { idToken });
    return response.data;
  },

  async alterarSenha(senhaAtual: string, novaSenha: string): Promise<void> {
    await api.post('/auth/alterar-senha', { senhaAtual, novaSenha });
  },

  async register(payload: RegisterInput): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', payload);
    return response.data;
  },

  async consultarCadastro(payload: ConsultaCadastroInput): Promise<DadosUsuarioRecuperacao> {
    const response = await api.post<DadosUsuarioRecuperacao>('/auth/consultar-cadastro', payload);
    return response.data;
  },

  async atualizarCadastro(payload: AtualizarCadastroInput): Promise<void> {
    await api.post('/auth/atualizar-cadastro', payload);
  },

  async getMe(): Promise<UserDTO> {
    const response = await api.get<UserDTO>('/users/me');
    return response.data;
  },

  async updateMe(payload: UpdateMeInput): Promise<UserDTO> {
    const response = await api.put<UserDTO>('/users/me', payload);
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
