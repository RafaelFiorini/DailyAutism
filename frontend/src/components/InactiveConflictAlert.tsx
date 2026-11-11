import { AlertTriangle, Loader2, RotateCcw } from 'lucide-react';
import type { InactiveConflictInfo } from '../utils/apiError';

interface InactiveConflictAlertProps {
  conflict: InactiveConflictInfo | null;
  onReactivate: () => void;
  loading?: boolean;
}

export default function InactiveConflictAlert({
  conflict,
  onReactivate,
  loading = false,
}: InactiveConflictAlertProps) {
  if (!conflict) return null;

  return (
    <div className="p-3.5 rounded-xl bg-amber-50 border border-amber-300 text-amber-900 text-sm space-y-2.5">
      <div className="flex items-start gap-2.5">
        <AlertTriangle className="h-4 w-4 text-amber-600 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <p className="font-semibold text-amber-900">Cadastro desativado encontrado</p>
          <p className="text-xs text-amber-800">{conflict.message}</p>
        </div>
      </div>
      <div className="flex justify-end">
        <button
          type="button"
          onClick={onReactivate}
          disabled={loading}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold transition-colors disabled:opacity-50"
        >
          {loading ? (
            <>
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
              Reativando...
            </>
          ) : (
            <>
              <RotateCcw className="h-3.5 w-3.5" />
              Reativar cadastro existente
            </>
          )}
        </button>
      </div>
    </div>
  );
}
