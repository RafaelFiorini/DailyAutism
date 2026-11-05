import { useAuth } from '../hooks/useAuth';
import { Users, Building2, HeartPulse, Briefcase, Activity, CheckCircle2 } from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuth();

  const cards = [
    { name: 'Usuários Cadastrados', count: '-', icon: Users, color: 'text-blue-600', bg: 'bg-blue-50' },
    { name: 'Clínicas Parceiras', count: '-', icon: Building2, color: 'text-indigo-600', bg: 'bg-indigo-50' },
    { name: 'Pacientes Acompanhados', count: '-', icon: HeartPulse, color: 'text-rose-600', bg: 'bg-rose-50' },
    { name: 'Profissionais Ativos', count: '-', icon: Briefcase, color: 'text-amber-600', bg: 'bg-amber-50' },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Olá, {user?.nomeUser || 'Administrador'}! 👋
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

      {/* Metrics Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div
              key={card.name}
              className="bg-white rounded-xl border border-gray-200 p-6 flex items-center justify-between shadow-sm hover:shadow-md transition-shadow"
            >
              <div>
                <p className="text-sm font-medium text-gray-500">{card.name}</p>
                <p className="text-2xl font-bold text-gray-900 mt-2">{card.count}</p>
              </div>
              <div className={`w-12 h-12 rounded-lg ${card.bg} ${card.color} flex items-center justify-center`}>
                <Icon className="h-6 w-6" />
              </div>
            </div>
          );
        })}
      </div>

      {/* Quick Access / Overview Card */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h3 className="text-base font-semibold text-gray-900 mb-4">Módulos Administrativos</h3>
        <p className="text-sm text-gray-600 mb-4">
          A partir desta interface você poderá gerenciar acessos, dados clínicos, cadastros de pacientes e configurações operacionais da clínica DailyAutism.
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

