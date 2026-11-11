import { useState, type FormEvent } from 'react';
import { X, FileCheck2, FileText, ShieldCheck, Loader2, AlertCircle, Lock } from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type { GuiaFaturamento } from '../types/financeiro';

interface AssinarGuiaModalProps {
  isOpen: boolean;
  guia: GuiaFaturamento | null;
  onClose: () => void;
  onSigned: (guia: GuiaFaturamento) => void;
}

const formatBRL = (v?: number | null) =>
  (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

const formatDateBR = (iso?: string | null) =>
  iso ? iso.slice(0, 10).split('-').reverse().join('/') : '-';

const formatDateTimeBR = (iso?: string | null) => {
  if (!iso) return '-';
  const [datePart, timePart] = iso.split('T');
  return `${formatDateBR(datePart)} ${timePart ? timePart.slice(0, 5) : ''}`.trim();
};

export default function AssinarGuiaModal({
  isOpen,
  guia,
  onClose,
  onSigned,
}: AssinarGuiaModalProps) {
  const [senha, setSenha] = useState('');
  const [aceite, setAceite] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen || !guia) return null;

  const handleDownloadPdf = async () => {
    setDownloading(true);
    setError('');
    try {
      const blob = await financeiroService.downloadGuiaPdf(guia.idGuiaFaturamento);
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank', 'noopener,noreferrer');
    } catch {
      setError('Não foi possível abrir o PDF da guia.');
    } finally {
      setDownloading(false);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (!aceite) {
      setError('Confirme que conferiu os atendimentos listados na guia.');
      return;
    }
    if (!senha.trim()) {
      setError('Informe sua senha de acesso para confirmar a assinatura eletrônica.');
      return;
    }

    setSubmitting(true);
    try {
      const assinada = await financeiroService.assinarGuia(guia.idGuiaFaturamento, senha);
      setSenha('');
      setAceite(false);
      onSigned(assinada);
      onClose();
    } catch (err: unknown) {
      let msg = 'Erro ao assinar guia eletronicamente.';
      if (err && typeof err === 'object' && 'response' in err) {
        const res = (err as { response?: { data?: { message?: string; errorMessage?: string } } }).response;
        msg = res?.data?.errorMessage || res?.data?.message || msg;
      }
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full overflow-hidden max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/60">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-lg bg-indigo-100 text-indigo-700 flex items-center justify-center">
              <FileCheck2 className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-gray-900">
                Conferência & Assinatura da Guia {guia.numeroLoteGuia}
              </h3>
              <p className="text-xs text-gray-500">
                {guia.nomePaciente} · {guia.nomePlanoSaude} · {formatDateBR(guia.competenciaInicio)} a{' '}
                {formatDateBR(guia.competenciaFim)}
              </p>
            </div>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 p-1 rounded-lg">
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
          {error && (
            <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-700 flex items-center gap-2">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="flex items-center justify-between bg-indigo-50/70 border border-indigo-100 rounded-xl p-3">
            <div className="text-xs text-indigo-950">
              <span className="font-semibold block">
                {guia.itens.length} atendimento(s) realizado(s) · Total: {formatBRL(guia.valorTotal)}
              </span>
              <span className="text-indigo-700">
                Confira os atendimentos abaixo ou abra o PDF completo antes de assinar.
              </span>
            </div>
            <button
              type="button"
              onClick={handleDownloadPdf}
              disabled={downloading}
              className="px-3 py-1.5 text-xs font-semibold text-indigo-700 bg-white border border-indigo-200 hover:bg-indigo-50 rounded-lg flex items-center gap-1.5 shrink-0"
            >
              {downloading ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <FileText className="h-3.5 w-3.5" />}
              Visualizar PDF
            </button>
          </div>

          <div className="border border-gray-200 rounded-xl overflow-hidden max-h-52 overflow-y-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-gray-50 text-gray-600 font-semibold border-b border-gray-200">
                <tr>
                  <th className="px-3 py-2">Data/Hora</th>
                  <th className="px-3 py-2">Especialidade</th>
                  <th className="px-3 py-2">Profissional</th>
                  <th className="px-3 py-2 text-right">Valor</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {guia.itens.map((item) => (
                  <tr key={item.idGuiaItem}>
                    <td className="px-3 py-2 text-gray-700">{formatDateTimeBR(item.dataHoraAtendimento)}</td>
                    <td className="px-3 py-2 font-medium text-gray-900">{item.tipoCompromissoNome}</td>
                    <td className="px-3 py-2 text-gray-600">
                      {item.profissionalNome}
                      {item.conselhoProfissional ? ` (${item.conselhoProfissional})` : ''}
                    </td>
                    <td className="px-3 py-2 text-right font-semibold text-gray-800">
                      {formatBRL(item.valorUnitarioConvenio)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <label className="flex items-start gap-2.5 p-3 rounded-xl bg-gray-50 border border-gray-200 text-xs text-gray-700 cursor-pointer">
            <input
              type="checkbox"
              checked={aceite}
              onChange={(e) => setAceite(e.target.checked)}
              className="mt-0.5 h-4 w-4 rounded border-gray-300 text-indigo-600"
            />
            <span>
              Declaro que conferi os atendimentos listados nesta guia do convênio{' '}
              <strong>{guia.nomePlanoSaude}</strong> e autorizo o envio pela clínica para faturamento.
            </span>
          </label>

          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1 flex items-center gap-1.5">
              <Lock className="h-3.5 w-3.5 text-gray-500" />
              Confirme sua senha de login para assinar *
            </label>
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="Digite sua senha atual..."
              className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg"
            >
              Fechar
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center gap-1.5 disabled:opacity-50"
            >
              {submitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <ShieldCheck className="h-4 w-4" />}
              Assinar Eletronicamente
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
