import React from 'react';
import { X, AlertTriangle, Calendar, Clock, User, CheckCircle2 } from 'lucide-react';
import type { Compromisso } from '../../types/compromisso';

interface AvisosConflitoModalProps {
  isOpen: boolean;
  onClose: () => void;
  conflitos: Compromisso[];
  nomePaciente?: string;
}

export const AvisosConflitoModal: React.FC<AvisosConflitoModalProps> = ({
  isOpen,
  onClose,
  conflitos,
  nomePaciente,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl max-w-2xl w-full max-h-[85vh] flex flex-col shadow-2xl border border-amber-200 dark:border-amber-800 animate-in fade-in zoom-in-95 duration-200">
        {/* Cabeçalho */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700 bg-amber-50/60 dark:bg-amber-950/20">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-amber-600 dark:text-amber-400" />
            <div>
              <h2 className="text-base font-bold text-gray-900 dark:text-gray-100">
                Avisos de Choque de Horário (Pendente Reagendamento)
              </h2>
              {nomePaciente && (
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Paciente: <span className="font-semibold text-gray-700 dark:text-gray-300">{nomePaciente}</span>
                </p>
              )}
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Lista de Conflitos */}
        <div className="p-6 overflow-y-auto flex-1 space-y-3">
          <div className="p-3 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl text-xs text-amber-800 dark:text-amber-200">
            Estes atendimentos colidiram com outros agendamentos pré-existentes do terapeuta durante a replicação da grade. Eles foram criados com status <span className="font-bold underline">PENDENTE_REAGENDAMENTO</span> para que a secretaria combine um horário pontual alternativo.
          </div>

          {conflitos.length === 0 ? (
            <div className="py-12 text-center text-gray-500">
              <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2 opacity-80" />
              <p className="font-medium text-sm">Nenhum choque de horário pendente!</p>
              <p className="text-xs text-gray-400 mt-1">Todos os agendamentos da grade foram alocados com sucesso.</p>
            </div>
          ) : (
            conflitos.map((c) => (
              <div
                key={c.idCompromisso}
                className="p-3.5 bg-gray-50 dark:bg-gray-750 border border-amber-200 dark:border-amber-800/60 rounded-xl flex items-center justify-between gap-3"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-sm text-gray-900 dark:text-gray-100">
                      {c.tituloCompromisso}
                    </span>
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
                      PENDENTE REAGENDAMENTO
                    </span>
                  </div>
                  <div className="flex flex-wrap items-center gap-3 mt-1.5 text-xs text-gray-600 dark:text-gray-300">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5 text-gray-400" />
                      {new Date(c.dataHoraCompromisso).toLocaleDateString('pt-BR')}
                    </span>
                    <span className="flex items-center gap-1 font-mono">
                      <Clock className="w-3.5 h-3.5 text-gray-400" />
                      {new Date(c.dataHoraCompromisso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    {c.nomeProfissional && (
                      <span className="flex items-center gap-1">
                        <User className="w-3.5 h-3.5 text-gray-400" />
                        {c.nomeProfissional}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Rodapé */}
        <div className="px-6 py-3 bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm font-semibold bg-primary-600 hover:bg-primary-700 text-white rounded-xl shadow-sm transition"
          >
            Entendido
          </button>
        </div>
      </div>
    </div>
  );
};
