import { useState, useEffect, useCallback, FormEvent } from 'react';
import {
  CalendarOff,
  Plus,
  Trash2,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Clock,
  Sparkles,
  X,
  Building2,
  ShieldCheck,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { bloqueioAgendaService } from '../services/bloqueioAgendaService';
import { clinicaService } from '../services/clinicaService';
import type { Clinica } from '../types/clinica';
import {
  TIPO_BLOQUEIO_LABELS,
  type BloqueioAgendaClinica,
  type SaveBloqueioAgendaClinicaInput,
  type TipoBloqueioAgenda,
} from '../types/bloqueioAgenda';
import { extractApiErrorMessage } from '../utils/apiError';

interface FeriadoSugerido {
  titulo: string;
  data: string; // YYYY-MM-DD
  tipo: TipoBloqueioAgenda;
  marcadoPadrao: boolean;
}

function gerarFeriadosNacionaisSugeridos(ano: number): FeriadoSugerido[] {
  return [
    { titulo: 'Confraternização Universal (Ano Novo)', data: `${ano}-01-01`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Tiradentes', data: `${ano}-04-21`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Dia do Trabalho', data: `${ano}-05-01`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Independência do Brasil', data: `${ano}-09-07`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Nossa Senhora Aparecida', data: `${ano}-10-12`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Finados', data: `${ano}-11-02`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Proclamação da República', data: `${ano}-11-15`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Dia da Consciência Negra', data: `${ano}-11-20`, tipo: 'FERIADO', marcadoPadrao: false },
    { titulo: 'Véspera de Natal', data: `${ano}-12-24`, tipo: 'RECESSO', marcadoPadrao: true },
    { titulo: 'Natal', data: `${ano}-12-25`, tipo: 'FERIADO', marcadoPadrao: true },
    { titulo: 'Véspera de Ano Novo', data: `${ano}-12-31`, tipo: 'RECESSO', marcadoPadrao: true },
  ];
}

function formatDateBR(iso?: string): string {
  if (!iso) return '-';
  const parts = iso.split('-');
  if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
  return iso;
}

function formatTimeShort(t?: string | null): string {
  if (!t) return '';
  return t.slice(0, 5);
}

export default function FeriadosRecessosPage() {
  const { isAdmin, user } = useAuth();

  const [bloqueios, setBloqueios] = useState<BloqueioAgendaClinica[]>([]);
  const [clinicas, setClinicas] = useState<Clinica[]>([]);
  const [idClinicaFiltro, setIdClinicaFiltro] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Modal Novo Bloqueio
  const [modalAberto, setModalAberto] = useState(false);
  const [saving, setSaving] = useState(false);
  const [titulo, setTitulo] = useState('');
  const [tipo, setTipo] = useState<TipoBloqueioAgenda>('FERIAS_COLETIVAS');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [diaInteiro, setDiaInteiro] = useState(true);
  const [horaInicio, setHoraInicio] = useState('12:00');
  const [horaFim, setHoraFim] = useState('23:59');
  const [observacao, setObservacao] = useState('');
  const [suspenderExistentes, setSuspenderExistentes] = useState(true);
  const [idClinicaForm, setIdClinicaForm] = useState('');

  // Modal Importar Feriados
  const [modalFeriados, setModalFeriados] = useState(false);
  const [anoImportacao, setAnoImportacao] = useState<number>(new Date().getFullYear());
  const [feriadosSelecionados, setFeriadosSelecionados] = useState<Record<string, boolean>>({});
  const [suspenderNoLote, setSuspenderNoLote] = useState(true);
  const [importing, setImporting] = useState(false);

  const carregarBloqueios = useCallback(async () => {
    setLoading(true);
    setErro(null);
    try {
      const data = await bloqueioAgendaService.list(idClinicaFiltro || undefined);
      setBloqueios(data);
    } catch (e) {
      setErro(extractApiErrorMessage(e, 'Não foi possível carregar os feriados e recessos.'));
    } finally {
      setLoading(false);
    }
  }, [idClinicaFiltro]);

  useEffect(() => {
    carregarBloqueios();
  }, [carregarBloqueios]);

  useEffect(() => {
    if (isAdmin) {
      clinicaService
        .list()
        .then((data) => {
          setClinicas(data);
          if (data.length === 1) {
            setIdClinicaForm(data[0].idClinica);
          }
        })
        .catch(() => setClinicas([]));
    }
  }, [isAdmin]);

  function abrirModalNovo() {
    setTitulo('');
    setTipo('FERIAS_COLETIVAS');
    const hoje = new Date().toISOString().split('T')[0];
    setDataInicio(hoje);
    setDataFim(hoje);
    setDiaInteiro(true);
    setHoraInicio('12:00');
    setHoraFim('23:59');
    setObservacao('');
    setSuspenderExistentes(true);
    setIdClinicaForm(idClinicaFiltro || user?.idClinica || (clinicas.length === 1 ? clinicas[0].idClinica : ''));
    setErro(null);
    setModalAberto(true);
  }

  function abrirModalFeriados(ano = new Date().getFullYear()) {
    setAnoImportacao(ano);
    const sugeridos = gerarFeriadosNacionaisSugeridos(ano);
    const iniciais: Record<string, boolean> = {};
    for (const f of sugeridos) {
      // Desmarca se já existe bloqueio exatamente nessa data
      const jaExiste = bloqueios.some((b) => b.dataInicio === f.data && b.dataFim === f.data);
      iniciais[f.data] = f.marcadoPadrao && !jaExiste;
    }
    setFeriadosSelecionados(iniciais);
    setSuspenderNoLote(true);
    setModalFeriados(true);
  }

  async function handleSalvarBloqueio(e: FormEvent) {
    e.preventDefault();
    if (!titulo.trim() || !dataInicio || !dataFim) return;

    setSaving(true);
    setErro(null);
    setSucesso(null);
    try {
      const payload: SaveBloqueioAgendaClinicaInput = {
        idClinica: idClinicaForm || undefined,
        titulo: titulo.trim(),
        tipo,
        dataInicio,
        dataFim,
        horaInicio: diaInteiro ? null : horaInicio,
        horaFim: diaInteiro ? null : horaFim,
        observacao: observacao.trim() || undefined,
        suspenderAgendamentosExistentes: suspenderExistentes,
      };
      const criado = await bloqueioAgendaService.create(payload);
      const qtd = criado.agendamentosSuspensos ?? 0;
      setSucesso(
        qtd > 0
          ? `Bloqueio "${criado.titulo}" salvo! ${qtd} agendamento(s) existente(s) foram suspensos com saldo de sessões preservado.`
          : `Bloqueio "${criado.titulo}" salvo com sucesso! Novas grades não agendarão neste período.`
      );
      setModalAberto(false);
      await carregarBloqueios();
    } catch (err) {
      setErro(extractApiErrorMessage(err, 'Erro ao salvar bloqueio de agenda.'));
    } finally {
      setSaving(false);
    }
  }

  async function handleImportarFeriados() {
    const sugeridos = gerarFeriadosNacionaisSugeridos(anoImportacao).filter((f) => feriadosSelecionados[f.data]);
    if (sugeridos.length === 0) {
      setErro('Selecione pelo menos um feriado para importar.');
      return;
    }

    setImporting(true);
    setErro(null);
    setSucesso(null);
    try {
      const itens: SaveBloqueioAgendaClinicaInput[] = sugeridos.map((f) => ({
        idClinica: idClinicaFiltro || user?.idClinica || (clinicas.length === 1 ? clinicas[0].idClinica : undefined),
        titulo: f.titulo,
        tipo: f.tipo,
        dataInicio: f.data,
        dataFim: f.data,
        suspenderAgendamentosExistentes: suspenderNoLote,
      }));
      const criados = await bloqueioAgendaService.importarEmLote(itens);
      const totalSuspensos = criados.reduce((acc, item) => acc + (item.agendamentosSuspensos ?? 0), 0);
      setSucesso(
        `${criados.length} feriado(s)/recesso(s) importados para a clínica!` +
          (totalSuspensos > 0 ? ` ${totalSuspensos} agendamento(s) suspensos com saldo preservado.` : '')
      );
      setModalFeriados(false);
      await carregarBloqueios();
    } catch (err) {
      setErro(extractApiErrorMessage(err, 'Erro ao importar feriados.'));
    } finally {
      setImporting(false);
    }
  }

  async function handleRemover(item: BloqueioAgendaClinica) {
    if (!window.confirm(`Deseja remover o bloqueio "${item.titulo}"? A clínica voltará a aceitar agendamentos nesta data.`)) {
      return;
    }
    setDeletingId(item.idBloqueio);
    setErro(null);
    try {
      await bloqueioAgendaService.remove(item.idBloqueio);
      setBloqueios((prev) => prev.filter((b) => b.idBloqueio !== item.idBloqueio));
      setSucesso(`Bloqueio "${item.titulo}" removido.`);
    } catch (err) {
      setErro(extractApiErrorMessage(err, 'Erro ao remover bloqueio.'));
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <CalendarOff className="w-6 h-6 text-indigo-600" />
            <h1 className="text-2xl font-bold text-gray-900">Feriados e Recessos da Clínica</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {bloqueios.length} cadastrado{bloqueios.length === 1 ? '' : 's'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Configure férias coletivas, feriados e dias sem atendimento desta clínica. Sessões suspensas preservam 100% do saldo de guias e pacotes.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={() => abrirModalFeriados()}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 border border-indigo-200 rounded-lg text-sm font-semibold transition-colors"
          >
            <Sparkles className="w-4 h-4" />
            Importar Feriados Nacionais
          </button>
          <button
            type="button"
            onClick={abrirModalNovo}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold transition-colors shadow-sm"
          >
            <Plus className="w-4 h-4" />
            Novo Bloqueio / Férias
          </button>
        </div>
      </div>

      {/* Aviso de Preservação de Saldo */}
      <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-4 flex items-start gap-3 text-sm text-emerald-800">
        <ShieldCheck className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
        <div>
          <p className="font-semibold">Preservação automática de sessões e autonomia por clínica</p>
          <p className="text-xs text-emerald-700 mt-0.5">
            Cada clínica decide quais feriados ou períodos de recesso deseja suspender. Agendamentos suspensos não consomem sessões da guia nem do pacote particular, e novas grades terapêuticas pulam automaticamente estas datas.
          </p>
        </div>
      </div>

      {/* Filtro por Clínica (apenas ADMIN com múltiplas clínicas) */}
      {isAdmin && clinicas.length > 1 && (
        <div className="bg-white rounded-xl border border-gray-200 p-4 flex items-center gap-3">
          <Building2 className="w-4 h-4 text-gray-400" />
          <label className="text-xs font-semibold text-gray-600 uppercase">Filtrar por Clínica:</label>
          <select
            value={idClinicaFiltro}
            onChange={(e) => setIdClinicaFiltro(e.target.value)}
            className="px-3 py-1.5 bg-gray-50 border border-gray-300 rounded-lg text-sm"
          >
            <option value="">Todas as clínicas</option>
            {clinicas.map((c) => (
              <option key={c.idClinica} value={c.idClinica}>
                {c.nomeClinica}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Feedback */}
      {sucesso && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-800 text-sm flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
            <span>{sucesso}</span>
          </div>
          <button onClick={() => setSucesso(null)} className="text-xs font-bold underline">
            Fechar
          </button>
        </div>
      )}

      {erro && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-800 text-sm flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5 text-red-600 shrink-0" />
            <span>{erro}</span>
          </div>
          <button onClick={() => setErro(null)} className="text-xs font-bold underline">
            Fechar
          </button>
        </div>
      )}

      {/* Lista de Bloqueios */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
        {loading ? (
          <div className="py-16 flex flex-col items-center justify-center text-gray-500">
            <Loader2 className="w-8 h-8 animate-spin text-indigo-600 mb-3" />
            <p className="text-sm">Carregando feriados e recessos...</p>
          </div>
        ) : bloqueios.length === 0 ? (
          <div className="py-16 text-center px-6">
            <CalendarOff className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <h3 className="text-base font-semibold text-gray-900">Nenhum feriado ou recesso cadastrado</h3>
            <p className="text-sm text-gray-500 mt-1 max-w-md mx-auto">
              Cadastre férias coletivas, feriados ou pontos facultativos para suspender os atendimentos da clínica e impedir replicação nestas datas.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-600">
              <thead className="bg-gray-50 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500">
                <tr>
                  <th className="px-6 py-3.5">Título / Motivo</th>
                  <th className="px-6 py-3.5">Tipo</th>
                  <th className="px-6 py-3.5">Período</th>
                  <th className="px-6 py-3.5">Horário</th>
                  {isAdmin && <th className="px-6 py-3.5">Clínica</th>}
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {bloqueios.map((item) => {
                  const tipoInfo = TIPO_BLOQUEIO_LABELS[item.tipo] || TIPO_BLOQUEIO_LABELS.OUTRO;
                  const mesmoDia = item.dataInicio === item.dataFim;
                  return (
                    <tr key={item.idBloqueio} className="hover:bg-gray-50/70 transition-colors">
                      <td className="px-6 py-4">
                        <p className="font-semibold text-gray-900">{item.titulo}</p>
                        {item.observacao && <p className="text-xs text-gray-400 mt-0.5">{item.observacao}</p>}
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${tipoInfo.badgeClass}`}>
                          {tipoInfo.label}
                        </span>
                      </td>
                      <td className="px-6 py-4 font-medium text-gray-800">
                        {mesmoDia
                          ? formatDateBR(item.dataInicio)
                          : `${formatDateBR(item.dataInicio)} até ${formatDateBR(item.dataFim)}`}
                      </td>
                      <td className="px-6 py-4 text-xs">
                        {item.horaInicio && item.horaFim ? (
                          <span className="inline-flex items-center gap-1 text-gray-700 font-medium">
                            <Clock className="w-3.5 h-3.5 text-gray-400" />
                            {formatTimeShort(item.horaInicio)} às {formatTimeShort(item.horaFim)}
                          </span>
                        ) : (
                          <span className="text-gray-500">Dia inteiro</span>
                        )}
                      </td>
                      {isAdmin && (
                        <td className="px-6 py-4 text-xs text-gray-700">
                          {item.nomeClinica || '-'}
                        </td>
                      )}
                      <td className="px-6 py-4 text-right">
                        <button
                          type="button"
                          onClick={() => handleRemover(item)}
                          disabled={deletingId === item.idBloqueio}
                          className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-40"
                          title="Remover bloqueio"
                        >
                          {deletingId === item.idBloqueio ? (
                            <Loader2 className="w-4 h-4 animate-spin" />
                          ) : (
                            <Trash2 className="w-4 h-4" />
                          )}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Novo Bloqueio / Férias */}
      {modalAberto && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-gray-200 overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <div className="flex items-center gap-2">
                <CalendarOff className="w-5 h-5 text-indigo-600" />
                <h2 className="text-lg font-bold text-gray-900">Novo Bloqueio / Férias Coletivas</h2>
              </div>
              <button
                type="button"
                onClick={() => !saving && setModalAberto(false)}
                className="p-1 text-gray-400 hover:text-gray-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSalvarBloqueio} className="p-6 space-y-4">
              {isAdmin && clinicas.length > 0 && (
                <div>
                  <label className="block text-xs font-semibold text-gray-600 uppercase mb-1">Clínica *</label>
                  <select
                    value={idClinicaForm}
                    onChange={(e) => setIdClinicaForm(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  >
                    <option value="">Selecione a clínica...</option>
                    {clinicas.map((c) => (
                      <option key={c.idClinica} value={c.idClinica}>
                        {c.nomeClinica}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-600 uppercase mb-1">Título / Motivo *</label>
                <input
                  type="text"
                  required
                  placeholder="Ex: Férias Coletivas de Fim de Ano, Véspera de Natal..."
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 uppercase mb-1">Tipo *</label>
                <select
                  value={tipo}
                  onChange={(e) => setTipo(e.target.value as TipoBloqueioAgenda)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                >
                  <option value="FERIAS_COLETIVAS">Férias Coletivas</option>
                  <option value="FERIADO">Feriado</option>
                  <option value="RECESSO">Recesso</option>
                  <option value="PONTO_FACULTATIVO">Ponto Facultativo</option>
                  <option value="OUTRO">Outro</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 uppercase mb-1">Data Início *</label>
                  <input
                    type="date"
                    required
                    value={dataInicio}
                    onChange={(e) => {
                      setDataInicio(e.target.value);
                      if (!dataFim || e.target.value > dataFim) {
                        setDataFim(e.target.value);
                      }
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 uppercase mb-1">Data Término *</label>
                  <input
                    type="date"
                    required
                    min={dataInicio}
                    value={dataFim}
                    onChange={(e) => setDataFim(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={diaInteiro}
                    onChange={(e) => setDiaInteiro(e.target.checked)}
                    className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>Suspender o dia inteiro</span>
                </label>

                {!diaInteiro && (
                  <div className="grid grid-cols-2 gap-3 pl-6">
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Hora Início</label>
                      <input
                        type="time"
                        value={horaInicio}
                        onChange={(e) => setHoraInicio(e.target.value)}
                        className="w-full px-3 py-1.5 border border-gray-300 rounded-lg text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Hora Fim</label>
                      <input
                        type="time"
                        value={horaFim}
                        onChange={(e) => setHoraFim(e.target.value)}
                        className="w-full px-3 py-1.5 border border-gray-300 rounded-lg text-sm"
                      />
                    </div>
                  </div>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 uppercase mb-1">Observação (opcional)</label>
                <input
                  type="text"
                  placeholder="Ex: Retorno normal das atividades em 04/01"
                  value={observacao}
                  onChange={(e) => setObservacao(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                />
              </div>

              <div className="p-3 bg-indigo-50 border border-indigo-200 rounded-xl">
                <label className="flex items-start gap-2.5 text-xs text-indigo-900 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={suspenderExistentes}
                    onChange={(e) => setSuspenderExistentes(e.target.checked)}
                    className="mt-0.5 rounded border-indigo-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>
                    <strong>Suspender agendamentos já marcados neste período</strong> — os compromissos futuros serão cancelados preservando integralmente o saldo de sessões das guias e pacotes.
                  </span>
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setModalAberto(false)}
                  disabled={saving}
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="inline-flex items-center gap-2 px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-sm disabled:opacity-50"
                >
                  {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                  Salvar Bloqueio
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Importar Feriados Nacionais */}
      {modalFeriados && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-xl w-full shadow-2xl border border-gray-200 overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <div className="flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-indigo-600" />
                <h2 className="text-lg font-bold text-gray-900">Importar Feriados Nacionais</h2>
              </div>
              <button
                type="button"
                onClick={() => !importing && setModalFeriados(false)}
                className="p-1 text-gray-400 hover:text-gray-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4">
              <div className="flex items-center justify-between gap-3">
                <p className="text-xs text-gray-600">
                  Marque apenas os feriados em que <strong>esta clínica NÃO terá atendimento</strong>:
                </p>
                <select
                  value={anoImportacao}
                  onChange={(e) => abrirModalFeriados(Number(e.target.value))}
                  className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm font-semibold"
                >
                  {[new Date().getFullYear(), new Date().getFullYear() + 1].map((ano) => (
                    <option key={ano} value={ano}>
                      {ano}
                    </option>
                  ))}
                </select>
              </div>

              <div className="max-h-72 overflow-y-auto divide-y divide-gray-100 border border-gray-200 rounded-xl">
                {gerarFeriadosNacionaisSugeridos(anoImportacao).map((f) => {
                  const jaCadastrado = bloqueios.some((b) => b.dataInicio === f.data && b.dataFim === f.data);
                  return (
                    <label
                      key={f.data}
                      className={`flex items-center justify-between px-4 py-2.5 text-sm ${
                        jaCadastrado ? 'bg-gray-50 text-gray-400' : 'hover:bg-indigo-50/40 cursor-pointer'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <input
                          type="checkbox"
                          disabled={jaCadastrado}
                          checked={!!feriadosSelecionados[f.data]}
                          onChange={(e) =>
                            setFeriadosSelecionados((prev) => ({
                              ...prev,
                              [f.data]: e.target.checked,
                            }))
                          }
                          className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                        />
                        <span className="font-medium">{f.titulo}</span>
                      </div>
                      <span className="text-xs font-mono">
                        {formatDateBR(f.data)} {jaCadastrado && '(já cadastrado)'}
                      </span>
                    </label>
                  );
                })}
              </div>

              <div className="p-3 bg-indigo-50 border border-indigo-200 rounded-xl">
                <label className="flex items-start gap-2.5 text-xs text-indigo-900 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={suspenderNoLote}
                    onChange={(e) => setSuspenderNoLote(e.target.checked)}
                    className="mt-0.5 rounded border-indigo-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>
                    Suspender automaticamente agendamentos já marcados nos feriados selecionados (preservando o saldo de sessões).
                  </span>
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setModalFeriados(false)}
                  disabled={importing}
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  onClick={handleImportarFeriados}
                  disabled={importing}
                  className="inline-flex items-center gap-2 px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-sm disabled:opacity-50"
                >
                  {importing && <Loader2 className="w-4 h-4 animate-spin" />}
                  Importar Selecionados
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
