import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  CalendarClock,
  Plus,
  Search,
  Loader2,
  AlertCircle,
  Pencil,
  Trash2,
  ShieldAlert,
  Clock,
  FileText,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { periodicidadeService } from '../services/periodicidadeService';
import type { Periodicidade } from '../types/periodicidade';
import PeriodicidadeModal from '../components/PeriodicidadeModal';

export default function Periodicidades() {
  const { isAdmin, isAdminClinica } = useAuth();
  const canAccess = isAdmin || isAdminClinica;

  const [periodicidades, setPeriodicidades] = useState<Periodicidade[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPeriodicidade, setEditingPeriodicidade] = useState<Periodicidade | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchPeriodicidades = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await periodicidadeService.list();
      setPeriodicidades(data);
    } catch {
      setError('Não foi possível carregar a lista de periodicidades.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (canAccess) {
      fetchPeriodicidades();
    }
  }, [canAccess, fetchPeriodicidades]);

  const filteredPeriodicidades = useMemo(() => {
    if (!search.trim()) return periodicidades;
    const term = search.toLowerCase();
    return periodicidades.filter((p) => {
      const matchNome = (p.nomePeriodicidade || '').toLowerCase().includes(term);
      const matchDesc = (p.descricaoPeriodicidade || '').toLowerCase().includes(term);
      return matchNome || matchDesc;
    });
  }, [periodicidades, search]);

  const handleSaved = (saved: Periodicidade) => {
    setPeriodicidades((prev) => {
      const idx = prev.findIndex((p) => p.idPeriodicidade === saved.idPeriodicidade);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = saved;
        return copy;
      }
      return [saved, ...prev];
    });
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja realmente desativar a periodicidade "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await periodicidadeService.delete(id);
      setPeriodicidades((prev) => prev.filter((p) => p.idPeriodicidade !== id));
    } catch {
      alert('Erro ao excluir periodicidade.');
    } finally {
      setDeletingId(null);
    }
  };

  // Se o usuário não for ADMIN nem ADMIN_CLINICA, exibe acesso restrito
  if (!canAccess) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6">
        <div className="p-4 bg-red-50 text-red-600 rounded-full mb-4">
          <ShieldAlert className="h-12 w-12" />
        </div>
        <h2 className="text-xl font-bold text-gray-900 mb-2">Acesso Restrito</h2>
        <p className="text-gray-500 max-w-md text-sm">
          Apenas administradores do sistema e administradores de clínicas possuem permissão para
          gerenciar as periodicidades de atendimento.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CalendarClock className="h-7 w-7 text-indigo-600" />
            Gestão de Periodicidades
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Configure as regras de recorrência e periodicidade para os agendamentos e compromissos.
          </p>
        </div>

        <button
          onClick={() => {
            setEditingPeriodicidade(null);
            setIsModalOpen(true);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg text-sm font-semibold hover:bg-indigo-700 transition-colors shadow-sm self-start sm:self-auto"
        >
          <Plus className="h-4 w-4" />
          Nova Periodicidade
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl p-5 border border-gray-200/80 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-indigo-50 rounded-lg text-indigo-600">
            <CalendarClock className="h-6 w-6" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Total de Periodicidades
            </div>
            <div className="text-2xl font-bold text-gray-900 mt-0.5">
              {periodicidades.length}
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
              placeholder="Buscar por nome da periodicidade, descrição..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all placeholder:text-gray-400"
            />
          </div>
          {search && (
            <span className="text-xs text-gray-500">
              {filteredPeriodicidades.length} de {periodicidades.length} periodicidade(s)
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
                onClick={fetchPeriodicidades}
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
            <p className="text-sm text-gray-500">Carregando periodicidades...</p>
          </div>
        ) : filteredPeriodicidades.length === 0 ? (
          <div className="p-12 text-center">
            <CalendarClock className="h-10 w-10 text-gray-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Nenhuma periodicidade encontrada</p>
            <p className="text-xs text-gray-400 mt-1">
              {search
                ? 'Nenhum resultado corresponde à sua pesquisa.'
                : 'Cadastre a primeira periodicidade clicando no botão acima.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-3.5">Nome</th>
                  <th className="px-6 py-3.5">Intervalo de Recorrência</th>
                  <th className="px-6 py-3.5">Descrição</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredPeriodicidades.map((item) => {
                  return (
                    <tr key={item.idPeriodicidade} className="hover:bg-gray-50/60 transition-colors">
                      {/* Nome */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-indigo-50 text-indigo-700 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            <CalendarClock className="h-5 w-5" />
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900">
                              {item.nomePeriodicidade}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Intervalo */}
                      <td className="px-6 py-4">
                        {item.diasIntervalo !== undefined && item.diasIntervalo !== null ? (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                            <Clock className="h-3 w-3" />
                            {item.diasIntervalo === 0
                              ? 'Avulsa (0 dias)'
                              : item.diasIntervalo === 1
                              ? '1 dia'
                              : `${item.diasIntervalo} dias`}
                          </span>
                        ) : (
                          <span className="text-gray-400 text-xs italic">Não definido</span>
                        )}
                      </td>

                      {/* Descrição */}
                      <td className="px-6 py-4">
                        {item.descricaoPeriodicidade ? (
                          <div className="flex items-center gap-1.5 text-xs text-gray-600 max-w-md">
                            <FileText className="h-3.5 w-3.5 text-gray-400 shrink-0" />
                            <span>{item.descricaoPeriodicidade}</span>
                          </div>
                        ) : (
                          <span className="text-gray-400 text-xs">-</span>
                        )}
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => {
                              setEditingPeriodicidade(item);
                              setIsModalOpen(true);
                            }}
                            className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                            title="Editar periodicidade"
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(item.idPeriodicidade, item.nomePeriodicidade)}
                            disabled={deletingId === item.idPeriodicidade}
                            className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                            title="Desativar periodicidade"
                          >
                            {deletingId === item.idPeriodicidade ? (
                              <Loader2 className="h-4 w-4 animate-spin text-red-500" />
                            ) : (
                              <Trash2 className="h-4 w-4" />
                            )}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal de Criação / Edição */}
      <PeriodicidadeModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingPeriodicidade(null);
        }}
        onSuccess={handleSaved}
        initialData={editingPeriodicidade}
      />
    </div>
  );
}
