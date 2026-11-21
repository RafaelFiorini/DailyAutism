import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useContext } from 'react';
import { AuthProvider, AuthContext } from './AuthContext';
import { authService } from '../services/authService';
import type { UserDTO } from '../types/user';

vi.mock('../services/authService', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    getMe: vi.fn(),
  },
}));

function FlagsInspector() {
  const auth = useContext(AuthContext)!;
  if (auth.loading) return <div>loading</div>;
  return (
    <div>
      <span data-testid="isAdmin">{String(auth.isAdmin)}</span>
      <span data-testid="isAdminClinica">{String(auth.isAdminClinica)}</span>
      <span data-testid="isFuncionarioClinica">{String(auth.isFuncionarioClinica)}</span>
      <span data-testid="isProfissional">{String(auth.isProfissional)}</span>
      <span data-testid="isResponsavel">{String(auth.isResponsavel)}</span>
    </div>
  );
}

interface ProfileCase {
  label: string;
  perfis: string[];
  expected: {
    isAdmin: boolean;
    isAdminClinica: boolean;
    isFuncionarioClinica: boolean;
    isProfissional: boolean;
    isResponsavel: boolean;
  };
}

const PROFILE_MATRIX: ProfileCase[] = [
  // Perfis isolados
  {
    label: 'ADMIN (Admin Geral)',
    perfis: ['ADMIN'],
    expected: { isAdmin: true, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: false, isResponsavel: false },
  },
  {
    label: 'ADMIN_CLINICA (Administrador da Clínica)',
    perfis: ['ADMIN_CLINICA'],
    expected: { isAdmin: false, isAdminClinica: true, isFuncionarioClinica: true, isProfissional: false, isResponsavel: false },
  },
  {
    label: 'FUNCIONARIO_CLINICA (Secretária / Funcionário)',
    perfis: ['FUNCIONARIO_CLINICA'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: true, isProfissional: false, isResponsavel: false },
  },
  {
    label: 'CLINICA (Legado Clínica)',
    perfis: ['CLINICA'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: true, isProfissional: false, isResponsavel: false },
  },
  {
    label: 'PROFISSIONAL (Profissional de Clínica)',
    perfis: ['PROFISSIONAL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: true, isResponsavel: false },
  },
  {
    label: 'RESPONSAVEL (Responsável / Família)',
    perfis: ['RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: false, isResponsavel: true },
  },
  {
    label: 'PACIENTE (Isolado)',
    perfis: ['PACIENTE'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: false, isResponsavel: false },
  },

  // Mesclas (Sobreposição de perfis)
  {
    label: 'ADMIN_CLINICA + PROFISSIONAL',
    perfis: ['ADMIN_CLINICA', 'PROFISSIONAL'],
    expected: { isAdmin: false, isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true, isResponsavel: false },
  },
  {
    label: 'ADMIN_CLINICA + RESPONSAVEL',
    perfis: ['ADMIN_CLINICA', 'RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: true, isFuncionarioClinica: true, isProfissional: false, isResponsavel: true },
  },
  {
    label: 'ADMIN_CLINICA + PROFISSIONAL + RESPONSAVEL',
    perfis: ['ADMIN_CLINICA', 'PROFISSIONAL', 'RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true, isResponsavel: true },
  },
  {
    label: 'FUNCIONARIO_CLINICA + PROFISSIONAL',
    perfis: ['FUNCIONARIO_CLINICA', 'PROFISSIONAL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: true, isProfissional: true, isResponsavel: false },
  },
  {
    label: 'FUNCIONARIO_CLINICA + RESPONSAVEL',
    perfis: ['FUNCIONARIO_CLINICA', 'RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: true, isProfissional: false, isResponsavel: true },
  },
  {
    label: 'FUNCIONARIO_CLINICA + PROFISSIONAL + RESPONSAVEL',
    perfis: ['FUNCIONARIO_CLINICA', 'PROFISSIONAL', 'RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: true, isProfissional: true, isResponsavel: true },
  },
  {
    label: 'PROFISSIONAL + RESPONSAVEL',
    perfis: ['PROFISSIONAL', 'RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: true, isResponsavel: true },
  },
  {
    label: 'PACIENTE + RESPONSAVEL',
    perfis: ['PACIENTE', 'RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: false, isResponsavel: true },
  },
  {
    label: 'ADMIN + PROFISSIONAL',
    perfis: ['ADMIN', 'PROFISSIONAL'],
    expected: { isAdmin: true, isAdminClinica: false, isFuncionarioClinica: false, isProfissional: true, isResponsavel: false },
  },
  {
    label: 'Prefixo ROLE_ na mescla ROLE_ADMIN_CLINICA + ROLE_PROFISSIONAL + ROLE_RESPONSAVEL',
    perfis: ['ROLE_ADMIN_CLINICA', 'ROLE_PROFISSIONAL', 'ROLE_RESPONSAVEL'],
    expected: { isAdmin: false, isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true, isResponsavel: true },
  },
];

describe('AuthContext - Matriz de Perfis Isolados e Mesclados', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('dailyautism_token', 'valid-token');
  });

  it.each(PROFILE_MATRIX)('calcula flags corretamente para $label', async ({ perfis, expected }) => {
    const mockUser: UserDTO = {
      idUser: 'u-1',
      nomeUser: 'Usuário Teste',
      emailUser: 'teste@clinica.com',
      perfis,
    };

    vi.mocked(authService.getMe).mockResolvedValueOnce(mockUser);

    render(
      <AuthProvider>
        <FlagsInspector />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.queryByText('loading')).not.toBeInTheDocument();
    });

    expect(screen.getByTestId('isAdmin').textContent).toBe(String(expected.isAdmin));
    expect(screen.getByTestId('isAdminClinica').textContent).toBe(String(expected.isAdminClinica));
    expect(screen.getByTestId('isFuncionarioClinica').textContent).toBe(String(expected.isFuncionarioClinica));
    expect(screen.getByTestId('isProfissional').textContent).toBe(String(expected.isProfissional));
    expect(screen.getByTestId('isResponsavel').textContent).toBe(String(expected.isResponsavel));
  });
});
