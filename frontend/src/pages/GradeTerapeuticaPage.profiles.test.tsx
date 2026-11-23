import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import GradeTerapeuticaPage from './GradeTerapeuticaPage';
import { AuthContext, type AuthContextType } from '../context/AuthContext';
import { pacienteService } from '../services/pacienteService';
import { gradeTerapeuticaService } from '../services/gradeTerapeuticaService';

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn(),
    getById: vi.fn(),
  },
}));

vi.mock('../services/gradeTerapeuticaService', () => ({
  gradeTerapeuticaService: {
    listPorPaciente: vi.fn(),
    getConflitosPorPaciente: vi.fn(),
    criarProposta: vi.fn(),
    enviarParaAprovacao: vi.fn(),
    aprovar: vi.fn(),
    sugerirAlteracao: vi.fn(),
    rejeitar: vi.fn(),
    cancelarProfissional: vi.fn(),
  },
}));

interface GradeProfileCase {
  label: string;
  flags: Partial<AuthContextType>;
  canPropose: boolean;
}

const GRADE_MATRIX: GradeProfileCase[] = [
  // Perfis isolados
  { label: 'ADMIN', flags: { isAdmin: true }, canPropose: true },
  { label: 'ADMIN_CLINICA', flags: { isAdminClinica: true, isFuncionarioClinica: true }, canPropose: true },
  { label: 'FUNCIONARIO_CLINICA (Secretária)', flags: { isFuncionarioClinica: true }, canPropose: true },
  { label: 'PROFISSIONAL', flags: { isProfissional: true }, canPropose: true },
  { label: 'RESPONSAVEL puro', flags: { isResponsavel: true }, canPropose: false },

  // Mesclas
  { label: 'ADMIN_CLINICA + PROFISSIONAL', flags: { isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true }, canPropose: true },
  { label: 'ADMIN_CLINICA + RESPONSAVEL', flags: { isAdminClinica: true, isFuncionarioClinica: true, isResponsavel: true }, canPropose: true },
  { label: 'ADMIN_CLINICA + PROFISSIONAL + RESPONSAVEL', flags: { isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true, isResponsavel: true }, canPropose: true },
  { label: 'FUNCIONARIO_CLINICA + PROFISSIONAL', flags: { isFuncionarioClinica: true, isProfissional: true }, canPropose: true },
  { label: 'FUNCIONARIO_CLINICA + RESPONSAVEL', flags: { isFuncionarioClinica: true, isResponsavel: true }, canPropose: true },
  { label: 'PROFISSIONAL + RESPONSAVEL', flags: { isProfissional: true, isResponsavel: true }, canPropose: true },
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

describe('GradeTerapeuticaPage - Matriz de Perfis Isolados e Mesclados', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(pacienteService.list).mockResolvedValue([
      {
        idPaciente: 'pac-1',
        idPessoa: 'pes-1',
        nomePessoa: 'Lucas Mendes',
        ativo: true,
      } as any,
    ]);
    vi.mocked(gradeTerapeuticaService.listPorPaciente).mockResolvedValue([]);
    vi.mocked(gradeTerapeuticaService.getConflitosPorPaciente).mockResolvedValue([]);
  });

  it.each(GRADE_MATRIX)(
    'carrega combo de pacientes e controla permissão de criar proposta para $label',
    async ({ flags, canPropose }) => {
      render(
        <MemoryRouter>
          <AuthContext.Provider value={createAuthValue(flags)}>
            <GradeTerapeuticaPage />
          </AuthContext.Provider>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Lucas Mendes' })).toBeInTheDocument();
        expect(screen.queryByText(/Carregando grade terapêutica/i)).not.toBeInTheDocument();
      });

      if (canPropose) {
        expect(screen.getByRole('button', { name: /Nova Proposta de Grade/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Criar Grade Terapêutica/i })).toBeInTheDocument();
      } else {
        expect(screen.queryByRole('button', { name: /Nova Proposta de Grade/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /Criar Grade Terapêutica/i })).not.toBeInTheDocument();
      }
    }
  );
});
