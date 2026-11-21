import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Pacientes from './Pacientes';
import { AuthContext, type AuthContextType } from '../context/AuthContext';
import { pacienteService } from '../services/pacienteService';

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn(),
    listInativos: vi.fn(),
    delete: vi.fn(),
    reativar: vi.fn(),
  },
}));

interface PacientesProfileCase {
  label: string;
  flags: Partial<AuthContextType>;
  expectedHeading: string;
  canCreate: boolean;
  canManageInativos: boolean;
}

const PACIENTES_MATRIX: PacientesProfileCase[] = [
  // Perfis isolados
  {
    label: 'ADMIN',
    flags: { isAdmin: true },
    expectedHeading: 'Pacientes',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'ADMIN_CLINICA',
    flags: { isAdminClinica: true, isFuncionarioClinica: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'FUNCIONARIO_CLINICA (Secretária)',
    flags: { isFuncionarioClinica: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'PROFISSIONAL puro',
    flags: { isProfissional: true },
    expectedHeading: 'Pacientes em Atendimento',
    canCreate: false,
    canManageInativos: false,
  },
  {
    label: 'RESPONSAVEL puro',
    flags: { isResponsavel: true },
    expectedHeading: 'Meus Pacientes',
    canCreate: true,
    canManageInativos: false,
  },

  // Mesclas
  {
    label: 'ADMIN_CLINICA + PROFISSIONAL',
    flags: { isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'ADMIN_CLINICA + RESPONSAVEL',
    flags: { isAdminClinica: true, isFuncionarioClinica: true, isResponsavel: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'ADMIN_CLINICA + PROFISSIONAL + RESPONSAVEL',
    flags: { isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true, isResponsavel: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'FUNCIONARIO_CLINICA + PROFISSIONAL',
    flags: { isFuncionarioClinica: true, isProfissional: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'FUNCIONARIO_CLINICA + RESPONSAVEL',
    flags: { isFuncionarioClinica: true, isResponsavel: true },
    expectedHeading: 'Pacientes da Clínica',
    canCreate: true,
    canManageInativos: true,
  },
  {
    label: 'PROFISSIONAL + RESPONSAVEL',
    flags: { isProfissional: true, isResponsavel: true },
    expectedHeading: 'Meus Pacientes',
    canCreate: true,
    canManageInativos: false,
  },
];

function createAuthValue(overrides: Partial<AuthContextType>): AuthContextType {
  return {
    user: {
      idUser: 'u-1',
      nomeUser: 'Usuário Teste',
      emailUser: 'teste@dailyautism.com',
      perfis: [],
    },
    token: 'fake-token',
    isAuthenticated: true,
    isAdmin: false,
    isResponsavel: false,
    isProfissional: false,
    isFuncionarioClinica: false,
    isAdminClinica: false,
    loading: false,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    ...overrides,
  };
}

describe('Pacientes Page - Matriz de Perfis Isolados e Mesclados', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(pacienteService.list).mockResolvedValue([
      {
        idPaciente: 'pac-1',
        idPessoa: 'pes-1',
        nomePessoa: 'Enzo Gabriel',
        apelidoPaciente: 'Enzinho',
        cpf: '12345678901',
        ativo: true,
      } as any,
    ]);
  });

  it.each(PACIENTES_MATRIX)(
    'exibe título, permissão de criação e filtro de inativos corretos para $label',
    async ({ flags, expectedHeading, canCreate, canManageInativos }) => {
      render(
        <MemoryRouter>
          <AuthContext.Provider value={createAuthValue(flags)}>
            <Pacientes />
          </AuthContext.Provider>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Enzo Gabriel')).toBeInTheDocument();
      });

      expect(screen.getByRole('heading', { name: expectedHeading })).toBeInTheDocument();

      if (canCreate) {
        expect(screen.getByRole('button', { name: /Novo Paciente/i })).toBeInTheDocument();
      } else {
        expect(screen.queryByRole('button', { name: /Novo Paciente/i })).not.toBeInTheDocument();
      }

      if (canManageInativos) {
        expect(screen.getByRole('button', { name: 'Inativos' })).toBeInTheDocument();
      } else {
        expect(screen.queryByRole('button', { name: 'Inativos' })).not.toBeInTheDocument();
      }
    }
  );
});
