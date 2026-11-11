import { useState, useMemo } from 'react';
import {
  Wallet,
  Plus,
  Search,
  CheckCircle2,
  XCircle,
  ChevronDown,
  ChevronUp,
  Clock,
} from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type { ContratoParticular, PacoteParticular, TabelaPreco } from '../types/financeiro';
import type { Paciente } from '../types/paciente';
import type { TipoCompromisso } from '../types/compromisso';
import ContratoParticularModal from './ContratoParticularModal';

interface ContratosParticularesTabProps {
  clinicaId: string;
  canManage: boolean;
  isResponsavelView: boolean;
  contratos: ContratoParticular[];
  pacientes: Paciente[];
  pacotes: PacoteParticular[];
  precos: TabelaPreco[];
  tiposCompromisso: TipoCompromisso[];
  onRefresh: () => void;
}

const formatBRL = (v?: number | null) =>
  (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

const formatDateBR = (iso?: string | null) =>
  iso ? iso.slice(0, 10).split('-').reverse().join('/') : '-';

export default function ContratosParticularesTab({
  clinicaId,
  canManage,
  isResponsavelView,
  contratos,
  pacientes,
  pacotes,
  precos,
  tiposCompromisso,
  onRefresh,
}: ContratosParticularesTabProps) {
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  const filtered = useMemo(() => {
    if (!search.trim()) return contratos;
    const t = search.toLowerCase();
    return contratos.filter(
      (c) =>
        (c.nomePaciente || '').toLowerCase().includes(t) ||
        (c.descricao || '').toLowerCase().includes(t) ||
        (c.nomeResponsavel || '').toLowerCase().includes(t)
    );
  }, [contratos, search]);

  const handlePagar = async (id: string) => {
    setBusyId(id);
    try {
      await financeiroService.registrarPagamentoContrato(id, { formaPagamento: 'PIX' });
      onRefresh();
    } finally {
      setBusyId(null);
    }
  };

  const handleCancelar = async (id: string) => {
    if (!window.confirm('Deseja cancelar este contrato particular?')) return;
    setBusyId(id);
    try {
      await financeiroService.cancelarContratoParticular(id);
      onRefresh();
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="relative flex-1 max-w-md">
          <Search className="h-4 w-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por paciente, responsável ou descrição..."
            className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
        {canManage && (
          <button
            onClick={() => setModalOpen(true)}
            className="px-4 py-2 text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl flex items-center gap-2 shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Novo Fechamento Particular
          </button>
        )}
      </div>

      {filtered.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-200 p-10 text-center space-y-2">
          <Wallet className="h-10 w-10 text-gray-300 mx-auto" />
          <p className="text-sm font-semibold text-gray-700">
            {isResponsavelView
              ? 'Nenhum pacote ou fechamento particular encontrado para seus pacientes.'
              : 'Nenhum contrato particular cadastrado ainda.'}
          </p>
          <p className="text-xs text-gray-500">
            {isResponsavelView
              ? 'Quando a clínica fechar sessões ou combos pré-pagos, o saldo e os abatimentos aparecerão aqui.'
              : 'Crie um fechamento por pacote ou especialidades para acompanhar o pagamento e os abatimentos.'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((ctr) => {
            const expanded = expandedId === ctr.idContratoParticular;
            return (
              <div
                key={ctr.idContratoParticular}
                className="bg-white rounded-2xl border border-gray-200 shadow-sm p-4 space-y-3"
              >
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-bold text-gray-900">{ctr.nomePaciente}</span>
                      <span
                        className={`px-2 py-0.5 rounded-full text-[11px] font-semibold ${
                          ctr.statusPagamento === 'PAGO'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : ctr.statusPagamento === 'PENDENTE'
                            ? 'bg-amber-50 text-amber-700 border border-amber-200'
                            : 'bg-gray-100 text-gray-600'
                        }`}
                      >
                        {ctr.statusPagamento === 'PAGO'
                          ? `Pago${ctr.formaPagamento ? ` (${ctr.formaPagamento})` : ''}`
                          : ctr.statusPagamento === 'PENDENTE'
                          ? 'Pagamento Pendente'
                          : 'Cancelado'}
                      </span>
                      <span
                        className={`px-2 py-0.5 rounded-full text-[11px] font-semibold ${
                          ctr.statusContrato === 'ATIVO'
                            ? 'bg-indigo-50 text-indigo-700'
                            : ctr.statusContrato === 'CONCLUIDO'
                            ? 'bg-purple-50 text-purple-700'
                            : 'bg-red-50 text-red-700'
                        }`}
                      >
                        {ctr.statusContrato}
                      </span>
                    </div>
                    <p className="text-xs text-gray-600">
                      {ctr.descricao} · Início {formatDateBR(ctr.dataInicio)}
                      {ctr.dataFimPrevista ? ` até ${formatDateBR(ctr.dataFimPrevista)}` : ''}
                      {ctr.nomeClinica ? ` · ${ctr.nomeClinica}` : ''}
                    </p>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <span className="text-base font-bold text-gray-900 block">
                        {formatBRL(ctr.valorCobrado)}
                      </span>
                      <span className="text-[11px] text-gray-500">
                        {ctr.totalSessoesConsumidas}/{ctr.totalSessoesContratadas} sessões usadas ·{' '}
                        <strong className="text-emerald-700">{ctr.totalSessoesRestantes} restantes</strong>
                      </span>
                    </div>
                  </div>
                </div>

                {/* Cotas por especialidade */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
                  {ctr.itens.map((item) => {
                    const pct =
                      item.quantidadeContratada > 0
                        ? Math.min(100, Math.round((item.quantidadeConsumida / item.quantidadeContratada) * 100))
                        : 0;
                    return (
                      <div key={item.idContratoItem} className="p-2.5 rounded-xl bg-gray-50 border border-gray-100 space-y-1.5">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-semibold text-gray-800">{item.nomeTipoCompromisso}</span>
                          <span className="text-gray-600 font-medium">
                            {item.quantidadeConsumida}/{item.quantidadeContratada}
                          </span>
                        </div>
                        <div className="h-1.5 w-full bg-gray-200 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-emerald-500 rounded-full transition-all"
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                        <div className="text-[11px] text-gray-500">
                          Restam <strong>{item.quantidadeRestante}</strong> sessão(ões)
                        </div>
                      </div>
                    );
                  })}
                </div>

                {/* Ações e extrato */}
                <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-gray-100">
                  <button
                    onClick={() => setExpandedId(expanded ? null : ctr.idContratoParticular)}
                    className="text-xs font-medium text-indigo-600 hover:underline flex items-center gap-1"
                  >
                    <Clock className="h-3.5 w-3.5" />
                    Histórico de abatimentos
                    {expanded ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                  </button>

                  {canManage && ctr.statusContrato !== 'CANCELADO' && (
                    <div className="flex items-center gap-2">
                      {ctr.statusPagamento === 'PENDENTE' && (
                        <button
                          onClick={() => handlePagar(ctr.idContratoParticular)}
                          disabled={busyId === ctr.idContratoParticular}
                          className="px-3 py-1 text-xs font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-lg flex items-center gap-1"
                        >
                          <CheckCircle2 className="h-3.5 w-3.5" />
                          Registrar Pagamento
                        </button>
                      )}
                      <button
                        onClick={() => handleCancelar(ctr.idContratoParticular)}
                        disabled={busyId === ctr.idContratoParticular}
                        className="px-3 py-1 text-xs font-medium text-red-600 hover:bg-red-50 rounded-lg flex items-center gap-1"
                      >
                        <XCircle className="h-3.5 w-3.5" />
                        Cancelar
                      </button>
                    </div>
                  )}
                </div>

                {expanded && (
                  <div className="bg-gray-50 rounded-xl p-3 space-y-1.5 text-xs">
                    {ctr.itens.every((i) => !i.movimentacoes || i.movimentacoes.length === 0) ? (
                      <p className="text-gray-400 italic">
                        Nenhuma sessão abatida ainda. Os abatimentos ocorrem automaticamente quando um agendamento é marcado como Realizado.
                      </p>
                    ) : (
                      ctr.itens.flatMap((it) =>
                        (it.movimentacoes || []).map((m) => (
                          <div key={m.idMovimentacao} className="flex items-center justify-between py-1 border-b border-gray-200/70 last:border-0">
                            <div>
                              <span
                                className={`font-semibold mr-2 ${
                                  m.tipoMovimentacao === 'ABATIMENTO' ? 'text-emerald-700' : 'text-amber-700'
                                }`}
                              >
                                {m.tipoMovimentacao === 'ABATIMENTO' ? '-1 sessão' : '+1 estorno'}
                              </span>
                              <span className="text-gray-800">{it.nomeTipoCompromisso}</span>
                              {m.tituloCompromisso ? (
                                <span className="text-gray-500"> · {m.tituloCompromisso}</span>
                              ) : null}
                            </div>
                            <span className="text-gray-400">{formatDateBR(m.dataMovimentacao)}</span>
                          </div>
                        ))
                      )
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      <ContratoParticularModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSuccess={onRefresh}
        clinicaId={clinicaId}
        pacientes={pacientes}
        pacotes={pacotes}
        precos={precos}
        tiposCompromisso={tiposCompromisso}
      />
    </div>
  );
}
