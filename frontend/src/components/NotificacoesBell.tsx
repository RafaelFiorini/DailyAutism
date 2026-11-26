import { useState, useEffect, useRef, useCallback } from 'react';
import { Bell, CheckCheck, CalendarX2, Info, Loader2 } from 'lucide-react';
import { notificacaoService, type Notificacao } from '../services/notificacaoService';

function formatarDataHora(iso?: string): string {
  if (!iso) return '';
  try {
    const dt = new Date(iso);
    return dt.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return iso;
  }
}

export default function NotificacoesBell() {
  const [open, setOpen] = useState(false);
  const [naoLidas, setNaoLidas] = useState(0);
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const carregarContagem = useCallback(async () => {
    try {
      const count = await notificacaoService.countNaoLidas();
      setNaoLidas(count);
    } catch {
      // Silencioso em background (ex.: testes sem mock de notificações)
    }
  }, []);

  const carregarLista = useCallback(async () => {
    setLoading(true);
    try {
      const data = await notificacaoService.list();
      setNotificacoes(data);
      setNaoLidas(data.filter((n) => !n.lida).length);
    } catch {
      // Silencioso
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    carregarContagem();
    const interval = setInterval(carregarContagem, 60000);
    return () => clearInterval(interval);
  }, [carregarContagem]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const handleToggle = () => {
    const next = !open;
    setOpen(next);
    if (next) {
      carregarLista();
    }
  };

  const handleMarcarLida = async (n: Notificacao) => {
    if (n.lida) return;
    try {
      await notificacaoService.marcarLida(n.idNotificacao);
      setNotificacoes((prev) =>
        prev.map((item) => (item.idNotificacao === n.idNotificacao ? { ...item, lida: true } : item))
      );
      setNaoLidas((prev) => Math.max(0, prev - 1));
    } catch {
      // Silencioso
    }
  };

  const handleMarcarTodasLidas = async () => {
    try {
      await notificacaoService.marcarTodasLidas();
      setNotificacoes((prev) => prev.map((item) => ({ ...item, lida: true })));
      setNaoLidas(0);
    } catch {
      // Silencioso
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        type="button"
        onClick={handleToggle}
        title="Notificações e avisos"
        aria-label="Notificações"
        className="relative p-2 rounded-lg text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 transition-colors"
      >
        <Bell className="w-5 h-5" />
        {naoLidas > 0 && (
          <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 rounded-full bg-red-600 text-white text-[10px] font-bold flex items-center justify-center shadow-sm">
            {naoLidas > 99 ? '99+' : naoLidas}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-2xl shadow-2xl border border-gray-200 z-50 overflow-hidden">
          <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between bg-gray-50/70">
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-gray-900">Notificações</span>
              {naoLidas > 0 && (
                <span className="px-2 py-0.5 text-[11px] font-semibold bg-red-100 text-red-700 rounded-full">
                  {naoLidas} nova{naoLidas === 1 ? '' : 's'}
                </span>
              )}
            </div>
            {naoLidas > 0 && (
              <button
                type="button"
                onClick={handleMarcarTodasLidas}
                className="inline-flex items-center gap-1 text-xs font-semibold text-indigo-600 hover:text-indigo-800"
              >
                <CheckCheck className="w-3.5 h-3.5" />
                Marcar lidas
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto divide-y divide-gray-100">
            {loading ? (
              <div className="py-10 flex flex-col items-center justify-center text-gray-400">
                <Loader2 className="w-6 h-6 animate-spin text-indigo-600 mb-2" />
                <span className="text-xs">Carregando avisos...</span>
              </div>
            ) : notificacoes.length === 0 ? (
              <div className="py-10 px-4 text-center text-gray-400">
                <Bell className="w-8 h-8 mx-auto mb-2 opacity-40" />
                <p className="text-xs font-medium text-gray-500">Nenhuma notificação no momento</p>
              </div>
            ) : (
              notificacoes.map((n) => (
                <button
                  key={n.idNotificacao}
                  type="button"
                  onClick={() => handleMarcarLida(n)}
                  className={`w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors flex items-start gap-3 ${
                    !n.lida ? 'bg-indigo-50/40' : ''
                  }`}
                >
                  <div
                    className={`p-1.5 rounded-lg shrink-0 mt-0.5 ${
                      n.tipo === 'CANCELAMENTO_SESSAO'
                        ? 'bg-red-100 text-red-600'
                        : 'bg-indigo-100 text-indigo-600'
                    }`}
                  >
                    {n.tipo === 'CANCELAMENTO_SESSAO' ? (
                      <CalendarX2 className="w-4 h-4" />
                    ) : (
                      <Info className="w-4 h-4" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <p className={`text-xs font-semibold truncate ${!n.lida ? 'text-gray-900' : 'text-gray-700'}`}>
                        {n.titulo}
                      </p>
                      <span className="text-[10px] text-gray-400 shrink-0">{formatarDataHora(n.criadoEm)}</span>
                    </div>
                    <p className="text-xs text-gray-600 mt-0.5 leading-relaxed">{n.mensagem}</p>
                  </div>
                  {!n.lida && <span className="w-2 h-2 rounded-full bg-indigo-600 shrink-0 mt-2" />}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
