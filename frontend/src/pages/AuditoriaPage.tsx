import React, { useState, useEffect } from 'react';
import {
  Shield,
  Search,
  RotateCcw,
  Eye,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  FileSpreadsheet,
} from 'lucide-react';
import { auditoriaService } from '../services/auditoriaService';
import type {
  AuditoriaLog,
  FiltroAuditoria,
  PageAuditoria,
  CategoriaAuditoria,
  AcaoAuditoria,
} from '../types/auditoria';
import { CATEGORIAS_CONFIG, ACOES_CONFIG } from '../types/auditoria';
import { DetalhesAuditoriaModal } from '../components/auditoria/DetalhesAuditoriaModal';

export const AuditoriaPage: React.FC = () => {
  const [pageData, setPageData] = useState<PageAuditoria | null>(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  // Filtros
  const [buscaTexto, setBuscaTexto] = useState('');
  const [categoria, setCategoria] = useState<CategoriaAuditoria | ''>('');
  const [acao, setAcao] = useState<AcaoAuditoria | ''>('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [paginaAtual, setPaginaAtual] = useState(0);

  // Modal de Detalhes
  const [logSelecionado, setLogSelecionado] = useState<AuditoriaLog | null>(null);
  const [modalDetalhesOpen, setModalDetalhesOpen] = useState(false);

  useEffect(() => {
    carregarLogs(0);
  }, [categoria, acao, dataInicio, dataFim]);

  async function carregarLogs(page = 0) {
    try {
      setLoading(true);
      setErro(null);
      const filtro: FiltroAuditoria = {
        buscaTexto: buscaTexto.trim() || undefined,
        categoria: categoria || undefined,
        acao: acao || undefined,
        dataInicio: dataInicio || undefined,
        dataFim: dataFim || undefined,
        page,
        size: 20,
      };

      const data = await auditoriaService.listar(filtro);
      setPageData(data);
      setPaginaAtual(page);
    } catch (e: any) {
      console.error('Erro ao carregar auditoria:', e);
      setErro(e.response?.data?.message || e.message || 'Erro ao carregar logs de auditoria.');
    } finally {
      setLoading(false);
    }
  }

  function handleBuscar(e: React.FormEvent) {
    e.preventDefault();
    carregarLogs(0);
  }

  function handleLimparFiltros() {
    setBuscaTexto('');
    setCategoria('');
    setAcao('');
    setDataInicio('');
    setDataFim('');
    setPaginaAtual(0);
  }

  async function handleExportarCsv() {
    try {
      setExporting(true);
      const blob = await auditoriaService.exportarCsv({
        buscaTexto: buscaTexto.trim() || undefined,
        categoria: categoria || undefined,
        acao: acao || undefined,
        dataInicio: dataInicio || undefined,
        dataFim: dataFim || undefined,
      });

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      const dataStr = new Date().toISOString().split('T')[0];
      a.download = `auditoria_dailyautism_${dataStr}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (e: any) {
      console.error('Erro ao exportar CSV:', e);
      setErro('Falha ao exportar relatório CSV.');
    } finally {
      setExporting(false);
    }
  }

  function formatarDataHora(iso?: string) {
    if (!iso) return '-';
    try {
      const d = new Date(iso);
      return d.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return iso;
    }
  }

  return (
    <div className="space-y-6">
      {/* Topo da Página */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <div className="p-2 bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300 rounded-xl">
              <Shield className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100">
                Auditoria & Logs de Atividades
              </h1>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Trilha de rastreabilidade, conformidade com a LGPD e governança clínica.
              </p>
            </div>
          </div>
        </div>

        <button
          onClick={handleExportarCsv}
          disabled={exporting || loading}
          className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 rounded-xl shadow-sm transition disabled:opacity-50"
        >
          <FileSpreadsheet className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          <span>{exporting ? 'Exportando CSV...' : 'Exportar Relatório CSV'}</span>
        </button>
      </div>

      {erro && (
        <div className="flex items-start gap-2 p-3.5 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-300 text-sm">
          <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
          <span>{erro}</span>
        </div>
      )}

      {/* Barra de Filtros */}
      <div className="bg-white dark:bg-gray-800 p-4 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-3">
        <form onSubmit={handleBuscar} className="grid grid-cols-1 md:grid-cols-12 gap-3">
          {/* Busca Texto Livre */}
          <div className="md:col-span-4 relative">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={buscaTexto}
              onChange={(e) => setBuscaTexto(e.target.value)}
              placeholder="Buscar por usuário, ação, descrição..."
              className="w-full pl-9 pr-3 py-2 text-xs bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-primary-500 focus:outline-none"
            />
          </div>

          {/* Categoria */}
          <div className="md:col-span-2">
            <select
              value={categoria}
              onChange={(e) => setCategoria(e.target.value as CategoriaAuditoria)}
              className="w-full px-2.5 py-2 text-xs bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl font-medium"
            >
              <option value="">Todas as Categorias</option>
              {Object.entries(CATEGORIAS_CONFIG).map(([key, val]) => (
                <option key={key} value={key}>
                  {val.label}
                </option>
              ))}
            </select>
          </div>

          {/* Ação */}
          <div className="md:col-span-2">
            <select
              value={acao}
              onChange={(e) => setAcao(e.target.value as AcaoAuditoria)}
              className="w-full px-2.5 py-2 text-xs bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl font-medium"
            >
              <option value="">Todas as Ações</option>
              {Object.entries(ACOES_CONFIG).map(([key, val]) => (
                <option key={key} value={key}>
                  {val.label}
                </option>
              ))}
            </select>
          </div>

          {/* Data Início */}
          <div className="md:col-span-2">
            <input
              type="date"
              value={dataInicio}
              onChange={(e) => setDataInicio(e.target.value)}
              className="w-full px-2.5 py-2 text-xs bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl"
              title="Data Início"
            />
          </div>

          {/* Data Fim */}
          <div className="md:col-span-2 flex gap-2">
            <input
              type="date"
              value={dataFim}
              onChange={(e) => setDataFim(e.target.value)}
              className="w-full px-2.5 py-2 text-xs bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl"
              title="Data Fim"
            />
            <button
              type="submit"
              className="px-3 py-2 bg-primary-600 hover:bg-primary-700 text-white rounded-xl text-xs font-semibold transition shrink-0"
              title="Filtrar"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={handleLimparFiltros}
              className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition shrink-0"
              title="Limpar Filtros"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </form>
      </div>

      {/* Tabela de Logs */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-gray-50 dark:bg-gray-700/50 text-gray-500 dark:text-gray-400 uppercase tracking-wider text-[11px] font-semibold border-b border-gray-200 dark:border-gray-700">
              <tr>
                <th className="py-3 px-4">Data / Hora</th>
                <th className="py-3 px-4">Usuário</th>
                <th className="py-3 px-4">Categoria / Ação</th>
                <th className="py-3 px-4">Entidade</th>
                <th className="py-3 px-4">Descrição</th>
                <th className="py-3 px-4">IP Origem</th>
                <th className="py-3 px-4 text-right">Ação</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {loading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-gray-400 dark:text-gray-500">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="w-6 h-6 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
                      <span>Carregando registros de auditoria...</span>
                    </div>
                  </td>
                </tr>
              ) : !pageData || pageData.content.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-gray-400 dark:text-gray-500 italic">
                    Nenhum registro de auditoria encontrado com os filtros aplicados.
                  </td>
                </tr>
              ) : (
                pageData.content.map((item) => {
                  const categoriaCfg = CATEGORIAS_CONFIG[item.categoria] || {
                    label: item.categoria,
                    bg: 'bg-gray-100',
                    text: 'text-gray-800',
                    border: 'border-gray-200',
                  };
                  const acaoCfg = ACOES_CONFIG[item.acao] || {
                    label: item.acao,
                    badgeBg: 'bg-gray-100',
                    badgeText: 'text-gray-800',
                  };

                  return (
                    <tr
                      key={item.idAuditoriaLog}
                      className="hover:bg-gray-50 dark:hover:bg-gray-750 transition"
                    >
                      {/* Data / Hora */}
                      <td className="py-3 px-4 whitespace-nowrap font-mono text-gray-600 dark:text-gray-300">
                        {formatarDataHora(item.dataHora)}
                      </td>

                      {/* Usuário */}
                      <td className="py-3 px-4">
                        <div className="font-semibold text-gray-900 dark:text-gray-100">
                          {item.nomeUsuario || item.loginUsuario || 'Sistema'}
                        </div>
                        {item.perfilUsuario && (
                          <div className="text-[10px] text-gray-500 dark:text-gray-400 uppercase font-mono">
                            {item.perfilUsuario}
                          </div>
                        )}
                      </td>

                      {/* Categoria / Ação */}
                      <td className="py-3 px-4">
                        <div className="flex flex-col gap-1 items-start">
                          <span
                            className={`text-[10px] font-semibold px-2 py-0.5 rounded-md border ${categoriaCfg.bg} ${categoriaCfg.text} ${categoriaCfg.border}`}
                          >
                            {categoriaCfg.label}
                          </span>
                          <span
                            className={`text-[10px] font-bold px-2 py-0.5 rounded-md ${acaoCfg.badgeBg} ${acaoCfg.badgeText}`}
                          >
                            {acaoCfg.label}
                          </span>
                        </div>
                      </td>

                      {/* Entidade */}
                      <td className="py-3 px-4 font-medium text-gray-800 dark:text-gray-200">
                        {item.entidade || '-'}
                      </td>

                      {/* Descrição */}
                      <td className="py-3 px-4 max-w-xs truncate text-gray-700 dark:text-gray-300" title={item.descricao}>
                        {item.descricao || '-'}
                      </td>

                      {/* IP Origem */}
                      <td className="py-3 px-4 font-mono text-[11px] text-gray-500 dark:text-gray-400">
                        {item.ipOrigem || '-'}
                      </td>

                      {/* Botão Ver Detalhes */}
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => {
                            setLogSelecionado(item);
                            setModalDetalhesOpen(true);
                          }}
                          className="inline-flex items-center gap-1 px-2.5 py-1 text-[11px] font-semibold text-primary-600 hover:text-primary-700 dark:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/30 rounded-lg transition"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          <span>Detalhes</span>
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Rodapé de Paginação */}
        {pageData && pageData.totalPages > 1 && (
          <div className="px-4 py-3 bg-gray-50 dark:bg-gray-700/50 border-t border-gray-200 dark:border-gray-700 flex items-center justify-between">
            <span className="text-xs text-gray-500 dark:text-gray-400">
              Página <strong>{paginaAtual + 1}</strong> de <strong>{pageData.totalPages}</strong> ({pageData.totalElements} registros no total)
            </span>

            <div className="flex gap-1.5">
              <button
                onClick={() => carregarLogs(paginaAtual - 1)}
                disabled={paginaAtual <= 0 || loading}
                className="p-1.5 rounded-lg border border-gray-300 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-30 transition"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                onClick={() => carregarLogs(paginaAtual + 1)}
                disabled={paginaAtual >= pageData.totalPages - 1 || loading}
                className="p-1.5 rounded-lg border border-gray-300 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-30 transition"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modal de Detalhes */}
      <DetalhesAuditoriaModal
        isOpen={modalDetalhesOpen}
        onClose={() => setModalDetalhesOpen(false)}
        log={logSelecionado}
      />
    </div>
  );
};

export default AuditoriaPage;
