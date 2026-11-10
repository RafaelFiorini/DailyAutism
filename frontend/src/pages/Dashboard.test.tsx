import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Dashboard from './Dashboard';
import { AuthContext, AuthContextType } from '../context/AuthContext';
import { userService } from '../services/userService';
import { clinicaService } from '../services/clinicaService';
import { pacienteService } from '../services/pacienteService';
import { profissionalService } from '../services/profissionalService';
import { responsavelService } from '../services/responsavelService';
import { compromissoService } from '../services/compromissoService';
import type { Compromisso } from '../types/compromisso';

vi.mock('../services/userService', () => ({ userService: { list: vi.fn() } }));
vi.mock('../services/clinicaService', () => ({ clinicaService: { list: vi.fn() } }));
vi.mock('../services/pacienteService', () => ({ pacienteService: { list: vi.fn() } }));
vi.mock('../services/profissionalService', () => ({ profissionalService: { list: vi.fn() } }));
vi.mock('../services/responsavelService', () => ({ responsavelService: { list: vi.fn() } }));
vi.mock('../services/compromissoService', () => ({
  compromissoService: {
    list: vi.fn(),
    aprovar: vi.fn(),
    rejeitar: vi.fn(),
    updateStatus: vi.fn(),
  },
}));

function getTodayISO(): string {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function getTomorrowISO(): string {
  const now = new Date();
  now.setDate(now.getDate() + 1);
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function renderWithAuth(authOverrides: Partial<AuthContextType>) {
  const defaultAuth: AuthContextType = {
    user: { idUser: '1', nomeUser: 'Teste User', emailUser: 't@t.com', perfis: [] },
    token: 'tok',
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
    ...authOverrides,
  };

  return render(
    <MemoryRouter>
      <AuthContext.Provider value={defaultAuth}>
        <Dashboard />
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe('Dashboard Page - Visão Geral por Perfil', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ADMIN: exibe contadores reais de usuários, clínicas, pacientes e profissionais', async () => {
    vi.mocked(userService.list).mockResolvedValue([{}, {}, {}] as any);
    vi.mocked(clinicaService.list).mockResolvedValue([{}, {}] as any);
    vi.mocked(pacienteService.list).mockResolvedValue([{}, {}, {}, {}, {}] as any);
    vi.mocked(profissionalService.list).mockResolvedValue([{}, {}, {}, {}] as any);

    renderWithAuth({
      isAdmin: true,
      user: { idUser: '1', nomeUser: 'Admin Master', emailUser: 'admin@daily.com', perfis: ['ADMIN'] },
    });

    await waitFor(() => {
      expect(screen.getByText('Usuários Cadastrados')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
      expect(screen.getByText('4')).toBeInTheDocument();
    });
  });

  it('ADMIN_CLINICA: exibe indicadores específicos da sua clínica', async () => {
    const today = getTodayISO();
    vi.mocked(pacienteService.list).mockResolvedValue([{}, {}] as any);
    vi.mocked(profissionalService.list).mockResolvedValue([{}, {}, {}] as any);
    vi.mocked(responsavelService.list).mockResolvedValue([{}] as any);
    vi.mocked(compromissoService.list).mockResolvedValue([
      { idCompromisso: 'c1', dataHoraCompromisso: `${today}T09:00:00`, status: 'AGENDADO' },
      { idCompromisso: 'c2', dataHoraCompromisso: `${getTomorrowISO()}T10:00:00`, status: 'AGENDADO' },
    ] as any);

    renderWithAuth({
      isAdminClinica: true,
      isFuncionarioClinica: true,
      user: {
        idUser: '2',
        nomeUser: 'Gestora Attenzione',
        emailUser: 'gestora@clinica.com',
        perfis: ['ADMIN_CLINICA'],
        nomeClinica: 'Clínica Attenzione',
      },
    });

    await waitFor(() => {
      expect(screen.getByText('Clínica Attenzione')).toBeInTheDocument();
      expect(screen.getByText('Pacientes da Clínica')).toBeInTheDocument();
      expect(screen.getByText('Profissionais da Clínica')).toBeInTheDocument();
      expect(screen.getByText('Responsáveis Vinculados')).toBeInTheDocument();
      expect(screen.getByText('Agendamentos de Hoje')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
    });
  });

  it('FUNCIONARIO_CLINICA: exibe agenda do dia organizada em ordem alfabética de profissional e navega dias', async () => {
    const today = getTodayISO();
    const tomorrow = getTomorrowISO();

    vi.mocked(profissionalService.list).mockResolvedValue([
      { idProf: 'p2', nomeProfissional: 'Carlos Eduardo', conselhoProf: 'CRP 02' },
      { idProf: 'p1', nomeProfissional: 'Ana Beatriz', conselhoProf: 'CRFa 01' },
    ] as any);

    const mockComps: Compromisso[] = [
      {
        idCompromisso: 'c1',
        tituloCompromisso: 'Fono',
        dataHoraCompromisso: `${today}T10:00:00`,
        status: 'AGENDADO',
        idPaciente: 'pac1',
        nomePaciente: 'Pedro Henrique',
        idTipoCompromisso: 't1',
        nomeTipoCompromisso: 'Fonoaudiologia',
        idPeriodicidade: 'per1',
        nomePeriodicidade: 'Semanal',
        idProfissional: 'p1',
        nomeProfissional: 'Ana Beatriz',
        idUser: 'u1',
      },
      {
        idCompromisso: 'c2',
        tituloCompromisso: 'Psicologia',
        dataHoraCompromisso: `${tomorrow}T15:00:00`,
        status: 'AGENDADO',
        idPaciente: 'pac2',
        nomePaciente: 'Maria Clara',
        idTipoCompromisso: 't2',
        nomeTipoCompromisso: 'Psicologia / ABA',
        idPeriodicidade: 'per1',
        nomePeriodicidade: 'Semanal',
        idProfissional: 'p2',
        nomeProfissional: 'Carlos Eduardo',
        idUser: 'u1',
      },
    ];
    vi.mocked(compromissoService.list).mockResolvedValue(mockComps);

    renderWithAuth({
      isFuncionarioClinica: true,
      user: {
        idUser: '3',
        nomeUser: 'Ana Recepção',
        emailUser: 'ana@clinica.com',
        perfis: ['FUNCIONARIO_CLINICA'],
        nomeClinica: 'Attenzione',
      },
    });

    await waitFor(() => {
      expect(screen.getByText('Ana Beatriz')).toBeInTheDocument();
      expect(screen.getByText('Carlos Eduardo')).toBeInTheDocument();
      expect(screen.getByText('Pedro Henrique')).toBeInTheDocument();
      expect(screen.queryByText('Maria Clara')).not.toBeInTheDocument();
    });

    // Verifica ordem alfabética (Ana Beatriz aparece antes de Carlos Eduardo no DOM)
    const headings = screen.getAllByRole('heading', { level: 3 }).map((el) => el.textContent);
    expect(headings).toEqual(['Ana Beatriz', 'Carlos Eduardo']);

    // Navega para o próximo dia
    fireEvent.click(screen.getByTitle('Próximo dia'));

    await waitFor(() => {
      expect(screen.queryByText('Pedro Henrique')).not.toBeInTheDocument();
      expect(screen.getByText('Maria Clara')).toBeInTheDocument();
    });
  });

  it('PROFISSIONAL: exibe agenda do dia ordenada por horário com pacientes', async () => {
    const today = getTodayISO();

    vi.mocked(compromissoService.list).mockResolvedValue([
      {
        idCompromisso: 'c2',
        tituloCompromisso: 'Sessão Tarde',
        dataHoraCompromisso: `${today}T14:30:00`,
        status: 'AGENDADO',
        idPaciente: 'pac2',
        nomePaciente: 'Paciente 14h30',
        idTipoCompromisso: 't1',
        nomeTipoCompromisso: 'Terapia Ocupacional',
        idPeriodicidade: 'per1',
        nomePeriodicidade: 'Semanal',
        idUser: 'u1',
      },
      {
        idCompromisso: 'c1',
        tituloCompromisso: 'Sessão Manhã',
        dataHoraCompromisso: `${today}T08:00:00`,
        status: 'AGENDADO',
        idPaciente: 'pac1',
        nomePaciente: 'Paciente 08h00',
        idTipoCompromisso: 't1',
        nomeTipoCompromisso: 'Terapia Ocupacional',
        idPeriodicidade: 'per1',
        nomePeriodicidade: 'Semanal',
        idUser: 'u1',
      },
    ] as any);

    renderWithAuth({
      isProfissional: true,
      user: { idUser: '4', nomeUser: 'Dra. Camila', emailUser: 'camila@clinica.com', perfis: ['PROFISSIONAL'] },
    });

    await waitFor(() => {
      expect(screen.getByText('Paciente 08h00')).toBeInTheDocument();
      expect(screen.getByText('Paciente 14h30')).toBeInTheDocument();
    });

    const patientHeadings = screen.getAllByRole('heading', { level: 3 }).map((el) => el.textContent);
    expect(patientHeadings).toEqual(['Paciente 08h00', 'Paciente 14h30']);
  });

  it('RESPONSAVEL: exibe agenda do dia e deixa área limpa quando não há agendamentos no dia', async () => {
    const today = getTodayISO();

    vi.mocked(compromissoService.list).mockResolvedValue([
      {
        idCompromisso: 'c1',
        tituloCompromisso: 'Fonoaudiologia',
        dataHoraCompromisso: `${today}T11:00:00`,
        status: 'AGENDADO',
        idPaciente: 'pac1',
        nomePaciente: 'Lucas Filho',
        idTipoCompromisso: 't1',
        nomeTipoCompromisso: 'Fonoaudiologia',
        idPeriodicidade: 'per1',
        nomePeriodicidade: 'Semanal',
        idProfissional: 'p1',
        nomeProfissional: 'Dra. Fernanda',
        conselhoProfissional: 'CRFa 999',
        idUser: 'u1',
      },
    ] as any);

    renderWithAuth({
      isResponsavel: true,
      user: { idUser: '5', nomeUser: 'Pai do Lucas', emailUser: 'pai@email.com', perfis: ['RESPONSAVEL'] },
    });

    await waitFor(() => {
      expect(screen.getByText('Fonoaudiologia')).toBeInTheDocument();
      expect(screen.getByText('Lucas Filho')).toBeInTheDocument();
      expect(screen.getByText(/Dra\. Fernanda/)).toBeInTheDocument();
    });

    // Avança para amanhã onde não há nenhum agendamento -> deve deixar área limpa
    fireEvent.click(screen.getByTitle('Próximo dia'));

    await waitFor(() => {
      expect(screen.queryByText('Lucas Filho')).not.toBeInTheDocument();
      expect(screen.getByLabelText('Sem agendamentos no dia')).toBeInTheDocument();
    });
  });
});
