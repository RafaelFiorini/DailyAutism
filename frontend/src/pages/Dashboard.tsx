import { useState, useEffect, useMemo, useCallback, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { userService } from '../services/userService';
import { clinicaService } from '../services/clinicaService';
import { pacienteService } from '../services/pacienteService';
import { profissionalService } from '../services/profissionalService';
import { responsavelService } from '../services/responsavelService';
import { compromissoService } from '../services/compromissoService';
import { financeiroService } from '../services/financeiroService';
import { extractApiErrorMessage } from '../utils/apiError';
import type { Compromisso, StatusCompromisso } from '../types/compromisso';
import type { Profissional } from '../types/profissional';
import type { GuiaFaturamento } from '../types/financeiro';
import AssinarGuiaModal from '../components/AssinarGuiaModal';
import {
  Users,
  Building2,
  HeartPulse,
  Briefcase,
  Activity,
  CheckCircle2,
  UserCheck,
  Calendar,
  ChevronLeft,
  ChevronRight,
  Clock,
  MapPin,
  Tag,
  Loader2,
  Check,
  XCircle,
  UserX,
  Wallet,
  CalendarCheck,
  FileCheck2,
  ShieldCheck,
} from 'lucide-react';

const STATUS_BADGE: Record<StatusCompromisso, { label: string; className: string }> = {
  AGENDADO: {
    label: 'Agendado',
    className: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  },
  AGUARDANDO_APROVACAO: {
    label: 'Aguardando Aprovação',
    className: 'bg-amber-50 text-amber-700 border-amber-200',
  },
  REALIZADO: {
    label: 'Realizado',
    className: 'bg-blue-50 text-blue-700 border-blue-200',
  },
  CANCELADO: {
    label: 'Cancelado',
    className: 'bg-gray-100 text-gray-600 border-gray-200',
  },
  FALTOU: {
    label: 'Faltou',
    className: 'bg-rose-50 text-rose-700 border-rose-200',
  },
  REJEITADO: {
    label: 'Rejeitado',
    className: 'bg-red-50 text-red-700 border-red-200',
  },
};

function getTodayISO(): string {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function addDaysISO(dateISO: string, days: number): string {
  const [y, m, d] = dateISO.split('-').map(Number);
  const dt = new Date(y, m - 1, d);
  dt.setDate(dt.getDate() + days);
  const ny = dt.getFullYear();
  const nm = String(dt.getMonth() + 1).padStart(2, '0');
  const nd = String(dt.getDate()).padStart(2, '0');
  return `${ny}-${nm}-${nd}`;
}

function formatDayHeading(dateISO: string): string {
  const [y, m, d] = dateISO.split('-').map(Number);
  const dt = new Date(y, m - 1, d);
  return dt.toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
}

function formatTime(dateTimeStr?: string): string {
  if (!dateTimeStr) return '--:--';
  const timePart = dateTimeStr.split('T')[1];
  if (timePart && timePart.length >= 5) {
    return timePart.substring(0, 5);
  }
  return '--:--';
}

interface DayNavigatorProps {
  selectedDate: string;
  onChangeDate: (newDate: string) => void;
}

function DayNavigator({ selectedDate, onChangeDate }: DayNavigatorProps) {
  const todayISO = getTodayISO();
  const isToday = selectedDate === todayISO;

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-sm">
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onChangeDate(addDaysISO(selectedDate, -1))}
          aria-label="Dia anterior"
          title="Dia anterior"
          className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-indigo-600 transition-colors"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>

        <div className="px-2">
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-indigo-600 shrink-0" />
            <span className="text-base font-semibold text-gray-900 capitalize">
              {formatDayHeading(selectedDate)}
            </span>
            {isToday && (
              <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-200">
                Hoje
              </span>
            )}
          </div>
        </div>

        <button
          type="button"
          onClick={() => onChangeDate(addDaysISO(selectedDate, 1))}
          aria-label="Próximo dia"
          title="Próximo dia"
          className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-indigo-600 transition-colors"
        >
          <ChevronRight className="w-5 h-5" />
        </button>
      </div>

      <div className="flex items-center gap-2">
        {!isToday && (
          <button
            type="button"
            onClick={() => onChangeDate(todayISO)}
            className="px-3 py-1.5 rounded-lg border border-indigo-200 bg-indigo-50 text-indigo-700 text-xs font-medium hover:bg-indigo-100 transition-colors"
          >
            Ir para Hoje
          </button>
        )}
        <input
          type="date"
          value={selectedDate}
          onChange={(e) => {
            if (e.target.value) onChangeDate(e.target.value);
          }}
          aria-label="Selecionar data"
          className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>
    </div>
  );
}

export default function Dashboard() {
  const auth = useContext(AuthContext);
  const user = auth?.user ?? null;
  const isAdmin = auth?.isAdmin ?? false;
  const isAdminClinica = auth?.isAdminClinica ?? false;
  const isFuncionarioClinica = auth?.isFuncionarioClinica ?? false;
  const isProfissional = auth?.isProfissional ?? false;
  const isResponsavel = auth?.isResponsavel ?? false;

  // Estados Admin Geral / Admin Clínica
  const [loadingStats, setLoadingStats] = useState(false);
  const [adminStats, setAdminStats] = useState({
    usuarios: 0,
    clinicas: 0,
    pacientes: 0,
    profissionais: 0,
  });
  const [clinicaAdminStats, setClinicaAdminStats] = useState({
    pacientes: 0,
    profissionais: 0,
    responsaveis: 0,
    agendamentosHoje: 0,
  });

  // Estados para Agendas (Funcionário Clínica, Profissional, Responsável)
  const [selectedDate, setSelectedDate] = useState<string>(() => getTodayISO());
  const [loadingAgenda, setLoadingAgenda] = useState(false);
  const [compromissos, setCompromissos] = useState<Compromisso[]>([]);
  const [profissionais, setProfissionais] = useState<Profissional[]>([]);
  const [guiasPendentesAssinatura, setGuiasPendentesAssinatura] = useState<GuiaFaturamento[]>([]);
  const [guiaSelecionadaAssinar, setGuiaSelecionadaAssinar] = useState<GuiaFaturamento | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  const loadDashboardData = useCallback(async () => {
    if (isAdmin) {
      setLoadingStats(true);
      try {
        const [usersList, clinicasList, pacientesList, profList] = await Promise.all([
          userService.list().catch(() => []),
          clinicaService.list().catch(() => []),
          pacienteService.list().catch(() => []),
          profissionalService.list().catch(() => []),
        ]);
        setAdminStats({
          usuarios: usersList.length,
          clinicas: clinicasList.length,
          pacientes: pacientesList.length,
          profissionais: profList.length,
        });
      } finally {
        setLoadingStats(false);
      }
      return;
    }

    if (isAdminClinica) {
      setLoadingStats(true);
      try {
        const todayISO = getTodayISO();
        const [pacientesList, profList, respList, compList] = await Promise.all([
          pacienteService.list().catch(() => []),
          profissionalService.list().catch(() => []),
          responsavelService.list().catch(() => []),
          compromissoService.list().catch(() => []),
        ]);
        const hojeCount = compList.filter(
          (c) => c.dataHoraCompromisso && c.dataHoraCompromisso.slice(0, 10) === todayISO
        ).length;
        setClinicaAdminStats({
          pacientes: pacientesList.length,
          profissionais: profList.length,
          responsaveis: respList.length,
          agendamentosHoje: hojeCount,
        });
      } finally {
        setLoadingStats(false);
      }
      return;
    }

    if (isFuncionarioClinica || isProfissional || isResponsavel) {
      setLoadingAgenda(true);
      try {
        const [compList, profList, guiasPend] = await Promise.all([
          compromissoService.list().catch(() => []),
          isFuncionarioClinica ? profissionalService.list().catch(() => []) : Promise.resolve([]),
          isResponsavel
            ? financeiroService.listGuiasConvenio({ status: 'AGUARDANDO_ASSINATURA' }).catch(() => [])
            : Promise.resolve([]),
        ]);
        setCompromissos(compList);
        setProfissionais(profList);
        setGuiasPendentesAssinatura(guiasPend);
      } finally {
        setLoadingAgenda(false);
      }
    }
  }, [isAdmin, isAdminClinica, isFuncionarioClinica, isProfissional, isResponsavel]);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  // Compromissos filtrados pelo dia selecionado e ordenados por horário crescente
  const dayCompromissos = useMemo(() => {
    return compromissos
      .filter((c) => c.dataHoraCompromisso && c.dataHoraCompromisso.slice(0, 10) === selectedDate)
      .sort((a, b) => (a.dataHoraCompromisso || '').localeCompare(b.dataHoraCompromisso || ''));
  }, [compromissos, selectedDate]);

  // Agrupamento alfabético por profissional para o perfil Funcionário Clínica
  const clinicaGroupedByProfissional = useMemo(() => {
    const groupsMap = new Map<
      string,
      { idProf: string; nomeProfissional: string; conselhoProf?: string; itens: Compromisso[] }
    >();

    // Inicializa todos os profissionais cadastrados na clínica
    profissionais.forEach((p) => {
      groupsMap.set(p.idProf, {
        idProf: p.idProf,
        nomeProfissional: p.nomeProfissional,
        conselhoProf: p.conselhoProf,
        itens: [],
      });
    });

    const semProfissional: Compromisso[] = [];

    dayCompromissos.forEach((c) => {
      if (c.idProfissional) {
        const existing = groupsMap.get(c.idProfissional);
        if (existing) {
          existing.itens.push(c);
        } else {
          groupsMap.set(c.idProfissional, {
            idProf: c.idProfissional,
            nomeProfissional: c.nomeProfissional || 'Profissional',
            conselhoProf: c.conselhoProfissional,
            itens: [c],
          });
        }
      } else {
        semProfissional.push(c);
      }
    });

    const sortedGroups = Array.from(groupsMap.values()).sort((a, b) =>
      a.nomeProfissional.localeCompare(b.nomeProfissional, 'pt-BR', { sensitivity: 'base' })
    );

    return { sortedGroups, semProfissional };
  }, [profissionais, dayCompromissos]);

  const handleAprovar = async (compromisso: Compromisso) => {
    setActionLoadingId(compromisso.idCompromisso);
    try {
      await compromissoService.aprovar(compromisso.idCompromisso);
      await loadDashboardData();
    } catch (err: unknown) {
      alert(extractApiErrorMessage(err, 'Erro ao aprovar agendamento.'));
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleRejeitar = async (compromisso: Compromisso) => {
    setActionLoadingId(compromisso.idCompromisso);
    try {
      const updated = await compromissoService.rejeitar(compromisso.idCompromisso);
      setCompromissos((prev) =>
        prev.map((c) => (c.idCompromisso === updated.idCompromisso ? updated : c))
      );
    } catch (err: unknown) {
      alert(extractApiErrorMessage(err, 'Erro ao rejeitar agendamento.'));
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleUpdateStatus = async (id: string, status: StatusCompromisso) => {
    setActionLoadingId(id);
    try {
      const updated = await compromissoService.updateStatus(id, status);
      setCompromissos((prev) =>
        prev.map((c) => (c.idCompromisso === updated.idCompromisso ? updated : c))
      );
    } catch (err: unknown) {
      alert(extractApiErrorMessage(err, 'Erro ao atualizar status.'));
    } finally {
      setActionLoadingId(null);
    }
  };

  // =========================================================================
  // 1. VISÃO ADMIN GERAL
  // =========================================================================
  if (isAdmin) {
    const cards = [
      {
        name: 'Usuários Cadastrados',
        count: loadingStats ? '...' : adminStats.usuarios,
        href: '/admin/usuarios',
        icon: Users,
        color: 'text-blue-600',
        bg: 'bg-blue-50',
      },
      {
        name: 'Clínicas Parceiras',
        count: loadingStats ? '...' : adminStats.clinicas,
        href: '/admin/clinicas',
        icon: Building2,
        color: 'text-indigo-600',
        bg: 'bg-indigo-50',
      },
      {
        name: 'Pacientes Acompanhados',
        count: loadingStats ? '...' : adminStats.pacientes,
        href: '/admin/pacientes',
        icon: HeartPulse,
        color: 'text-rose-600',
        bg: 'bg-rose-50',
      },
      {
        name: 'Profissionais Ativos',
        count: loadingStats ? '...' : adminStats.profissionais,
        href: '/admin/profissionais',
        icon: Briefcase,
        color: 'text-amber-600',
        bg: 'bg-amber-50',
      },
    ];

    return (
      <div className="space-y-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Olá, {user?.nomeUser || 'Administrador'}!
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Bem-vindo à Visão Geral do painel de administração da plataforma DailyAutism.
            </p>
          </div>
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-medium self-start sm:self-auto">
            <Activity className="h-4 w-4 animate-pulse text-emerald-600" />
            <span>Backend Conectado</span>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {cards.map((card) => {
            const Icon = card.icon;
            return (
              <Link
                key={card.name}
                to={card.href}
                className="bg-white rounded-xl border border-gray-200 p-6 flex items-center justify-between shadow-sm hover:shadow-md transition-shadow"
              >
                <div>
                  <p className="text-sm font-medium text-gray-500">{card.name}</p>
                  <p className="text-2xl font-bold text-gray-900 mt-2">{card.count}</p>
                </div>
                <div className={`w-12 h-12 rounded-lg ${card.bg} ${card.color} flex items-center justify-center`}>
                  <Icon className="h-6 w-6" />
                </div>
              </Link>
            );
          })}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="text-base font-semibold text-gray-900 mb-4">Módulos Administrativos</h3>
          <p className="text-sm text-gray-600 mb-4">
            A partir desta interface você poderá gerenciar acessos, dados clínicos, cadastros de pacientes e configurações operacionais da plataforma DailyAutism.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
            <div className="p-4 rounded-lg bg-gray-50 border border-gray-100 flex items-start gap-3">
              <CheckCircle2 className="h-5 w-5 text-indigo-600 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-medium text-gray-900">Autenticação JWT</h4>
                <p className="text-xs text-gray-500 mt-0.5">Sessão segura com verificação de papéis e expiração.</p>
              </div>
            </div>
            <div className="p-4 rounded-lg bg-gray-50 border border-gray-100 flex items-start gap-3">
              <CheckCircle2 className="h-5 w-5 text-indigo-600 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-medium text-gray-900">Controle de Acesso RBAC</h4>
                <p className="text-xs text-gray-500 mt-0.5">Proteção estrita para o perfil de administrador.</p>
              </div>
            </div>
            <div className="p-4 rounded-lg bg-gray-50 border border-gray-100 flex items-start gap-3">
              <CheckCircle2 className="h-5 w-5 text-indigo-600 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-medium text-gray-900">Integração com Spring Boot</h4>
                <p className="text-xs text-gray-500 mt-0.5">Comunicação REST padronizada com a API central.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // =========================================================================
  // 2. VISÃO ADMIN DA CLÍNICA
  // =========================================================================
  if (isAdminClinica) {
    const cards = [
      {
        name: 'Pacientes da Clínica',
        count: loadingStats ? '...' : clinicaAdminStats.pacientes,
        href: '/admin/pacientes',
        icon: HeartPulse,
        color: 'text-rose-600',
        bg: 'bg-rose-50',
      },
      {
        name: 'Profissionais da Clínica',
        count: loadingStats ? '...' : clinicaAdminStats.profissionais,
        href: '/admin/profissionais',
        icon: Briefcase,
        color: 'text-amber-600',
        bg: 'bg-amber-50',
      },
      {
        name: 'Responsáveis Vinculados',
        count: loadingStats ? '...' : clinicaAdminStats.responsaveis,
        href: '/admin/responsaveis',
        icon: UserCheck,
        color: 'text-blue-600',
        bg: 'bg-blue-50',
      },
      {
        name: 'Agendamentos de Hoje',
        count: loadingStats ? '...' : clinicaAdminStats.agendamentosHoje,
        href: '/admin/agenda',
        icon: CalendarCheck,
        color: 'text-emerald-600',
        bg: 'bg-emerald-50',
      },
    ];

    return (
      <div className="space-y-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Olá, {user?.nomeUser || 'Gestor'}!
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Visão Geral de Gestão —{' '}
              <span className="font-semibold text-indigo-600">
                {user?.nomeClinica || 'Minha Clínica'}
              </span>
            </p>
          </div>
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-indigo-50 border border-indigo-200 text-indigo-700 text-xs font-medium self-start sm:self-auto">
            <Building2 className="h-4 w-4 text-indigo-600" />
            <span>Administração da Clínica</span>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {cards.map((card) => {
            const Icon = card.icon;
            return (
              <Link
                key={card.name}
                to={card.href}
                className="bg-white rounded-xl border border-gray-200 p-6 flex items-center justify-between shadow-sm hover:shadow-md transition-shadow"
              >
                <div>
                  <p className="text-sm font-medium text-gray-500">{card.name}</p>
                  <p className="text-2xl font-bold text-gray-900 mt-2">{card.count}</p>
                </div>
                <div className={`w-12 h-12 rounded-lg ${card.bg} ${card.color} flex items-center justify-center`}>
                  <Icon className="h-6 w-6" />
                </div>
              </Link>
            );
          })}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="text-base font-semibold text-gray-900 mb-4">Módulos da Clínica</h3>
          <p className="text-sm text-gray-600 mb-4">
            Acompanhe os indicadores da sua unidade, configure convênios, periodicidades e gerencie a equipe multidisciplinar.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
            <Link
              to="/admin/agenda"
              className="p-4 rounded-lg bg-gray-50 border border-gray-100 hover:border-indigo-200 flex items-start gap-3 transition-colors"
            >
              <Calendar className="h-5 w-5 text-indigo-600 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-medium text-gray-900">Agenda Integrada</h4>
                <p className="text-xs text-gray-500 mt-0.5">Gerencie sessões, aprovações e horários dos profissionais.</p>
              </div>
            </Link>
            <Link
              to="/admin/planos"
              className="p-4 rounded-lg bg-gray-50 border border-gray-100 hover:border-indigo-200 flex items-start gap-3 transition-colors"
            >
              <CheckCircle2 className="h-5 w-5 text-indigo-600 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-medium text-gray-900">Planos & Periodicidades</h4>
                <p className="text-xs text-gray-500 mt-0.5">Configure convênios aceitos e intervalos de recorrência.</p>
              </div>
            </Link>
            <div className="p-4 rounded-lg bg-gray-50 border border-dashed border-gray-200 flex items-start gap-3 opacity-80">
              <Wallet className="h-5 w-5 text-gray-400 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-medium text-gray-700">Módulo Financeiro (Em breve)</h4>
                <p className="text-xs text-gray-500 mt-0.5">Faturamento de guias, repasses e controle financeiro da unidade.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // =========================================================================
  // 3. VISÃO FUNCIONÁRIO DA CLÍNICA / RECEPÇÃO (Agenda por Profissional A-Z)
  // =========================================================================
  if (isFuncionarioClinica) {
    return (
      <div className="space-y-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Agenda do Dia {user?.nomeClinica ? `— ${user.nomeClinica}` : ''}
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Atendimentos organizados em ordem alfabética por profissional.
            </p>
          </div>
          <Link
            to="/admin/agenda"
            className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors self-start sm:self-auto"
          >
            <Calendar className="w-4 h-4" />
            Gerenciar Agenda Completa
          </Link>
        </div>

        <DayNavigator selectedDate={selectedDate} onChangeDate={setSelectedDate} />

        {loadingAgenda ? (
          <div className="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-3">
            <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
            <p className="text-sm text-gray-500">Carregando agenda do dia...</p>
          </div>
        ) : clinicaGroupedByProfissional.sortedGroups.length === 0 &&
          clinicaGroupedByProfissional.semProfissional.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <h3 className="text-base font-medium text-gray-900">Nenhum profissional ou atendimento</h3>
            <p className="text-sm text-gray-500 mt-1">
              Não há profissionais ou agendamentos cadastrados para este dia.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {clinicaGroupedByProfissional.sortedGroups.map((grupo) => (
              <div
                key={grupo.idProf}
                className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden"
              >
                <div className="px-6 py-4 bg-gray-50/75 border-b border-gray-200/80 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold text-sm">
                      {grupo.nomeProfissional.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <h3 className="text-base font-semibold text-gray-900">
                        {grupo.nomeProfissional}
                      </h3>
                      {grupo.conselhoProf && (
                        <p className="text-xs text-gray-500 font-mono">{grupo.conselhoProf}</p>
                      )}
                    </div>
                  </div>
                  <span className="px-2.5 py-1 rounded-full text-xs font-medium bg-white border border-gray-200 text-gray-700">
                    {grupo.itens.length} {grupo.itens.length === 1 ? 'atendimento' : 'atendimentos'}
                  </span>
                </div>

                {grupo.itens.length === 0 ? (
                  <div className="px-6 py-4 text-sm text-gray-400 italic">
                    Nenhum paciente agendado para este profissional nesta data.
                  </div>
                ) : (
                  <div className="divide-y divide-gray-100">
                    {grupo.itens.map((c) => {
                      const badge = STATUS_BADGE[c.status] || STATUS_BADGE.AGENDADO;
                      return (
                        <div
                          key={c.idCompromisso}
                          className="px-6 py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-gray-50/50 transition-colors"
                        >
                          <div className="flex items-start sm:items-center gap-4">
                            <div className="px-3 py-1.5 rounded-lg bg-indigo-50 text-indigo-700 font-bold text-sm flex items-center gap-1.5 shrink-0">
                              <Clock className="w-3.5 h-3.5" />
                              {formatTime(c.dataHoraCompromisso)}
                            </div>
                            <div>
                              <p className="font-semibold text-gray-900 text-sm">
                                {c.nomePaciente}
                              </p>
                              <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mt-0.5">
                                <span className="flex items-center gap-1">
                                  <Tag className="w-3 h-3 text-gray-400" />
                                  {c.nomeTipoCompromisso || c.tituloCompromisso}
                                </span>
                                {c.localCompromisso && (
                                  <span className="flex items-center gap-1">
                                    <MapPin className="w-3 h-3 text-gray-400" />
                                    {c.localCompromisso}
                                  </span>
                                )}
                              </div>
                            </div>
                          </div>

                          <span
                            className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border self-start sm:self-center ${badge.className}`}
                          >
                            {badge.label}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            ))}

            {clinicaGroupedByProfissional.semProfissional.length > 0 && (
              <div className="bg-white rounded-xl border border-amber-200/80 shadow-sm overflow-hidden">
                <div className="px-6 py-4 bg-amber-50/60 border-b border-amber-200/80 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Briefcase className="w-5 h-5 text-amber-600" />
                    <h3 className="text-base font-semibold text-amber-900">
                      Profissional a Definir
                    </h3>
                  </div>
                  <span className="px-2.5 py-1 rounded-full text-xs font-medium bg-white border border-amber-200 text-amber-800">
                    {clinicaGroupedByProfissional.semProfissional.length}
                  </span>
                </div>
                <div className="divide-y divide-gray-100">
                  {clinicaGroupedByProfissional.semProfissional.map((c) => {
                    const badge = STATUS_BADGE[c.status] || STATUS_BADGE.AGENDADO;
                    return (
                      <div
                        key={c.idCompromisso}
                        className="px-6 py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                      >
                        <div className="flex items-center gap-4">
                          <div className="px-3 py-1.5 rounded-lg bg-amber-50 text-amber-700 font-bold text-sm flex items-center gap-1.5">
                            <Clock className="w-3.5 h-3.5" />
                            {formatTime(c.dataHoraCompromisso)}
                          </div>
                          <div>
                            <p className="font-semibold text-gray-900 text-sm">{c.nomePaciente}</p>
                            <p className="text-xs text-gray-500">
                              {c.nomeTipoCompromisso || c.tituloCompromisso}
                              {c.localCompromisso ? ` • ${c.localCompromisso}` : ''}
                            </p>
                          </div>
                        </div>
                        <span
                          className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border self-start sm:self-center ${badge.className}`}
                        >
                          {badge.label}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    );
  }

  // =========================================================================
  // 4. VISÃO PROFISSIONAL (Agenda do Dia organizada por horário)
  // =========================================================================
  if (isProfissional) {
    return (
      <div className="space-y-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Olá, {user?.nomeUser || 'Profissional'}!
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Sua agenda de atendimentos organizada por horário.
            </p>
          </div>
          <Link
            to="/admin/agenda"
            className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-lg text-sm font-medium hover:bg-indigo-100 transition-colors self-start sm:self-auto"
          >
            <Calendar className="w-4 h-4" />
            Ver Agenda Completa
          </Link>
        </div>

        <DayNavigator selectedDate={selectedDate} onChangeDate={setSelectedDate} />

        {loadingAgenda ? (
          <div className="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-3">
            <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
            <p className="text-sm text-gray-500">Carregando seus horários...</p>
          </div>
        ) : dayCompromissos.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <h3 className="text-base font-medium text-gray-900">Nenhum atendimento para este dia</h3>
            <p className="text-sm text-gray-500 mt-1">
              Utilize os botões acima para navegar entre os dias.
            </p>
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm divide-y divide-gray-100">
            {dayCompromissos.map((c) => {
              const badge = STATUS_BADGE[c.status] || STATUS_BADGE.AGENDADO;
              const isActionLoading = actionLoadingId === c.idCompromisso;

              return (
                <div
                  key={c.idCompromisso}
                  className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-gray-50/50 transition-colors"
                >
                  <div className="flex items-start sm:items-center gap-4">
                    <div className="px-3.5 py-2 rounded-xl bg-indigo-50 text-indigo-700 font-bold text-base flex items-center gap-1.5 shrink-0">
                      <Clock className="w-4 h-4" />
                      {formatTime(c.dataHoraCompromisso)}
                    </div>
                    <div>
                      <h3 className="text-base font-semibold text-gray-900">{c.nomePaciente}</h3>
                      <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mt-1">
                        <span className="flex items-center gap-1 font-medium text-gray-700">
                          <Tag className="w-3.5 h-3.5 text-indigo-500" />
                          {c.nomeTipoCompromisso || c.tituloCompromisso}
                        </span>
                        {c.localCompromisso && (
                          <span className="flex items-center gap-1">
                            <MapPin className="w-3.5 h-3.5 text-gray-400" />
                            {c.localCompromisso}
                          </span>
                        )}
                      </div>
                      {c.descricaoCompromisso && (
                        <p className="text-xs text-gray-500 mt-1">{c.descricaoCompromisso}</p>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-2 self-end sm:self-center">
                    <span
                      className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${badge.className}`}
                    >
                      {badge.label}
                    </span>

                    {c.status === 'AGENDADO' && (
                      <div className="flex items-center gap-1 ml-2">
                        {isActionLoading ? (
                          <Loader2 className="w-4 h-4 animate-spin text-indigo-600" />
                        ) : (
                          <>
                            <button
                              type="button"
                              onClick={() => handleUpdateStatus(c.idCompromisso, 'REALIZADO')}
                              title="Marcar como Realizado"
                              className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            >
                              <Check className="w-4 h-4" />
                            </button>
                            <button
                              type="button"
                              onClick={() => handleUpdateStatus(c.idCompromisso, 'FALTOU')}
                              title="Marcar Falta"
                              className="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                            >
                              <UserX className="w-4 h-4" />
                            </button>
                          </>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  }

  // =========================================================================
  // 5. VISÃO RESPONSÁVEL (Pais / Tutores)
  // =========================================================================
  if (isResponsavel) {
    return (
      <div className="space-y-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Olá, {user?.nomeUser || 'Responsável'}!
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Agenda de terapias e consultas dos seus dependentes.
            </p>
          </div>
          <Link
            to="/admin/agenda"
            className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-lg text-sm font-medium hover:bg-indigo-100 transition-colors self-start sm:self-auto"
          >
            <Calendar className="w-4 h-4" />
            Agenda Completa
          </Link>
        </div>

        {guiasPendentesAssinatura.length > 0 && (
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 space-y-3">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex items-center gap-2.5 text-amber-900">
                <FileCheck2 className="w-5 h-5 text-amber-600 shrink-0" />
                <div>
                  <h2 className="text-sm font-bold">
                    {guiasPendentesAssinatura.length} guia(s) de convênio aguardando sua assinatura
                  </h2>
                  <p className="text-xs text-amber-800">
                    Confira os atendimentos realizados e assine eletronicamente para a clínica enviar ao plano.
                  </p>
                </div>
              </div>
              <Link
                to="/admin/financeiro?tab=convenios"
                className="text-xs font-semibold text-amber-900 underline self-start sm:self-center"
              >
                Ver todas no Financeiro
              </Link>
            </div>
            <div className="space-y-2">
              {guiasPendentesAssinatura.map((g) => (
                <div
                  key={g.idGuiaFaturamento}
                  className="bg-white rounded-lg border border-amber-200 p-3 flex flex-col sm:flex-row sm:items-center justify-between gap-2"
                >
                  <div className="text-xs">
                    <span className="font-bold text-gray-900">{g.numeroLoteGuia}</span>
                    <span className="text-gray-600 ml-2">
                      {g.nomePaciente} · {g.nomePlanoSaude} · {g.itens.length} sessão(ões)
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setGuiaSelecionadaAssinar(g)}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg self-start sm:self-auto"
                  >
                    <ShieldCheck className="w-3.5 h-3.5" />
                    Conferir e Assinar
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        <AssinarGuiaModal
          isOpen={Boolean(guiaSelecionadaAssinar)}
          guia={guiaSelecionadaAssinar}
          onClose={() => setGuiaSelecionadaAssinar(null)}
          onSigned={() => loadDashboardData()}
        />

        <DayNavigator selectedDate={selectedDate} onChangeDate={setSelectedDate} />

        {loadingAgenda ? (
          <div className="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-3">
            <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
            <p className="text-sm text-gray-500">Carregando agenda...</p>
          </div>
        ) : dayCompromissos.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-200 min-h-[180px]" aria-label="Sem agendamentos no dia" />
        ) : (
          <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm divide-y divide-gray-100">
            {dayCompromissos.map((c) => {
              const badge = STATUS_BADGE[c.status] || STATUS_BADGE.AGENDADO;
              const isActionLoading = actionLoadingId === c.idCompromisso;

              return (
                <div
                  key={c.idCompromisso}
                  className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-gray-50/50 transition-colors"
                >
                  <div className="flex items-start sm:items-center gap-4">
                    <div className="px-3.5 py-2 rounded-xl bg-indigo-50 text-indigo-700 font-bold text-base flex items-center gap-1.5 shrink-0">
                      <Clock className="w-4 h-4" />
                      {formatTime(c.dataHoraCompromisso)}
                    </div>
                    <div>
                      <div className="flex flex-wrap items-center gap-2">
                        <h3 className="text-base font-semibold text-gray-900">
                          {c.nomeTipoCompromisso || c.tituloCompromisso}
                        </h3>
                        <span className="px-2 py-0.5 rounded-md text-xs font-medium bg-gray-100 text-gray-700">
                          {c.nomePaciente}
                        </span>
                      </div>
                      <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mt-1">
                        {c.nomeProfissional && (
                          <span className="flex items-center gap-1 text-gray-700 font-medium">
                            <Briefcase className="w-3.5 h-3.5 text-purple-500" />
                            {c.nomeProfissional}
                            {c.conselhoProfissional ? ` (${c.conselhoProfissional})` : ''}
                          </span>
                        )}
                        {c.localCompromisso && (
                          <span className="flex items-center gap-1">
                            <MapPin className="w-3.5 h-3.5 text-gray-400" />
                            {c.localCompromisso}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 self-end sm:self-center">
                    <span
                      className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${badge.className}`}
                    >
                      {badge.label}
                    </span>

                    {c.status === 'AGUARDANDO_APROVACAO' && (
                      <div className="flex items-center gap-1 ml-2">
                        {isActionLoading ? (
                          <Loader2 className="w-4 h-4 animate-spin text-indigo-600" />
                        ) : (
                          <>
                            <button
                              type="button"
                              onClick={() => handleAprovar(c)}
                              title="Aprovar Agendamento"
                              className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                            >
                              <CheckCircle2 className="w-5 h-5" />
                            </button>
                            <button
                              type="button"
                              onClick={() => handleRejeitar(c)}
                              title="Rejeitar Agendamento"
                              className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            >
                              <XCircle className="w-5 h-5" />
                            </button>
                          </>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  }

  // Fallback genérico caso algum perfil novo exista
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6">
      <h1 className="text-2xl font-bold text-gray-900">
        Olá, {user?.nomeUser || 'Usuário'}!
      </h1>
      <p className="text-sm text-gray-500 mt-1">
        Bem-vindo à plataforma DailyAutism. Utilize o menu lateral para navegar.
      </p>
    </div>
  );
}
