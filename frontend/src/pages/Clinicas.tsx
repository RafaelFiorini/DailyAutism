import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Building2,
  Plus,
  Search,
  Loader2,
  AlertCircle,
  Pencil,
  Trash2,
  ShieldAlert,
  Phone,
  Mail,
  MapPin,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { clinicaService } from '../services/clinicaService';
import type { Clinica } from '../types/clinica';
import ClinicaModal from '../components/ClinicaModal';

export default function Clinicas() {
  const { isAdmin } = useAuth();

  const [clinicas, setClinicas] = useState<Clinica[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingClinica, setEditingClinica] = useState<Clinica | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchClinicas = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await clinicaService.list();
      setClinicas(data);
    } catch {
      setError('Não foi possível carregar a lista de clínicas.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAdmin) {
      fetchClinicas();
    }
  }, [isAdmin, fetchClinicas]);

  const formatCnpj = (cnpj?: string | null) => {
    if (!cnpj) return '-';
    const c = cnpj.replace(/\D/g, '');
    if (c.length !== 14) return cnpj;
    return `${c.slice(0, 2)}.${c.slice(2, 5)}.${c.slice(5, 8)}/${c.slice(8, 12)}-${c.slice(12)}`;
  };

  const filteredClinicas = useMemo(() => {
    if (!search.trim()) return clinicas;
    const term = search.toLowerCase();
    return clinicas.filter((c) => {
      const matchNome = (c.nomeClinica || '').toLowerCase().includes(term);
      const matchCnpj = (c.cnpjClinica || '').includes(term);
      const matchEmail = (c.emailClinica || '').toLowerCase().includes(term);
      const matchTel = (c.telefoneClinica || '').includes(term) || (c.whatsappClinica || '').includes(term);
      const matchEnd = (c.enderecoClinica || '').toLowerCase().includes(term);
      return matchNome || matchCnpj || matchEmail || matchTel || matchEnd;
    });
  }, [clinicas, search]);

  const handleSaved = (saved: Clinica) => {
    setClinicas((prev) => {
      const idx = prev.findIndex((c) => c.idClinica === saved.idClinica);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = saved;
        return copy;
      }
      return [saved, ...prev];
    });
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja realmente desativar a clínica "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await clinicaService.delete(id);
      setClinicas((prev) => prev.filter((c) => c.idClinica !== id));
    } catch {
      alert('Erro ao excluir clínica.');
    } finally {
      setDeletingId(null);
    }
  };

  if (!isAdmin) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-lg mx-auto mt-10">
        <ShieldAlert className="h-12 w-12 text-amber-500 mx-auto mb-3" />
        <h2 className="text-xl font-bold text-gray-900">Acesso Restrito</h2>
        <p className="text-sm text-gray-500 mt-2">
          A área de gestão de clínicas é exclusiva para Administradores do sistema DailyAutism.
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
            <Building2 className="h-7 w-7 text-indigo-600" />
            Gestão de Clínicas
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Cadastre e gerencie as unidades de atendimento credenciadas.
          </p>
        </div>

        <button
          onClick={() => {
            setEditingClinica(null);
            setIsModalOpen(true);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg text-sm font-semibold hover:bg-indigo-700 transition-colors shadow-sm self-start sm:self-auto"
        >
          <Plus className="h-4 w-4" />
          Nova Clínica
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl p-5 border border-gray-200/80 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-indigo-50 rounded-lg text-indigo-600">
            <Building2 className="h-6 w-6" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Total de Clínicas
            </div>
            <div className="text-2xl font-bold text-gray-900 mt-0.5">
              {clinicas.length}
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
              placeholder="Buscar por nome, CNPJ, e-mail, telefone..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all placeholder:text-gray-400"
            />
          </div>
          {search && (
            <span className="text-xs text-gray-500">
              {filteredClinicas.length} de {clinicas.length} clínica(s)
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
                onClick={fetchClinicas}
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
            <p className="text-sm text-gray-500">Carregando clínicas...</p>
          </div>
        ) : filteredClinicas.length === 0 ? (
          <div className="p-12 text-center">
            <Building2 className="h-10 w-10 text-gray-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Nenhuma clínica encontrada</p>
            <p className="text-xs text-gray-400 mt-1">
              {search
                ? 'Nenhum resultado corresponde à sua pesquisa.'
                : 'Cadastre a primeira clínica clicando no botão acima.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-3.5">Clínica</th>
                  <th className="px-6 py-3.5">CNPJ</th>
                  <th className="px-6 py-3.5">Contato</th>
                  <th className="px-6 py-3.5">Endereço</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredClinicas.map((clinica) => {
                  const initials = (clinica.nomeClinica || 'C')
                    .split(' ')
                    .map((n) => n[0])
                    .slice(0, 2)
                    .join('')
                    .toUpperCase();

                  return (
                    <tr key={clinica.idClinica} className="hover:bg-gray-50/60 transition-colors">
                      {/* Nome */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-teal-100 text-teal-800 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            {initials}
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900">
                              {clinica.nomeClinica}
                            </div>
                            {clinica.emailClinica && (
                              <div className="text-xs text-gray-500 flex items-center gap-1 mt-0.5">
                                <Mail className="h-3 w-3 text-gray-400" />
                                {clinica.emailClinica}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* CNPJ */}
                      <td className="px-6 py-4 font-mono text-xs text-gray-700">
                        {formatCnpj(clinica.cnpjClinica)}
                      </td>

                      {/* Contato */}
                      <td className="px-6 py-4 text-xs text-gray-600">
                        {clinica.telefoneClinica || clinica.whatsappClinica ? (
                          <div className="space-y-0.5">
                            {clinica.telefoneClinica && (
                              <div className="flex items-center gap-1">
                                <Phone className="h-3 w-3 text-gray-400" />
                                <span>{clinica.telefoneClinica}</span>
                              </div>
                            )}
                            {clinica.whatsappClinica && (
                              <div className="flex items-center gap-1 text-emerald-600">
                                <Phone className="h-3 w-3" />
                                <span>Zap: {clinica.whatsappClinica}</span>
                              </div>
                            )}
                          </div>
                        ) : (
                          <span className="text-gray-400">-</span>
                        )}
                      </td>

                      {/* Endereço */}
                      <td className="px-6 py-4 text-xs text-gray-600 max-w-xs truncate">
                        {clinica.enderecoClinica ? (
                          <div className="flex items-center gap-1" title={clinica.enderecoClinica}>
                            <MapPin className="h-3 w-3 text-gray-400 shrink-0" />
                            <span className="truncate">{clinica.enderecoClinica}</span>
                          </div>
                        ) : (
                          <span className="text-gray-400">-</span>
                        )}
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => {
                              setEditingClinica(clinica);
                              setIsModalOpen(true);
                            }}
                            className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                            title="Editar clínica"
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(clinica.idClinica, clinica.nomeClinica)}
                            disabled={deletingId === clinica.idClinica}
                            className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                            title="Desativar clínica"
                          >
                            {deletingId === clinica.idClinica ? (
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

      {/* Modal */}
      <ClinicaModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleSaved}
        initialData={editingClinica}
      />
    </div>
  );
}
