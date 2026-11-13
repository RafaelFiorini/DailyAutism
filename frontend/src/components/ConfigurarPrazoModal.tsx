import { useState, useEffect } from 'react';
import { X, CalendarClock, Loader2, AlertCircle } from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type { ClinicaPlanoPrazo } from '../types/financeiro';
import type { PlanoSaude } from '../types/planoSaude';

interface ConfigurarPrazoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  clinicaId: string;
  plano: PlanoSaude | null;
  prazoAtual?: ClinicaPlanoPrazo | null;
}

export default function ConfigurarPrazoModal({
  isOpen,
  onClose,
  onSuccess,
  clinicaId,
  plano,
  prazoAtual,
}: ConfigurarPrazoModalProps) {
  const [diaCorteEnvio, setDiaCorteEnvio] = useState<string>('20');
  const [prazoPagamentoDias, setPrazoPagamentoDias] = useState<string>('30');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (prazoAtual) {
      setDiaCorteEnvio(prazoAtual.diaCorteEnvio ? String(prazoAtual.diaCorteEnvio) : '20');
      setPrazoPagamentoDias(
        prazoAtual.prazoPagamentoDias ? String(prazoAtual.prazoPagamentoDias) : '30'
      );
    } else {
      setDiaCorteEnvio('20');
      setPrazoPagamentoDias('30');
    }
    setError('');
  }, [prazoAtual, isOpen]);

  if (!isOpen || !plano) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const diaCorte = Number(diaCorteEnvio);
    const prazoDias = Number(prazoPagamentoDias);

    if (isNaN(diaCorte) || diaCorte < 1 || diaCorte > 31) {
      setError('Informe um dia de corte válido entre 1 e 31.');
      return;
    }

    if (isNaN(prazoDias) || prazoDias < 1) {
      setError('Informe um prazo de pagamento válido em dias (mínimo 1).');
      return;
    }

    setLoading(true);
    try {
      await financeiroService.savePrazoConvenio({
        idClinica: clinicaId,
        idPlanoSaude: plano.idPlanoSaude,
        diaCorteEnvio: diaCorte,
        prazoPagamentoDias: prazoDias,
      });
      onSuccess();
      onClose();
    } catch {
      setError('Erro ao salvar os prazos do convênio. Verifique e tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-md w-full overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center">
              <CalendarClock className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900 text-sm">Prazos de Faturamento</h3>
              <p className="text-[11px] text-gray-500">{plano.nomePlano}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-500 rounded-lg p-1 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-700 text-xs">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
              Dia de Corte Mensal (Envio do Lote)
            </label>
            <input
              type="number"
              min="1"
              max="31"
              value={diaCorteEnvio}
              onChange={(e) => setDiaCorteEnvio(e.target.value)}
              required
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-all"
            />
            <p className="text-[11px] text-gray-400 mt-1">
              Dia do mês em que a clínica fecha e despacha as guias assinadas para este convênio.
            </p>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
              Prazo para Pagamento / Resposta (dias)
            </label>
            <input
              type="number"
              min="1"
              max="180"
              value={prazoPagamentoDias}
              onChange={(e) => setPrazoPagamentoDias(e.target.value)}
              required
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-all"
            />
            <p className="text-[11px] text-gray-400 mt-1">
              Quantidade de dias após o envio do lote para o pagamento ser creditado na clínica (ex: 30, 45 ou 60 dias).
            </p>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 border border-gray-200 rounded-lg text-xs font-medium text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-purple-600 text-white rounded-lg text-xs font-semibold hover:bg-purple-700 transition-colors flex items-center gap-2 shadow-sm disabled:opacity-50"
            >
              {loading && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              Salvar Prazos
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
