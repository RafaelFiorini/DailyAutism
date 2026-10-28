import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authService } from './authService';
import api from './api';

vi.mock('./api');

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('login sends credentials and returns token', async () => {
    const mockResponse = { data: { token: 'mock-jwt-token' } };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const result = await authService.login({
      emailUser: 'admin@clinica.com',
      passwordUser: 'admin123',
    });

    expect(api.post).toHaveBeenCalledWith('/auth/login', {
      emailUser: 'admin@clinica.com',
      passwordUser: 'admin123',
    });
    expect(result).toEqual({ token: 'mock-jwt-token' });
  });

  it('getMe requests authenticated user profile', async () => {
    const mockUser = {
      idUser: '123',
      nomeUser: 'Administrador Geral',
      emailUser: 'admin@clinica.com',
      perfis: ['ADMIN'],
    };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockUser });

    const result = await authService.getMe();

    expect(api.get).toHaveBeenCalledWith('/users/me');
    expect(result).toEqual(mockUser);
  });
});
