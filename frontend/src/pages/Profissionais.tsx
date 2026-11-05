import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Users,
  UserPlus,
  Search,
  Loader2,
  AlertCircle,
  Phone,
  Mail,
  Trash2,
  Award,
} from 'lucide-react';
import { profissionalService } from '../services/profissionalService';
import type { Profissional } from '../types/profissional';
import ProfissionalModal from '../components/ProfissionalModal';

export default function Profissionais() {
  const [profissionais, setProfissionais] = useState<Profissional[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchProfissionais = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await profissionalService.list();
      setProfissionais(data);
    } catch {
      setError('Não foi possível carregar a lista de profissionais.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfissionais();
  }, [fetchProfissionais]);

  const filteredProfissionais = useMemo(() => {
    if (!search.trim()) return profissionais;
    const term = search.toLowerCase();
    return profissionais.filter(
      (p) =>
        p.nomeProfissional?.toLowerCase().includes(term) ||
        p.conselhoProf?.toLowerCase().includes(term) ||
        p.cpf?.includes(term) ||
        p.email?.toLowerCase().includes(term)
    );
  }, [profissionais, search]);

  const handleCreated = (novo: Profissional) => {
    setProfissionais((prev) => [novo, ...prev]);
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja desativar o profissional "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await profissionalService.delete(id);
      setProfissionais((prev) => prev.filter((p) => p.idProf !== id));
    } catch {
      alert('Erro ao excluir profissional.');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">Profissionais de Saúde</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {profissionais.length} cadastrado{profissionais.length === 1 ? '' : 's'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Gestão da equipe médica, terapeutas e especialistas da clínica DailyAutism.
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium shadow-sm transition-colors"
        >
          <UserPlus className="h-4 w-4" />
          Novo Profissional
        </button>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-xl border border-gray-200 p-4 flex items-center justify-between gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="h-4 w-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, conselho ou CPF..."
            className="w-full pl-9 pr-4 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
          />
        </div>
        {search && (
          <button
            onClick={() => setSearch('')}
            className="text-xs text-gray-500 hover:text-gray-800 underline"
          >
            Limpar busca
          </button>
        )}
      </div>

      {/* Error Alert */}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-center gap-3 text-red-700 text-sm">
          <AlertCircle className="h-5 w-5 flex-shrink-0" />
          <span>{error}</span>
          <button
            onClick={fetchProfissionais}
            className="ml-auto font-semibold hover:underline text-xs"
          >
            Tentar novamente
          </button>
        </div>
      )}

      {/* Content Table / Loading / Empty */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
        {loading ? (
          <div className="py-16 text-center text-gray-500 flex flex-col items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-indigo-600 mb-2" />
            <span className="text-sm">Carregando profissionais...</span>
          </div>
        ) : filteredProfissionais.length === 0 ? (
          <div className="py-16 text-center text-gray-500 flex flex-col items-center justify-center px-4">
            <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-3">
              <Users className="h-6 w-6 text-gray-400" />
            </div>
            <h3 className="text-base font-semibold text-gray-800">Nenhum profissional encontrado</h3>
            <p className="text-sm text-gray-500 mt-1 max-w-sm">
              {search
                ? 'Tente ajustar os termos da sua busca para encontrar o profissional desejado.'
                : 'Cadastre o primeiro médico ou terapeuta da clínica clicando em "Novo Profissional".'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-600">
              <thead className="bg-gray-50 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500">
                <tr>
                  <th className="px-6 py-3.5">Profissional</th>
                  <th className="px-6 py-3.5">Conselho</th>
                  <th className="px-6 py-3.5">Contato</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredProfissionais.map((prof) => {
                  const initials = prof.nomeProfissional
                    ? prof.nomeProfissional
                        .split(' ')
                        .map((n) => n[0])
                        .slice(0, 2)
                        .join('')
                        .toUpperCase()
                    : 'PR';

                  return (
                    <tr key={prof.idProf} className="hover:bg-gray-50/70 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-indigo-100 text-indigo-700 font-bold flex items-center justify-center text-xs flex-shrink-0">
                            {initials}
                          </div>
                          <div>
                            <span className="font-semibold text-gray-900 block">
                              {prof.nomeProfissional}
                            </span>
                            {prof.cpf && (
                              <span className="text-xs text-gray-400 block">
                                CPF: {prof.cpf}
                              </span>
                            )}
                          </div>
                        </div>
                      </td>

                      <td className="px-6 py-4">
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-200">
                          <Award className="h-3.5 w-3.5 text-indigo-500" />
                          {prof.conselhoProf || 'Não informado'}
                        </span>
                      </td>

                      <td className="px-6 py-4">
                        <div className="space-y-1 text-xs">
                          {prof.email && (
                            <div className="flex items-center gap-1.5 text-gray-600">
                              <Mail className="h-3.5 w-3.5 text-gray-400" />
                              <span>{prof.email}</span>
                            </div>
                          )}
                          {prof.telefone && (
                            <div className="flex items-center gap-1.5 text-gray-600">
                              <Phone className="h-3.5 w-3.5 text-gray-400" />
                              <span>{prof.telefone}</span>
                            </div>
                          )}
                          {!prof.email && !prof.telefone && (
                            <span className="text-gray-400 italic">Sem contato cadastrado</span>
                          )}
                        </div>
                      </td>

                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={() => handleDelete(prof.idProf, prof.nomeProfissional)}
                          disabled={deletingId === prof.idProf}
                          className="text-gray-400 hover:text-red-600 p-1.5 rounded-lg hover:bg-red-50 transition-colors disabled:opacity-50"
                          title="Excluir profissional"
                        >
                          {deletingId === prof.idProf ? (
                            <Loader2 className="h-4 w-4 animate-spin text-red-600" />
                          ) : (
                            <Trash2 className="h-4 w-4" />
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

      {/* Modal */}
      <ProfissionalModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleCreated}
      />
    </div>
  );
}
