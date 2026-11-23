import { useState, FormEvent } from 'react';
import { ShieldAlert, Lock, Loader2, AlertCircle, CheckCircle2 } from 'lucide-react';
import { authService } from '../services/authService';
import { extractApiErrorMessage } from '../utils/apiError';

interface TrocarSenhaObrigatoriaModalProps {
  isOpen: boolean;
  onSuccess: () => Promise<void> | void;
}

export default function TrocarSenhaObrigatoriaModal({
  isOpen,
  onSuccess,
}: TrocarSenhaObrigatoriaModalProps) {
  const [senhaAtual, setSenhaAtual] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const tem8Chars = novaSenha.length >= 8;
  const temMaiuscula = /[A-Z]/.test(novaSenha);
  const temMinuscula = /[a-z]/.test(novaSenha);
  const temNumero = /\d/.test(novaSenha);
  const temSimbolo = /[^A-Za-z0-9]/.test(novaSenha);
  const senhaForteValida = tem8Chars && temMaiuscula && temMinuscula && temNumero && temSimbolo;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (!senhaAtual.trim()) {
      setError('Informe sua senha atual ou temporária.');
      return;
    }
    if (!senhaForteValida) {
      setError('A nova senha não atende a todos os critérios de segurança.');
      return;
    }
    if (novaSenha !== confirmarSenha) {
      setError('A confirmação de senha não confere.');
      return;
    }

    setSubmitting(true);
    try {
      await authService.alterarSenha(senhaAtual, novaSenha);
      setSenhaAtual('');
      setNovaSenha('');
      setConfirmarSenha('');
      await onSuccess();
    } catch (err) {
      setError(extractApiErrorMessage(err, 'Não foi possível atualizar sua senha.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-[70] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-gray-200 overflow-hidden">
        <div className="px-6 py-5 bg-indigo-50 border-b border-indigo-100 flex items-start gap-3">
          <div className="p-2 bg-indigo-100 text-indigo-700 rounded-xl shrink-0">
            <ShieldAlert className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-bold text-gray-900">Defina sua nova senha de acesso</h2>
            <p className="text-xs text-gray-600 mt-0.5">
              Por segurança, é obrigatório substituir a senha temporária antes de continuar usando o sistema.
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">Senha Atual / Temporária</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
              <input
                type="password"
                required
                value={senhaAtual}
                onChange={(e) => setSenhaAtual(e.target.value)}
                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                placeholder="Digite sua senha atual"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">Nova Senha</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
              <input
                type="password"
                required
                value={novaSenha}
                onChange={(e) => setNovaSenha(e.target.value)}
                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                placeholder="Crie uma senha forte"
              />
            </div>

            <div className="mt-2 grid grid-cols-2 gap-1 text-[11px]">
              {[
                { ok: tem8Chars, label: 'Mínimo 8 caracteres' },
                { ok: temMaiuscula, label: '1 letra maiúscula' },
                { ok: temMinuscula, label: '1 letra minúscula' },
                { ok: temNumero, label: '1 número' },
                { ok: temSimbolo, label: '1 símbolo (@, #, !, $)' },
              ].map((regra) => (
                <div
                  key={regra.label}
                  className={`flex items-center gap-1 ${regra.ok ? 'text-emerald-600 font-medium' : 'text-gray-400'}`}
                >
                  <CheckCircle2 className="w-3 h-3 shrink-0" />
                  <span>{regra.label}</span>
                </div>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">Confirmar Nova Senha</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
              <input
                type="password"
                required
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                placeholder="Repita a nova senha"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting || !senhaForteValida}
            className="w-full inline-flex items-center justify-center gap-2 py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-300 text-white text-sm font-semibold rounded-lg transition-colors shadow-sm"
          >
            {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
            Salvar Nova Senha e Continuar
          </button>
        </form>
      </div>
    </div>
  );
}
