import React, { useState } from 'react';
import { X, UserMinus, AlertTriangle } from 'lucide-react';
import type { GradeTerapeutica } from '../../types/gradeTerapeutica';

interface CancelarProfissionalModalProps {
  isOpen: boolean;
  onClose: () => void;
  grade: GradeTerapeutica;
  onConfirmar: (idProfissional: string, dataCorte: string, motivo: string) => Promise<void>;
}

export const CancelarProfissionalModal: React.FC<CancelarProfissionalModalProps> = ({
  isOpen,
  onClose,
  grade,
  onConfirmar,
}) => {
  // Extrai lista única de profissionais presentes nesta grade
  const profissionaisNaGrade = Array.from(
    new Map(
      grade.itens
        .filter((i) => i.idProfissional)
        .map((i) => [i.idProfissional!, { id: i.idProfissional!, nome: i.nomeProfissional || 'Terapeuta' }])
    ).values()
  );

  const [idProfissional, setIdProfissional] = useState(profissionaisNaGrade[0]?.id || '');
  const [dataCorte, setDataCorte] = useState(new Date().toISOString().split('T')[0]);
  const [motivo, setMotivo] = useState('Profissional desligou-se da clínica e não haverá reposição deste horário');
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  if (!isOpen) return null;

  async function handleConfirmar() {
    if (!idProfissional) {
      setErro('Por favor, selecione o profissional.');
      return;
    }
    if (!dataCorte) {
      setErro('Por favor, informe a data de corte.');
      return;
    }

    try {
      setLoading(true);
      await onConfirmar(idProfissional, dataCorte, motivo);
      onClose();
    } catch (e: any) {
      setErro(e.response?.data?.message || e.message || 'Erro ao cancelar compromissos.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl max-w-lg w-full flex flex-col shadow-2xl border border-red-200 dark:border-red-900/50 animate-in fade-in zoom-in-95 duration-200">
        {/* Cabeçalho */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700 bg-red-50/50 dark:bg-red-950/20">
          <div className="flex items-center gap-2">
            <UserMinus className="w-5 h-5 text-red-600 dark:text-red-400" />
            <h2 className="text-base font-bold text-gray-900 dark:text-gray-100">
              Cancelar Horários Futuros de Profissional
            </h2>
          </div>
          <button
            onClick={() => !loading && onClose()}
            className="p-1 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Corpo */}
        <div className="p-6 space-y-4">
          <div className="p-3 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl text-xs text-amber-800 dark:text-amber-200 flex items-start gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5 text-amber-600" />
            <span>
              Esta ação cancelará definitivamente todos os atendimentos futuros deste profissional vinculados à grade a partir da data de corte. <span className="font-semibold underline">Os atendimentos passados não serão alterados</span>.
            </span>
          </div>

          {erro && (
            <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-xs text-red-700 dark:text-red-300">
              {erro}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase mb-1">
              Profissional *
            </label>
            <select
              value={idProfissional}
              onChange={(e) => setIdProfissional(e.target.value)}
              disabled={loading}
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
            >
              {profissionaisNaGrade.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nome}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase mb-1">
              Data de Corte (a partir de) *
            </label>
            <input
              type="date"
              value={dataCorte}
              onChange={(e) => setDataCorte(e.target.value)}
              disabled={loading}
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase mb-1">
              Motivo do Cancelamento
            </label>
            <textarea
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              disabled={loading}
              rows={2}
              className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
            />
          </div>
        </div>

        {/* Rodapé */}
        <div className="px-6 py-4 bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl"
          >
            Voltar
          </button>
          <button
            type="button"
            onClick={handleConfirmar}
            disabled={loading}
            className="px-4 py-2 text-sm font-semibold bg-red-600 hover:bg-red-700 text-white rounded-xl shadow-sm disabled:opacity-50"
          >
            {loading ? 'Cancelando...' : 'Confirmar Cancelamento'}
          </button>
        </div>
      </div>
    </div>
  );
};
