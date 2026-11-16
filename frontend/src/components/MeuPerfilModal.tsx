import { useState, useEffect, FormEvent } from 'react';
import {
  X,
  Lock,
  User,
  Phone,
  MapPin,
  AlertCircle,
  Loader2,
  CheckCircle2,
  Eye,
  EyeOff,
  ShieldCheck,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { authService } from '../services/authService';

interface MeuPerfilModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function MeuPerfilModal({ isOpen, onClose }: MeuPerfilModalProps) {
  const { user } = useAuth();

  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [telefone, setTelefone] = useState('');
  const [celular, setCelular] = useState('');
  const [endereco, setEndereco] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (user && isOpen) {
      setTelefone(user.telefone || '');
      setCelular(user.celular || '');
      setEndereco(user.endereco || '');
      setNovaSenha('');
      setConfirmarSenha('');
      setError('');
      setSuccess(false);
    }
  }, [user, isOpen]);

  if (!isOpen || !user) return null;

  const handleSalvar = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (novaSenha) {
      if (novaSenha.length < 6) {
        setError('A nova senha deve ter no mínimo 6 caracteres.');
        return;
      }
      if (novaSenha !== confirmarSenha) {
        setError('As senhas não coincidem.');
        return;
      }
    }

    setLoading(true);
    try {
      await authService.updateMe({
        novaSenha: novaSenha.trim() || undefined,
        telefone: telefone.replace(/\D/g, '') || undefined,
        celular: celular.replace(/\D/g, '') || undefined,
        endereco: endereco.trim() || undefined,
      });

      setSuccess(true);
      setTimeout(() => {
        onClose();
      }, 1800);
    } catch (err: any) {
      const msg = err.response?.data?.errorMessage || err.message || 'Erro ao atualizar dados.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleFormatCpf = (val?: string) => {
    if (!val) return 'Não informado';
    const raw = val.replace(/\D/g, '').slice(0, 11);
    if (raw.length <= 3) return raw;
    if (raw.length <= 6) return `${raw.slice(0, 3)}.${raw.slice(3)}`;
    if (raw.length <= 9) return `${raw.slice(0, 3)}.${raw.slice(3, 6)}.${raw.slice(6)}`;
    return `${raw.slice(0, 3)}.${raw.slice(3, 6)}.${raw.slice(6, 9)}-${raw.slice(9)}`;
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center p-4">
      <div className="relative bg-white rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-indigo-100 text-indigo-700 rounded-lg">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900">Meu Perfil & Senha</h3>
              <p className="text-xs text-gray-500">
                Atualize sua senha, telefones ou endereço de contato
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 rounded-lg p-1.5 hover:bg-gray-100 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 overflow-y-auto flex-1">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-700 text-sm">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {success ? (
            <div className="py-8 text-center">
              <div className="inline-flex items-center justify-center w-14 h-14 bg-green-100 text-green-600 rounded-full mb-3">
                <CheckCircle2 className="h-8 w-8" />
              </div>
              <h4 className="text-lg font-bold text-gray-900">Dados Salvos com Sucesso!</h4>
              <p className="text-sm text-gray-600 mt-1">
                Suas informações cadastrais foram atualizadas.
              </p>
            </div>
          ) : (
            <form onSubmit={handleSalvar} className="space-y-4">
              {/* Titular */}
              <div className="p-3 bg-indigo-50 border border-indigo-100 rounded-lg flex items-center gap-3">
                <div className="p-2 bg-indigo-200 text-indigo-800 rounded-full">
                  <User className="h-4 w-4" />
                </div>
                <div>
                  <p className="text-xs text-indigo-700 font-medium">Usuário logado:</p>
                  <p className="text-sm font-bold text-indigo-950">{user.nomeUser}</p>
                </div>
              </div>

              {/* Campos Bloqueados: E-mail e CPF */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1 flex items-center justify-between">
                    <span>E-mail</span>
                    <span className="text-[10px] text-amber-700 flex items-center gap-0.5">
                      <Lock className="h-3 w-3" /> Bloqueado
                    </span>
                  </label>
                  <input
                    type="text"
                    disabled
                    value={user.emailUser}
                    className="block w-full px-3 py-2 bg-gray-100 border border-gray-200 rounded-lg text-sm text-gray-600 cursor-not-allowed select-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1 flex items-center justify-between">
                    <span>CPF</span>
                    <span className="text-[10px] text-amber-700 flex items-center gap-0.5">
                      <Lock className="h-3 w-3" /> Bloqueado
                    </span>
                  </label>
                  <input
                    type="text"
                    disabled
                    value={handleFormatCpf(user.cpf || undefined)}
                    className="block w-full px-3 py-2 bg-gray-100 border border-gray-200 rounded-lg text-sm text-gray-600 cursor-not-allowed select-none"
                  />
                </div>
              </div>

              {/* Redefinição de Senha */}
              <div className="pt-2 border-t border-gray-100">
                <h4 className="text-xs font-bold text-gray-800 uppercase tracking-wider mb-2">
                  Alterar Senha
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Nova Senha (mín. 6)
                    </label>
                    <div className="relative">
                      <input
                        type={showPassword ? 'text' : 'password'}
                        value={novaSenha}
                        onChange={(e) => setNovaSenha(e.target.value)}
                        placeholder="••••••••"
                        className="block w-full pr-9 pl-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                      >
                        {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Confirmar Nova Senha
                    </label>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      value={confirmarSenha}
                      onChange={(e) => setConfirmarSenha(e.target.value)}
                      placeholder="••••••••"
                      className="block w-full px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    />
                  </div>
                </div>
                <p className="text-[11px] text-gray-500 mt-1">
                  Deixe em branco caso deseje manter a senha atual.
                </p>
              </div>

              {/* Telefones e Endereço */}
              <div className="pt-2 border-t border-gray-100">
                <h4 className="text-xs font-bold text-gray-800 uppercase tracking-wider mb-2">
                  Dados de Contato & Endereço
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Telefone
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                        <Phone className="h-4 w-4" />
                      </div>
                      <input
                        type="text"
                        value={telefone}
                        onChange={(e) => setTelefone(e.target.value)}
                        placeholder="(00) 0000-0000"
                        className="block w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Celular / WhatsApp
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                        <Phone className="h-4 w-4" />
                      </div>
                      <input
                        type="text"
                        value={celular}
                        onChange={(e) => setCelular(e.target.value)}
                        placeholder="(00) 90000-0000"
                        className="block w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                    </div>
                  </div>
                </div>

                <div className="mt-3">
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    Endereço Completo
                  </label>
                  <div className="relative">
                    <div className="absolute top-2.5 left-3 pointer-events-none text-gray-400">
                      <MapPin className="h-4 w-4" />
                    </div>
                    <input
                      type="text"
                      value={endereco}
                      onChange={(e) => setEndereco(e.target.value)}
                      placeholder="Rua, número, bairro, cidade - UF"
                      className="block w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    />
                  </div>
                </div>
              </div>

              {/* Botões */}
              <div className="pt-3 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 border border-gray-300 text-gray-700 hover:bg-gray-100 rounded-lg text-sm font-medium transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-medium rounded-lg text-sm transition-colors shadow-sm"
                >
                  {loading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      Salvando dados...
                    </>
                  ) : (
                    'Salvar Alterações'
                  )}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
