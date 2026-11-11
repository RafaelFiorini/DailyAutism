import { useState, useEffect, useMemo, useCallback, useContext } from 'react';
import {
  Calendar,
  Plus,
  Search,
  Loader2,
  AlertCircle,
  Pencil,
  Trash2,
  CheckCircle2,
  XCircle,
  Clock,
  MapPin,
  Tag,
  CalendarClock,
  Filter,
  Check,
  Ban,
  UserX,
  Briefcase,
  User,
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { compromissoService } from '../services/compromissoService';
import { profissionalService } from '../services/profissionalService';
import { extractApiErrorMessage } from '../utils/apiError';
import type { Compromisso, StatusCompromisso } from '../types/compromisso';
import type { Profissional } from '../types/profissional';
import CompromissoModal from '../components/CompromissoModal';

const STATUS_CONFIG: Record<
  StatusCompromisso,
  { label: string; bg: string; text: string; border: string; icon: any }
> = {
  AGENDADO: {
    label: 'Agendado',
    bg: 'bg-emerald-50',
    text: 'text-emerald-700',
    border: 'border-emerald-200',
    icon: CheckCircle2,
  },
  AGUARDANDO_APROVACAO: {
    label: 'Aguardando Aprovação',
    bg: 'bg-amber-50',
    text: 'text-amber-700',
    border: 'border-amber-200',
    icon: Clock,
  },
  REALIZADO: {
    label: 'Realizado',
    bg: 'bg-blue-50',
    text: 'text-blue-700',
    border: 'border-blue-200',
    icon: Check,
  },
  CANCELADO: {
    label: 'Cancelado',
    bg: 'bg-gray-100',
    text: 'text-gray-600',
    border: 'border-gray-200',
    icon: Ban,
  },
  FALTOU: {
    label: 'Faltou',
    bg: 'bg-rose-50',
    text: 'text-rose-700',
    border: 'border-rose-200',
    icon: UserX,
  },
  REJEITADO: {
    label: 'Rejeitado',
    bg: 'bg-red-50',
    text: 'text-red-700',
    border: 'border-red-200',
    icon: XCircle,
  },
};

export default function Agenda() {
  const auth = useContext(AuthContext);
  const isResponsavel = auth?.isResponsavel ?? false;
  const isProfissional = auth?.isProfissional ?? false;

  const [compromissos, setCompromissos] = useState<Compromisso[]>([]);
  const [profissionais, setProfissionais] = useState<Profissional[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('TODOS');
  const [profissionalFilter, setProfissionalFilter] = useState<string>('TODOS');
  const [dateFilter, setDateFilter] = useState<'TODOS' | 'HOJE' | 'SEMANA' | 'FUTUROS'>('TODOS');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCompromisso, setEditingCompromisso] = useState<Compromisso | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  const fetchCompromissos = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [compList, profList] = await Promise.all([
        compromissoService.list(),
        profissionalService.list().catch(() => []),
      ]);
      setCompromissos(compList);
      setProfissionais(profList);
    } catch {
      setError('Não foi possível carregar a agenda de compromissos.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCompromissos();
  }, [fetchCompromissos]);

  const filteredCompromissos = useMemo(() => {
    const now = new Date();
    const todayStr = now.toISOString().slice(0, 10);

    const weekAhead = new Date();
    weekAhead.setDate(weekAhead.getDate() + 7);

    return compromissos.filter((c) => {
      // Filtro de Status
      if (statusFilter !== 'TODOS' && c.status !== statusFilter) {
        return false;
      }

      // Filtro de Profissional
      if (profissionalFilter !== 'TODOS' && c.idProfissional !== profissionalFilter) {
        return false;
      }

      // Filtro de Data
      if (dateFilter !== 'TODOS' && c.dataHoraCompromisso) {
        const d = new Date(c.dataHoraCompromisso);
        const dStr = c.dataHoraCompromisso.slice(0, 10);
        if (dateFilter === 'HOJE' && dStr !== todayStr) return false;
        if (dateFilter === 'SEMANA' && (d < now || d > weekAhead)) return false;
        if (dateFilter === 'FUTUROS' && d < now) return false;
      }

      // Filtro de Busca
      if (search.trim()) {
        const term = search.toLowerCase();
        const matchTitulo = (c.tituloCompromisso || '').toLowerCase().includes(term);
        const matchPaciente = (c.nomePaciente || '').toLowerCase().includes(term);
        const matchProfissional = (c.nomeProfissional || '').toLowerCase().includes(term);
        const matchTipo = (c.nomeTipoCompromisso || '').toLowerCase().includes(term);
        const matchLocal = (c.localCompromisso || '').toLowerCase().includes(term);
        return matchTitulo || matchPaciente || matchProfissional || matchTipo || matchLocal;
      }

      return true;
    });
  }, [compromissos, search, statusFilter, profissionalFilter, dateFilter]);

  // Contadores para cards de resumo
  const stats = useMemo(() => {
    const total = compromissos.length;
    const agendados = compromissos.filter((c) => c.status === 'AGENDADO').length;
    const pendentes = compromissos.filter((c) => c.status === 'AGUARDANDO_APROVACAO').length;
    const realizados = compromissos.filter((c) => c.status === 'REALIZADO').length;
    return { total, agendados, pendentes, realizados };
  }, [compromissos]);

  const handleSaved = (_saved: Compromisso) => {
    fetchCompromissos();
  };

  const handleAprovar = async (compromisso: Compromisso) => {
    const confirmar = window.confirm(
      `Deseja aprovar o agendamento de "${compromisso.tituloCompromisso}" para ${compromisso.nomePaciente}?` +
        (compromisso.nomePeriodicidade &&
        !compromisso.nomePeriodicidade.toLowerCase().includes('única') &&
        !compromisso.nomePeriodicidade.toLowerCase().includes('avulsa')
          ? '\n\nIsso gerará as sessões periódicas correspondentes.'
          : '')
    );
    if (!confirmar) return;

    setActionLoadingId(compromisso.idCompromisso);
    try {
      await compromissoService.aprovar(compromisso.idCompromisso);
      await fetchCompromissos();
    } catch (err: unknown) {
      alert(extractApiErrorMessage(err, 'Erro ao aprovar agendamento.'));
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleRejeitar = async (compromisso: Compromisso) => {
    if (!window.confirm(`Deseja rejeitar o agendamento "${compromisso.tituloCompromisso}"?`)) return;

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
      alert(extractApiErrorMessage(err, 'Erro ao atualizar status do agendamento.'));
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleDelete = async (id: string, titulo: string) => {
    if (!window.confirm(`Deseja realmente excluir o agendamento "${titulo}"?`)) return;

    setActionLoadingId(id);
    try {
      await compromissoService.delete(id);
      setCompromissos((prev) => prev.filter((c) => c.idCompromisso !== id));
    } catch (err: unknown) {
      alert(extractApiErrorMessage(err, 'Erro ao excluir agendamento.'));
    } finally {
      setActionLoadingId(null);
    }
  };

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '-';
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      return d.toLocaleDateString('pt-BR', {
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  const pageSubtitle = isResponsavel
    ? 'Acompanhe as sessões e terapias agendadas com os profissionais para seus dependentes.'
    : isProfissional
    ? 'Acompanhe seus horários de atendimento e a sequência de pacientes agendados.'
    : 'Gestão centralizada de atendimentos, escalonamento de profissionais e agenda de pacientes.';

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">
              {isProfissional ? 'Minha Agenda de Atendimentos' : 'Agenda'}
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-100">
              {compromissos.length} agendamentos
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">{pageSubtitle}</p>
        </div>

        <button
          onClick={() => {
            setEditingCompromisso(null);
            setIsModalOpen(true);
          }}
          className="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors shadow-sm self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          Novo Agendamento
        </button>
      </div>

      {/* Cards de Métricas */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-gray-200/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-500 uppercase tracking-wider">Total</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{stats.total}</p>
          </div>
          <div className="w-10 h-10 rounded-lg bg-gray-100 text-gray-600 flex items-center justify-center">
            <Calendar className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-emerald-600 uppercase tracking-wider">Agendados</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{stats.agendados}</p>
          </div>
          <div className="w-10 h-10 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-amber-600 uppercase tracking-wider">Pendentes</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{stats.pendentes}</p>
          </div>
          <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-blue-600 uppercase tracking-wider">Realizados</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{stats.realizados}</p>
          </div>
          <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
            <Check className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Barra de Filtros */}
      <div className="bg-white p-4 rounded-xl border border-gray-200/80 shadow-sm space-y-3">
        <div className="flex flex-col lg:flex-row gap-3">
          {/* Busca textual */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por paciente, profissional, atendimento ou local..."
              className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {/* Filtro por Profissional (para clínica/admin ou responsável) */}
            {!isProfissional && profissionais.length > 0 && (
              <div className="flex items-center gap-1.5">
                <Briefcase className="w-4 h-4 text-gray-400 shrink-0 hidden sm:block" />
                <select
                  value={profissionalFilter}
                  onChange={(e) => setProfissionalFilter(e.target.value)}
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                >
                  <option value="TODOS">Todos os Profissionais</option>
                  {profissionais.map((prof) => (
                    <option key={prof.idProf} value={prof.idProf}>
                      {prof.nomeProfissional}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Filtro de Status */}
            <div className="flex items-center gap-1.5">
              <Filter className="w-4 h-4 text-gray-400 shrink-0 hidden sm:block" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              >
                <option value="TODOS">Todos os Status</option>
                <option value="AGENDADO">Agendado</option>
                <option value="AGUARDANDO_APROVACAO">Aguardando Aprovação</option>
                <option value="REALIZADO">Realizado</option>
                <option value="FALTOU">Faltou</option>
                <option value="CANCELADO">Cancelado</option>
                <option value="REJEITADO">Rejeitado</option>
              </select>
            </div>

            {/* Filtro de Período */}
            <div className="flex rounded-lg border border-gray-200 bg-gray-50 p-1 text-xs font-medium">
              {(['TODOS', 'HOJE', 'SEMANA', 'FUTUROS'] as const).map((periodo) => (
                <button
                  key={periodo}
                  type="button"
                  onClick={() => setDateFilter(periodo)}
                  className={`px-3 py-1 rounded-md transition-colors ${
                    dateFilter === periodo
                      ? 'bg-white text-indigo-700 shadow-sm font-semibold'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  {periodo === 'TODOS'
                    ? 'Todos'
                    : periodo === 'HOJE'
                    ? 'Hoje'
                    : periodo === 'SEMANA'
                    ? 'Próx. 7 dias'
                    : 'Futuros'}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Conteúdo Principal */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3 text-sm text-red-700">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
          <p className="text-sm text-gray-500">Carregando agendamentos...</p>
        </div>
      ) : filteredCompromissos.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
          <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h3 className="text-base font-medium text-gray-900">Nenhum agendamento encontrado</h3>
          <p className="text-sm text-gray-500 mt-1 max-w-sm mx-auto">
            {search || statusFilter !== 'TODOS' || profissionalFilter !== 'TODOS' || dateFilter !== 'TODOS'
              ? 'Tente ajustar os filtros ou termos da busca para localizar o compromisso.'
              : 'Clique no botão acima para registrar o primeiro agendamento de atendimento.'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-600">
              <thead className="bg-gray-50/75 border-b border-gray-200/80 text-xs uppercase font-medium text-gray-500 tracking-wider">
                <tr>
                  <th className="px-6 py-3.5">Data & Horário</th>
                  <th className="px-6 py-3.5">Paciente</th>
                  <th className="px-6 py-3.5">Atendimento / Especialidade</th>
                  <th className="px-6 py-3.5">Profissional / Terapeuta</th>
                  <th className="px-6 py-3.5">Periodicidade & Local</th>
                  <th className="px-6 py-3.5">Status</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200/80">
                {filteredCompromissos.map((c) => {
                  const cfg = STATUS_CONFIG[c.status] || STATUS_CONFIG.AGENDADO;
                  const StatusIcon = cfg.icon;
                  const isActionLoading = actionLoadingId === c.idCompromisso;

                  return (
                    <tr key={c.idCompromisso} className="hover:bg-gray-50/50 transition-colors">
                      {/* Data & Horário */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <Clock className="w-4 h-4 text-indigo-500 shrink-0" />
                          <div>
                            <span className="font-semibold text-gray-900 block capitalize">
                              {formatDateTime(c.dataHoraCompromisso)}
                            </span>
                            <span className="text-xs text-gray-400 font-medium">{c.tituloCompromisso}</span>
                          </div>
                        </div>
                      </td>

                      {/* Paciente */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <div className="w-7 h-7 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center text-xs font-bold shrink-0">
                            {c.nomePaciente ? c.nomePaciente.charAt(0).toUpperCase() : 'P'}
                          </div>
                          <div>
                            <span className="font-medium text-gray-900 block">{c.nomePaciente}</span>
                          </div>
                        </div>
                      </td>

                      {/* Especialidade / Tipo */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-1.5">
                          <Tag className="w-3.5 h-3.5 text-gray-400" />
                          <span className="text-gray-800 font-medium">{c.nomeTipoCompromisso || '-'}</span>
                        </div>
                      </td>

                      {/* Profissional / Terapeuta */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        {c.nomeProfissional ? (
                          <div className="flex items-center gap-2">
                            <div className="w-6 h-6 rounded-full bg-purple-50 text-purple-600 flex items-center justify-center text-xs font-semibold shrink-0">
                              <User className="w-3.5 h-3.5" />
                            </div>
                            <div>
                              <span className="font-medium text-gray-800 block text-xs">
                                {c.nomeProfissional}
                              </span>
                              {c.conselhoProfissional && (
                                <span className="text-[11px] text-gray-400 block font-mono">
                                  {c.conselhoProfissional}
                                </span>
                              )}
                            </div>
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">A definir</span>
                        )}
                      </td>

                      {/* Periodicidade & Local */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="space-y-0.5">
                          <div className="flex items-center gap-1.5 text-xs text-gray-600">
                            <CalendarClock className="w-3.5 h-3.5 text-gray-400" />
                            <span>{c.nomePeriodicidade || 'Avulsa'}</span>
                          </div>
                          {c.localCompromisso && (
                            <div className="flex items-center gap-1.5 text-xs text-gray-500">
                              <MapPin className="w-3.5 h-3.5 text-gray-400" />
                              <span>{c.localCompromisso}</span>
                            </div>
                          )}
                        </div>
                      </td>

                      {/* Status */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${cfg.bg} ${cfg.text} ${cfg.border}`}
                        >
                          <StatusIcon className="w-3.5 h-3.5" />
                          {cfg.label}
                        </span>
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {isActionLoading ? (
                            <Loader2 className="w-4 h-4 text-indigo-600 animate-spin" />
                          ) : (
                            <>
                              {/* Aprovar e Rejeitar para pendentes */}
                              {c.status === 'AGUARDANDO_APROVACAO' && (
                                <>
                                  <button
                                    onClick={() => handleAprovar(c)}
                                    title="Aprovar Agendamento"
                                    className="p-1.5 text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 rounded-lg transition-colors"
                                  >
                                    <CheckCircle2 className="w-4 h-4" />
                                  </button>
                                  <button
                                    onClick={() => handleRejeitar(c)}
                                    title="Rejeitar Agendamento"
                                    className="p-1.5 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                                  >
                                    <XCircle className="w-4 h-4" />
                                  </button>
                                </>
                              )}

                              {/* Ações de status para agendado */}
                              {c.status === 'AGENDADO' && (
                                <>
                                  <button
                                    onClick={() => handleUpdateStatus(c.idCompromisso, 'REALIZADO')}
                                    title="Marcar como Realizado"
                                    className="p-1.5 text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-lg transition-colors"
                                  >
                                    <Check className="w-4 h-4" />
                                  </button>
                                  <button
                                    onClick={() => handleUpdateStatus(c.idCompromisso, 'FALTOU')}
                                    title="Marcar Falta"
                                    className="p-1.5 text-amber-600 hover:text-amber-700 hover:bg-amber-50 rounded-lg transition-colors"
                                  >
                                    <UserX className="w-4 h-4" />
                                  </button>
                                  <button
                                    onClick={() => handleUpdateStatus(c.idCompromisso, 'CANCELADO')}
                                    title="Cancelar Atendimento"
                                    className="p-1.5 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                                  >
                                    <Ban className="w-4 h-4" />
                                  </button>
                                </>
                              )}

                              {/* Editar */}
                              <button
                                onClick={() => {
                                  setEditingCompromisso(c);
                                  setIsModalOpen(true);
                                }}
                                title="Editar Agendamento"
                                className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                              >
                                <Pencil className="w-4 h-4" />
                              </button>

                              {/* Excluir */}
                              <button
                                onClick={() => handleDelete(c.idCompromisso, c.tituloCompromisso)}
                                title="Excluir Agendamento"
                                className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal de Criação / Edição */}
      <CompromissoModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingCompromisso(null);
        }}
        onSuccess={handleSaved}
        initialData={editingCompromisso}
      />
    </div>
  );
}
