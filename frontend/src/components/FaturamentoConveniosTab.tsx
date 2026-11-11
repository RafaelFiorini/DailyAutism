import { useState, useMemo, type FormEvent } from 'react';
import {
  FileCheck2,
  Search,
  FileText,
  Send,
  ShieldCheck,
  CheckCircle2,
  XCircle,
  Loader2,
  AlertCircle,
  CalendarRange,
} from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type {
  GuiaFaturamento,
  SessaoElegivel,
  StatusGuiaFaturamento,
} from '../types/financeiro';
import type { Paciente, PlanoSaudeOption } from '../types/paciente';
import AssinarGuiaModal from './AssinarGuiaModal';

interface FaturamentoConveniosTabProps {
  clinicaId: string;
  canManage: boolean;
  canSign: boolean;
  isResponsavelView: boolean;
  guias: GuiaFaturamento[];
  pacientes: Paciente[];
  planos: PlanoSaudeOption[];
  onRefresh: () => void;
}

const STATUS_STYLE: Record<StatusGuiaFaturamento, { label: string; className: string }> = {
  RASCUNHO: { label: 'Rascunho', className: 'bg-gray-100 text-gray-700 border-gray-200' },
  AGUARDANDO_ASSINATURA: {
    label: 'Aguardando Assinatura',
    className: 'bg-amber-50 text-amber-700 border-amber-200',
  },
  ASSINADA: { label: 'Assinada', className: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  ENVIADA_CONVENIO: {
    label: 'Enviada ao Convênio',
    className: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  },
  PAGA: { label: 'Recebida / Paga', className: 'bg-purple-50 text-purple-700 border-purple-200' },
  CANCELADA: { label: 'Cancelada', className: 'bg-red-50 text-red-700 border-red-200' },
};

const formatBRL = (v?: number | null) =>
  (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

const formatDateBR = (iso?: string | null) =>
  iso ? iso.slice(0, 10).split('-').reverse().join('/') : '-';

const formatDateTimeBR = (iso?: string | null) => {
  if (!iso) return '-';
  const [d, t] = iso.split('T');
  return `${formatDateBR(d)} ${t ? t.slice(0, 5) : ''}`.trim();
};

export default function FaturamentoConveniosTab({
  clinicaId,
  canManage,
  canSign,
  isResponsavelView,
  guias,
  pacientes,
  planos,
  onRefresh,
}: FaturamentoConveniosTabProps) {
  const [pacienteId, setPacienteId] = useState('');
  const [planoId, setPlanoId] = useState('');
  const [inicio, setInicio] = useState(() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`;
  });
  const [fim, setFim] = useState(() => new Date().toISOString().slice(0, 10));
  const [liberarDireto, setLiberarDireto] = useState(true);
  const [elegiveis, setElegiveis] = useState<SessaoElegivel[] | null>(null);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [loadingElegiveis, setLoadingElegiveis] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [guiaParaAssinar, setGuiaParaAssinar] = useState<GuiaFaturamento | null>(null);
  const [feedback, setFeedback] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);

  const pacientesComPlano = useMemo(() => {
    const comPlano = pacientes.filter((p) => p.idPlanoSaude || (p.planos && p.planos.length > 0));
    return comPlano.length > 0 ? comPlano : pacientes;
  }, [pacientes]);

  const handlePacienteChange = (id: string) => {
    setPacienteId(id);
    setElegiveis(null);
    const pac = pacientes.find((p) => p.idPaciente === id);
    if (pac?.idPlanoSaude) {
      setPlanoId(pac.idPlanoSaude);
    }
  };

  const handleBuscarSessoes = async (e: FormEvent) => {
    e.preventDefault();
    if (!pacienteId || !planoId || !inicio || !fim) {
      setFeedback({ type: 'err', text: 'Selecione paciente, convênio e período.' });
      return;
    }
    setLoadingElegiveis(true);
    setFeedback(null);
    try {
      const lista = await financeiroService.listSessoesElegiveis({
        clinicaId: clinicaId || undefined,
        pacienteId,
        planoSaudeId: planoId,
        inicio,
        fim,
      });
      setElegiveis(lista);
      setSelectedIds(lista.map((s) => s.idCompromisso));
    } catch {
      setFeedback({ type: 'err', text: 'Não foi possível buscar os atendimentos elegíveis.' });
    } finally {
      setLoadingElegiveis(false);
    }
  };

  const handleGerarGuia = async () => {
    if (!pacienteId || !planoId || selectedIds.length === 0) return;
    setGenerating(true);
    setFeedback(null);
    try {
      const criada = await financeiroService.gerarGuiaConvenio({
        idClinica: clinicaId || undefined,
        idPaciente: pacienteId,
        idPlanoSaude: planoId,
        competenciaInicio: inicio,
        competenciaFim: fim,
        idsCompromissos: selectedIds,
        liberarParaAssinatura: liberarDireto,
      });
      setElegiveis(null);
      setSelectedIds([]);
      setFeedback({
        type: 'ok',
        text: `Guia ${criada.numeroLoteGuia} gerada (${formatBRL(criada.valorTotal)}).`,
      });
      onRefresh();
    } catch (err: unknown) {
      let msg = 'Não foi possível gerar a guia.';
      if (err && typeof err === 'object' && 'response' in err) {
        const res = (err as { response?: { data?: { message?: string; errorMessage?: string } } }).response;
        msg = res?.data?.errorMessage || res?.data?.message || msg;
      }
      setFeedback({ type: 'err', text: msg });
    } finally {
      setGenerating(false);
    }
  };

  const totalSelecionado = useMemo(() => {
    if (!elegiveis) return 0;
    return elegiveis
      .filter((e) => selectedIds.includes(e.idCompromisso))
      .reduce((acc, item) => acc + (item.valorUnitarioConvenio || 0), 0);
  }, [elegiveis, selectedIds]);

  const handleVerPdf = async (idGuia: string) => {
    setBusyId(idGuia);
    try {
      const blob = await financeiroService.downloadGuiaPdf(idGuia);
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank', 'noopener,noreferrer');
    } catch {
      setFeedback({ type: 'err', text: 'Erro ao baixar o PDF da guia.' });
    } finally {
      setBusyId(null);
    }
  };

  const runAction = async (idGuia: string, fn: () => Promise<unknown>, okText: string) => {
    setBusyId(idGuia);
    setFeedback(null);
    try {
      await fn();
      setFeedback({ type: 'ok', text: okText });
      onRefresh();
    } catch (err: unknown) {
      let msg = 'Operação não concluída.';
      if (err && typeof err === 'object' && 'response' in err) {
        const res = (err as { response?: { data?: { message?: string; errorMessage?: string } } }).response;
        msg = res?.data?.errorMessage || res?.data?.message || msg;
      }
      setFeedback({ type: 'err', text: msg });
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="space-y-6">
      {feedback && (
        <div
          className={`flex items-center gap-2 p-3 rounded-xl text-sm border ${
            feedback.type === 'ok'
              ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
              : 'bg-red-50 border-red-200 text-red-700'
          }`}
        >
          {feedback.type === 'ok' ? (
            <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
          ) : (
            <AlertCircle className="h-4 w-4 flex-shrink-0" />
          )}
          <span>{feedback.text}</span>
        </div>
      )}

      {/* Gerador de Fechamento de Convênio (somente clínica / admin) */}
      {canManage && (
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 space-y-4">
          <div className="flex items-center gap-2">
            <CalendarRange className="h-5 w-5 text-indigo-600" />
            <div>
              <h3 className="text-base font-bold text-gray-900">Fechar Período e Gerar Guia de Convênio</h3>
              <p className="text-xs text-gray-500">
                Consolide atendimentos com status Realizado para gerar o PDF e liberar ao responsável.
              </p>
            </div>
          </div>

          <form onSubmit={handleBuscarSessoes} className="grid grid-cols-1 md:grid-cols-5 gap-3 bg-gray-50 p-3.5 rounded-xl border border-gray-200">
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Paciente</label>
              <select
                value={pacienteId}
                onChange={(e) => handlePacienteChange(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Selecionar...</option>
                {pacientesComPlano.map((p) => (
                  <option key={p.idPaciente} value={p.idPaciente}>
                    {p.nomePessoa}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Convênio</label>
              <select
                value={planoId}
                onChange={(e) => setPlanoId(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Selecionar...</option>
                {planos.map((pl) => (
                  <option key={pl.idPlanoSaude} value={pl.idPlanoSaude}>
                    {pl.nomePlano}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Competência Início</label>
              <input
                type="date"
                value={inicio}
                onChange={(e) => setInicio(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Competência Fim</label>
              <input
                type="date"
                value={fim}
                onChange={(e) => setFim(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              />
            </div>
            <div className="flex items-end">
              <button
                type="submit"
                disabled={loadingElegiveis}
                className="w-full px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center justify-center gap-1.5 disabled:opacity-50"
              >
                {loadingElegiveis ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Search className="h-3.5 w-3.5" />}
                Buscar Sessões
              </button>
            </div>
          </form>

          {elegiveis !== null && (
            <div className="border border-indigo-100 rounded-xl p-4 bg-indigo-50/30 space-y-3">
              {elegiveis.length === 0 ? (
                <p className="text-xs text-gray-500 italic">
                  Nenhum atendimento com status Realizado encontrado (ou já faturado) neste período.
                </p>
              ) : (
                <>
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="text-xs font-bold text-indigo-950">
                      {selectedIds.length} de {elegiveis.length} sessão(ões) selecionada(s) · Total:{' '}
                      {formatBRL(totalSelecionado)}
                    </span>
                    <label className="flex items-center gap-1.5 text-xs text-gray-700 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={liberarDireto}
                        onChange={(e) => setLiberarDireto(e.target.checked)}
                        className="h-3.5 w-3.5 rounded text-indigo-600"
                      />
                      Liberar imediatamente para assinatura do responsável
                    </label>
                  </div>

                  <div className="bg-white rounded-lg border border-gray-200 divide-y divide-gray-100 max-h-56 overflow-y-auto">
                    {elegiveis.map((s) => {
                      const checked = selectedIds.includes(s.idCompromisso);
                      return (
                        <label
                          key={s.idCompromisso}
                          className="flex items-center justify-between px-3 py-2 text-xs hover:bg-gray-50 cursor-pointer"
                        >
                          <div className="flex items-center gap-2.5">
                            <input
                              type="checkbox"
                              checked={checked}
                              onChange={() =>
                                setSelectedIds((prev) =>
                                  checked ? prev.filter((i) => i !== s.idCompromisso) : [...prev, s.idCompromisso]
                                )
                              }
                              className="h-3.5 w-3.5 rounded text-indigo-600"
                            />
                            <div>
                              <span className="font-semibold text-gray-800">{s.nomeTipoCompromisso}</span>
                              <span className="text-gray-500 ml-2">
                                {formatDateTimeBR(s.dataHoraAtendimento)} · {s.nomeProfissional}
                              </span>
                            </div>
                          </div>
                          <span className="font-semibold text-indigo-700">
                            {formatBRL(s.valorUnitarioConvenio)}
                          </span>
                        </label>
                      );
                    })}
                  </div>

                  <div className="flex justify-end">
                    <button
                      type="button"
                      onClick={handleGerarGuia}
                      disabled={generating || selectedIds.length === 0}
                      className="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg flex items-center gap-1.5 disabled:opacity-50"
                    >
                      {generating ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <FileCheck2 className="h-3.5 w-3.5" />}
                      Gerar Guia em PDF
                    </button>
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      )}

      {/* Listagem de Guias de Convênio */}
      <div className="space-y-3">
        {guias.length === 0 ? (
          <div className="bg-white rounded-2xl border border-gray-200 p-10 text-center space-y-2">
            <FileCheck2 className="h-10 w-10 text-gray-300 mx-auto" />
            <p className="text-sm font-semibold text-gray-700">
              {isResponsavelView
                ? 'Nenhuma guia de convênio liberada para conferência ainda.'
                : 'Nenhuma guia de faturamento gerada ainda.'}
            </p>
          </div>
        ) : (
          guias.map((g) => {
            const st = STATUS_STYLE[g.status] || STATUS_STYLE.RASCUNHO;
            const isBusy = busyId === g.idGuiaFaturamento;
            return (
              <div
                key={g.idGuiaFaturamento}
                className="bg-white rounded-2xl border border-gray-200 shadow-sm p-4 space-y-3"
              >
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-bold text-gray-900">{g.numeroLoteGuia}</span>
                      <span className={`px-2 py-0.5 rounded-full text-[11px] font-semibold border ${st.className}`}>
                        {st.label}
                      </span>
                      <span className="px-2 py-0.5 rounded-md bg-indigo-50 text-indigo-700 text-[11px] font-medium">
                        {g.nomePlanoSaude}
                      </span>
                    </div>
                    <p className="text-xs text-gray-600">
                      Paciente: <strong>{g.nomePaciente}</strong> · Competência {formatDateBR(g.competenciaInicio)} a{' '}
                      {formatDateBR(g.competenciaFim)} · {g.itens.length} sessão(ões)
                    </p>
                    {g.dataHoraAssinatura && (
                      <p className="text-[11px] text-emerald-700">
                        Assinada por {g.assinadoPorNome} em {formatDateTimeBR(g.dataHoraAssinatura)} · Cód.:{' '}
                        <strong>{g.codigoVerificador}</strong>
                      </p>
                    )}
                    {g.dataEnvioConvenio && (
                      <p className="text-[11px] text-indigo-700">
                        Enviada em {formatDateBR(g.dataEnvioConvenio)}
                        {g.dataPrevisaoPagamento ? ` · Previsão de pagamento: ${formatDateBR(g.dataPrevisaoPagamento)}` : ''}
                        {g.dataPagamento ? ` · Recebida em ${formatDateBR(g.dataPagamento)}` : ''}
                      </p>
                    )}
                  </div>

                  <div className="text-right">
                    <span className="text-base font-bold text-gray-900 block">{formatBRL(g.valorTotal)}</span>
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-end gap-2 pt-2 border-t border-gray-100">
                  <button
                    type="button"
                    onClick={() => handleVerPdf(g.idGuiaFaturamento)}
                    disabled={isBusy}
                    className="px-3 py-1.5 text-xs font-medium text-gray-700 bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-lg flex items-center gap-1"
                  >
                    <FileText className="h-3.5 w-3.5" />
                    Ver PDF
                  </button>

                  {canSign && g.status === 'AGUARDANDO_ASSINATURA' && (
                    <button
                      type="button"
                      onClick={() => setGuiaParaAssinar(g)}
                      className="px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center gap-1"
                    >
                      <ShieldCheck className="h-3.5 w-3.5" />
                      Conferir e Assinar
                    </button>
                  )}

                  {canManage && g.status === 'RASCUNHO' && (
                    <button
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        runAction(
                          g.idGuiaFaturamento,
                          () => financeiroService.liberarGuiaAssinatura(g.idGuiaFaturamento),
                          'Guia liberada para assinatura do responsável.'
                        )
                      }
                      className="px-3 py-1.5 text-xs font-semibold text-amber-800 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-lg flex items-center gap-1"
                    >
                      <Send className="h-3.5 w-3.5" />
                      Liberar p/ Assinatura
                    </button>
                  )}

                  {canManage && g.status === 'ASSINADA' && (
                    <button
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        runAction(
                          g.idGuiaFaturamento,
                          () => financeiroService.enviarGuiaConvenio(g.idGuiaFaturamento),
                          'Guia marcada como enviada ao convênio.'
                        )
                      }
                      className="px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center gap-1"
                    >
                      <Send className="h-3.5 w-3.5" />
                      Enviar ao Convênio
                    </button>
                  )}

                  {canManage && (g.status === 'ENVIADA_CONVENIO' || g.status === 'ASSINADA') && (
                    <button
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        runAction(
                          g.idGuiaFaturamento,
                          () => financeiroService.registrarPagamentoGuia(g.idGuiaFaturamento),
                          'Recebimento do convênio registrado.'
                        )
                      }
                      className="px-3 py-1.5 text-xs font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-lg flex items-center gap-1"
                    >
                      <CheckCircle2 className="h-3.5 w-3.5" />
                      Registrar Recebimento
                    </button>
                  )}

                  {canManage && g.status !== 'PAGA' && g.status !== 'CANCELADA' && (
                    <button
                      type="button"
                      disabled={isBusy}
                      onClick={() => {
                        if (!window.confirm('Deseja cancelar esta guia de faturamento?')) return;
                        runAction(
                          g.idGuiaFaturamento,
                          () => financeiroService.cancelarGuiaConvenio(g.idGuiaFaturamento),
                          'Guia cancelada.'
                        );
                      }}
                      className="px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 rounded-lg flex items-center gap-1"
                    >
                      <XCircle className="h-3.5 w-3.5" />
                      Cancelar
                    </button>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      <AssinarGuiaModal
        isOpen={Boolean(guiaParaAssinar)}
        guia={guiaParaAssinar}
        onClose={() => setGuiaParaAssinar(null)}
        onSigned={() => {
          setFeedback({ type: 'ok', text: 'Guia assinada eletronicamente com sucesso!' });
          onRefresh();
        }}
      />
    </div>
  );
}
