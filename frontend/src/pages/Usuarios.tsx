import { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Users,
  UserPlus,
  Search,
  Loader2,
  AlertCircle,
  Pencil,
  Trash2,
  ShieldAlert,
  ShieldCheck,
  Stethoscope,
  UserCheck,
  Building2,
  Phone,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { userService } from '../services/userService';
import type { User } from '../types/user';
import UserModal from '../components/UserModal';

export default function Usuarios() {
  const { isAdmin } = useAuth();

  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await userService.list();
      setUsers(data);
    } catch {
      setError('Não foi possível carregar a lista de usuários.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAdmin) {
      fetchUsers();
    }
  }, [isAdmin, fetchUsers]);

  const filteredUsers = useMemo(() => {
    if (!search.trim()) return users;
    const term = search.toLowerCase();
    return users.filter((u) => {
      const matchNome = (u.nomeUser || u.nomePessoa || '').toLowerCase().includes(term);
      const matchEmail = (u.emailUser || '').toLowerCase().includes(term);
      const matchCpf = (u.cpf || '').includes(term);
      const matchConselho = (u.conselhoProf || '').toLowerCase().includes(term);
      const matchClinica = (u.nomeClinica || '').toLowerCase().includes(term);
      const matchCargo = (u.cargoClinica || '').toLowerCase().includes(term);
      const matchPerfis = (u.perfis || []).some((p) => p.toLowerCase().includes(term));
      return matchNome || matchEmail || matchCpf || matchConselho || matchClinica || matchCargo || matchPerfis;
    });
  }, [users, search]);

  const handleSaved = (saved: User) => {
    setUsers((prev) => {
      const idx = prev.findIndex((u) => u.idUser === saved.idUser);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = saved;
        return copy;
      }
      return [saved, ...prev];
    });
  };

  const handleDelete = async (id: string, nome: string) => {
    if (!window.confirm(`Deseja realmente desativar o usuário "${nome}"?`)) return;

    setDeletingId(id);
    try {
      await userService.delete(id);
      setUsers((prev) => prev.filter((u) => u.idUser !== id));
    } catch {
      alert('Erro ao excluir usuário.');
    } finally {
      setDeletingId(null);
    }
  };

  const formatCpf = (cpf?: string | null) => {
    if (!cpf) return '-';
    const c = cpf.replace(/\D/g, '');
    if (c.length !== 11) return cpf;
    return `${c.slice(0, 3)}.${c.slice(3, 6)}.${c.slice(6, 9)}-${c.slice(9)}`;
  };

  const formatTelefone = (celular?: string | null, tel?: string | null) => {
    const raw = (celular || tel || '').replace(/\D/g, '');
    if (!raw) return '-';
    if (raw.length === 11) {
      return `(${raw.slice(0, 2)}) ${raw.slice(2, 7)}-${raw.slice(7)}`;
    }
    if (raw.length === 10) {
      return `(${raw.slice(0, 2)}) ${raw.slice(2, 6)}-${raw.slice(6)}`;
    }
    return raw;
  };

  if (!isAdmin) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-lg mx-auto mt-10">
        <ShieldAlert className="h-12 w-12 text-amber-500 mx-auto mb-3" />
        <h2 className="text-xl font-bold text-gray-900">Acesso Restrito</h2>
        <p className="text-sm text-gray-500 mt-2">
          A área de gestão de usuários é exclusiva para Administradores do sistema DailyAutism.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">Usuários</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {users.length} usuário{users.length === 1 ? '' : 's'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Gestão dos acessos ao sistema, vínculos pessoais e atribuição de papéis (Responsável e/ou Profissional).
          </p>
        </div>

        <button
          onClick={() => {
            setEditingUser(null);
            setIsModalOpen(true);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold transition-colors shadow-sm self-start sm:self-auto"
        >
          <UserPlus className="h-4 w-4" />
          Novo Usuário
        </button>
      </div>

      {/* Search */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="relative">
          <Search className="h-5 w-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, e-mail, CPF, conselho profissional ou papel..."
            className="w-full pl-11 pr-4 py-2.5 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
          />
        </div>
      </div>

      {/* Table Container */}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center">
            <Loader2 className="h-8 w-8 animate-spin text-indigo-600 mx-auto" />
            <p className="text-sm text-gray-500 mt-2">Carregando usuários...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center">
            <AlertCircle className="h-8 w-8 text-red-500 mx-auto" />
            <p className="text-sm text-red-600 mt-2">{error}</p>
            <button
              onClick={fetchUsers}
              className="mt-3 text-xs font-semibold text-indigo-600 hover:text-indigo-700"
            >
              Tentar novamente
            </button>
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-12 text-center">
            <Users className="h-10 w-10 text-gray-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Nenhum usuário encontrado</p>
            <p className="text-xs text-gray-400 mt-1">
              {search
                ? 'Nenhum resultado corresponde à sua pesquisa.'
                : 'Cadastre o primeiro usuário clicando no botão acima.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/80 border-b border-gray-200 text-xs font-semibold uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-3.5">Usuário</th>
                  <th className="px-6 py-3.5">CPF & Contato</th>
                  <th className="px-6 py-3.5">Papéis de Acesso</th>
                  <th className="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {filteredUsers.map((user) => {
                  const displayName = user.nomeUser || user.nomePessoa || 'Sem nome';
                  const initials = displayName
                    .split(' ')
                    .map((n) => n[0])
                    .slice(0, 2)
                    .join('')
                    .toUpperCase();

                  const isAdm = Boolean(user.isAdmin ?? (user as any).admin ?? user.perfis?.includes('ADMIN'));
                  const isResp = Boolean(user.isResponsavel ?? (user as any).responsavel ?? user.perfis?.includes('RESPONSAVEL'));
                  const isProf = Boolean(user.isProfissional ?? (user as any).profissional ?? user.perfis?.includes('PROFISSIONAL'));
                  const isAdmClin = Boolean(user.isAdminClinica ?? (user as any).adminClinica ?? user.perfis?.includes('ADMIN_CLINICA'));
                  const isFunc = Boolean(
                    user.isFuncionarioClinica ??
                    (user as any).funcionarioClinica ??
                    isAdmClin ??
                    user.perfis?.includes('CLINICA') ??
                    user.perfis?.includes('FUNCIONARIO_CLINICA')
                  );

                  return (
                    <tr key={user.idUser} className="hover:bg-gray-50/60 transition-colors">
                      {/* Usuário (Nome + Email) */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-indigo-100 text-indigo-700 font-bold flex items-center justify-center flex-shrink-0 text-sm">
                            {initials}
                          </div>
                          <div>
                            <div className="font-semibold text-gray-900">
                              {displayName}
                            </div>
                            <div className="text-xs text-gray-500">
                              {user.emailUser}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* CPF & Contato */}
                      <td className="px-6 py-4">
                        <div className="text-xs text-gray-700 font-medium">
                          CPF: {formatCpf(user.cpf)}
                        </div>
                        <div className="text-xs text-gray-500 mt-0.5 flex items-center gap-1">
                          <Phone className="h-3 w-3 text-gray-400" />
                          {formatTelefone(user.celular, user.telefone)}
                        </div>
                      </td>

                      {/* Papéis de Acesso */}
                      <td className="px-6 py-4">
                        <div className="flex flex-wrap gap-1.5">
                          {isAdm && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-50 text-purple-700 border border-purple-200">
                              <ShieldCheck className="h-3.5 w-3.5" />
                              Admin
                            </span>
                          )}

                          {isResp && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                              <UserCheck className="h-3.5 w-3.5" />
                              Responsável
                            </span>
                          )}

                          {isProf && (
                            <span
                              className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200"
                              title={user.conselhoProf ? `Conselho: ${user.conselhoProf}` : undefined}
                            >
                              <Stethoscope className="h-3.5 w-3.5" />
                              Profissional
                              {user.conselhoProf && (
                                <span className="text-[10px] opacity-80">
                                  ({user.conselhoProf})
                                </span>
                              )}
                            </span>
                          )}

                          {isFunc && (
                            <span
                              className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium border ${
                                isAdmClin
                                  ? 'bg-amber-50 text-amber-800 border-amber-300'
                                  : 'bg-teal-50 text-teal-700 border-teal-200'
                              }`}
                              title={
                                user.nomeClinica
                                  ? `Clínica: ${user.nomeClinica}${user.cargoClinica ? ` (${user.cargoClinica})` : ''}`
                                  : undefined
                              }
                            >
                              <Building2 className="h-3.5 w-3.5" />
                              {isAdmClin ? 'Admin Clínica' : (user.nomeClinica || 'Clínica')}
                              {user.cargoClinica && (
                                <span className="text-[10px] opacity-80">
                                  ({user.cargoClinica})
                                </span>
                              )}
                            </span>
                          )}

                          {!isAdm && !isResp && !isProf && !isFunc && (
                            <span className="text-xs text-gray-400 italic">
                              Sem papéis definidos
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Ações */}
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() => {
                              setEditingUser(user);
                              setIsModalOpen(true);
                            }}
                            className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                            title="Editar Usuário"
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(user.idUser, displayName)}
                            disabled={deletingId === user.idUser}
                            className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                            title="Desativar Usuário"
                          >
                            {deletingId === user.idUser ? (
                              <Loader2 className="h-4 w-4 animate-spin text-red-600" />
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

      {/* Modal de Usuário */}
      <UserModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingUser(null);
        }}
        onSuccess={handleSaved}
        initialData={editingUser}
      />
    </div>
  );
}
