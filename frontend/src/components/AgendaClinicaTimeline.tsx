import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import type { Compromisso, StatusCompromisso } from '../types/compromisso';
import {
  Calendar,
  Clock,
  MapPin,
  Tag,
  Loader2,
  Check,
  UserX,
  User,
  Stethoscope,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  CalendarDays,
  XCircle,
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

interface AgendaClinicaTimelineProps {
  selectedDate: string;
  onChangeDate: (date: string) => void;
  compromissos: Compromisso[];
  loading: boolean;
  onUpdateStatus?: (id: string, status: StatusCompromisso) => Promise<void>;
  actionLoadingId?: string | null;
  title?: string;
  subtitle?: string;
  showManageLink?: boolean;
}

export default function AgendaClinicaTimeline({
  selectedDate,
  onChangeDate,
  compromissos,
  loading,
  onUpdateStatus,
  actionLoadingId,
  title = 'Agenda do Dia',
  subtitle = 'Atendimentos organizados em sequência por horário.',
  showManageLink = true,
}: AgendaClinicaTimelineProps) {
  const todayISO = getTodayISO();
  const isToday = selectedDate === todayISO;

  // Filtra compromissos do dia selecionado
  const dayCompromissos = useMemo(() => {
    return compromissos
      .filter((c) => c.dataHoraCompromisso && c.dataHoraCompromisso.slice(0, 10) === selectedDate)
      .sort((a, b) => (a.dataHoraCompromisso || '').localeCompare(b.dataHoraCompromisso || ''));
  }, [compromissos, selectedDate]);

  // Estatísticas rápidas do dia
  const stats = useMemo(() => {
    const total = dayCompromissos.length;
    const agendados = dayCompromissos.filter((c) => c.status === 'AGENDADO').length;
    const realizados = dayCompromissos.filter((c) => c.status === 'REALIZADO').length;
    const aguardando = dayCompromissos.filter((c) => c.status === 'AGUARDANDO_APROVACAO').length;
    const faltas = dayCompromissos.filter((c) => c.status === 'FALTOU' || c.status === 'CANCELADO' || c.status === 'REJEITADO').length;
    return { total, agendados, realizados, aguardando, faltas };
  }, [dayCompromissos]);

  // Agrupa atendimentos por horário (ex: "08:30", "09:30", ...)
  const timeSlots = useMemo(() => {
    const slotsMap = new Map<string, Compromisso[]>();

    dayCompromissos.forEach((c) => {
      const hora = formatTime(c.dataHoraCompromisso);
      if (!slotsMap.has(hora)) {
        slotsMap.set(hora, []);
      }
      slotsMap.get(hora)!.push(c);
    });

    const sortedHours = Array.from(slotsMap.keys()).sort();
    return sortedHours.map((hora) => ({
      hora,
      atendimentos: slotsMap.get(hora)!,
    }));
  }, [dayCompromissos]);

  return (
    <div className="space-y-5">
      {/* Cabeçalho da Agenda */}
      <div className="bg-white rounded-xl border border-gray-200 p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-sm">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <CalendarDays className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">{title}</h2>
              <p className="text-xs text-gray-500 mt-0.5">{subtitle}</p>
            </div>
          </div>
        </div>

        {showManageLink && (
          <Link
            to="/admin/agenda"
            className="inline-flex items-center gap-2 px-3.5 py-2 bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200 rounded-lg text-xs font-semibold transition-colors self-start sm:self-auto"
          >
            <Calendar className="w-4 h-4" />
            Gerenciar Agenda Completa
          </Link>
        )}
      </div>

      {/* Navegador de Dias */}
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

      {/* Resumo Rápido das Sessões do Dia */}
      {!loading && dayCompromissos.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="bg-white rounded-lg border border-gray-200 p-3 flex items-center justify-between">
            <div>
              <p className="text-xs text-gray-500 font-medium">Total no Dia</p>
              <p className="text-lg font-bold text-gray-900 mt-0.5">{stats.total}</p>
            </div>
            <Clock className="w-5 h-5 text-gray-400" />
          </div>
          <div className="bg-emerald-50/60 rounded-lg border border-emerald-200/80 p-3 flex items-center justify-between">
            <div>
              <p className="text-xs text-emerald-700 font-medium">Agendados</p>
              <p className="text-lg font-bold text-emerald-900 mt-0.5">{stats.agendados}</p>
            </div>
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="bg-blue-50/60 rounded-lg border border-blue-200/80 p-3 flex items-center justify-between">
            <div>
              <p className="text-xs text-blue-700 font-medium">Realizados</p>
              <p className="text-lg font-bold text-blue-900 mt-0.5">{stats.realizados}</p>
            </div>
            <Check className="w-5 h-5 text-blue-600" />
          </div>
          <div className="bg-amber-50/60 rounded-lg border border-amber-200/80 p-3 flex items-center justify-between">
            <div>
              <p className="text-xs text-amber-700 font-medium">Aguardando / Outros</p>
              <p className="text-lg font-bold text-amber-900 mt-0.5">{stats.aguardando + stats.faltas}</p>
            </div>
            <UserX className="w-5 h-5 text-amber-600" />
          </div>
        </div>
      )}

      {/* Conteúdo: Loading, Vazio ou Lista por Horários */}
      {loading ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
          <p className="text-sm text-gray-500">Carregando programação do dia...</p>
        </div>
      ) : timeSlots.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center shadow-sm">
          <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-gray-900">
            Nenhum atendimento programado para este dia
          </h3>
          <p className="text-sm text-gray-500 mt-1 max-w-md mx-auto">
            Não há sessões agendadas nesta data. Use os botões de navegação acima para consultar outros dias ou acesse a agenda completa.
          </p>
          {showManageLink && (
            <Link
              to="/admin/agenda"
              className="inline-flex items-center gap-2 mt-4 px-4 py-2 bg-indigo-600 text-white rounded-lg text-xs font-semibold hover:bg-indigo-700 transition-colors"
            >
              <Calendar className="w-4 h-4" />
              Abrir Agenda Completa
            </Link>
          )}
        </div>
      ) : (
        /* Timeline dividida em horas */
        <div className="space-y-4">
          {timeSlots.map(({ hora, atendimentos }) => (
            <div
              key={hora}
              className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden hover:border-gray-300 transition-colors"
            >
              {/* Barra do Horário */}
              <div className="px-5 py-3 bg-gradient-to-r from-gray-50 to-white border-b border-gray-200 flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="px-3 py-1 rounded-lg bg-indigo-600 text-white font-bold text-sm flex items-center gap-1.5 shadow-sm">
                    <Clock className="w-3.5 h-3.5" />
                    <span>{hora}</span>
                  </div>
                  <span className="text-xs font-medium text-gray-500">
                    {atendimentos.length} {atendimentos.length === 1 ? 'sessão programada' : 'sessões simultâneas'}
                  </span>
                </div>
              </div>

              {/* Caixinhas dos Atendimentos daquele horário */}
              <div className="p-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
                {atendimentos.map((c) => {
                  const badge = STATUS_BADGE[c.status] || STATUS_BADGE.AGENDADO;
                  const isActionLoading = actionLoadingId === c.idCompromisso;

                  return (
                    <div
                      key={c.idCompromisso}
                      className="bg-gray-50/70 hover:bg-white rounded-xl border border-gray-200/90 p-3.5 flex flex-col justify-between gap-3 shadow-xs hover:shadow-md transition-all group"
                    >
                      {/* Topo do Card: Paciente e Status */}
                      <div>
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex items-center gap-2 min-w-0">
                            <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center font-bold text-xs shrink-0">
                              {c.nomePaciente ? c.nomePaciente.charAt(0).toUpperCase() : 'P'}
                            </div>
                            <div className="min-w-0">
                              <h4
                                className="text-sm font-bold text-gray-900 truncate"
                                title={c.nomePaciente}
                              >
                                {c.nomePaciente || 'Paciente não identificado'}
                              </h4>
                              <p className="text-[11px] text-gray-500 truncate">
                                Paciente
                              </p>
                            </div>
                          </div>

                          <span
                            className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold border shrink-0 ${badge.className}`}
                          >
                            {badge.label}
                          </span>
                        </div>

                        {/* Dados Clínicos: Profissional e Especialidade */}
                        <div className="mt-3 pt-2.5 border-t border-gray-200/60 space-y-1.5 text-xs">
                          {/* Profissional */}
                          <div className="flex items-center gap-1.5 text-gray-700">
                            <Stethoscope className="w-3.5 h-3.5 text-purple-600 shrink-0" />
                            <span
                              className="font-medium truncate"
                              title={c.nomeProfissional || 'Profissional a definir'}
                            >
                              {c.nomeProfissional || 'Profissional a definir'}
                            </span>
                          </div>

                          {/* Especialidade / Tipo */}
                          <div className="flex items-center gap-1.5 text-gray-600">
                            <Tag className="w-3.5 h-3.5 text-indigo-500 shrink-0" />
                            <span className="truncate">
                              {c.nomeTipoCompromisso || c.tituloCompromisso}
                            </span>
                          </div>

                          {/* Local / Sala */}
                          {c.localCompromisso && (
                            <div className="flex items-center gap-1.5 text-gray-500 text-[11px]">
                              <MapPin className="w-3.5 h-3.5 text-gray-400 shrink-0" />
                              <span className="truncate">{c.localCompromisso}</span>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Rodapé: Ações Rápidas */}
                      {onUpdateStatus && c.status === 'AGENDADO' && (
                        <div className="pt-2 border-t border-gray-200/60 flex items-center justify-end gap-1.5">
                          {isActionLoading ? (
                            <Loader2 className="w-4 h-4 animate-spin text-indigo-600" />
                          ) : (
                            <>
                              <button
                                type="button"
                                onClick={() => onUpdateStatus(c.idCompromisso, 'REALIZADO')}
                                title="Marcar como Realizado"
                                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold text-blue-700 bg-blue-50 hover:bg-blue-100 border border-blue-200 transition-colors"
                              >
                                <Check className="w-3.5 h-3.5" />
                                <span>Realizado</span>
                              </button>
                              <button
                                type="button"
                                onClick={() => onUpdateStatus(c.idCompromisso, 'FALTOU')}
                                title="Marcar Falta"
                                className="inline-flex items-center gap-1 px-2 py-1 rounded-md text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 transition-colors"
                              >
                                <UserX className="w-3.5 h-3.5" />
                                <span>Falta</span>
                              </button>
                            </>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
