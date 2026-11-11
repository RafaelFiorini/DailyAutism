import { useState, useMemo, type FormEvent } from 'react';
import { X, Plus, Trash2, Loader2, AlertCircle, Wallet } from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type {
  ContratoParticular,
  ModalidadeContratoParticular,
  PacoteParticular,
  TabelaPreco,
} from '../types/financeiro';
import type { Paciente } from '../types/paciente';
import type { TipoCompromisso } from '../types/compromisso';

interface ContratoParticularModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (contrato: ContratoParticular) => void;
  clinicaId: string;
  pacientes: Paciente[];
  pacotes: PacoteParticular[];
  precos: TabelaPreco[];
  tiposCompromisso: TipoCompromisso[];
}

export default function ContratoParticularModal({
  isOpen,
  onClose,
  onSuccess,
  clinicaId,
  pacientes,
  pacotes,
  precos,
  tiposCompromisso,
}: ContratoParticularModalProps) {
  const [idPaciente, setIdPaciente] = useState('');
  const [idPacote, setIdPacote] = useState('');
  const [multiplicadorMeses, setMultiplicadorMeses] = useState('1');
  const [modalidade, setModalidade] = useState<ModalidadeContratoParticular>('POR_SESSOES');
  const [descricao, setDescricao] = useState('');
  const [dataInicio, setDataInicio] = useState(() => new Date().toISOString().slice(0, 10));
  const [dataFimPrevista, setDataFimPrevista] = useState('');
  const [valorCobrado, setValorCobrado] = useState('');
  const [pagoNaContratacao, setPagoNaContratacao] = useState(true);
  const [formaPagamento, setFormaPagamento] = useState('PIX');
  const [itensManuais, setItensManuais] = useState<{ idTipoCompromisso: string; quantidadeContratada: number }[]>([
    { idTipoCompromisso: '', quantidadeContratada: 4 },
  ]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const pacoteSelecionado = useMemo(
    () => pacotes.find((p) => p.idPacote === idPacote) || null,
    [pacotes, idPacote]
  );

  const valorSugerido = useMemo(() => {
    const mult = Math.max(1, Number(multiplicadorMeses) || 1);
    if (pacoteSelecionado) {
      return pacoteSelecionado.valorTotalPacote * mult;
    }
    return itensManuais.reduce((acc, it) => {
      const preco = precos.find(
        (p) => p.particular && p.idTipoCompromisso === it.idTipoCompromisso
      );
      return acc + (preco?.valorSessao ?? 0) * (it.quantidadeContratada || 0) * mult;
    }, 0);
  }, [pacoteSelecionado, multiplicadorMeses, itensManuais, precos]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (!idPaciente) {
      setError('Selecione o paciente.');
      return;
    }

    const mult = Math.max(1, Number(multiplicadorMeses) || 1);
    const itensValidos = itensManuais.filter((i) => i.idTipoCompromisso && i.quantidadeContratada > 0);
    if (!idPacote && itensValidos.length === 0) {
      setError('Selecione um pacote pronto ou informe ao menos uma especialidade.');
      return;
    }

    setSubmitting(true);
    try {
      const created = await financeiroService.createContratoParticular({
        idClinica: clinicaId || undefined,
        idPaciente,
        idPacote: idPacote || undefined,
        multiplicadorMeses: mult,
        modalidade,
        descricao: descricao.trim() || undefined,
        dataInicio: dataInicio || undefined,
        dataFimPrevista: dataFimPrevista || undefined,
        valorCobrado: valorCobrado ? Number(valorCobrado) : valorSugerido > 0 ? valorSugerido : undefined,
        pagoNaContratacao,
        formaPagamento: pagoNaContratacao ? formaPagamento : undefined,
        itens: idPacote ? undefined : itensValidos,
      });
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      let msg = 'Erro ao criar contrato particular.';
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
      <div className="bg-white rounded-2xl shadow-2xl max-w-xl w-full overflow-hidden max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/60">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center">
              <Wallet className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-gray-900">Novo Fechamento Particular</h3>
              <p className="text-xs text-gray-500">Contrate sessões ou combo mensal pré-pago por especialidade</p>
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

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Paciente *</label>
              <select
                value={idPaciente}
                onChange={(e) => setIdPaciente(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Selecionar paciente...</option>
                {pacientes.map((p) => (
                  <option key={p.idPaciente} value={p.idPaciente}>
                    {p.nomePessoa}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Modalidade</label>
              <select
                value={modalidade}
                onChange={(e) => setModalidade(e.target.value as ModalidadeContratoParticular)}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="POR_SESSOES">Por Quantidade de Sessões</option>
                <option value="POR_PERIODO_MENSAL">Por Período Mensal</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="md:col-span-2">
              <label className="block text-xs font-medium text-gray-700 mb-1">Usar Pacote / Combo (opcional)</label>
              <select
                value={idPacote}
                onChange={(e) => setIdPacote(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Personalizado (montar itens manualmente)</option>
                {pacotes.map((pkg) => (
                  <option key={pkg.idPacote} value={pkg.idPacote}>
                    {pkg.nomePacote} — R$ {pkg.valorTotalPacote.toFixed(2)}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Meses / Multiplicador</label>
              <input
                type="number"
                min="1"
                value={multiplicadorMeses}
                onChange={(e) => setMultiplicadorMeses(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg bg-white"
              />
            </div>
          </div>

          {pacoteSelecionado ? (
            <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-900 space-y-1">
              <div className="font-semibold">
                Cotas contratadas ({Number(multiplicadorMeses) || 1}x {pacoteSelecionado.nomePacote}):
              </div>
              <div className="flex flex-wrap gap-1.5">
                {pacoteSelecionado.itens.map((it, i) => (
                  <span key={i} className="px-2 py-0.5 rounded bg-white border border-emerald-200 font-medium">
                    {it.quantidadeSessoes * Math.max(1, Number(multiplicadorMeses) || 1)}x {it.nomeTipoCompromisso}
                  </span>
                ))}
              </div>
            </div>
          ) : (
            <div className="border border-gray-200 rounded-xl p-3 space-y-2 bg-gray-50/60">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-gray-700">Cotas por Especialidade</span>
                <button
                  type="button"
                  onClick={() =>
                    setItensManuais((prev) => [...prev, { idTipoCompromisso: '', quantidadeContratada: 2 }])
                  }
                  className="text-xs font-medium text-indigo-600 hover:underline flex items-center gap-1"
                >
                  <Plus className="h-3 w-3" /> Adicionar especialidade
                </button>
              </div>
              {itensManuais.map((item, idx) => (
                <div key={idx} className="grid grid-cols-1 md:grid-cols-3 gap-2 items-center">
                  <select
                    value={item.idTipoCompromisso}
                    onChange={(e) =>
                      setItensManuais((prev) =>
                        prev.map((it, i) => (i === idx ? { ...it, idTipoCompromisso: e.target.value } : it))
                      )
                    }
                    className="md:col-span-2 px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                  >
                    <option value="">Selecionar especialidade...</option>
                    {tiposCompromisso.map((t) => (
                      <option key={t.idTipoCompromisso} value={t.idTipoCompromisso}>
                        {t.nomeTipo}
                      </option>
                    ))}
                  </select>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      min="1"
                      value={item.quantidadeContratada}
                      onChange={(e) =>
                        setItensManuais((prev) =>
                          prev.map((it, i) =>
                            i === idx ? { ...it, quantidadeContratada: Number(e.target.value) || 1 } : it
                          )
                        )
                      }
                      className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                    />
                    {itensManuais.length > 1 && (
                      <button
                        type="button"
                        onClick={() => setItensManuais((prev) => prev.filter((_, i) => i !== idx))}
                        className="text-gray-400 hover:text-red-600 p-1"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Início</label>
              <input
                type="date"
                value={dataInicio}
                onChange={(e) => setDataInicio(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Previsão Término</label>
              <input
                type="date"
                value={dataFimPrevista}
                onChange={(e) => setDataFimPrevista(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Valor Cobrado (Sugestão: R$ {valorSugerido.toFixed(2)})
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={valorCobrado}
                onChange={(e) => setValorCobrado(e.target.value)}
                placeholder={valorSugerido > 0 ? valorSugerido.toFixed(2) : '0.00'}
                className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg font-semibold"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">Descrição (opcional)</label>
            <input
              type="text"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Ex: Fechamento bimestral - 8 TO + 4 Psicologia"
              className="w-full px-3 py-2 text-xs border border-gray-200 rounded-lg"
            />
          </div>

          <div className="bg-gray-50 p-3 rounded-xl border border-gray-200 flex flex-wrap items-center justify-between gap-3">
            <label className="flex items-center gap-2 text-xs font-medium text-gray-800 cursor-pointer">
              <input
                type="checkbox"
                checked={pagoNaContratacao}
                onChange={(e) => setPagoNaContratacao(e.target.checked)}
                className="h-4 w-4 rounded border-gray-300 text-emerald-600"
              />
              Pagamento antecipado já recebido
            </label>
            {pagoNaContratacao && (
              <select
                value={formaPagamento}
                onChange={(e) => setFormaPagamento(e.target.value)}
                className="px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="PIX">PIX</option>
                <option value="CARTAO">Cartão</option>
                <option value="DINHEIRO">Dinheiro</option>
                <option value="TRANSFERENCIA">Transferência</option>
              </select>
            )}
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg flex items-center gap-1.5 disabled:opacity-50"
            >
              {submitting ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Salvar Contrato
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
