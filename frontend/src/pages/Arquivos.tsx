import { useState, useEffect, useMemo, useCallback, useContext } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  FileText,
  Upload,
  Search,
  Loader2,
  AlertCircle,
  Download,
  Share2,
  Trash2,
  Lock,
  UserCheck,
  Building2,
  Briefcase,
  ArrowUpDown,
  Filter,
  User,
  Tag,
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { arquivoService } from '../services/arquivoService';
import { pacienteService } from '../services/pacienteService';
import type { Arquivo, TipoArquivo } from '../types/arquivo';
import type { Paciente } from '../types/paciente';
import ArquivoUploadModal from '../components/ArquivoUploadModal';
import ArquivoCompartilharModal from '../components/ArquivoCompartilharModal';

type SortOption = 'DATA_DESC' | 'DATA_ASC' | 'NOME_ASC' | 'NOME_DESC';

export default function Arquivos() {
  const auth = useContext(AuthContext);
  const user = auth?.user ?? null;
  const isAdmin = auth?.isAdmin ?? false;

  const [searchParams, setSearchParams] = useSearchParams();
  const initialPacienteId = searchParams.get('pacienteId') || 'TODOS';

  const [arquivos, setArquivos] = useState<Arquivo[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [tipos, setTipos] = useState<TipoArquivo[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [search, setSearch] = useState('');
  const [pacienteFilter, setPacienteFilter] = useState<string>(initialPacienteId);
  const [tipoFilter, setTipoFilter] = useState<string>('TODOS');
  const [sortBy, setSortBy] = useState<SortOption>('DATA_DESC');

  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [sharingArquivo, setSharingArquivo] = useState<Arquivo | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  useEffect(() => {
    const paramPac = searchParams.get('pacienteId');
    if (paramPac) {
      setPacienteFilter(paramPac);
    }
  }, [searchParams]);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [arqList, pacList, tipoList] = await Promise.all([
        arquivoService.list(),
        pacienteService.list().catch(() => []),
        arquivoService.listTipos().catch(() => []),
      ]);
      setArquivos(arqList);
      setPacientes(pacList);
      setTipos(tipoList);
    } catch {
      setError('Não foi possível carregar a lista de documentos.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const handlePacienteFilterChange = (val: string) => {
    setPacienteFilter(val);
    if (val === 'TODOS') {
      searchParams.delete('pacienteId');
      setSearchParams(searchParams, { replace: true });
    } else {
      setSearchParams({ pacienteId: val }, { replace: true });
    }
  };

  const filteredAndSortedArquivos = useMemo(() => {
    const filtered = arquivos.filter((a) => {
      if (pacienteFilter !== 'TODOS' && a.idPaciente !== pacienteFilter) {
        return false;
      }
      if (tipoFilter !== 'TODOS' && a.idTipoArquivo !== tipoFilter) {
        return false;
      }
      if (search.trim()) {
        const term = search.toLowerCase();
        const matchNome = (a.nomeArquivo || '').toLowerCase().includes(term);
        const matchDesc = (a.descricao || '').toLowerCase().includes(term);
        const matchPac = (a.nomePaciente || '').toLowerCase().includes(term);
        const matchTipo = (a.nomeTipoArquivo || '').toLowerCase().includes(term);
        const matchAutor = (a.nomeUploadadoPor || '').toLowerCase().includes(term);
        return matchNome || matchDesc || matchPac || matchTipo || matchAutor;
      }
      return true;
    });

    return [...filtered].sort((a, b) => {
      switch (sortBy) {
        case 'DATA_ASC':
          return (a.dataArquivo || '').localeCompare(b.dataArquivo || '');
        case 'NOME_ASC':
          return (a.nomeArquivo || '').localeCompare(b.nomeArquivo || '', 'pt-BR', {
            sensitivity: 'base',
          });
        case 'NOME_DESC':
          return (b.nomeArquivo || '').localeCompare(a.nomeArquivo || '', 'pt-BR', {
            sensitivity: 'base',
          });
        case 'DATA_DESC':
        default:
          return (b.dataArquivo || '').localeCompare(a.dataArquivo || '');
      }
    });
  }, [arquivos, pacienteFilter, tipoFilter, search, sortBy]);

  const handleDownload = async (arquivo: Arquivo) => {
    setActionLoadingId(arquivo.idArquivo);
    try {
      const downloadData = await arquivoService.download(arquivo.idArquivo);
      arquivoService.triggerBrowserDownload(downloadData);
    } catch {
      alert('Não foi possível baixar o arquivo.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleDelete = async (arquivo: Arquivo) => {
    if (!window.confirm(`Deseja realmente excluir o documento "${arquivo.nomeArquivo}"?`)) {
      return;
    }
    setActionLoadingId(arquivo.idArquivo);
    try {
      await arquivoService.delete(arquivo.idArquivo);
      setArquivos((prev) => prev.filter((a) => a.idArquivo !== arquivo.idArquivo));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao excluir documento.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
  };

  const getExtBadgeColor = (ext?: string) => {
    const clean = (ext || '').toLowerCase();
    if (clean === 'pdf') return 'bg-red-50 text-red-700 border-red-200';
    if (clean === 'doc' || clean === 'docx') return 'bg-blue-50 text-blue-700 border-blue-200';
    return 'bg-gray-100 text-gray-700 border-gray-200';
  };

  const canManage = (a: Arquivo) => isAdmin || (user?.idUser && a.idUser === user.idUser);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">Documentos & Arquivos</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-100">
              {filteredAndSortedArquivos.length} documentos
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Laudos, receitas, acompanhamentos periódicos, solicitações e resultados de exames.
          </p>
        </div>

        <button
          onClick={() => setIsUploadOpen(true)}
          className="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors shadow-sm self-start sm:self-auto"
        >
          <Upload className="w-4 h-4" />
          Enviar Documento
        </button>
      </div>

      {/* Filtros e Ordenação */}
      <div className="bg-white p-4 rounded-xl border border-gray-200/80 shadow-sm space-y-3">
        <div className="flex flex-col lg:flex-row gap-3">
          {/* Busca */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por nome do documento, tipo, observação ou autor..."
              className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {/* Filtro por Paciente */}
            <div className="flex items-center gap-1.5">
              <User className="w-4 h-4 text-gray-400 shrink-0 hidden sm:block" />
              <select
                aria-label="Filtrar por paciente"
                value={pacienteFilter}
                onChange={(e) => handlePacienteFilterChange(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-500"
              >
                <option value="TODOS">Todos os Pacientes</option>
                {pacientes.map((p) => (
                  <option key={p.idPaciente} value={p.idPaciente}>
                    {p.nomePessoa || p.apelidoPaciente}
                  </option>
                ))}
              </select>
            </div>

            {/* Filtro por Tipo */}
            <div className="flex items-center gap-1.5">
              <Filter className="w-4 h-4 text-gray-400 shrink-0 hidden sm:block" />
              <select
                aria-label="Filtrar por tipo de documento"
                value={tipoFilter}
                onChange={(e) => setTipoFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-500"
              >
                <option value="TODOS">Todos os Tipos</option>
                {tipos.map((t) => (
                  <option key={t.idTipoArquivo} value={t.idTipoArquivo}>
                    {t.nomeTipo}
                  </option>
                ))}
              </select>
            </div>

            {/* Ordenação */}
            <div className="flex items-center gap-1.5">
              <ArrowUpDown className="w-4 h-4 text-gray-400 shrink-0 hidden sm:block" />
              <select
                aria-label="Ordenar documentos"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as SortOption)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-500"
              >
                <option value="DATA_DESC">Mais recentes primeiro</option>
                <option value="DATA_ASC">Mais antigos primeiro</option>
                <option value="NOME_ASC">Nome (A - Z)</option>
                <option value="NOME_DESC">Nome (Z - A)</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Mensagem de erro */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3 text-sm text-red-700">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Listagem */}
      {loading ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
          <p className="text-sm text-gray-500">Carregando documentos...</p>
        </div>
      ) : filteredAndSortedArquivos.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center">
          <FileText className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h3 className="text-base font-medium text-gray-900">Nenhum documento encontrado</h3>
          <p className="text-sm text-gray-500 mt-1 max-w-md mx-auto">
            {search || pacienteFilter !== 'TODOS' || tipoFilter !== 'TODOS'
              ? 'Nenhum arquivo corresponde aos filtros selecionados ou ainda não foi liberado para sua visualização.'
              : 'Clique no botão acima para enviar um laudo, receita, relatório ou resultado de exame.'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200/80 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-600">
              <thead className="bg-gray-50/75 border-b border-gray-200/80 text-xs uppercase font-medium text-gray-500 tracking-wider">
                <tr>
                  <th className="px-6 py-3.5">Documento</th>
                  <th className="px-6 py-3.5">Tipo & Paciente</th>
                  <th className="px-6 py-3.5">Datas & Autor</th>
                  <th className="px-6 py-3.5">Acesso / Liberação</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200/80">
                {filteredAndSortedArquivos.map((arq) => {
                  const isPrivado =
                    !arq.compartilhadoComResponsavel &&
                    !arq.compartilhadoComClinica &&
                    (!arq.profissionaisCompartilhados ||
                      arq.profissionaisCompartilhados.length === 0);
                  const isBusy = actionLoadingId === arq.idArquivo;

                  return (
                    <tr key={arq.idArquivo} className="hover:bg-gray-50/50 transition-colors">
                      {/* Documento */}
                      <td className="px-6 py-4">
                        <div className="flex items-start gap-3">
                          <span
                            className={`px-2 py-1 rounded text-[10px] font-bold uppercase border shrink-0 mt-0.5 ${getExtBadgeColor(
                              arq.extensaoArquivo
                            )}`}
                          >
                            {arq.extensaoArquivo}
                          </span>
                          <div className="min-w-0">
                            <p className="font-semibold text-gray-900 break-all text-xs sm:text-sm">
                              {arq.nomeArquivo}
                            </p>
                            {arq.descricao && (
                              <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">
                                {arq.descricao}
                              </p>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* Tipo & Paciente */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="space-y-1">
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-100">
                            <Tag className="w-3 h-3" />
                            {arq.nomeTipoArquivo}
                          </span>
                          <div className="text-xs font-medium text-gray-800 flex items-center gap-1">
                            <User className="w-3 h-3 text-gray-400" />
                            {arq.nomePaciente}
                          </div>
                        </div>
                      </td>

                      {/* Datas & Autor */}
                      <td className="px-6 py-4 whitespace-nowrap text-xs">
                        <div className="space-y-0.5">
                          <p className="font-medium text-gray-800">
                            Doc: {formatDate(arq.dataArquivo)}
                          </p>
                          <p className="text-gray-400">Envio: {formatDate(arq.dataUpload)}</p>
                          {arq.nomeUploadadoPor && (
                            <p className="text-[11px] text-gray-500">Por: {arq.nomeUploadadoPor}</p>
                          )}
                        </div>
                      </td>

                      {/* Acesso / Liberação */}
                      <td className="px-6 py-4">
                        {isPrivado ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                            <Lock className="w-3 h-3" />
                            Privado (Restrito ao autor)
                          </span>
                        ) : (
                          <div className="flex flex-wrap gap-1.5">
                            {arq.compartilhadoComResponsavel && (
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                                <UserCheck className="w-3 h-3" />
                                Responsável
                              </span>
                            )}
                            {arq.compartilhadoComClinica && (
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-teal-50 text-teal-700 border border-teal-200">
                                <Building2 className="w-3 h-3" />
                                {arq.nomeClinicaCompartilhada
                                  ? `Clínica ${arq.nomeClinicaCompartilhada}`
                                  : 'Clínica'}
                              </span>
                            )}
                            {arq.profissionaisCompartilhados?.map((prof) => (
                              <span
                                key={prof.idProf}
                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-purple-50 text-purple-700 border border-purple-200"
                              >
                                <Briefcase className="w-3 h-3" />
                                {prof.nomeProfissional}
                              </span>
                            ))}
                          </div>
                        )}
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {isBusy ? (
                            <Loader2 className="w-4 h-4 animate-spin text-indigo-600" />
                          ) : (
                            <>
                              <button
                                onClick={() => handleDownload(arq)}
                                title="Baixar Documento"
                                className="p-1.5 text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                              >
                                <Download className="w-4 h-4" />
                              </button>

                              {canManage(arq) && (
                                <>
                                  <button
                                    onClick={() => setSharingArquivo(arq)}
                                    title="Gerenciar Acesso / Liberar"
                                    className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                                  >
                                    <Share2 className="w-4 h-4" />
                                  </button>

                                  <button
                                    onClick={() => handleDelete(arq)}
                                    title="Excluir Documento"
                                    className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </>
                              )}
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

      {/* Modais */}
      <ArquivoUploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onSuccess={(created) => setArquivos((prev) => [created, ...prev])}
        defaultPacienteId={pacienteFilter !== 'TODOS' ? pacienteFilter : undefined}
      />

      <ArquivoCompartilharModal
        isOpen={!!sharingArquivo}
        onClose={() => setSharingArquivo(null)}
        arquivo={sharingArquivo}
        onSuccess={(updated) =>
          setArquivos((prev) =>
            prev.map((item) => (item.idArquivo === updated.idArquivo ? updated : item))
          )
        }
      />
    </div>
  );
}
