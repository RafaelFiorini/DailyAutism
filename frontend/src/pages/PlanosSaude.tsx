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
} from 'lucide-react';
import { planoSaudeService } from '../services/planoSaudeService';
import type { PlanoSaude } from '../types/planoSaude';
import PlanoSaudeModal from '../components/PlanoSaudeModal';
import { useAuth } from '../hooks/useAuth';

export default function PlanosSaude() {
  const { isAdmin, isAdminClinica } = useAuth();
  const canManagePlanos = isAdmin || isAdminClinica;

  const [planos, setPlanos] = useState<PlanoSaude[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPlano, setEditingPlano] = useState<PlanoSaude | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchPlanos = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await planoSaudeService.list();
      setPlanos(data);
    } catch {
      setError('Não foi possível carregar a lista de planos de saúde.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPlanos();
  }, [fetchPlanos]);

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
    } catch {
      alert('Erro ao excluir plano de saúde.');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CreditCard className="h-7 w-7 text-indigo-600" />
            Gestão de Planos de Saúde
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Cadastre e gerencie os convênios e planos de saúde aceitos para atendimento.
          </p>
        </div>

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

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl p-5 border border-gray-200/80 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-indigo-50 rounded-lg text-indigo-600">
            <CreditCard className="h-6 w-6" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Total de Planos
            </div>
            <div className="text-2xl font-bold text-gray-900 mt-0.5">
              {planos.length}
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
                onClick={fetchPlanos}
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
                  <th className="px-6 py-3.5">Operadora</th>
                  <th className="px-6 py-3.5">Registro ANS</th>
                  {canManagePlanos && <th className="px-6 py-3.5 text-right">Ações</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredPlanos.map((plano) => {
                  return (
                    <tr key={plano.idPlanoSaude} className="hover:bg-gray-50/60 transition-colors">
                      {/* Nome do Plano */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            <CreditCard className="h-5 w-5" />
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900">
                              {plano.nomePlano}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Operadora */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-1.5 text-xs font-medium text-gray-800">
                          <Building className="h-3.5 w-3.5 text-gray-400" />
                          {plano.operadora}
                        </div>
                      </td>

                      {/* Registro ANS */}
                      <td className="px-6 py-4">
                        {plano.ansReg ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-mono font-medium bg-gray-100 text-gray-700 border border-gray-200">
                            <ShieldCheck className="h-3 w-3 text-emerald-600" />
                            ANS: {plano.ansReg}
                          </span>
                        ) : (
                          <span className="text-gray-400 text-xs">-</span>
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
                              title="Editar plano de saúde"
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

      {/* Modal */}
      <PlanoSaudeModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleSaved}
        initialData={editingPlano}
      />
    </div>
  );
}
