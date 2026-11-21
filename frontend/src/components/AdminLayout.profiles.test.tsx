import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import AdminLayout from './AdminLayout';
import { AuthContext, type AuthContextType } from '../context/AuthContext';

interface LayoutProfileCase {
  label: string;
  flags: Partial<AuthContextType>;
  expectedLinks: string[];
  forbiddenLinks: string[];
}

const LAYOUT_MATRIX: LayoutProfileCase[] = [
  // Perfis isolados
  {
    label: 'ADMIN (Admin Geral)',
    flags: { isAdmin: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Usuários',
      'Clínicas',
      'Pacientes',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
      'Periodicidades',
      'Auditoria & Logs',
    ],
    forbiddenLinks: ['Meu Financeiro'],
  },
  {
    label: 'ADMIN_CLINICA (Administrador da Clínica)',
    flags: { isAdminClinica: true, isFuncionarioClinica: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
      'Periodicidades',
    ],
    forbiddenLinks: ['Meu Financeiro', 'Usuários', 'Clínicas', 'Auditoria & Logs'],
  },
  {
    label: 'FUNCIONARIO_CLINICA (Secretária / Funcionário)',
    flags: { isFuncionarioClinica: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
    ],
    forbiddenLinks: ['Meu Financeiro', 'Periodicidades', 'Usuários', 'Clínicas', 'Auditoria & Logs'],
  },
  {
    label: 'PROFISSIONAL (Profissional de Clínica)',
    flags: { isProfissional: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Meu Financeiro',
      'Pacientes em Atendimento',
      'Planos de Saúde',
    ],
    forbiddenLinks: ['Financeiro', 'Responsáveis', 'Profissionais', 'Periodicidades', 'Usuários', 'Auditoria & Logs'],
  },
  {
    label: 'RESPONSAVEL (Responsável / Família)',
    flags: { isResponsavel: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Meus Pacientes',
      'Planos de Saúde',
    ],
    forbiddenLinks: ['Meu Financeiro', 'Responsáveis', 'Profissionais', 'Periodicidades', 'Usuários', 'Auditoria & Logs'],
  },

  // Mesclas (Sobreposição de perfis)
  {
    label: 'ADMIN_CLINICA + PROFISSIONAL (ex: Alexandra)',
    flags: { isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Meu Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
      'Periodicidades',
    ],
    forbiddenLinks: ['Usuários', 'Clínicas', 'Auditoria & Logs', 'Pacientes em Atendimento'],
  },
  {
    label: 'ADMIN_CLINICA + RESPONSAVEL',
    flags: { isAdminClinica: true, isFuncionarioClinica: true, isResponsavel: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
      'Periodicidades',
    ],
    forbiddenLinks: ['Meu Financeiro', 'Usuários', 'Auditoria & Logs'],
  },
  {
    label: 'ADMIN_CLINICA + PROFISSIONAL + RESPONSAVEL',
    flags: { isAdminClinica: true, isFuncionarioClinica: true, isProfissional: true, isResponsavel: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Meu Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
      'Periodicidades',
    ],
    forbiddenLinks: ['Usuários', 'Clínicas', 'Auditoria & Logs'],
  },
  {
    label: 'FUNCIONARIO_CLINICA + PROFISSIONAL',
    flags: { isFuncionarioClinica: true, isProfissional: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Meu Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
    ],
    forbiddenLinks: ['Periodicidades', 'Usuários', 'Auditoria & Logs'],
  },
  {
    label: 'FUNCIONARIO_CLINICA + RESPONSAVEL',
    flags: { isFuncionarioClinica: true, isResponsavel: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Pacientes da Clínica',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
    ],
    forbiddenLinks: ['Meu Financeiro', 'Periodicidades', 'Usuários', 'Auditoria & Logs'],
  },
  {
    label: 'PROFISSIONAL + RESPONSAVEL',
    flags: { isProfissional: true, isResponsavel: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Meu Financeiro',
      'Pacientes em Atendimento',
      'Planos de Saúde',
    ],
    forbiddenLinks: ['Responsáveis', 'Profissionais', 'Periodicidades', 'Usuários', 'Auditoria & Logs'],
  },
  {
    label: 'ADMIN + PROFISSIONAL',
    flags: { isAdmin: true, isProfissional: true },
    expectedLinks: [
      'Visão Geral',
      'Agenda',
      'Grade Terapêutica',
      'Documentos',
      'Financeiro',
      'Meu Financeiro',
      'Usuários',
      'Clínicas',
      'Pacientes',
      'Responsáveis',
      'Profissionais',
      'Planos de Saúde',
      'Periodicidades',
      'Auditoria & Logs',
    ],
    forbiddenLinks: [],
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

describe('AdminLayout - Matriz de Navegação para Perfis Isolados e Mesclados', () => {
  it.each(LAYOUT_MATRIX)('renderiza exatamente os menus esperados para $label', ({ flags, expectedLinks, forbiddenLinks }) => {
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <AuthContext.Provider value={createAuthValue(flags)}>
          <AdminLayout />
        </AuthContext.Provider>
      </MemoryRouter>
    );

    for (const linkText of expectedLinks) {
      expect(screen.getByRole('link', { name: linkText })).toBeInTheDocument();
    }

    for (const forbiddenText of forbiddenLinks) {
      expect(screen.queryByRole('link', { name: forbiddenText })).not.toBeInTheDocument();
    }
  });
});
