import { DollarSign } from 'lucide-react';
import FinanceiroProfissionalTab from '../components/FinanceiroProfissionalTab';

export default function FinanceiroProfissionalPage() {
  return (
    <div className="space-y-6">
      {/* Cabeçalho Dedicado */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
            <DollarSign className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-gray-900">Meu Financeiro & Repasses</h1>
            <p className="text-xs text-gray-500">
              Acompanhe repasses de múltiplas clínicas, sessões particulares e quitações.
            </p>
          </div>
        </div>
      </div>

      {/* Conteúdo autônomo do financeiro do profissional */}
      <FinanceiroProfissionalTab />
    </div>
  );
}
