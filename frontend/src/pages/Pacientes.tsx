import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  HeartPulse,
  UserPlus,
  Search,
  Loader2,
  AlertCircle,
  Pencil,
  Trash2,
  UserCheck,
  CreditCard,
  Building2,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { pacienteService } from '../services/pacienteService';
import type { Paciente } from '../types/paciente';
import PacienteModal from '../components/PacienteModal';

export default function Pacientes() {
  const { isAdmin, isResponsavel, isProfissional } = useAuth();
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPaciente, setEditingPaciente] = useState<Paciente | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchPacientes = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await pacienteService.list();
      setPacientes(data);
    } catch {
      setError('Não foi possível carregar a lista de pacientes.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPacientes();
  }, [fetchPacientes]);

  const filteredPacientes = useMemo(() => {
    if (!search.trim()) return pacientes;
    const term = search.toLowerCase();
    return pacientes.filter((p) => {
      const matchNome = p.nomePessoa?.toLowerCase().includes(term);
      const matchApelido = p.apelidoPaciente?.toLowerCase().includes(term);
      const matchCpf = p.cpf?.includes(term);
      const matchEmail = p.email?.toLowerCase().includes(term);
      const matchResp = p.responsaveis?.some((r) => r.toLowerCase().includes(term));
      const matchPlano = p.planos?.some((pl) => pl.toLowerCase().includes(term));
      const matchClinica = p.clinicas?.some((c) => c.toLowerCase().includes(term));
      return matchNome || matchApelido || matchCpf || matchEmail || matchResp || matchPlano || matchClinica;
    });
  }, [pacientes, search]);

  const handleSaved = (item: Paciente) => {
    setPacientes((prev) => {
      const index = prev.findIndex((p) => p.idPaciente === item.idPaciente);
      if (index >= 0) {
        const copy = [...prev];
        copy[index] = item;
        return copy;
      }
      return [item, ...prev];
    });
    fetchPacientes();
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja desativar o paciente "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await pacienteService.delete(id);
      setPacientes((prev) => prev.filter((p) => p.idPaciente !== id));
    } catch {
      alert('Erro ao excluir paciente.');
    } finally {
      setDeletingId(null);
    }
  };

  const formatCpf = (cpf?: string) => {
    if (!cpf) return '-';
    const c = cpf.replace(/\D/g, '');
    if (c.length !== 11) return cpf;
    return `${c.slice(0, 3)}.${c.slice(3, 6)}.${c.slice(6, 9)}-${c.slice(9)}`;
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">
              {isResponsavel
                ? 'Meus Pacientes'
                : isProfissional
                ? 'Pacientes em Atendimento'
                : 'Pacientes'}
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {pacientes.length} cadastrado{pacientes.length === 1 ? '' : 's'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            {isResponsavel
              ? 'Pacientes sob sua responsabilidade e acompanhamento.'
              : isProfissional
              ? 'Pacientes com agendamentos e consultas vinculados a você.'
              : 'Gestão dos pacientes da clínica, vínculos com responsáveis, planos de saúde e clínicas.'}
          </p>
        </div>

        {(isAdmin || isResponsavel) && (
          <button
            onClick={() => {
              setEditingPaciente(null);
              setIsModalOpen(true);
            }}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold transition-colors shadow-sm self-start sm:self-auto"
          >
            <UserPlus className="h-4 w-4" />
            Novo Paciente
          </button>
        )}
      </div>

      {/* Search Bar */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="relative">
          <Search className="h-5 w-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, apelido, CPF, responsável, plano ou clínica..."
            className="w-full pl-11 pr-4 py-2.5 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
          />
        </div>
      </div>

      {/* Table Container */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center">
            <Loader2 className="h-8 w-8 animate-spin text-indigo-600 mx-auto" />
            <p className="text-sm text-gray-500 mt-2">Carregando pacientes...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center">
            <AlertCircle className="h-8 w-8 text-red-500 mx-auto" />
            <p className="text-sm text-red-600 mt-2">{error}</p>
            <button
              onClick={fetchPacientes}
              className="mt-3 text-xs font-semibold text-indigo-600 hover:text-indigo-700"
            >
              Tentar novamente
            </button>
          </div>
        ) : filteredPacientes.length === 0 ? (
          <div className="p-12 text-center">
            <HeartPulse className="h-10 w-10 text-gray-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Nenhum paciente encontrado</p>
            <p className="text-xs text-gray-400 mt-1">
              {search
                ? 'Nenhum resultado corresponde à sua pesquisa.'
                : 'Cadastre o primeiro paciente clicando no botão acima.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-3.5">Paciente</th>
                  <th className="px-6 py-3.5">Responsáveis</th>
                  <th className="px-6 py-3.5">Plano de Saúde</th>
                  <th className="px-6 py-3.5">Clínica</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredPacientes.map((pac) => {
                  const initials = pac.nomePessoa
                    ? pac.nomePessoa
                        .split(' ')
                        .map((n) => n[0])
                        .slice(0, 2)
                        .join('')
                        .toUpperCase()
                    : 'PC';

                  return (
                    <tr key={pac.idPaciente} className="hover:bg-gray-50/60 transition-colors">
                      {/* Nome + Avatar + Badge Responsável */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-rose-100 text-rose-700 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            {initials}
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900 flex items-center gap-2">
                              <span>{pac.nomePessoa}</span>
                              {pac.apelidoPaciente && (
                                <span className="text-xs text-gray-500 font-normal">
                                  ({pac.apelidoPaciente})
                                </span>
                              )}
                              {pac.isResponsavel && (
                                <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-blue-50 text-blue-700 border border-blue-200">
                                  <UserCheck className="h-3 w-3" />
                                  É o Responsável
                                </span>
                              )}
                            </div>
                            <div className="text-xs text-gray-500 mt-0.5">
                              CPF: {formatCpf(pac.cpf)}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Responsáveis */}
                      <td className="px-6 py-4">
                        {pac.responsaveis && pac.responsaveis.length > 0 ? (
                          <div className="flex flex-wrap gap-1">
                            {pac.responsaveis.map((resp, i) => (
                              <span
                                key={i}
                                className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-gray-100 text-gray-800 border border-gray-200"
                              >
                                {resp}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">
                            {pac.isResponsavel ? 'Próprio paciente' : 'Nenhum responsável'}
                          </span>
                        )}
                      </td>

                      {/* Plano de Saúde */}
                      <td className="px-6 py-4">
                        {pac.planos && pac.planos.length > 0 ? (
                          <div className="flex flex-wrap gap-1">
                            {pac.planos.map((pl, i) => (
                              <span
                                key={i}
                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200"
                              >
                                <CreditCard className="h-3 w-3" />
                                {pl}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">Particular</span>
                        )}
                      </td>

                      {/* Clínica */}
                      <td className="px-6 py-4">
                        {pac.clinicas && pac.clinicas.length > 0 ? (
                          <div className="flex flex-wrap gap-1">
                            {pac.clinicas.map((c, i) => (
                              <span
                                key={i}
                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-purple-50 text-purple-700 border border-purple-200"
                              >
                                <Building2 className="h-3 w-3" />
                                {c}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">Geral / Não vinculada</span>
                        )}
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() => {
                              setEditingPaciente(pac);
                              setIsModalOpen(true);
                            }}
                            className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                            title="Editar Paciente"
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          {isAdmin && (
                            <button
                              onClick={() => handleDelete(pac.idPaciente, pac.nomePessoa)}
                              disabled={deletingId === pac.idPaciente}
                              className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                              title="Excluir Paciente"
                            >
                              {deletingId === pac.idPaciente ? (
                                <Loader2 className="h-4 w-4 animate-spin text-red-600" />
                              ) : (
                                <Trash2 className="h-4 w-4" />
                              )}
                            </button>
                          )}
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

      {/* Modal de Cadastro/Edição */}
      <PacienteModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingPaciente(null);
        }}
        onSuccess={handleSaved}
        initialData={editingPaciente}
      />
    </div>
  );
}
