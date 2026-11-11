import { useState, type FormEvent } from 'react';
import {
  DollarSign,
  Package,
  CalendarClock,
  Plus,
  Trash2,
  Loader2,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';
import { financeiroService } from '../services/financeiroService';
import type { ClinicaPlanoPrazo, PacoteParticular, TabelaPreco } from '../types/financeiro';
import type { TipoCompromisso } from '../types/compromisso';
import type { PlanoSaudeOption } from '../types/paciente';

interface TabelaPrecosTabProps {
  clinicaId: string;
  canManage: boolean;
  precos: TabelaPreco[];
  pacotes: PacoteParticular[];
  prazosConvenios: ClinicaPlanoPrazo[];
  tiposCompromisso: TipoCompromisso[];
  planos: PlanoSaudeOption[];
  onRefresh: () => void;
}

const formatBRL = (v?: number | null) =>
  (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function TabelaPrecosTab({
  clinicaId,
  canManage,
  precos,
  pacotes,
  prazosConvenios,
  tiposCompromisso,
  planos,
  onRefresh,
}: TabelaPrecosTabProps) {
  // Formulário de preço unitário
  const [precoTipoId, setPrecoTipoId] = useState('');
  const [precoPlanoId, setPrecoPlanoId] = useState('');
  const [precoValor, setPrecoValor] = useState('');
  const [savingPreco, setSavingPreco] = useState(false);

  // Formulário de pacote particular
  const [nomePacote, setNomePacote] = useState('');
  const [descricaoPacote, setDescricaoPacote] = useState('');
  const [valorPacote, setValorPacote] = useState('');
  const [validadeMeses, setValidadeMeses] = useState('1');
  const [pacoteItens, setPacoteItens] = useState<{ idTipoCompromisso: string; quantidadeSessoes: number }[]>([
    { idTipoCompromisso: '', quantidadeSessoes: 4 },
  ]);
  const [savingPacote, setSavingPacote] = useState(false);

  // Formulário de prazo de convênio
  const [prazoPlanoId, setPrazoPlanoId] = useState('');
  const [diaCorteEnvio, setDiaCorteEnvio] = useState('5');
  const [prazoPagamentoDias, setPrazoPagamentoDias] = useState('30');
  const [savingPrazo, setSavingPrazo] = useState(false);

  const [feedback, setFeedback] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);

  const handleSalvarPreco = async (e: FormEvent) => {
    e.preventDefault();
    if (!precoTipoId || !precoValor || Number(precoValor) <= 0) return;
    setSavingPreco(true);
    setFeedback(null);
    try {
      await financeiroService.savePreco({
        idClinica: clinicaId || undefined,
        idTipoCompromisso: precoTipoId,
        idPlanoSaude: precoPlanoId || null,
        valorSessao: Number(precoValor),
      });
      setPrecoValor('');
      setFeedback({ type: 'ok', text: 'Valor salvo na tabela da clínica.' });
      onRefresh();
    } catch {
      setFeedback({ type: 'err', text: 'Não foi possível salvar o valor.' });
    } finally {
      setSavingPreco(false);
    }
  };

  const handleExcluirPreco = async (id: string) => {
    setFeedback(null);
    try {
      await financeiroService.deletePreco(id);
      onRefresh();
    } catch {
      setFeedback({ type: 'err', text: 'Não foi possível excluir o preço.' });
    }
  };

  const handleSalvarPacote = async (e: FormEvent) => {
    e.preventDefault();
    const itensValidos = pacoteItens.filter((i) => i.idTipoCompromisso && i.quantidadeSessoes > 0);
    if (!nomePacote.trim() || !valorPacote || Number(valorPacote) <= 0 || itensValidos.length === 0) {
      setFeedback({ type: 'err', text: 'Preencha nome, valor total e ao menos uma especialidade do pacote.' });
      return;
    }
    setSavingPacote(true);
    setFeedback(null);
    try {
      await financeiroService.createPacote({
        idClinica: clinicaId || undefined,
        nomePacote: nomePacote.trim(),
        descricao: descricaoPacote.trim() || undefined,
        valorTotalPacote: Number(valorPacote),
        validadeMesesSugerida: validadeMeses ? Number(validadeMeses) : undefined,
        itens: itensValidos,
      });
      setNomePacote('');
      setDescricaoPacote('');
      setValorPacote('');
      setPacoteItens([{ idTipoCompromisso: '', quantidadeSessoes: 4 }]);
      setFeedback({ type: 'ok', text: 'Pacote/Combo cadastrado com sucesso.' });
      onRefresh();
    } catch {
      setFeedback({ type: 'err', text: 'Erro ao salvar pacote particular.' });
    } finally {
      setSavingPacote(false);
    }
  };

  const handleExcluirPacote = async (id: string) => {
    try {
      await financeiroService.deletePacote(id);
      onRefresh();
    } catch {
      setFeedback({ type: 'err', text: 'Não foi possível excluir o pacote.' });
    }
  };

  const handleSalvarPrazo = async (e: FormEvent) => {
    e.preventDefault();
    if (!prazoPlanoId || !clinicaId) return;
    setSavingPrazo(true);
    setFeedback(null);
    try {
      await financeiroService.savePrazoConvenio({
        idClinica: clinicaId,
        idPlanoSaude: prazoPlanoId,
        diaCorteEnvio: diaCorteEnvio ? Number(diaCorteEnvio) : undefined,
        prazoPagamentoDias: prazoPagamentoDias ? Number(prazoPagamentoDias) : 30,
      });
      setFeedback({ type: 'ok', text: 'Prazos do convênio atualizados.' });
      onRefresh();
    } catch {
      setFeedback({ type: 'err', text: 'Não foi possível salvar os prazos do convênio.' });
    } finally {
      setSavingPrazo(false);
    }
  };

  const precosParticulares = precos.filter((p) => p.particular);
  const precosConvenios = precos.filter((p) => !p.particular);

  return (
    <div className="space-y-6">
      {feedback && (
        <div
          className={`flex items-center gap-2 p-3 rounded-xl text-sm border ${
            feedback.type === 'ok'
              ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
              : 'bg-red-50 border-red-200 text-red-700'
          }`}
        >
          {feedback.type === 'ok' ? (
            <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
          ) : (
            <AlertCircle className="h-4 w-4 flex-shrink-0" />
          )}
          <span>{feedback.text}</span>
        </div>
      )}

      {/* 1. Tabela de Preços Unitários (Particular e Convênios) */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 space-y-4">
        <div className="flex items-center gap-2">
          <DollarSign className="h-5 w-5 text-indigo-600" />
          <div>
            <h3 className="text-base font-bold text-gray-900">Tabela de Preços por Especialidade</h3>
            <p className="text-xs text-gray-500">
              Defina o valor particular padrão da clínica e os valores negociados por convênio.
            </p>
          </div>
        </div>

        {canManage && (
          <form onSubmit={handleSalvarPreco} className="grid grid-cols-1 md:grid-cols-4 gap-3 bg-gray-50 p-3.5 rounded-xl border border-gray-200">
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Especialidade</label>
              <select
                value={precoTipoId}
                onChange={(e) => setPrecoTipoId(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Selecionar especialidade...</option>
                {tiposCompromisso.map((t) => (
                  <option key={t.idTipoCompromisso} value={t.idTipoCompromisso}>
                    {t.nomeTipo}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Tabela (Particular / Convênio)</label>
              <select
                value={precoPlanoId}
                onChange={(e) => setPrecoPlanoId(e.target.value)}
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Particular (Padrão)</option>
                {planos.map((pl) => (
                  <option key={pl.idPlanoSaude} value={pl.idPlanoSaude}>
                    {pl.nomePlano}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Valor da Sessão (R$)</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={precoValor}
                onChange={(e) => setPrecoValor(e.target.value)}
                placeholder="Ex: 100.00"
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              />
            </div>
            <div className="flex items-end">
              <button
                type="submit"
                disabled={savingPreco}
                className="w-full px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center justify-center gap-1.5 disabled:opacity-50"
              >
                {savingPreco ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Plus className="h-3.5 w-3.5" />}
                Salvar Preço
              </button>
            </div>
          </form>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="border border-gray-100 rounded-xl p-3.5">
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-600 mb-2.5">
              Valores Particulares
            </h4>
            {precosParticulares.length === 0 ? (
              <p className="text-xs text-gray-400 italic">Nenhum preço particular cadastrado.</p>
            ) : (
              <div className="space-y-1.5">
                {precosParticulares.map((pr) => (
                  <div key={pr.idTabelaPreco} className="flex items-center justify-between text-xs py-1.5 border-b border-gray-100 last:border-0">
                    <span className="font-medium text-gray-800">{pr.nomeTipoCompromisso}</span>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-emerald-700">{formatBRL(pr.valorSessao)}</span>
                      {canManage && (
                        <button
                          onClick={() => handleExcluirPreco(pr.idTabelaPreco)}
                          className="text-gray-400 hover:text-red-600 p-1"
                          title="Remover preço"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="border border-gray-100 rounded-xl p-3.5">
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-600 mb-2.5">
              Repasse por Convênio
            </h4>
            {precosConvenios.length === 0 ? (
              <p className="text-xs text-gray-400 italic">Nenhum preço de convênio cadastrado.</p>
            ) : (
              <div className="space-y-1.5">
                {precosConvenios.map((pr) => (
                  <div key={pr.idTabelaPreco} className="flex items-center justify-between text-xs py-1.5 border-b border-gray-100 last:border-0">
                    <div>
                      <span className="font-medium text-gray-800">{pr.nomeTipoCompromisso}</span>
                      <span className="ml-1.5 px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-700 text-[10px] font-semibold">
                        {pr.nomePlanoSaude}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-indigo-700">{formatBRL(pr.valorSessao)}</span>
                      {canManage && (
                        <button
                          onClick={() => handleExcluirPreco(pr.idTabelaPreco)}
                          className="text-gray-400 hover:text-red-600 p-1"
                          title="Remover preço"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 2. Pacotes e Combos Particulares */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 space-y-4">
        <div className="flex items-center gap-2">
          <Package className="h-5 w-5 text-emerald-600" />
          <div>
            <h3 className="text-base font-bold text-gray-900">Pacotes & Combos Particulares</h3>
            <p className="text-xs text-gray-500">
              Monte pacotes promocionais (ex.: 10 sessões por R$ 850 ou combo mensal 4 TO + 2 Psicologia).
            </p>
          </div>
        </div>

        {canManage && (
          <form onSubmit={handleSalvarPacote} className="bg-gray-50 p-3.5 rounded-xl border border-gray-200 space-y-3">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              <div className="md:col-span-2">
                <label className="block text-[11px] font-medium text-gray-600 mb-1">Nome do Pacote / Combo</label>
                <input
                  type="text"
                  value={nomePacote}
                  onChange={(e) => setNomePacote(e.target.value)}
                  placeholder="Ex: Combo Mensal 4 TO + 2 Psico"
                  className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                />
              </div>
              <div>
                <label className="block text-[11px] font-medium text-gray-600 mb-1">Valor Fechado (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={valorPacote}
                  onChange={(e) => setValorPacote(e.target.value)}
                  placeholder="Ex: 850.00"
                  className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                />
              </div>
              <div>
                <label className="block text-[11px] font-medium text-gray-600 mb-1">Ciclo Sugerido (meses)</label>
                <input
                  type="number"
                  min="1"
                  value={validadeMeses}
                  onChange={(e) => setValidadeMeses(e.target.value)}
                  className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-semibold text-gray-700">Sessões por Especialidade</span>
                <button
                  type="button"
                  onClick={() => setPacoteItens((prev) => [...prev, { idTipoCompromisso: '', quantidadeSessoes: 2 }])}
                  className="text-xs font-medium text-indigo-600 hover:underline flex items-center gap-1"
                >
                  <Plus className="h-3 w-3" /> Adicionar especialidade
                </button>
              </div>
              {pacoteItens.map((item, idx) => (
                <div key={idx} className="grid grid-cols-1 md:grid-cols-3 gap-2 items-center">
                  <select
                    value={item.idTipoCompromisso}
                    onChange={(e) =>
                      setPacoteItens((prev) =>
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
                      value={item.quantidadeSessoes}
                      onChange={(e) =>
                        setPacoteItens((prev) =>
                          prev.map((it, i) =>
                            i === idx ? { ...it, quantidadeSessoes: Number(e.target.value) || 1 } : it
                          )
                        )
                      }
                      className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                    />
                    {pacoteItens.length > 1 && (
                      <button
                        type="button"
                        onClick={() => setPacoteItens((prev) => prev.filter((_, i) => i !== idx))}
                        className="text-gray-400 hover:text-red-600 p-1"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={savingPacote}
                className="px-4 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg flex items-center gap-1.5 disabled:opacity-50"
              >
                {savingPacote ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Plus className="h-3.5 w-3.5" />}
                Cadastrar Pacote
              </button>
            </div>
          </form>
        )}

        {pacotes.length === 0 ? (
          <p className="text-xs text-gray-400 italic">Nenhum pacote particular cadastrado ainda.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {pacotes.map((pkg) => (
              <div key={pkg.idPacote} className="border border-gray-200 rounded-xl p-3.5 flex flex-col justify-between gap-2">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h4 className="text-sm font-bold text-gray-900">{pkg.nomePacote}</h4>
                    <p className="text-[11px] text-gray-500">
                      {pkg.totalSessoes} sessões
                      {pkg.validadeMesesSugerida ? ` · ciclo ${pkg.validadeMesesSugerida} mês(es)` : ''}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="text-sm font-bold text-emerald-700 block">
                      {formatBRL(pkg.valorTotalPacote)}
                    </span>
                    {pkg.valorSemDescontoCalculado && pkg.valorSemDescontoCalculado > pkg.valorTotalPacote ? (
                      <span className="text-[10px] text-gray-400 line-through">
                        {formatBRL(pkg.valorSemDescontoCalculado)}
                      </span>
                    ) : null}
                  </div>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {pkg.itens.map((it, idx) => (
                    <span
                      key={idx}
                      className="px-2 py-0.5 rounded-md bg-gray-100 text-gray-700 text-[11px] font-medium"
                    >
                      {it.quantidadeSessoes}x {it.nomeTipoCompromisso}
                    </span>
                  ))}
                </div>
                {canManage && (
                  <div className="flex justify-end pt-1">
                    <button
                      onClick={() => handleExcluirPacote(pkg.idPacote)}
                      className="text-[11px] text-red-600 hover:underline flex items-center gap-1"
                    >
                      <Trash2 className="h-3 w-3" /> Excluir
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 3. Prazos de Corte e Recebimento dos Convênios */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 space-y-4">
        <div className="flex items-center gap-2">
          <CalendarClock className="h-5 w-5 text-purple-600" />
          <div>
            <h3 className="text-base font-bold text-gray-900">Prazos de Envio e Recebimento dos Convênios</h3>
            <p className="text-xs text-gray-500">
              Configure até que dia enviar as guias ao convênio e em quantos dias o pagamento entra na clínica.
            </p>
          </div>
        </div>

        {canManage && clinicaId && (
          <form onSubmit={handleSalvarPrazo} className="grid grid-cols-1 md:grid-cols-4 gap-3 bg-gray-50 p-3.5 rounded-xl border border-gray-200">
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Convênio</label>
              <select
                value={prazoPlanoId}
                onChange={(e) => setPrazoPlanoId(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              >
                <option value="">Selecionar plano...</option>
                {planos.map((pl) => (
                  <option key={pl.idPlanoSaude} value={pl.idPlanoSaude}>
                    {pl.nomePlano}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Dia de Corte / Envio</label>
              <input
                type="number"
                min="1"
                max="31"
                value={diaCorteEnvio}
                onChange={(e) => setDiaCorteEnvio(e.target.value)}
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">Prazo de Pagamento (dias)</label>
              <input
                type="number"
                min="1"
                value={prazoPagamentoDias}
                onChange={(e) => setPrazoPagamentoDias(e.target.value)}
                required
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
              />
            </div>
            <div className="flex items-end">
              <button
                type="submit"
                disabled={savingPrazo}
                className="w-full px-3 py-1.5 text-xs font-semibold text-white bg-purple-600 hover:bg-purple-700 rounded-lg flex items-center justify-center gap-1.5 disabled:opacity-50"
              >
                {savingPrazo ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : null}
                Salvar Prazos
              </button>
            </div>
          </form>
        )}

        {prazosConvenios.length === 0 ? (
          <p className="text-xs text-gray-400 italic">Nenhum prazo de convênio configurado para esta clínica.</p>
        ) : (
          <div className="divide-y divide-gray-100">
            {prazosConvenios.map((p) => (
              <div key={p.idClinicaPlanoSaude} className="py-2 flex items-center justify-between text-xs">
                <div>
                  <span className="font-semibold text-gray-800">{p.nomePlano}</span>
                  {p.operadora ? <span className="text-gray-400 ml-1.5">({p.operadora})</span> : null}
                </div>
                <div className="flex items-center gap-4 text-gray-600">
                  <span>{p.diaCorteEnvio ? `Envio até dia ${p.diaCorteEnvio}` : 'Sem dia de corte'}</span>
                  <span className="font-semibold text-purple-700">Recebe em {p.prazoPagamentoDias} dias</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
