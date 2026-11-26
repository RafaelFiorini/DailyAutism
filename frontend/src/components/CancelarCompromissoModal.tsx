import { useState, FormEvent } from 'react';
import { CalendarX2, X, Loader2, AlertCircle, Bell } from 'lucide-react';
import { compromissoService } from '../services/compromissoService';
import type { Compromisso } from '../types/compromisso';
import { extractApiErrorMessage } from '../utils/apiError';

interface CancelarCompromissoModalProps {
  compromisso: Compromisso | null;
  onClose: () => void;
  onCanceled: (atualizado: Compromisso) => void;
}

const SUGESTOES_MOTIVO = [
  'Imprevisto familiar / pessoal',
  'Paciente com sintomas / indisposição',
  'Imprevisto do profissional',
  'Viagem / compromisso escolar',
];

export default function CancelarCompromissoModal({
  compromisso,
  onClose,
  onCanceled,
}: CancelarCompromissoModalProps) {
  const [motivo, setMotivo] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (!compromisso) return null;

  const dataFormatada = compromisso.dataHoraCompromisso
    ? new Date(compromisso.dataHoraCompromisso).toLocaleString('pt-BR', {
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '';

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!compromisso) return;
    if (motivo.trim().length < 5) {
      setError('Informe uma justificativa com pelo menos 5 caracteres.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const atualizado = await compromissoService.cancelar(compromisso.idCompromisso, motivo.trim());
      setMotivo('');
      onCanceled(atualizado);
      onClose();
    } catch (err) {
      setError(extractApiErrorMessage(err, 'Não foi possível cancelar a sessão.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-red-50/70">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-red-100 text-red-600 rounded-xl">
              <CalendarX2 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-gray-900">Cancelar Sessão</h2>
              <p className="text-xs text-gray-600">Aviso antecipado com justificativa</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => !submitting && onClose()}
            className="p-1 text-gray-400 hover:text-gray-600 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="p-3 bg-gray-50 border border-gray-200 rounded-xl text-xs space-y-1">
            <p className="font-semibold text-gray-900">
              {compromisso.nomeTipoCompromisso || compromisso.tituloCompromisso}
            </p>
            <p className="text-gray-600">
              <strong>Paciente:</strong> {compromisso.nomePaciente}
            </p>
            {compromisso.nomeProfissional && (
              <p className="text-gray-600">
                <strong>Profissional:</strong> {compromisso.nomeProfissional}
              </p>
            )}
            <p className="text-gray-600">
              <strong>Data e Horário:</strong> {dataFormatada}
            </p>
          </div>

          <div className="p-3 bg-indigo-50 border border-indigo-200 rounded-xl text-xs text-indigo-800 flex items-start gap-2">
            <Bell className="w-4 h-4 text-indigo-600 shrink-0 mt-0.5" />
            <span>
              Os demais envolvidos (profissional, responsável e clínica) receberão uma <strong>notificação automática</strong> no sistema Web e no App com a sua justificativa, e o saldo de sessões será preservado.
            </span>
          </div>

          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1.5">
              Motivo / Justificativa *
            </label>
            <div className="flex flex-wrap gap-1.5 mb-2">
              {SUGESTOES_MOTIVO.map((sug) => (
                <button
                  key={sug}
                  type="button"
                  onClick={() => setMotivo(sug)}
                  className="px-2 py-1 text-[11px] bg-gray-100 hover:bg-indigo-50 hover:text-indigo-700 text-gray-600 rounded-md transition-colors"
                >
                  {sug}
                </button>
              ))}
            </div>
            <textarea
              rows={3}
              required
              minLength={5}
              maxLength={500}
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              placeholder="Explique brevemente o motivo do cancelamento para avisar a equipe..."
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500 focus:outline-none"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
            >
              Voltar
            </button>
            <button
              type="submit"
              disabled={submitting || motivo.trim().length < 5}
              className="inline-flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 disabled:bg-red-300 text-white text-sm font-semibold rounded-lg shadow-sm"
            >
              {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
              Confirmar Cancelamento
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
