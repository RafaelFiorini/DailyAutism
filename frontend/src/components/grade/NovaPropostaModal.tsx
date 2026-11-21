import React, { useState, useEffect } from 'react';
import { X, Plus, Trash2, AlertCircle, Send, Save, Sparkles } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import type { SaveGradeTerapeutica, SaveGradeTerapeuticaItem, DiaSemana } from '../../types/gradeTerapeutica';
import { DIAS_SEMANA_ORDENADOS } from '../../types/gradeTerapeutica';
import { pacienteService } from '../../services/pacienteService';
import { profissionalService } from '../../services/profissionalService';
import { compromissoService } from '../../services/compromissoService';
import { periodicidadeService } from '../../services/periodicidadeService';
import type { Paciente } from '../../types/paciente';
import type { Profissional } from '../../types/profissional';
import type { TipoCompromisso } from '../../types/compromisso';
import type { Periodicidade } from '../../types/periodicidade';

interface NovaPropostaModalProps {
  isOpen: boolean;
  onClose: () => Unit | void;
  onSalvar: (dados: SaveGradeTerapeutica) => Promise<void>;
  idPacientePreselecionado?: string;
}

type Unit = undefined;

export const NovaPropostaModal: React.FC<NovaPropostaModalProps> = ({
  isOpen,
  onClose,
  onSalvar,
  idPacientePreselecionado,
}) => {
  const { user } = useAuth();
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [profissionais, setProfissionais] = useState<Profissional[]>([]);
  const [tiposCompromisso, setTiposCompromisso] = useState<TipoCompromisso[]>([]);
  const [periodicidades, setPeriodicidades] = useState<Periodicidade[]>([]);

  const [idPaciente, setIdPaciente] = useState(idPacientePreselecionado || '');
  const [dataInicioVigencia, setDataInicioVigencia] = useState('');
  const [dataFimVigencia, setDataFimVigencia] = useState('');
  const [tipoOrigemVigencia, setTipoOrigemVigencia] = useState('GUIA_CONVENIO');
  const [infoGuia, setInfoGuia] = useState<string | null>(null);

  const [itens, setItens] = useState<SaveGradeTerapeuticaItem[]>([
    {
      diaSemana: 'SEGUNDA',
      horaInicio: '14:00',
      horaFim: '15:00',
      idTipoCompromisso: '',
      idProfissional: '',
      sala: '',
    },
  ]);

  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      carregarListasAuxiliares();
    }
  }, [isOpen]);

  useEffect(() => {
    if (idPacientePreselecionado) {
      setIdPaciente(idPacientePreselecionado);
      carregarVigenciaPaciente(idPacientePreselecionado);
    }
  }, [idPacientePreselecionado]);

  async function carregarListasAuxiliares() {
    try {
      const [pacData, profData, tiposData, periodData] = await Promise.all([
        pacienteService.list(),
        profissionalService.list(),
        compromissoService.listTipos(),
        periodicidadeService.list(),
      ]);
      setPacientes(pacData);
      setProfissionais(profData);
      setTiposCompromisso(tiposData);
      setPeriodicidades(periodData);

      // Se nenhum paciente foi pré-selecionado na prop, mas há pacientes, pré-seleciona o primeiro
      if (!idPacientePreselecionado && !idPaciente && pacData.length > 0) {
        setIdPaciente(pacData[0].idPaciente);
        carregarVigenciaPaciente(pacData[0].idPaciente);
      }

      // Define periodicidade padrão (ex: Semanal) no item inicial se ainda não preenchido
      const semanal = periodData.find((p) => p.nomePeriodicidade?.toLowerCase().includes('semanal')) || periodData[0];
      if (semanal) {
        setItens((prev) =>
          prev.map((it) => (it.idPeriodicidade ? it : { ...it, idPeriodicidade: semanal.idPeriodicidade }))
        );
      }
    } catch (e: any) {
      console.error('Erro ao carregar dados auxiliares:', e);
    }
  }

  function formatarDataBR(dataIso?: string): string {
    if (!dataIso) return '';
    const partes = dataIso.split('-');
    if (partes.length === 3) {
      return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataIso;
  }

  async function carregarVigenciaPaciente(pacId: string) {
    if (!pacId) return;
    try {
      const pac = await pacienteService.getById(pacId);
      const qtdSessoes = pac.quantidadeSessoesLiberadas ?? 0;
      const isPorQuantidade =
        pac.tipoLiberacao === 'QUANTIDADE' || (!pac.vencimentoLiberacao && qtdSessoes > 0);
      const isPorData = !isPorQuantidade && Boolean(pac.vencimentoLiberacao);

      if (isPorQuantidade) {
        const hoje = new Date();
        const seisMeses = new Date();
        seisMeses.setMonth(seisMeses.getMonth() + 6);
        setDataInicioVigencia(pac.dataInicioLiberacao || hoje.toISOString().split('T')[0]);
        setDataFimVigencia(pac.vencimentoLiberacao || seisMeses.toISOString().split('T')[0]);
        setTipoOrigemVigencia('GUIA_CONVENIO');
        setInfoGuia(
          `Guia de Convênio: ${qtdSessoes} ${qtdSessoes === 1 ? 'sessão autorizada' : 'sessões autorizadas'}`
        );
      } else if (isPorData && pac.vencimentoLiberacao) {
        setDataFimVigencia(pac.vencimentoLiberacao);
        setDataInicioVigencia(pac.dataInicioLiberacao || new Date().toISOString().split('T')[0]);
        setTipoOrigemVigencia('GUIA_CONVENIO');
        setInfoGuia(`Guia de Convênio: Vencimento em ${formatarDataBR(pac.vencimentoLiberacao)}`);
      } else {
        // Padrão de 6 meses
        const hoje = new Date();
        const seisMeses = new Date();
        seisMeses.setMonth(seisMeses.getMonth() + 6);
        setDataInicioVigencia(hoje.toISOString().split('T')[0]);
        setDataFimVigencia(seisMeses.toISOString().split('T')[0]);
        setTipoOrigemVigencia('CONTRATO_PARTICULAR');
        setInfoGuia('Plano/Contrato Particular: Vigência estimada de 6 meses');
      }
    } catch (e) {
      console.error('Erro ao buscar dados do paciente:', e);
    }
  }

  function handleSelecionarPaciente(pacId: string) {
    setIdPaciente(pacId);
    carregarVigenciaPaciente(pacId);
  }

  function handleAddItem() {
    const semanal = periodicidades.find((p) => p.nomePeriodicidade?.toLowerCase().includes('semanal')) || periodicidades[0];
    setItens([
      ...itens,
      {
        diaSemana: 'SEGUNDA',
        horaInicio: '14:00',
        horaFim: '15:00',
        idTipoCompromisso: tiposCompromisso[0]?.idTipoCompromisso || '',
        idProfissional: '',
        idPeriodicidade: semanal?.idPeriodicidade || '',
        sala: '',
      },
    ]);
  }

  function handleRemoveItem(index: number) {
    if (itens.length <= 1) {
      setErro('A grade terapêutica deve conter ao menos um horário semanal.');
      return;
    }
    setItens(itens.filter((_, i) => i !== index));
  }

  function handleChangeItem(index: number, field: keyof SaveGradeTerapeuticaItem, value: any) {
    const novos = [...itens];
    novos[index] = { ...novos[index], [field]: value };
    setItens(novos);
  }

  async function handleSubmit(enviarParaAprovacao: boolean) {
    setErro(null);

    if (!idPaciente) {
      setErro('Por favor, selecione o paciente.');
      return;
    }
    if (!dataInicioVigencia || !dataFimVigencia) {
      setErro('Por favor, informe o período de vigência.');
      return;
    }
    if (new Date(dataInicioVigencia) > new Date(dataFimVigencia)) {
      setErro('A data inicial da vigência não pode ser posterior à data final.');
      return;
    }

    for (let i = 0; i < itens.length; i++) {
      const item = itens[i];
      if (!item.idTipoCompromisso) {
        setErro(`Selecione a especialidade na linha ${i + 1}.`);
        return;
      }
      if (!item.horaInicio || !item.horaFim) {
        setErro(`Informe os horários de início e término na linha ${i + 1}.`);
        return;
      }
      if (item.horaInicio >= item.horaFim) {
        setErro(`A hora de início deve ser menor que a hora de término na linha ${i + 1}.`);
        return;
      }
    }

    try {
      setLoading(true);
      const payload: SaveGradeTerapeutica = {
        idPaciente,
        idClinica: user?.idClinica || undefined,
        dataInicioVigencia,
        dataFimVigencia,
        tipoOrigemVigencia,
        enviarParaAprovacao,
        itens,
      };
      await onSalvar(payload);
      onClose();
    } catch (e: any) {
      console.error('Erro ao salvar proposta:', e);
      setErro(e.response?.data?.message || e.message || 'Erro ao salvar grade terapêutica.');
    } finally {
      setLoading(false);
    }
  }

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl max-w-5xl w-full max-h-[92vh] flex flex-col shadow-2xl border border-gray-200 dark:border-gray-700 animate-in fade-in zoom-in-95 duration-200">
        {/* Topo do Modal */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-primary-600" />
            <h2 className="text-lg font-bold text-gray-900 dark:text-gray-100">
              Nova Proposta de Grade Terapêutica
            </h2>
          </div>
          <button
            onClick={() => !loading && onClose()}
            className="p-1 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Corpo do Modal rolável */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          {erro && (
            <div className="flex items-start gap-2 p-3.5 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-300 text-sm">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <span>{erro}</span>
            </div>
          )}

          {/* Seleção do Paciente e Vigência */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1.5">
                Paciente *
              </label>
              <select
                value={idPaciente}
                onChange={(e) => handleSelecionarPaciente(e.target.value)}
                disabled={loading || !!idPacientePreselecionado}
                className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:outline-none"
              >
                <option value="">Selecione o paciente...</option>
                {pacientes.map((p) => (
                  <option key={p.idPaciente} value={p.idPaciente}>
                    {p.nomePessoa || p.apelidoPaciente || 'Paciente'}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1.5">
                Início da Vigência *
              </label>
              <input
                type="date"
                value={dataInicioVigencia}
                onChange={(e) => setDataInicioVigencia(e.target.value)}
                disabled={loading}
                className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider mb-1.5">
                Término da Vigência (Guia/Contrato) *
              </label>
              <input
                type="date"
                value={dataFimVigencia}
                onChange={(e) => setDataFimVigencia(e.target.value)}
                disabled={loading}
                className="w-full px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:outline-none"
              />
            </div>
          </div>

          {infoGuia && (
            <div className="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-xl text-blue-800 dark:text-blue-200 text-xs flex items-center justify-between">
              <span className="font-medium">📋 {infoGuia}</span>
              <span className="text-[11px] text-blue-600 dark:text-blue-300 italic">
                A grade programa apenas as especialidades com profissionais disponíveis na clínica.
              </span>
            </div>
          )}

          {/* Horários Semanais */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <div>
                <h3 className="font-bold text-gray-900 dark:text-gray-100 text-sm">
                  Horários Semanais ({itens.length} {itens.length === 1 ? 'sessão' : 'sessões'} por semana)
                </h3>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Defina os dias da semana e horários fixos que serão replicados até o fim da vigência.
                </p>
              </div>
              <button
                type="button"
                onClick={handleAddItem}
                disabled={loading}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary-50 hover:bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 rounded-lg text-xs font-semibold transition"
              >
                <Plus className="w-3.5 h-3.5" /> Adicionar Horário
              </button>
            </div>

            <div className="space-y-3">
              {itens.map((item, idx) => (
                <div
                  key={idx}
                  className="p-3 bg-gray-50 dark:bg-gray-750 border border-gray-200 dark:border-gray-700 rounded-xl grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-12 gap-3 items-end"
                >
                  {/* Dia da semana */}
                  <div className="lg:col-span-2 min-w-0">
                    <label className="block text-[11px] font-semibold text-gray-500 mb-1">Dia da Semana</label>
                    <select
                      value={item.diaSemana}
                      onChange={(e) => handleChangeItem(idx, 'diaSemana', e.target.value as DiaSemana)}
                      className="w-full px-2 py-1.5 text-xs bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg font-medium"
                    >
                      {DIAS_SEMANA_ORDENADOS.map((d) => (
                        <option key={d.key} value={d.key}>
                          {d.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Horários Início e Fim */}
                  <div className="lg:col-span-3 min-w-0 grid grid-cols-2 gap-2">
                    <div className="min-w-0">
                      <label className="block text-[11px] font-semibold text-gray-500 mb-1">Início</label>
                      <input
                        type="time"
                        value={item.horaInicio}
                        onChange={(e) => handleChangeItem(idx, 'horaInicio', e.target.value)}
                        className="w-full min-w-0 px-2 py-1.5 text-xs bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg"
                      />
                    </div>
                    <div className="min-w-0">
                      <label className="block text-[11px] font-semibold text-gray-500 mb-1">Fim</label>
                      <input
                        type="time"
                        value={item.horaFim}
                        onChange={(e) => handleChangeItem(idx, 'horaFim', e.target.value)}
                        className="w-full min-w-0 px-2 py-1.5 text-xs bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg"
                      />
                    </div>
                  </div>

                  {/* Especialidade */}
                  <div className="lg:col-span-2 min-w-0">
                    <label className="block text-[11px] font-semibold text-gray-500 mb-1">Especialidade *</label>
                    <select
                      value={item.idTipoCompromisso}
                      onChange={(e) => handleChangeItem(idx, 'idTipoCompromisso', e.target.value)}
                      className="w-full px-2 py-1.5 text-xs bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg font-medium"
                    >
                      <option value="">Selecione...</option>
                      {tiposCompromisso.map((t) => (
                        <option key={t.idTipoCompromisso} value={t.idTipoCompromisso}>
                          {t.nomeTipo || t.descricaoTipo || 'Especialidade'}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Terapeuta */}
                  <div className="lg:col-span-2 min-w-0">
                    <label className="block text-[11px] font-semibold text-gray-500 mb-1">Terapeuta</label>
                    <select
                      value={item.idProfissional || ''}
                      onChange={(e) => handleChangeItem(idx, 'idProfissional', e.target.value)}
                      className="w-full px-2 py-1.5 text-xs bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg"
                    >
                      <option value="">A definir</option>
                      {profissionais.map((prof) => (
                        <option key={prof.idProf} value={prof.idProf}>
                          {prof.nomeProfissional || 'Profissional'}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Periodicidade */}
                  <div className="lg:col-span-2 min-w-0">
                    <label className="block text-[11px] font-semibold text-gray-500 mb-1">Periodicidade</label>
                    <select
                      value={item.idPeriodicidade || ''}
                      onChange={(e) => handleChangeItem(idx, 'idPeriodicidade', e.target.value)}
                      className="w-full px-2 py-1.5 text-xs bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg font-medium text-primary-700 dark:text-primary-300"
                    >
                      {periodicidades.map((p) => (
                        <option key={p.idPeriodicidade} value={p.idPeriodicidade}>
                          {p.nomePeriodicidade}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Botão Remover */}
                  <div className="lg:col-span-1 flex justify-end">
                    <button
                      type="button"
                      onClick={() => handleRemoveItem(idx)}
                      disabled={itens.length <= 1}
                      title="Remover horário"
                      className="p-1.5 text-gray-400 hover:text-red-600 dark:hover:text-red-400 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 transition disabled:opacity-30"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Rodapé de Ações */}
        <div className="px-6 py-4 bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 flex flex-wrap items-center justify-between gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition"
          >
            Cancelar
          </button>

          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => handleSubmit(false)}
              disabled={loading}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl text-gray-700 dark:text-gray-200 transition disabled:opacity-50"
            >
              <Save className="w-4 h-4" /> Salvar Rascunho
            </button>
            <button
              type="button"
              onClick={() => handleSubmit(true)}
              disabled={loading}
              className="inline-flex items-center gap-1.5 px-5 py-2 text-sm font-semibold bg-primary-600 hover:bg-primary-700 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              <Send className="w-4 h-4" /> Enviar para Aprovação do Responsável
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
