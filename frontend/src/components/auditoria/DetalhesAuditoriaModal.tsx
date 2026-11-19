import React from 'react';
import { X, Shield, Clock, User, Globe, FileCode2 } from 'lucide-react';
import type { AuditoriaLog } from '../../types/auditoria';
import { CATEGORIAS_CONFIG, ACOES_CONFIG } from '../../types/auditoria';

interface DetalhesAuditoriaModalProps {
  isOpen: boolean;
  onClose: () => void;
  log: AuditoriaLog | null;
}

export const DetalhesAuditoriaModal: React.FC<DetalhesAuditoriaModalProps> = ({
  isOpen,
  onClose,
  log,
}) => {
  if (!isOpen || !log) return null;

  const categoriaCfg = CATEGORIAS_CONFIG[log.categoria] || {
    label: log.categoria,
    bg: 'bg-gray-100 dark:bg-gray-700',
    text: 'text-gray-800 dark:text-gray-200',
    border: 'border-gray-200 dark:border-gray-600',
  };

  const acaoCfg = ACOES_CONFIG[log.acao] || {
    label: log.acao,
    badgeBg: 'bg-gray-100 dark:bg-gray-700',
    badgeText: 'text-gray-800 dark:text-gray-200',
  };

  function formatarDataHora(iso?: string) {
    if (!iso) return '-';
    try {
      const d = new Date(iso);
      return d.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return iso;
    }
  }

  function formatarJson(str?: string) {
    if (!str) return null;
    try {
      const parsed = JSON.parse(str);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return str;
    }
  }

  const jsonAntes = formatarJson(log.dadosAnteriores);
  const jsonDepois = formatarJson(log.dadosNovos);

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl max-w-2xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-gray-200 dark:border-gray-700 animate-in fade-in zoom-in-95 duration-200">
        {/* Cabeçalho */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-primary-50 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 rounded-xl">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-gray-900 dark:text-gray-100">
                Detalhes do Registro de Auditoria
              </h2>
              <div className="flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                <Clock className="w-3.5 h-3.5" />
                <span>{formatarDataHora(log.dataHora)}</span>
                <span className="font-mono text-[10px] text-gray-400 ml-1">#{log.idAuditoriaLog.substring(0, 8)}</span>
              </div>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Corpo do Modal */}
        <div className="p-6 overflow-y-auto flex-1 space-y-4">
          {/* Badges de Categoria e Ação */}
          <div className="flex flex-wrap gap-2 items-center">
            <span
              className={`text-xs font-semibold px-2.5 py-1 rounded-lg border ${categoriaCfg.bg} ${categoriaCfg.text} ${categoriaCfg.border}`}
            >
              {categoriaCfg.label}
            </span>
            <span
              className={`text-xs font-bold px-2.5 py-1 rounded-lg ${acaoCfg.badgeBg} ${acaoCfg.badgeText}`}
            >
              {acaoCfg.label}
            </span>
            {log.entidade && (
              <span className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 px-2.5 py-1 rounded-lg font-medium">
                Entidade: <strong>{log.entidade}</strong>
              </span>
            )}
          </div>

          {/* Descrição */}
          <div className="p-3.5 bg-gray-50 dark:bg-gray-750 border border-gray-200 dark:border-gray-700 rounded-xl">
            <p className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">
              Descrição do Evento
            </p>
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {log.descricao || 'Sem descrição registrada.'}
            </p>
          </div>

          {/* Cards de Usuário e Rede */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Usuário */}
            <div className="p-3.5 bg-gray-50 dark:bg-gray-750 border border-gray-200 dark:border-gray-700 rounded-xl space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                <User className="w-3.5 h-3.5 text-primary-600" />
                <span>Responsável pela Ação</span>
              </div>
              <p className="text-xs font-bold text-gray-900 dark:text-gray-100">
                {log.nomeUsuario || log.loginUsuario || 'Sistema / Não autenticado'}
              </p>
              {log.loginUsuario && (
                <p className="text-xs text-gray-500 dark:text-gray-400 font-mono">
                  {log.loginUsuario}
                </p>
              )}
              {log.perfilUsuario && (
                <span className="inline-block mt-1 text-[11px] px-2 py-0.5 bg-primary-100 text-primary-700 dark:bg-primary-900/40 dark:text-primary-300 rounded font-semibold">
                  Perfil: {log.perfilUsuario}
                </span>
              )}
            </div>

            {/* Rede e Dispositivo */}
            <div className="p-3.5 bg-gray-50 dark:bg-gray-750 border border-gray-200 dark:border-gray-700 rounded-xl space-y-1">
              <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                <Globe className="w-3.5 h-3.5 text-primary-600" />
                <span>Origem e Rastreabilidade</span>
              </div>
              <p className="text-xs font-mono font-bold text-gray-900 dark:text-gray-100">
                IP: {log.ipOrigem || 'UNKNOWN'}
              </p>
              <p className="text-[11px] text-gray-500 dark:text-gray-400 truncate" title={log.userAgent}>
                Navegador: {log.userAgent || 'Desconhecido'}
              </p>
              {log.idEntidade && (
                <p className="text-[11px] text-gray-500 dark:text-gray-400 font-mono mt-1">
                  ID Registro: <span className="text-gray-700 dark:text-gray-300">{log.idEntidade}</span>
                </p>
              )}
            </div>
          </div>

          {/* Comparativo de Dados (Antes vs Depois) */}
          {(jsonAntes || jsonDepois) && (
            <div className="space-y-2 pt-2 border-t border-gray-200 dark:border-gray-700">
              <div className="flex items-center gap-1.5 text-xs font-bold text-gray-700 dark:text-gray-300">
                <FileCode2 className="w-4 h-4 text-primary-600" />
                <span>Comparativo de Dados (Snapshot de Alteração)</span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {jsonAntes && (
                  <div>
                    <span className="block text-[11px] font-semibold text-amber-700 dark:text-amber-400 mb-1">
                      Estado Anterior:
                    </span>
                    <pre className="p-2.5 bg-gray-900 text-amber-300 text-[11px] font-mono rounded-lg overflow-x-auto max-h-48">
                      {jsonAntes}
                    </pre>
                  </div>
                )}
                {jsonDepois && (
                  <div>
                    <span className="block text-[11px] font-semibold text-emerald-700 dark:text-emerald-400 mb-1">
                      Novo Estado:
                    </span>
                    <pre className="p-2.5 bg-gray-900 text-emerald-300 text-[11px] font-mono rounded-lg overflow-x-auto max-h-48">
                      {jsonDepois}
                    </pre>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Rodapé */}
        <div className="px-6 py-3.5 bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
};
