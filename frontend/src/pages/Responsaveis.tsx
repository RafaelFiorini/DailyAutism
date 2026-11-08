import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  UserCheck,
  UserPlus,
  Search,
  Loader2,
  AlertCircle,
  Phone,
  Mail,
  Trash2,
  HeartPulse,
} from 'lucide-react';
import { responsavelService } from '../services/responsavelService';
import type { Responsavel } from '../types/responsavel';
import ResponsavelModal from '../components/ResponsavelModal';

export default function Responsaveis() {
  const [responsaveis, setResponsaveis] = useState<Responsavel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchResponsaveis = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await responsavelService.list();
      setResponsaveis(data);
    } catch {
      setError('Não foi possível carregar a lista de responsáveis.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchResponsaveis();
  }, [fetchResponsaveis]);

  const filteredResponsaveis = useMemo(() => {
    if (!search.trim()) return responsaveis;
    const term = search.toLowerCase();
    return responsaveis.filter((r) => {
      const matchNome = r.nomeResponsavel?.toLowerCase().includes(term);
      const matchCpf = r.cpf?.includes(term);
      const matchEmail = r.email?.toLowerCase().includes(term);
      const matchPaciente = r.pacientes?.some((p) =>
        p.nomePaciente?.toLowerCase().includes(term) ||
        p.apelidoPaciente?.toLowerCase().includes(term)
      );
      return matchNome || matchCpf || matchEmail || matchPaciente;
    });
  }, [responsaveis, search]);

  const handleCreated = (novo: Responsavel) => {
    setResponsaveis((prev) => [novo, ...prev]);
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja desativar o responsável "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await responsavelService.delete(id);
      setResponsaveis((prev) => prev.filter((r) => r.idResponsavel !== id));
    } catch {
      alert('Erro ao excluir responsável.');
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
            <h1 className="text-2xl font-bold text-gray-900">Responsáveis</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {responsaveis.length} cadastrado{responsaveis.length === 1 ? '' : 's'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Gestão de pais, tutores e responsáveis legais pelos pacientes.
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold transition-colors shadow-sm self-start sm:self-auto"
        >
          <UserPlus className="h-4 w-4" />
          Novo Responsável
        </button>
      </div>

      {/* Search Bar */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="relative">
          <Search className="h-5 w-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, CPF ou paciente vinculado..."
            className="w-full pl-11 pr-4 py-2.5 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
          />
        </div>
      </div>

      {/* Table Container */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center">
            <Loader2 className="h-8 w-8 animate-spin text-indigo-600 mx-auto" />
            <p className="text-sm text-gray-500 mt-2">Carregando responsáveis...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center">
            <AlertCircle className="h-8 w-8 text-red-500 mx-auto" />
            <p className="text-sm text-red-600 mt-2">{error}</p>
            <button
              onClick={fetchResponsaveis}
              className="mt-3 text-xs font-semibold text-indigo-600 hover:text-indigo-700"
            >
              Tentar novamente
            </button>
          </div>
        ) : filteredResponsaveis.length === 0 ? (
          <div className="p-12 text-center">
            <UserCheck className="h-10 w-10 text-gray-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Nenhum responsável encontrado</p>
            <p className="text-xs text-gray-400 mt-1">
              {search
                ? 'Nenhum resultado corresponde à sua pesquisa.'
                : 'Cadastre o primeiro responsável clicando no botão acima.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-3.5">Responsável</th>
                  <th className="px-6 py-3.5">Contato</th>
                  <th className="px-6 py-3.5">Pacientes Sob Cuidados</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredResponsaveis.map((resp) => {
                  const initials = resp.nomeResponsavel
                    ? resp.nomeResponsavel
                        .split(' ')
                        .map((n) => n[0])
                        .slice(0, 2)
                        .join('')
                        .toUpperCase()
                    : 'RP';

                  const formatCpf = (cpf?: string) => {
                    if (!cpf) return '-';
                    const c = cpf.replace(/\D/g, '');
                    if (c.length !== 11) return cpf;
                    return `${c.slice(0, 3)}.${c.slice(3, 6)}.${c.slice(6, 9)}-${c.slice(9)}`;
                  };

                  return (
                    <tr key={resp.idResponsavel} className="hover:bg-gray-50/60 transition-colors">
                      {/* Nome + Avatar + Badge Paciente */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-indigo-100 text-indigo-700 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            {initials}
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900 flex items-center gap-2">
                              <span>{resp.nomeResponsavel}</span>
                              {resp.isPaciente && (
                                <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                                  <HeartPulse className="h-3 w-3" />
                                  Também é Paciente
                                </span>
                              )}
                            </div>
                            <div className="text-xs text-gray-500 mt-0.5">
                              CPF: {formatCpf(resp.cpf)}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Contato */}
                      <td className="px-6 py-4">
                        <div className="space-y-1 text-xs text-gray-600">
                          {resp.email && (
                            <div className="flex items-center gap-1.5">
                              <Mail className="h-3.5 w-3.5 text-gray-400" />
                              <span>{resp.email}</span>
                            </div>
                          )}
                          {resp.telefone && (
                            <div className="flex items-center gap-1.5">
                              <Phone className="h-3.5 w-3.5 text-gray-400" />
                              <span>{resp.telefone}</span>
                            </div>
                          )}
                          {!resp.email && !resp.telefone && (
                            <span className="text-gray-400 italic">Não informado</span>
                          )}
                        </div>
                      </td>

                      {/* Pacientes Vinculados */}
                      <td className="px-6 py-4">
                        {resp.pacientes && resp.pacientes.length > 0 ? (
                          <div className="flex flex-wrap gap-1.5">
                            {resp.pacientes.map((p) => (
                              <span
                                key={p.idPaciente}
                                className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-800 border border-gray-200"
                              >
                                {p.nomePaciente}
                                {p.apelidoPaciente && (
                                  <span className="text-gray-400 ml-1">({p.apelidoPaciente})</span>
                                )}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">Nenhum paciente</span>
                        )}
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={() => handleDelete(resp.idResponsavel, resp.nomeResponsavel)}
                          disabled={deletingId === resp.idResponsavel}
                          className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                          title="Excluir Responsável"
                        >
                          {deletingId === resp.idResponsavel ? (
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

      {/* Modal de Cadastro */}
      <ResponsavelModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleCreated}
      />
    </div>
  );
}
