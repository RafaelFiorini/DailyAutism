import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  CreditCard,
  Plus,
  Search,
  Loader2,
  AlertCircle,
  Pencil,
  Trash2,
  ShieldCheck,
  Building,
  Eye,
  CalendarClock,
  Send,
  CheckCircle2,
  Building2,
  FileCheck2,
} from 'lucide-react';
import { planoSaudeService } from '../services/planoSaudeService';
import { financeiroService } from '../services/financeiroService';
import { pacienteService } from '../services/pacienteService';
import type { PlanoSaude } from '../types/planoSaude';
import type { ClinicaPlanoPrazo, GuiaFaturamento } from '../types/financeiro';
import type { ClinicaOption } from '../types/paciente';
import PlanoSaudeModal from '../components/PlanoSaudeModal';
import ConfigurarPrazoModal from '../components/ConfigurarPrazoModal';
import { useAuth } from '../hooks/useAuth';

const formatBRL = (v?: number | null) =>
  (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function PlanosSaude() {
  const { user, isAdmin, isAdminClinica } = useAuth();
  const canManagePlanos = isAdmin || isAdminClinica;

  const [planos, setPlanos] = useState<PlanoSaude[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);
  const [search, setSearch] = useState('');

  // Modais
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPlano, setEditingPlano] = useState<PlanoSaude | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Configuração de prazos
  const [isPrazoModalOpen, setIsPrazoModalOpen] = useState(false);
  const [planoPrazoAlvo, setPlanoPrazoAlvo] = useState<PlanoSaude | null>(null);

  // Clínicas e dados financeiros
  const [clinicas, setClinicas] = useState<ClinicaOption[]>([]);
  const [selectedClinicaId, setSelectedClinicaId] = useState<string>(user?.idClinica || '');
  const [prazos, setPrazos] = useState<ClinicaPlanoPrazo[]>([]);
  const [guiasAssinadas, setGuiasAssinadas] = useState<GuiaFaturamento[]>([]);
  const [despachandoPlanoId, setDespachandoPlanoId] = useState<string | null>(null);

  const fetchDados = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [planosList, clinicasList] = await Promise.all([
        planoSaudeService.list(),
        pacienteService.listClinicas().catch(() => []),
      ]);
      setPlanos(planosList);
      setClinicas(clinicasList);

      const targetClinica =
        selectedClinicaId || (user?.idClinica ? user.idClinica : clinicasList[0]?.idClinica || '');

      if (targetClinica) {
        if (!selectedClinicaId) setSelectedClinicaId(targetClinica);
        const [prazosList, guiasList] = await Promise.all([
          financeiroService.listPrazosConvenios(targetClinica).catch(() => []),
          financeiroService.listGuiasConvenio({ clinicaId: targetClinica, status: 'ASSINADA' }).catch(() => []),
        ]);
        setPrazos(prazosList);
        setGuiasAssinadas(guiasList);
      }
    } catch {
      setError('Não foi possível carregar os dados de planos de saúde e faturamento.');
    } finally {
      setLoading(false);
    }
  }, [selectedClinicaId, user?.idClinica]);

  useEffect(() => {
    fetchDados();
  }, [fetchDados]);

  const refreshPrazosEGuias = async () => {
    if (!selectedClinicaId) return;
    try {
      const [prazosList, guiasList] = await Promise.all([
        financeiroService.listPrazosConvenios(selectedClinicaId),
        financeiroService.listGuiasConvenio({ clinicaId: selectedClinicaId, status: 'ASSINADA' }),
      ]);
      setPrazos(prazosList);
      setGuiasAssinadas(guiasList);
    } catch {
      // Ignora erro silencioso no refresh secundário
    }
  };

  const filteredPlanos = useMemo(() => {
    if (!search.trim()) return planos;
    const term = search.toLowerCase();
    return planos.filter((p) => {
      const matchNome = (p.nomePlano || '').toLowerCase().includes(term);
      const matchOperadora = (p.operadora || '').toLowerCase().includes(term);
      const matchAns = (p.ansReg || '').toLowerCase().includes(term);
      return matchNome || matchOperadora || matchAns;
    });
  }, [planos, search]);

  // Mapeia prazos e guias por idPlanoSaude
  const prazosPorPlano = useMemo(() => {
    const map = new Map<string, ClinicaPlanoPrazo>();
    prazos.forEach((p) => map.set(p.idPlanoSaude, p));
    return map;
  }, [prazos]);

  const guiasPorPlano = useMemo(() => {
    const map = new Map<string, GuiaFaturamento[]>();
    guiasAssinadas.forEach((g) => {
      const list = map.get(g.idPlanoSaude) || [];
      list.push(g);
      map.set(g.idPlanoSaude, list);
    });
    return map;
  }, [guiasAssinadas]);

  const totalGuiasAssinadasGeral = guiasAssinadas.length;
  const valorTotalAssinadoGeral = guiasAssinadas.reduce((acc, g) => acc + (g.valorTotal || 0), 0);

  const handleSaved = (saved: PlanoSaude) => {
    setPlanos((prev) => {
      const idx = prev.findIndex((p) => p.idPlanoSaude === saved.idPlanoSaude);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = saved;
        return copy;
      }
      return [saved, ...prev];
    });
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja realmente desativar o plano de saúde "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await planoSaudeService.delete(id);
      setPlanos((prev) => prev.filter((p) => p.idPlanoSaude !== id));
      setFeedback({ type: 'ok', text: `Plano "${nome}" desativado com sucesso.` });
    } catch {
      setFeedback({ type: 'err', text: 'Erro ao excluir plano de saúde.' });
    } finally {
      setDeletingId(null);
    }
  };

  const handleDespacharLote = async (plano: PlanoSaude, guiasDoPlano: GuiaFaturamento[]) => {
    if (!selectedClinicaId) return;
    const totalValor = guiasDoPlano.reduce((acc, g) => acc + (g.valorTotal || 0), 0);
    const confirmMsg = `Deseja despachar o lote de ${guiasDoPlano.length} guia(s) assinada(s) do convênio "${plano.nomePlano}" no valor total de ${formatBRL(totalValor)}?`;
    if (!window.confirm(confirmMsg)) return;

    setDespachandoPlanoId(plano.idPlanoSaude);
    setFeedback(null);
    try {
      const resultado = await financeiroService.enviarLoteConvenio({
        idClinica: selectedClinicaId,
        idPlanoSaude: plano.idPlanoSaude,
        idsGuias: guiasDoPlano.map((g) => g.idGuiaFaturamento),
      });

      setFeedback({
        type: 'ok',
        text: `Lote com ${resultado.quantidadeGuias} guia(s) do convênio ${resultado.nomePlano} enviado com sucesso! Previsão de pagamento em ${resultado.dataPrevisaoPagamento}.`,
      });
      await refreshPrazosEGuias();
    } catch (err: unknown) {
      let msg = 'Erro ao enviar lote ao convênio.';
      if (err && typeof err === 'object' && 'response' in err) {
        const res = (err as { response?: { data?: { message?: string; errorMessage?: string } } }).response;
        msg = res?.data?.errorMessage || res?.data?.message || msg;
      }
      setFeedback({ type: 'err', text: msg });
    } finally {
      setDespachandoPlanoId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CreditCard className="h-7 w-7 text-indigo-600" />
            Gestão de Planos de Saúde & Remessas
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Cadastre convênios, configure prazos de resposta e feche lotes de guias assinadas.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {isAdmin && clinicas.length > 0 && (
            <div className="flex items-center gap-2 bg-white px-3 py-1.5 border border-gray-200 rounded-lg shadow-sm">
              <Building2 className="h-4 w-4 text-gray-400" />
              <select
                value={selectedClinicaId}
                onChange={(e) => setSelectedClinicaId(e.target.value)}
                aria-label="Selecionar clínica"
                className="text-xs bg-transparent font-medium text-gray-700 focus:outline-none"
              >
                {clinicas.map((c) => (
                  <option key={c.idClinica} value={c.idClinica}>
                    {c.nomeClinica}
                  </option>
                ))}
              </select>
            </div>
          )}

          {canManagePlanos ? (
            <button
              onClick={() => {
                setEditingPlano(null);
                setIsModalOpen(true);
              }}
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg text-sm font-semibold hover:bg-indigo-700 transition-colors shadow-sm self-start sm:self-auto"
            >
              <Plus className="h-4 w-4" />
              Novo Plano de Saúde
            </button>
          ) : (
            <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 border border-gray-200 text-gray-600 rounded-lg text-xs font-medium self-start sm:self-auto">
              <Eye className="h-4 w-4 text-gray-500" />
              Modo Somente Leitura
            </div>
          )}
        </div>
      </div>

      {/* Feedback banner */}
      {feedback && (
        <div
          className={`p-3.5 rounded-xl border flex items-center gap-2 text-xs font-medium ${
            feedback.type === 'ok'
              ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
              : 'bg-red-50 border-red-200 text-red-700'
          }`}
        >
          {feedback.type === 'ok' ? (
            <CheckCircle2 className="h-4 w-4 shrink-0" />
          ) : (
            <AlertCircle className="h-4 w-4 shrink-0" />
          )}
          <span>{feedback.text}</span>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl p-5 border border-gray-200/80 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-indigo-50 rounded-lg text-indigo-600">
            <CreditCard className="h-6 w-6" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Total de Convênios
            </div>
            <div className="text-2xl font-bold text-gray-900 mt-0.5">
              {planos.length}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl p-5 border border-gray-200/80 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-emerald-50 rounded-lg text-emerald-600">
            <FileCheck2 className="h-6 w-6" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Guias Assinadas Prontas
            </div>
            <div className="text-2xl font-bold text-emerald-700 mt-0.5">
              {totalGuiasAssinadasGeral}{' '}
              <span className="text-xs font-normal text-gray-500">
                ({formatBRL(valorTotalAssinadoGeral)})
              </span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl p-5 border border-gray-200/80 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-purple-50 rounded-lg text-purple-600">
            <CalendarClock className="h-6 w-6" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Prazos Configurados
            </div>
            <div className="text-2xl font-bold text-purple-700 mt-0.5">
              {prazos.length}{' '}
              <span className="text-xs font-normal text-gray-500">de {planos.length} planos</span>
            </div>
          </div>
        </div>
      </div>

      {/* Search and Table Card */}
      <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden">
        {/* Search header */}
        <div className="p-4 border-b border-gray-200/80 bg-gray-50/50 flex items-center justify-between gap-4">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nome do plano, operadora, registro ANS..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all placeholder:text-gray-400"
            />
          </div>
          {search && (
            <span className="text-xs text-gray-500">
              {filteredPlanos.length} de {planos.length} plano(s)
            </span>
          )}
        </div>

        {/* State: Error */}
        {error && (
          <div className="p-6">
            <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm flex items-center justify-between">
              <div className="flex items-center gap-2">
                <AlertCircle className="h-4 w-4 shrink-0" />
                <span>{error}</span>
              </div>
              <button
                onClick={fetchDados}
                className="underline hover:no-underline text-xs font-semibold"
              >
                Tentar novamente
              </button>
            </div>
          </div>
        )}

        {/* State: Loading */}
        {loading ? (
          <div className="p-12 text-center">
            <Loader2 className="h-8 w-8 text-indigo-600 animate-spin mx-auto mb-2" />
            <p className="text-sm text-gray-500">Carregando planos de saúde...</p>
          </div>
        ) : filteredPlanos.length === 0 ? (
          <div className="p-12 text-center">
            <CreditCard className="h-10 w-10 text-gray-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Nenhum plano de saúde encontrado</p>
            <p className="text-xs text-gray-400 mt-1">
              {search
                ? 'Nenhum resultado corresponde à sua pesquisa.'
                : 'Cadastre o primeiro plano de saúde clicando no botão acima.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-3.5">Plano de Saúde</th>
                  <th className="px-6 py-3.5">Operadora / ANS</th>
                  <th className="px-6 py-3.5">Prazos de Faturamento</th>
                  <th className="px-6 py-3.5">Lote Assinado / Remessa</th>
                  {canManagePlanos && <th className="px-6 py-3.5 text-right">Ações</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredPlanos.map((plano) => {
                  const prazo = prazosPorPlano.get(plano.idPlanoSaude);
                  const guiasDoPlano = guiasPorPlano.get(plano.idPlanoSaude) || [];
                  const totalValorLote = guiasDoPlano.reduce(
                    (acc, g) => acc + (g.valorTotal || 0),
                    0
                  );
                  const isDespachando = despachandoPlanoId === plano.idPlanoSaude;

                  return (
                    <tr key={plano.idPlanoSaude} className="hover:bg-gray-50/60 transition-colors">
                      {/* Nome do Plano */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            <CreditCard className="h-5 w-5" />
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900">{plano.nomePlano}</div>
                          </div>
                        </div>
                      </td>

                      {/* Operadora / ANS */}
                      <td className="px-6 py-4">
                        <div className="flex flex-col gap-1">
                          <div className="flex items-center gap-1.5 text-xs font-medium text-gray-800">
                            <Building className="h-3.5 w-3.5 text-gray-400" />
                            {plano.operadora}
                          </div>
                          {plano.ansReg ? (
                            <span className="inline-flex items-center gap-1 text-[11px] font-mono text-gray-500">
                              <ShieldCheck className="h-3 w-3 text-emerald-600" />
                              ANS: {plano.ansReg}
                            </span>
                          ) : null}
                        </div>
                      </td>

                      {/* Prazos de Faturamento */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="text-xs">
                            {prazo ? (
                              <>
                                <span className="block font-medium text-gray-900">
                                  {prazo.diaCorteEnvio
                                    ? `Corte todo dia ${prazo.diaCorteEnvio}`
                                    : 'Sem corte fixo'}
                                </span>
                                <span className="text-[11px] text-purple-700 font-semibold block">
                                  Pagamento em {prazo.prazoPagamentoDias} dias
                                </span>
                              </>
                            ) : (
                              <>
                                <span className="block text-gray-500">Padrão do sistema</span>
                                <span className="text-[11px] text-purple-600 block">
                                  Prazo: 30 dias
                                </span>
                              </>
                            )}
                          </div>
                          {canManagePlanos && (
                            <button
                              onClick={() => {
                                setPlanoPrazoAlvo(plano);
                                setIsPrazoModalOpen(true);
                              }}
                              className="px-2 py-1 text-[11px] font-semibold text-purple-700 bg-purple-50 hover:bg-purple-100 border border-purple-200 rounded-lg flex items-center gap-1 transition"
                              title="Configurar dia de corte e prazo de pagamento deste convênio"
                            >
                              <CalendarClock className="h-3.5 w-3.5" />
                              {prazo ? 'Ajustar' : 'Configurar'}
                            </button>
                          )}
                        </div>
                      </td>

                      {/* Lote de Guias Assinadas */}
                      <td className="px-6 py-4">
                        {guiasDoPlano.length > 0 ? (
                          <div className="flex items-center gap-2">
                            <div className="text-xs">
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-100 text-emerald-800">
                                <FileCheck2 className="h-3 w-3" />
                                {guiasDoPlano.length} assinada(s)
                              </span>
                              <span className="block text-[11px] font-bold text-gray-800 mt-0.5">
                                {formatBRL(totalValorLote)}
                              </span>
                            </div>

                            {canManagePlanos && (
                              <button
                                onClick={() => handleDespacharLote(plano, guiasDoPlano)}
                                disabled={isDespachando}
                                className="px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center gap-1.5 shadow-sm transition disabled:opacity-50"
                                title="Despachar todas as guias assinadas deste convênio de uma só vez"
                              >
                                {isDespachando ? (
                                  <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                ) : (
                                  <Send className="h-3.5 w-3.5" />
                                )}
                                Enviar Lote
                              </button>
                            )}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">
                            Nenhuma guia assinada pendente
                          </span>
                        )}
                      </td>

                      {/* Ações */}
                      {canManagePlanos && (
                        <td className="px-6 py-4 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              onClick={() => {
                                setEditingPlano(plano);
                                setIsModalOpen(true);
                              }}
                              className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                              title="Editar dados cadastrais do plano"
                            >
                              <Pencil className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleDelete(plano.idPlanoSaude, plano.nomePlano)}
                              disabled={deletingId === plano.idPlanoSaude}
                              className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                              title="Desativar plano de saúde"
                            >
                              {deletingId === plano.idPlanoSaude ? (
                                <Loader2 className="h-4 w-4 animate-spin text-red-500" />
                              ) : (
                                <Trash2 className="h-4 w-4" />
                              )}
                            </button>
                          </div>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal de Cadastro / Edição do Plano */}
      <PlanoSaudeModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleSaved}
        initialData={editingPlano}
      />

      {/* Modal de Configuração de Prazos do Convênio */}
      <ConfigurarPrazoModal
        isOpen={isPrazoModalOpen}
        onClose={() => {
          setIsPrazoModalOpen(false);
          setPlanoPrazoAlvo(null);
        }}
        onSuccess={async () => {
          setFeedback({ type: 'ok', text: 'Prazos do convênio salvos com sucesso!' });
          await refreshPrazosEGuias();
        }}
        clinicaId={selectedClinicaId}
        plano={planoPrazoAlvo}
        prazoAtual={planoPrazoAlvo ? prazosPorPlano.get(planoPrazoAlvo.idPlanoSaude) : null}
      />
    </div>
  );
}
