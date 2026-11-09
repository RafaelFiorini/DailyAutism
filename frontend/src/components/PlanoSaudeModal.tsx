import { useState, useEffect } from 'react';
import { X, CreditCard, Loader2, AlertCircle } from 'lucide-react';
import { planoSaudeService } from '../services/planoSaudeService';
import type { PlanoSaude, SavePlanoSaudeInput } from '../types/planoSaude';

interface PlanoSaudeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (plano: PlanoSaude) => void;
  initialData?: PlanoSaude | null;
}

export default function PlanoSaudeModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
}: PlanoSaudeModalProps) {
  const [nomePlano, setNomePlano] = useState('');
  const [operadora, setOperadora] = useState('');
  const [ansReg, setAnsReg] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (initialData) {
      setNomePlano(initialData.nomePlano || '');
      setOperadora(initialData.operadora || '');
      setAnsReg(initialData.ansReg || '');
    } else {
      setNomePlano('');
      setOperadora('');
      setAnsReg('');
    }
    setError('');
  }, [initialData, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!nomePlano.trim()) {
      setError('Informe o nome do plano de saúde.');
      return;
    }

    if (!operadora.trim()) {
      setError('Informe a operadora do plano de saúde.');
      return;
    }

    const payload: SavePlanoSaudeInput = {
      nomePlano: nomePlano.trim(),
      operadora: operadora.trim(),
      ansReg: ansReg.trim() || undefined,
    };

    setLoading(true);
    try {
      let saved: PlanoSaude;
      if (initialData?.idPlanoSaude) {
        saved = await planoSaudeService.update(initialData.idPlanoSaude, payload);
      } else {
        saved = await planoSaudeService.create(payload);
      }
      onSuccess(saved);
      onClose();
    } catch (err: unknown) {
      const apiErr = err as { response?: { data?: { errorMessage?: string; message?: string } } };
      setError(
        apiErr.response?.data?.errorMessage ||
          apiErr.response?.data?.message ||
          'Erro ao salvar plano de saúde. Tente novamente.'
      );
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-gray-100 animate-in fade-in zoom-in duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
          <div className="flex items-center gap-2 text-indigo-900 font-semibold text-lg">
            <CreditCard className="h-5 w-5 text-indigo-600" />
            <h2>{initialData ? 'Editar Plano de Saúde' : 'Novo Plano de Saúde'}</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="flex items-center gap-2 p-3 text-sm text-red-600 bg-red-50 rounded-xl border border-red-100">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <p>{error}</p>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              Nome do Plano *
            </label>
            <input
              type="text"
              required
              value={nomePlano}
              onChange={(e) => setNomePlano(e.target.value)}
              placeholder="Ex: Amil Fácil S60, Unimed Pleno, Bradesco Saúde Top"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              Operadora *
            </label>
            <input
              type="text"
              required
              value={operadora}
              onChange={(e) => setOperadora(e.target.value)}
              placeholder="Ex: Amil, Unimed, Bradesco Saúde, SulAmérica, NotreDame"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              Registro ANS (Opcional)
            </label>
            <input
              type="text"
              value={ansReg}
              maxLength={10}
              onChange={(e) => setAnsReg(e.target.value)}
              placeholder="Ex: 417505"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition font-mono"
            />
            <p className="text-[11px] text-gray-400 mt-1">
              Código de registro da operadora na Agência Nacional de Saúde Suplementar.
            </p>
          </div>

          {/* Footer buttons */}
          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors shadow-sm"
            >
              {loading && <Loader2 className="h-4 w-4 animate-spin" />}
              {initialData ? 'Salvar Alterações' : 'Cadastrar Plano'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
