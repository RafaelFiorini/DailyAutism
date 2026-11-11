interface ActiveStatusFilterProps {
  showInativos: boolean;
  onChange: (showInativos: boolean) => void;
}

export default function ActiveStatusFilter({ showInativos, onChange }: ActiveStatusFilterProps) {
  return (
    <div
      className="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-1 text-xs font-medium shrink-0"
      role="group"
      aria-label="Filtro de registros ativos ou inativos"
    >
      <button
        type="button"
        onClick={() => onChange(false)}
        className={`px-3 py-1.5 rounded-md transition-colors ${
          !showInativos
            ? 'bg-white text-indigo-700 shadow-sm font-semibold'
            : 'text-gray-600 hover:text-gray-900'
        }`}
      >
        Ativos
      </button>
      <button
        type="button"
        onClick={() => onChange(true)}
        className={`px-3 py-1.5 rounded-md transition-colors ${
          showInativos
            ? 'bg-white text-amber-700 shadow-sm font-semibold'
            : 'text-gray-600 hover:text-gray-900'
        }`}
      >
        Inativos
      </button>
    </div>
  );
}
