import { useState, useMemo } from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import MeuPerfilModal from './MeuPerfilModal';
import TrocarSenhaObrigatoriaModal from './TrocarSenhaObrigatoriaModal';
import NotificacoesBell from './NotificacoesBell';
import logoImg from '../assets/logo.png';
import {
  LayoutDashboard,
  Users,
  Building2,
  HeartPulse,
  UserCheck,
  Briefcase,
  CreditCard,
  CalendarClock,
  Calendar,
  CalendarRange,
  CalendarOff,
  FileText,
  DollarSign,
  ShieldCheck,
  LogOut,
} from 'lucide-react';

export default function AdminLayout() {
  const { user, logout, refreshUser, isAdmin, isResponsavel, isProfissional, isFuncionarioClinica, isAdminClinica } = useAuth();
  const [isPerfilModalOpen, setIsPerfilModalOpen] = useState(false);

  const navigation = useMemo(() => {
    const isGestaoClinica = isAdminClinica || isFuncionarioClinica;

    if (isAdmin) {
      const items = [
        { name: 'Visão Geral', href: '/admin', icon: LayoutDashboard, end: true },
        { name: 'Agenda', href: '/admin/agenda', icon: Calendar, end: false },
        { name: 'Grade Terapêutica', href: '/admin/grade-terapeutica', icon: CalendarRange, end: false },
        { name: 'Feriados & Recessos', href: '/admin/feriados-recessos', icon: CalendarOff, end: false },
        { name: 'Documentos', href: '/admin/arquivos', icon: FileText, end: false },
        { name: 'Financeiro', href: '/admin/financeiro', icon: DollarSign, end: false },
        { name: 'Usuários', href: '/admin/usuarios', icon: Users, end: false },
        { name: 'Clínicas', href: '/admin/clinicas', icon: Building2, end: false },
        { name: 'Pacientes', href: '/admin/pacientes', icon: HeartPulse, end: false },
        { name: 'Responsáveis', href: '/admin/responsaveis', icon: UserCheck, end: false },
        { name: 'Profissionais', href: '/admin/profissionais', icon: Briefcase, end: false },
        { name: 'Planos de Saúde', href: '/admin/planos', icon: CreditCard, end: false },
        { name: 'Periodicidades', href: '/admin/periodicidades', icon: CalendarClock, end: false },
        { name: 'Auditoria & Logs', href: '/admin/auditoria', icon: ShieldCheck, end: false },
      ];

      if (isProfissional) {
        items.splice(5, 0, { name: 'Meu Financeiro', href: '/admin/meu-financeiro', icon: DollarSign, end: false });
      }

      return items;
    }

    const items: Array<{ name: string; href: string; icon: any; end: boolean }> = [
      { name: 'Visão Geral', href: '/admin', icon: LayoutDashboard, end: true },
      { name: 'Agenda', href: '/admin/agenda', icon: Calendar, end: false },
    ];

    // Grade Terapêutica: disponível para gestão clínica, profissionais e responsáveis
    if (isGestaoClinica || isProfissional || isResponsavel) {
      items.push({ name: 'Grade Terapêutica', href: '/admin/grade-terapeutica', icon: CalendarRange, end: false });
    }

    // Feriados & Recessos: gestão da clínica (férias coletivas e suspensão de atendimentos)
    if (isGestaoClinica) {
      items.push({ name: 'Feriados & Recessos', href: '/admin/feriados-recessos', icon: CalendarOff, end: false });
    }

    // Documentos sempre presente
    items.push({ name: 'Documentos', href: '/admin/arquivos', icon: FileText, end: false });

    // Financeiro da Clínica / Gestão
    if (isGestaoClinica || isResponsavel) {
      items.push({ name: 'Financeiro', href: '/admin/financeiro', icon: DollarSign, end: false });
    }

    // Meu Financeiro para quem é profissional
    if (isProfissional) {
      items.push({ name: 'Meu Financeiro', href: '/admin/meu-financeiro', icon: DollarSign, end: false });
    }

    // Pacientes: label dinâmico conforme o perfil principal do usuário
    const labelPacientes = isGestaoClinica
      ? 'Pacientes da Clínica'
      : isProfissional
      ? 'Pacientes em Atendimento'
      : isResponsavel
      ? 'Meus Pacientes'
      : 'Pacientes';
    items.push({ name: labelPacientes, href: '/admin/pacientes', icon: HeartPulse, end: false });

    // Gestão da Clínica: Responsáveis e Profissionais da equipe
    if (isGestaoClinica) {
      items.push({ name: 'Responsáveis', href: '/admin/responsaveis', icon: UserCheck, end: false });
      items.push({ name: 'Profissionais', href: '/admin/profissionais', icon: Briefcase, end: false });
    }

    // Planos de Saúde para todos os perfis
    items.push({ name: 'Planos de Saúde', href: '/admin/planos', icon: CreditCard, end: false });

    // Periodicidades apenas para Administrador da Clínica (ou ADMIN)
    if (isAdminClinica) {
      items.push({ name: 'Periodicidades', href: '/admin/periodicidades', icon: CalendarClock, end: false });
    }

    return items;
  }, [isAdmin, isResponsavel, isProfissional, isFuncionarioClinica, isAdminClinica]);

  const initials = user?.nomeUser
    ? user.nomeUser
        .split(' ')
        .map((n) => n[0])
        .slice(0, 2)
        .join('')
        .toUpperCase()
    : 'DA';

  const roleLabel = isAdmin
    ? 'Admin'
    : isAdminClinica
    ? 'Admin Clínica'
    : isFuncionarioClinica
    ? 'Clínica'
    : isProfissional && isResponsavel
    ? 'Profissional / Resp'
    : isProfissional
    ? 'Profissional'
    : isResponsavel
    ? 'Responsável'
    : 'Portal';

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col fixed inset-y-0 z-10">
        <div className="h-16 flex items-center gap-3 px-6 border-b border-gray-200">
          <div className="w-10 h-10 rounded-xl bg-slate-50 border border-slate-200 flex items-center justify-center p-1.5 shadow-sm">
            <img src={logoImg} alt="DailyAutism" className="w-full h-full object-contain" />
          </div>
          <div>
            <span className="font-bold text-gray-900 leading-tight block">DailyAutism</span>
            <span className="text-xs text-indigo-600 font-semibold uppercase tracking-wider block">
              {roleLabel}
            </span>
          </div>
        </div>

        <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
          {navigation.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.name}
                to={item.href}
                end={item.end}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-indigo-50 text-indigo-700'
                      : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                  }`
                }
              >
                <Icon className="h-5 w-5 flex-shrink-0" />
                {item.name}
              </NavLink>
            );
          })}
        </nav>

        <div className="p-4 border-t border-gray-200">
          <button
            onClick={logout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
          >
            <LogOut className="h-5 w-5" />
            Sair
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col pl-64 min-w-0">
        {/* Top Header */}
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between gap-4 px-6 lg:px-8 sticky top-0 z-10 min-w-0">
          <div className="min-w-0">
            <h2 className="text-lg font-semibold text-gray-800 truncate">
              {isAdmin ? 'Painel de Controle' : `Portal do ${roleLabel}`}
            </h2>
          </div>
          <div className="flex items-center gap-3 sm:gap-4 min-w-0 shrink-0">
            <NotificacoesBell />
            <button
              onClick={() => setIsPerfilModalOpen(true)}
              className="flex items-center gap-3 text-left hover:opacity-85 transition-opacity group cursor-pointer"
              title="Clique para editar seu perfil e senha"
            >
              <div className="text-right min-w-0 max-w-[220px] sm:max-w-xs">
                <span className="block text-sm font-medium text-gray-900 truncate group-hover:text-indigo-600 transition-colors">
                  {user?.nomeUser}
                </span>
                <span className="block text-xs text-gray-500 truncate">{user?.emailUser}</span>
              </div>
              <div className="w-10 h-10 rounded-full bg-indigo-100 text-indigo-700 font-semibold flex items-center justify-center text-sm border-2 border-indigo-200 shrink-0 group-hover:border-indigo-400 transition-colors">
                {initials}
              </div>
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 min-w-0 p-6 lg:p-8">
          <Outlet />
        </main>
      </div>

      <MeuPerfilModal
        isOpen={isPerfilModalOpen}
        onClose={() => setIsPerfilModalOpen(false)}
      />

      <TrocarSenhaObrigatoriaModal
        isOpen={Boolean(user?.trocarSenhaNoProximoLogin)}
        onSuccess={async () => {
          if (refreshUser) {
            await refreshUser();
          }
        }}
      />
    </div>
  );
}
