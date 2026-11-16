import { useState, useEffect, useCallback, useMemo } from 'react';
import {
  DollarSign,
  Building2,
  User,
  CheckCircle2,
  Clock,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Loader2,
  AlertCircle,
} from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type {
  ResumoFinanceiroProfissional,
  LancamentoRepasseProfissional,
  TotalPorOrigem,
} from '../types/financeiro';

export default function FinanceiroProfissionalTab() {
  const [mesCompetencia, setMesCompetencia] = useState<string>(() => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    return `${y}-${m}`;
  });

  const [resumo, setResumo] = useState<ResumoFinanceiroProfissional | null>(null);
  const [lancamentos, setLancamentos] = useState<LancamentoRepasseProfissional[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  // Filtros locais
  const [filtroStatus, setFiltroStatus] = useState<string | null>(null);
  const [filtroClinica, setFiltroClinica] = useState<string | null>(null);

  // Modais
  const [showModalValorParticular, setShowModalValorParticular] = useState(false);
  const [valorParticularInput, setValorParticularInput] = useState('');
  const [origemParaQuitar, setOrigemParaQuitar] = useState<TotalPorOrigem | null>(null);

  const carregarDados = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [resumoData, lancamentosData] = await Promise.all([
        financeiroService.getResumoProfissional(mesCompetencia),
        financeiroService.listLancamentosProfissional({ mesAno: mesCompetencia }),
      ]);
      setResumo(resumoData);
      setLancamentos(lancamentosData);
    } catch {
      setError('Não foi possível carregar os dados financeiros do profissional.');
    } finally {
      setLoading(false);
    }
  }, [mesCompetencia]);

  useEffect(() => {
    carregarDados();
  }, [carregarDados]);

  // Formatação do Mês
  const nomeMesAno = useMemo(() => {
    try {
      const [ano, mes] = mesCompetencia.split('-');
      const d = new Date(parseInt(ano), parseInt(mes) - 1, 1);
      const str = d.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
      return str.charAt(0).toUpperCase() + str.slice(1);
    } catch {
      return mesCompetencia;
    }
  }, [mesCompetencia]);

  const handleNavegarMes = (delta: number) => {
    const [ano, mes] = mesCompetencia.split('-').map(Number);
    const d = new Date(ano, mes - 1 + delta, 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    setMesCompetencia(`${y}-${m}`);
  };

  const handleDarBaixa = async (id: string) => {
    setActionLoading(true);
    try {
      await financeiroService.baixarLancamentoProfissional(id);
      await carregarDados();
    } catch {
      alert('Erro ao dar baixa no lançamento.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReverterBaixa = async (id: string) => {
    setActionLoading(true);
    try {
      await financeiroService.reverterLancamentoProfissional(id);
      await carregarDados();
    } catch {
      alert('Erro ao reverter baixa.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleConfirmarQuitacaoLote = async () => {
    if (!origemParaQuitar) return;
    setActionLoading(true);
    try {
      await financeiroService.baixarLoteClinicaProfissional(
        origemParaQuitar.idOrigem,
        mesCompetencia
      );
      setOrigemParaQuitar(null);
      await carregarDados();
    } catch {
      alert('Erro ao quitar sessões da clínica.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSalvarValorParticular = async () => {
    const num = parseFloat(valorParticularInput.replace(/\./g, '').replace(',', '.'));
    if (isNaN(num) || num < 0) {
      alert('Informe um valor válido.');
      return;
    }
    setActionLoading(true);
    try {
      await financeiroService.configurarValorParticularProfissional(num);
      setShowModalValorParticular(false);
      await carregarDados();
    } catch {
      alert('Erro ao salvar valor particular.');
    } finally {
      setActionLoading(false);
    }
  };

  const formatarMoeda = (val?: number) => {
    return (val || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  };

  const formatarDataBr = (dataIso?: string) => {
    if (!dataIso) return '-';
    try {
      const [ano, mes, dia] = dataIso.split('-');
      return `${dia}/${mes}/${ano}`;
    } catch {
      return dataIso;
    }
  };

  // Filtragem
  const lancamentosFiltrados = useMemo(() => {
    return lancamentos.filter((l) => {
      const bateClinica =
        !filtroClinica ||
        (filtroClinica === 'PARTICULAR' ? !l.idClinica : l.idClinica === filtroClinica);
      const bateStatus = !filtroStatus || l.statusPagamento === filtroStatus;
      return bateClinica && bateStatus;
    });
  }, [lancamentos, filtroClinica, filtroStatus]);

  if (loading && !resumo) {
    return (
      <div className="py-16 flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2 text-red-700 text-sm">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Navegador de Mês */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => handleNavegarMes(-1)}
            className="p-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
            title="Mês Anterior"
          >
            <ChevronLeft className="h-5 w-5" />
          </button>
          <div className="text-center sm:text-left min-w-[200px]">
            <span className="text-xs text-gray-500 font-medium block">Competência</span>
            <span className="text-lg font-bold text-gray-900">{nomeMesAno}</span>
          </div>
          <button
            onClick={() => handleNavegarMes(1)}
            className="p-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
            title="Próximo Mês"
          >
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>

        {/* Preço Base Particular */}
        <div className="flex items-center gap-3 bg-gray-50 border border-gray-200 px-4 py-2.5 rounded-lg w-full sm:w-auto justify-between">
          <div>
            <span className="text-xs text-gray-500 block">Sessão Particular Direta</span>
            <span className="text-sm font-bold text-gray-900">
              {resumo?.valorSessaoParticularPadrao
                ? formatarMoeda(resumo.valorSessaoParticularPadrao)
                : 'Não configurado'}
            </span>
          </div>
          <button
            onClick={() => {
              setValorParticularInput(
                resumo?.valorSessaoParticularPadrao
                  ? resumo.valorSessaoParticularPadrao.toFixed(2).replace('.', ',')
                  : ''
              );
              setShowModalValorParticular(true);
            }}
            className="inline-flex items-center gap-1.5 px-2.5 py-1.5 bg-white border border-gray-300 hover:bg-gray-50 text-indigo-700 text-xs font-semibold rounded-md shadow-2xs transition-colors"
          >
            <Edit2 className="h-3.5 w-3.5" />
            Configurar
          </button>
        </div>
      </div>

      {/* Cards de Métricas Principais */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Total Faturado */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">
              Total da Competência
            </span>
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              <DollarSign className="h-5 w-5" />
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-gray-900">
            {formatarMoeda(resumo?.totalFaturado)}
          </p>
          <p className="mt-1 text-xs text-gray-500">
            {resumo?.quantidadeTotalSessoes || 0} sessão(ões) realizada(s)
          </p>
        </div>

        {/* Recebido / Na Conta */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-green-700 uppercase tracking-wider">
              Recebido / Na Conta
            </span>
            <div className="p-2 bg-green-50 text-green-600 rounded-lg">
              <CheckCircle2 className="h-5 w-5" />
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-green-700">
            {formatarMoeda(resumo?.totalRecebido)}
          </p>
          <p className="mt-1 text-xs text-gray-500">Repasses e valores já baixados</p>
        </div>

        {/* Pendente / A Receber */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-amber-700 uppercase tracking-wider">
              Pendente / A Receber
            </span>
            <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
              <Clock className="h-5 w-5" />
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-amber-600">
            {formatarMoeda(resumo?.totalPendente)}
          </p>
          <p className="mt-1 text-xs text-gray-500">Aguardando quitação das clínicas/pais</p>
        </div>
      </div>

      {/* Resumo por Clínica & Atendimentos Particulares */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-5 border-b border-gray-100 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-gray-900">
              Fechamento por Clínica & Particular
            </h3>
            <p className="text-xs text-gray-500">
              Acompanhe o repasse apurado de cada parceiro para a competência
            </p>
          </div>
        </div>

        <div className="p-5 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {(!resumo?.totaisPorOrigem || resumo.totaisPorOrigem.length === 0) && (
            <p className="col-span-full text-center text-sm text-gray-500 py-4">
              Nenhuma sessão registrada nesta competência.
            </p>
          )}

          {resumo?.totaisPorOrigem?.map((orig) => (
            <div
              key={orig.idOrigem}
              className="border border-gray-200 rounded-xl p-4 bg-gray-50 hover:bg-white transition-colors flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <div className="p-1.5 bg-indigo-100 text-indigo-700 rounded-md">
                    {orig.isParticular ? (
                      <User className="h-4 w-4" />
                    ) : (
                      <Building2 className="h-4 w-4" />
                    )}
                  </div>
                  <h4 className="font-bold text-sm text-gray-900 truncate">{orig.nomeOrigem}</h4>
                </div>

                <div className="mt-3 space-y-1.5 text-xs">
                  <div className="flex justify-between text-gray-600">
                    <span>Sessões realizadas:</span>
                    <span className="font-semibold text-gray-900">{orig.quantidadeSessoes}</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Total apurado:</span>
                    <span className="font-bold text-gray-900">{formatarMoeda(orig.total)}</span>
                  </div>
                  <div className="flex justify-between text-green-700">
                    <span>Recebido:</span>
                    <span className="font-bold">{formatarMoeda(orig.totalRecebido)}</span>
                  </div>
                  <div className="flex justify-between text-amber-700">
                    <span>Pendente:</span>
                    <span className="font-bold">{formatarMoeda(orig.totalPendente)}</span>
                  </div>
                </div>
              </div>

              {orig.totalPendente > 0 && (
                <div className="mt-4 pt-3 border-t border-gray-200">
                  <button
                    onClick={() => setOrigemParaQuitar(orig)}
                    disabled={actionLoading}
                    className="w-full inline-flex items-center justify-center gap-2 py-2 px-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white rounded-lg text-xs font-semibold transition-colors shadow-2xs"
                  >
                    <CheckCircle2 className="h-3.5 w-3.5" />
                    Quitar Mês ({formatarMoeda(orig.totalPendente)})
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Extrato Analítico de Sessões */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-5 border-b border-gray-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <h3 className="text-base font-bold text-gray-900">Extrato Analítico de Atendimentos</h3>
            <p className="text-xs text-gray-500">
              Sessões com repasses calculados e controle individual de quitação
            </p>
          </div>

          {/* Filtros */}
          <div className="flex items-center gap-2 flex-wrap">
            <div className="flex items-center gap-1 bg-gray-100 p-1 rounded-lg text-xs">
              <button
                onClick={() => setFiltroStatus(null)}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  filtroStatus === null
                    ? 'bg-white text-indigo-700 shadow-2xs'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Todos
              </button>
              <button
                onClick={() => setFiltroStatus('PENDENTE')}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  filtroStatus === 'PENDENTE'
                    ? 'bg-white text-amber-700 shadow-2xs'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Pendentes
              </button>
              <button
                onClick={() => setFiltroStatus('PAGO')}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  filtroStatus === 'PAGO'
                    ? 'bg-white text-green-700 shadow-2xs'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Recebidos
              </button>
            </div>

            {resumo?.totaisPorOrigem && resumo.totaisPorOrigem.length > 1 && (
              <select
                value={filtroClinica || ''}
                onChange={(e) => setFiltroClinica(e.target.value || null)}
                className="text-xs border border-gray-300 rounded-lg px-2.5 py-1.5 bg-white text-gray-700"
              >
                <option value="">Todas as clínicas/particulares</option>
                {resumo.totaisPorOrigem.map((o) => (
                  <option key={o.idOrigem} value={o.idOrigem}>
                    {o.nomeOrigem}
                  </option>
                ))}
              </select>
            )}
          </div>
        </div>

        {/* Tabela de Lançamentos */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-700">
            <thead className="bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3">Data</th>
                <th className="px-5 py-3">Paciente</th>
                <th className="px-5 py-3">Origem / Clínica</th>
                <th className="px-5 py-3">Regra Repasse</th>
                <th className="px-5 py-3 text-right">Valor Líquido</th>
                <th className="px-5 py-3 text-center">Status</th>
                <th className="px-5 py-3 text-right">Ação</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {lancamentosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-5 py-8 text-center text-sm text-gray-500">
                    Nenhum atendimento encontrado para os filtros selecionados.
                  </td>
                </tr>
              ) : (
                lancamentosFiltrados.map((l) => {
                  const isPago = l.statusPagamento === 'PAGO';
                  return (
                    <tr key={l.idLancamento} className="hover:bg-gray-50 transition-colors">
                      <td className="px-5 py-3.5 whitespace-nowrap font-medium text-gray-900">
                        {formatarDataBr(l.dataAtendimento)}
                      </td>
                      <td className="px-5 py-3.5">
                        <span className="font-semibold text-gray-900 block">
                          {l.nomePaciente || 'Paciente'}
                        </span>
                        {l.tituloCompromisso && (
                          <span className="text-xs text-gray-400 block truncate max-w-xs">
                            {l.tituloCompromisso}
                          </span>
                        )}
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-600">
                        <span className="inline-flex items-center gap-1.5 font-medium">
                          {l.idClinica ? (
                            <Building2 className="h-3.5 w-3.5 text-gray-400" />
                          ) : (
                            <User className="h-3.5 w-3.5 text-indigo-500" />
                          )}
                          {l.nomeClinica || 'Particular Direto'}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-xs text-gray-500 whitespace-nowrap">
                        {l.tipoOrigem === 'CLINICA_FIXO' && (
                          <span className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded-md font-medium">
                            Fixo
                          </span>
                        )}
                        {l.tipoOrigem === 'CLINICA_PERCENTUAL' && (
                          <span className="px-2 py-0.5 bg-purple-50 text-purple-700 rounded-md font-medium">
                            {l.percentualRepasse}% da tabela
                          </span>
                        )}
                        {l.tipoOrigem === 'PARTICULAR_DIRETO' && (
                          <span className="px-2 py-0.5 bg-emerald-50 text-emerald-700 rounded-md font-medium">
                            Particular 100%
                          </span>
                        )}
                      </td>
                      <td className="px-5 py-3.5 text-right font-bold text-gray-900 whitespace-nowrap">
                        {formatarMoeda(l.valorLiquidoRepasse)}
                      </td>
                      <td className="px-5 py-3.5 text-center whitespace-nowrap">
                        {isPago ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-green-50 text-green-700 text-xs font-bold rounded-full">
                            <CheckCircle2 className="h-3.5 w-3.5" />
                            RECEBIDO
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-amber-50 text-amber-700 text-xs font-bold rounded-full">
                            <Clock className="h-3.5 w-3.5" />
                            PENDENTE
                          </span>
                        )}
                      </td>
                      <td className="px-5 py-3.5 text-right whitespace-nowrap">
                        {isPago ? (
                          <button
                            onClick={() => handleReverterBaixa(l.idLancamento)}
                            disabled={actionLoading}
                            className="text-xs text-red-600 hover:text-red-800 font-medium hover:underline"
                          >
                            Desfazer
                          </button>
                        ) : (
                          <button
                            onClick={() => handleDarBaixa(l.idLancamento)}
                            disabled={actionLoading}
                            className="inline-flex items-center gap-1 px-3 py-1 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold rounded-md transition-colors"
                          >
                            <CheckCircle2 className="h-3.5 w-3.5" />
                            Confirmar
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Configurar Valor Particular */}
      {showModalValorParticular && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-sm w-full p-6">
            <h4 className="text-base font-bold text-gray-900">Preço da Sessão Particular</h4>
            <p className="text-xs text-gray-500 mt-1">
              Defina o valor base cobrado por sessão nos atendimentos particulares autônomos (fora
              de clínicas):
            </p>

            <div className="mt-4">
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Valor por Sessão (R$)
              </label>
              <input
                type="text"
                value={valorParticularInput}
                onChange={(e) => setValorParticularInput(e.target.value)}
                placeholder="Ex: 150,00"
                className="block w-full px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>

            <div className="mt-5 flex items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowModalValorParticular(false)}
                className="px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg font-medium"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleSalvarValorParticular}
                disabled={actionLoading}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold transition-colors"
              >
                Salvar Valor
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Confirmar Quitação em Lote */}
      {origemParaQuitar && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h4 className="text-base font-bold text-gray-900">Quitar Mês da Clínica</h4>
            <p className="text-sm text-gray-600 mt-2">
              Deseja marcar como <strong>RECEBIDAS</strong> todas as sessões pendentes de{' '}
              <strong>{origemParaQuitar.nomeOrigem}</strong> na competência {nomeMesAno}?
            </p>

            <div className="mt-4 p-3 bg-indigo-50 rounded-lg text-xs space-y-1">
              <div className="flex justify-between text-gray-700">
                <span>Valor total a quitar:</span>
                <span className="font-bold text-indigo-900 text-sm">
                  {formatarMoeda(origemParaQuitar.totalPendente)}
                </span>
              </div>
            </div>

            <div className="mt-5 flex items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => setOrigemParaQuitar(null)}
                className="px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg font-medium"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmarQuitacaoLote}
                disabled={actionLoading}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold transition-colors"
              >
                Confirmar Quitação
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
