import { useState, useEffect } from 'react';
import { X, CalendarClock, Loader2, AlertCircle } from 'lucide-react';
import { periodicidadeService } from '../services/periodicidadeService';
import type { Periodicidade, SavePeriodicidadeInput } from '../types/periodicidade';

interface PeriodicidadeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (periodicidade: Periodicidade) => void;
  initialData?: Periodicidade | null;
}

const PRESETS = [
  { label: 'Diária', dias: 1, desc: 'Atendimento diário' },
  { label: 'Semanal', dias: 7, desc: 'Atendimento 1 vez por semana' },
  { label: 'Quinzenal', dias: 14, desc: 'Atendimento a cada duas semanas' },
  { label: 'Mensal', dias: 30, desc: 'Atendimento 1 vez ao mês' },
  { label: 'Avulsa', dias: 0, desc: 'Sessão ou avaliação avulsa' },
];

export default function PeriodicidadeModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
}: PeriodicidadeModalProps) {
  const [nomePeriodicidade, setNomePeriodicidade] = useState('');
  const [diasIntervalo, setDiasIntervalo] = useState<string>('');
  const [descricaoPeriodicidade, setDescricaoPeriodicidade] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (initialData) {
      setNomePeriodicidade(initialData.nomePeriodicidade || '');
      setDiasIntervalo(
        initialData.diasIntervalo !== undefined && initialData.diasIntervalo !== null
          ? String(initialData.diasIntervalo)
          : ''
      );
      setDescricaoPeriodicidade(initialData.descricaoPeriodicidade || '');
    } else {
      setNomePeriodicidade('');
      setDiasIntervalo('');
      setDescricaoPeriodicidade('');
    }
    setError('');
  }, [initialData, isOpen]);

  const handleApplyPreset = (preset: (typeof PRESETS)[0]) => {
    setNomePeriodicidade(preset.label);
    setDiasIntervalo(String(preset.dias));
    setDescricaoPeriodicidade(preset.desc);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!nomePeriodicidade.trim()) {
      setError('Informe o nome da periodicidade.');
      return;
    }

    let parsedDias: number | undefined = undefined;
    if (diasIntervalo.trim() !== '') {
      parsedDias = parseInt(diasIntervalo, 10);
      if (isNaN(parsedDias) || parsedDias < 0) {
        setError('O intervalo de dias deve ser um número maior ou igual a zero.');
        return;
      }
    }

    const payload: SavePeriodicidadeInput = {
      nomePeriodicidade: nomePeriodicidade.trim(),
      diasIntervalo: parsedDias,
      descricaoPeriodicidade: descricaoPeriodicidade.trim() || undefined,
    };

    setLoading(true);
    try {
      let saved: Periodicidade;
      if (initialData?.idPeriodicidade) {
        saved = await periodicidadeService.update(initialData.idPeriodicidade, payload);
      } else {
        saved = await periodicidadeService.create(payload);
      }
      onSuccess(saved);
      onClose();
    } catch (err: unknown) {
      const apiErr = err as { response?: { data?: { errorMessage?: string; message?: string } } };
      setError(
        apiErr.response?.data?.errorMessage ||
          apiErr.response?.data?.message ||
          'Erro ao salvar periodicidade. Tente novamente.'
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
            <CalendarClock className="h-5 w-5 text-indigo-600" />
            <h2>{initialData ? 'Editar Periodicidade' : 'Nova Periodicidade'}</h2>
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
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs flex items-center gap-2">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Atalhos Rápidos (Presets) */}
          {!initialData && (
            <div>
              <span className="block text-xs font-semibold text-gray-500 mb-1.5 uppercase tracking-wider">
                Sugestões Rápidas:
              </span>
              <div className="flex flex-wrap gap-1.5">
                {PRESETS.map((p) => (
                  <button
                    key={p.label}
                    type="button"
                    onClick={() => handleApplyPreset(p)}
                    className="px-2.5 py-1 text-xs font-medium rounded-md border border-indigo-200 bg-indigo-50/50 text-indigo-700 hover:bg-indigo-100 transition-colors"
                  >
                    {p.label} ({p.dias}d)
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Nome da Periodicidade */}
          <div>
            <label htmlFor="nomePeriodicidade" className="block text-xs font-semibold text-gray-700 mb-1">
              Nome da Periodicidade *
            </label>
            <input
              id="nomePeriodicidade"
              type="text"
              required
              placeholder="Ex: Semanal, Quinzenal, 2x por semana"
              value={nomePeriodicidade}
              onChange={(e) => setNomePeriodicidade(e.target.value)}
              className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all placeholder:text-gray-400"
            />
          </div>

          {/* Dias de Intervalo */}
          <div>
            <label htmlFor="diasIntervalo" className="block text-xs font-semibold text-gray-700 mb-1">
              Intervalo em Dias (opcional)
            </label>
            <input
              id="diasIntervalo"
              type="number"
              min="0"
              step="1"
              placeholder="Ex: 7 para semanal, 14 para quinzenal, 0 para avulsa"
              value={diasIntervalo}
              onChange={(e) => setDiasIntervalo(e.target.value)}
              className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all placeholder:text-gray-400"
            />
            <p className="text-[11px] text-gray-500 mt-1">
              Número de dias entre cada sessão da recorrência na agenda.
            </p>
          </div>

          {/* Descrição */}
          <div>
            <label htmlFor="descricaoPeriodicidade" className="block text-xs font-semibold text-gray-700 mb-1">
              Descrição (opcional)
            </label>
            <textarea
              id="descricaoPeriodicidade"
              rows={3}
              placeholder="Ex: Sessões regulares com recorrência semanal"
              value={descricaoPeriodicidade}
              onChange={(e) => setDescricaoPeriodicidade(e.target.value)}
              className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all placeholder:text-gray-400 resize-none"
            />
          </div>

          {/* Botões */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 px-5 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors disabled:opacity-50 shadow-sm"
            >
              {loading && <Loader2 className="h-4 w-4 animate-spin" />}
              {initialData ? 'Atualizar' : 'Salvar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
