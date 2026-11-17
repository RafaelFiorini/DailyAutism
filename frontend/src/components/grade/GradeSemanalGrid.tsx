import React from 'react';
import type { GradeTerapeuticaItem, DiaSemana } from '../../types/gradeTerapeutica';
import { DIAS_SEMANA_ORDENADOS } from '../../types/gradeTerapeutica';
import { Clock, User, MapPin } from 'lucide-react';

interface GradeSemanalGridProps {
  itens: GradeTerapeuticaItem[];
  compact?: boolean;
}

const ESPECIALIDADE_COLORS: Record<string, { bg: string; border: string; text: string }> = {
  psicologia: { bg: 'bg-blue-50 dark:bg-blue-900/20', border: 'border-blue-200 dark:border-blue-800', text: 'text-blue-700 dark:text-blue-300' },
  'terapia ocupacional': { bg: 'bg-purple-50 dark:bg-purple-900/20', border: 'border-purple-200 dark:border-purple-800', text: 'text-purple-700 dark:text-purple-300' },
  fonoaudiologia: { bg: 'bg-amber-50 dark:bg-amber-900/20', border: 'border-amber-200 dark:border-amber-800', text: 'text-amber-700 dark:text-amber-300' },
  musicoterapia: { bg: 'bg-emerald-50 dark:bg-emerald-900/20', border: 'border-emerald-200 dark:border-emerald-800', text: 'text-emerald-700 dark:text-emerald-300' },
  fisioterapia: { bg: 'bg-teal-50 dark:bg-teal-900/20', border: 'border-teal-200 dark:border-teal-800', text: 'text-teal-700 dark:text-teal-300' },
  psicopedagogia: { bg: 'bg-pink-50 dark:bg-pink-900/20', border: 'border-pink-200 dark:border-pink-800', text: 'text-pink-700 dark:text-pink-300' },
};

function getCorEspecialidade(nome?: string) {
  if (!nome) return { bg: 'bg-indigo-50 dark:bg-indigo-900/20', border: 'border-indigo-200 dark:border-indigo-800', text: 'text-indigo-700 dark:text-indigo-300' };
  const lower = nome.toLowerCase();
  for (const [key, val] of Object.entries(ESPECIALIDADE_COLORS)) {
    if (lower.includes(key)) return val;
  }
  return { bg: 'bg-indigo-50 dark:bg-indigo-900/20', border: 'border-indigo-200 dark:border-indigo-800', text: 'text-indigo-700 dark:text-indigo-300' };
}

export const GradeSemanalGrid: React.FC<GradeSemanalGridProps> = ({ itens, compact = false }) => {
  // Mostra Segunda a Sexta sempre; Sábado e Domingo apenas se houver sessões cadastradas
  const temFimDeSemana = itens.some((i) => i.diaSemana === 'SABADO' || i.diaSemana === 'DOMINGO');
  const diasParaExibir = DIAS_SEMANA_ORDENADOS.filter(
    (d) => d.key !== 'SABADO' && d.key !== 'DOMINGO' || temFimDeSemana
  );

  const itensPorDia: Record<DiaSemana, GradeTerapeuticaItem[]> = {
    SEGUNDA: [],
    TERCA: [],
    QUARTA: [],
    QUINTA: [],
    SEXTA: [],
    SABADO: [],
    DOMINGO: [],
  };

  itens.forEach((item) => {
    if (itensPorDia[item.diaSemana]) {
      itensPorDia[item.diaSemana].push(item);
    }
  });

  // Ordena itens de cada dia por hora de início
  Object.keys(itensPorDia).forEach((dia) => {
    itensPorDia[dia as DiaSemana].sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));
  });

  return (
    <div className={`grid grid-cols-1 md:grid-cols-5 ${temFimDeSemana ? 'lg:grid-cols-7' : 'lg:grid-cols-5'} gap-3`}>
      {diasParaExibir.map((dia) => {
        const sessoes = itensPorDia[dia.key];
        return (
          <div
            key={dia.key}
            className="flex flex-col bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden shadow-sm"
          >
            {/* Cabeçalho do dia */}
            <div className="bg-gray-50 dark:bg-gray-700/60 px-3 py-2.5 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
              <div>
                <span className="font-semibold text-gray-900 dark:text-gray-100 text-sm">{dia.label}</span>
                <span className="md:hidden ml-1 text-xs text-gray-500">({dia.abrev})</span>
              </div>
              <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                sessoes.length > 0
                  ? 'bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-400'
              }`}>
                {sessoes.length} {sessoes.length === 1 ? 'sessão' : 'sessões'}
              </span>
            </div>

            {/* Lista de sessões do dia */}
            <div className={`p-2.5 flex-1 flex flex-col gap-2 ${compact ? 'min-h-[140px]' : 'min-h-[220px]'}`}>
              {sessoes.length === 0 ? (
                <div className="h-full flex items-center justify-center text-xs text-gray-400 dark:text-gray-500 italic py-4">
                  Sem atendimento
                </div>
              ) : (
                sessoes.map((item, idx) => {
                  const cores = getCorEspecialidade(item.nomeTipoCompromisso);
                  return (
                    <div
                      key={item.idGradeTerapeuticaItem || idx}
                      className={`p-2.5 rounded-lg border ${cores.bg} ${cores.border} transition-all hover:shadow-sm`}
                    >
                      <div className="flex items-center justify-between gap-1 mb-1">
                        <span className={`font-semibold text-xs ${cores.text} truncate`}>
                          {item.nomeTipoCompromisso || 'Especialidade'}
                        </span>
                        <div className="flex items-center text-[11px] font-mono font-medium text-gray-600 dark:text-gray-300 shrink-0">
                          <Clock className="w-3 h-3 mr-1 text-gray-400" />
                          {item.horaInicio?.substring(0, 5)} - {item.horaFim?.substring(0, 5)}
                        </div>
                      </div>

                      {item.nomeProfissional && (
                        <div className="flex items-center text-xs text-gray-600 dark:text-gray-300 mt-1">
                          <User className="w-3 h-3 mr-1 text-gray-400 shrink-0" />
                          <span className="truncate">{item.nomeProfissional}</span>
                        </div>
                      )}

                      {item.sala && (
                        <div className="flex items-center text-[11px] text-gray-500 dark:text-gray-400 mt-0.5">
                          <MapPin className="w-2.5 h-2.5 mr-1 text-gray-400 shrink-0" />
                          <span className="truncate">{item.sala}</span>
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};
