import { useState, useEffect, FormEvent } from 'react';
import {
  X,
  User as UserIcon,
  Mail,
  Lock,
  CreditCard,
  Phone,
  Calendar,
  MapPin,
  AlertCircle,
  Loader2,
  Users,
  Stethoscope,
  ShieldCheck,
  Info,
} from 'lucide-react';
import { userService } from '../services/userService';
import type { User, SaveUserInput } from '../types/user';

interface UserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (user: User) => void;
  initialData?: User | null;
}

export default function UserModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
}: UserModalProps) {
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmSenha, setConfirmSenha] = useState('');
  const [telefone, setTelefone] = useState('');
  const [celular, setCelular] = useState('');
  const [rg, setRg] = useState('');
  const [datanasc, setDatanasc] = useState('');
  const [endereco, setEndereco] = useState('');

  // Papéis de acesso
  const [isResponsavel, setIsResponsavel] = useState(false);
  const [isProfissional, setIsProfissional] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [conselhoProf, setConselhoProf] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const formatCpf = (value: string) => {
    const raw = value.replace(/\D/g, '').slice(0, 11);
    if (raw.length <= 3) return raw;
    if (raw.length <= 6) return `${raw.slice(0, 3)}.${raw.slice(3)}`;
    if (raw.length <= 9) return `${raw.slice(0, 3)}.${raw.slice(3, 6)}.${raw.slice(6)}`;
    return `${raw.slice(0, 3)}.${raw.slice(3, 6)}.${raw.slice(6, 9)}-${raw.slice(9)}`;
  };

  useEffect(() => {
    if (initialData) {
      setNome(initialData.nomePessoa || initialData.nomeUser || '');
      setCpf(initialData.cpf ? formatCpf(initialData.cpf) : '');
      setEmail(initialData.emailUser || '');
      setSenha('');
      setConfirmSenha('');
      setTelefone(initialData.telefone || '');
      setCelular(initialData.celular || '');
      setRg(initialData.rg || '');
      setDatanasc(initialData.datanasc || '');
      setEndereco(initialData.endereco || '');

      setIsResponsavel(Boolean(initialData.isResponsavel));
      setIsProfissional(Boolean(initialData.isProfissional));
      setIsAdmin(Boolean(initialData.isAdmin));
      setConselhoProf(initialData.conselhoProf || '');
    } else {
      setNome('');
      setCpf('');
      setEmail('');
      setSenha('');
      setConfirmSenha('');
      setTelefone('');
      setCelular('');
      setRg('');
      setDatanasc('');
      setEndereco('');

      setIsResponsavel(true); // padrão responsável
      setIsProfissional(false);
      setIsAdmin(false);
      setConselhoProf('');
    }
    setError('');
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!nome.trim()) {
      setError('Informe o nome completo.');
      return;
    }

    const cleanCpf = cpf.replace(/\D/g, '');
    if (cleanCpf.length !== 11) {
      setError('CPF inválido. Deve conter 11 dígitos.');
      return;
    }

    if (!email.trim() || !email.includes('@')) {
      setError('Informe um e-mail válido.');
      return;
    }

    if (!initialData && !senha.trim()) {
      setError('Informe uma senha inicial para o usuário.');
      return;
    }

    if (senha) {
      if (senha.length < 6) {
        setError('A senha deve ter no mínimo 6 caracteres.');
        return;
      }
      if (senha !== confirmSenha) {
        setError('As senhas não coincidem.');
        return;
      }
    }

    if (isProfissional && !conselhoProf.trim()) {
      setError('Informe o registro no conselho do profissional (ex: CRM, CRP, CREFITO).');
      return;
    }

    if (!isResponsavel && !isProfissional && !isAdmin) {
      setError('Selecione pelo menos um papel de acesso (Responsável, Profissional ou Administrador).');
      return;
    }

    setLoading(true);
    try {
      const payload: SaveUserInput = {
        nome: nome.trim(),
        email: email.trim().toLowerCase(),
        senha: senha.trim() || undefined,
        cpf: cleanCpf,
        telefone: telefone.replace(/\D/g, '') || undefined,
        celular: celular.replace(/\D/g, '') || undefined,
        rg: rg.trim() || undefined,
        datanasc: datanasc || undefined,
        endereco: endereco.trim() || undefined,
        isResponsavel,
        isProfissional,
        conselhoProf: isProfissional ? conselhoProf.trim() : undefined,
        isAdmin,
      };

      let savedUser: User;
      if (initialData?.idUser) {
        savedUser = await userService.update(initialData.idUser, payload);
      } else {
        savedUser = await userService.create(payload);
      }

      onSuccess(savedUser);
      onClose();
    } catch (err: any) {
      const msg =
        err?.response?.data?.errorMessage ||
        err?.response?.data?.message ||
        'Não foi possível salvar o usuário. Verifique os dados e tente novamente.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-gradient-to-r from-indigo-50 to-blue-50">
          <div>
            <h2 className="text-xl font-bold text-gray-900">
              {initialData ? 'Editar Usuário' : 'Cadastrar Novo Usuário'}
            </h2>
            <p className="text-xs text-gray-500 mt-0.5">
              Defina os dados pessoais e os papéis de acesso no sistema DailyAutism.
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-white/80 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          {error && (
            <div className="p-3.5 rounded-lg bg-red-50 border border-red-200 flex items-start gap-2.5 text-red-700 text-sm">
              <AlertCircle className="h-5 w-5 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form id="user-form" onSubmit={handleSubmit} className="space-y-6">
            {/* Papéis de Acesso */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <label className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                  Papéis e Níveis de Acesso *
                </label>
                <span className="text-[11px] text-indigo-600 flex items-center gap-1">
                  <Info className="h-3.5 w-3.5" />
                  Pode ser Responsável e Profissional ao mesmo tempo
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {/* Responsável */}
                <label
                  className={`p-3.5 rounded-xl border flex flex-col justify-between cursor-pointer transition-all ${
                    isResponsavel
                      ? 'border-indigo-600 bg-indigo-50/70 ring-2 ring-indigo-500/20'
                      : 'border-gray-200 hover:border-gray-300 bg-white'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="p-2 rounded-lg bg-indigo-100 text-indigo-700">
                      <Users className="h-4 w-4" />
                    </div>
                    <input
                      type="checkbox"
                      checked={isResponsavel}
                      onChange={(e) => setIsResponsavel(e.target.checked)}
                      className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                    />
                  </div>
                  <div className="mt-2.5">
                    <span className="font-semibold text-gray-900 text-xs block">
                      Responsável
                    </span>
                    <span className="text-[11px] text-gray-500 block mt-0.5">
                      Pai, mãe ou tutor legal
                    </span>
                  </div>
                </label>

                {/* Profissional */}
                <label
                  className={`p-3.5 rounded-xl border flex flex-col justify-between cursor-pointer transition-all ${
                    isProfissional
                      ? 'border-indigo-600 bg-indigo-50/70 ring-2 ring-indigo-500/20'
                      : 'border-gray-200 hover:border-gray-300 bg-white'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="p-2 rounded-lg bg-emerald-100 text-emerald-700">
                      <Stethoscope className="h-4 w-4" />
                    </div>
                    <input
                      type="checkbox"
                      checked={isProfissional}
                      onChange={(e) => setIsProfissional(e.target.checked)}
                      className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                    />
                  </div>
                  <div className="mt-2.5">
                    <span className="font-semibold text-gray-900 text-xs block">
                      Profissional
                    </span>
                    <span className="text-[11px] text-gray-500 block mt-0.5">
                      Terapeuta, médico, etc.
                    </span>
                  </div>
                </label>

                {/* Administrador */}
                <label
                  className={`p-3.5 rounded-xl border flex flex-col justify-between cursor-pointer transition-all ${
                    isAdmin
                      ? 'border-indigo-600 bg-indigo-50/70 ring-2 ring-indigo-500/20'
                      : 'border-gray-200 hover:border-gray-300 bg-white'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="p-2 rounded-lg bg-purple-100 text-purple-700">
                      <ShieldCheck className="h-4 w-4" />
                    </div>
                    <input
                      type="checkbox"
                      checked={isAdmin}
                      onChange={(e) => setIsAdmin(e.target.checked)}
                      className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                    />
                  </div>
                  <div className="mt-2.5">
                    <span className="font-semibold text-gray-900 text-xs block">
                      Administrador
                    </span>
                    <span className="text-[11px] text-gray-500 block mt-0.5">
                      Acesso total ao sistema
                    </span>
                  </div>
                </label>
              </div>

              {/* Registro no Conselho (exibido se for profissional) */}
              {isProfissional && (
                <div className="p-3.5 bg-emerald-50/60 rounded-xl border border-emerald-200 mt-2">
                  <label className="block text-xs font-semibold text-emerald-900 mb-1">
                    Registro no Conselho Profissional *
                  </label>
                  <div className="relative">
                    <Stethoscope className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-emerald-600" />
                    <input
                      type="text"
                      required={isProfissional}
                      value={conselhoProf}
                      onChange={(e) => setConselhoProf(e.target.value)}
                      placeholder="Ex: CRP 06/12345, CRM 998877, CREFITO 1234"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-emerald-300 rounded-lg bg-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Dados Pessoais */}
            <div className="space-y-4 pt-4 border-t border-gray-100">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">
                Dados Pessoais
              </h3>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Nome Completo *
                </label>
                <div className="relative">
                  <UserIcon className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    required
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    placeholder="Ex: João da Silva"
                    className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    CPF *
                  </label>
                  <div className="relative">
                    <CreditCard className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      required
                      value={cpf}
                      onChange={(e) => setCpf(formatCpf(e.target.value))}
                      placeholder="000.000.000-00"
                      maxLength={14}
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    RG
                  </label>
                  <input
                    type="text"
                    value={rg}
                    onChange={(e) => setRg(e.target.value)}
                    placeholder="Número do RG"
                    className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Data de Nascimento
                  </label>
                  <div className="relative">
                    <Calendar className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="date"
                      value={datanasc}
                      onChange={(e) => setDatanasc(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Celular / WhatsApp
                  </label>
                  <div className="relative">
                    <Phone className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="tel"
                      value={celular}
                      onChange={(e) => setCelular(e.target.value)}
                      placeholder="(00) 00000-0000"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Telefone Fixo
                  </label>
                  <div className="relative">
                    <Phone className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="tel"
                      value={telefone}
                      onChange={(e) => setTelefone(e.target.value)}
                      placeholder="(00) 0000-0000"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Endereço Completo
                </label>
                <div className="relative">
                  <MapPin className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    value={endereco}
                    onChange={(e) => setEndereco(e.target.value)}
                    placeholder="Rua, número, complemento, bairro, cidade - UF"
                    className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>
            </div>

            {/* Dados de Acesso */}
            <div className="space-y-4 pt-4 border-t border-gray-100">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">
                Dados de Acesso
              </h3>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  E-mail de Login *
                </label>
                <div className="relative">
                  <Mail className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="usuario@dailyautism.com"
                    className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    {initialData ? 'Nova Senha (opcional)' : 'Senha de Acesso *'}
                  </label>
                  <div className="relative">
                    <Lock className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="password"
                      required={!initialData}
                      value={senha}
                      onChange={(e) => setSenha(e.target.value)}
                      placeholder={initialData ? 'Manter senha atual' : '••••••••'}
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Confirmar Senha {initialData && '(se alterada)'}
                  </label>
                  <div className="relative">
                    <Lock className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="password"
                      required={Boolean(senha)}
                      value={confirmSenha}
                      onChange={(e) => setConfirmSenha(e.target.value)}
                      placeholder="••••••••"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-100 bg-gray-50 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-4 py-2 text-xs font-semibold text-gray-700 hover:bg-gray-200 rounded-lg transition-colors"
          >
            Cancelar
          </button>
          <button
            type="submit"
            form="user-form"
            disabled={loading}
            className="inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 rounded-lg shadow-sm transition-colors"
          >
            {loading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Salvando...
              </>
            ) : initialData ? (
              'Atualizar Usuário'
            ) : (
              'Salvar Usuário'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
