import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  CalendarRange,
  Plus,
  AlertTriangle,
  Send,
  History,
  CheckCircle2,
  UserMinus,
  RefreshCw,
  MessageSquare,
  Sparkles,
} from 'lucide-react';
import { gradeTerapeuticaService } from '../services/gradeTerapeuticaService';
import { pacienteService } from '../services/pacienteService';
import type { GradeTerapeutica, SaveGradeTerapeutica } from '../types/gradeTerapeutica';
import { STATUS_GRADE_LABELS } from '../types/gradeTerapeutica';
import type { Paciente } from '../types/paciente';
import type { Compromisso } from '../types/compromisso';
import { GradeSemanalGrid } from '../components/grade/GradeSemanalGrid';
import { NovaPropostaModal } from '../components/grade/NovaPropostaModal';
import { AvisosConflitoModal } from '../components/grade/AvisosConflitoModal';
import { CancelarProfissionalModal } from '../components/grade/CancelarProfissionalModal';

export default function GradeTerapeuticaPage() {
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [idPacienteSelecionado, setIdPacienteSelecionado] = useState<string>('');
  const [grades, setGrades] = useState<GradeTerapeutica[]>([]);
  const [conflitos, setConflitos] = useState<Compromisso[]>([]);

  const [loading, setLoading] = useState(false);
  const [loadingAcao, setLoadingAcao] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);

  // Modais
  const [modalNovaProposta, setModalNovaProposta] = useState(false);
  const [modalConflitos, setModalConflitos] = useState(false);
  const [modalCancelarProf, setModalCancelarProf] = useState(false);

  useEffect(() => {
    carregarPacientes();
  }, []);

  useEffect(() => {
    if (idPacienteSelecionado) {
      carregarGradesPaciente(idPacienteSelecionado);
      carregarConflitosPaciente(idPacienteSelecionado);
    } else {
      setGrades([]);
      setConflitos([]);
    }
  }, [idPacienteSelecionado]);

  async function carregarPacientes() {
    try {
      const data = await pacienteService.list();
      setPacientes(data);
      if (data.length > 0 && !idPacienteSelecionado) {
        setIdPacienteSelecionado(data[0].idPaciente);
      }
    } catch (e: any) {
      setErro('Erro ao carregar lista de pacientes.');
    }
  }

  async function carregarGradesPaciente(idPac: string) {
    try {
      setLoading(true);
      const data = await gradeTerapeuticaService.listPorPaciente(idPac);
      setGrades(data);
    } catch (e: any) {
      setErro('Erro ao carregar grade terapêutica do paciente.');
    } finally {
      setLoading(false);
    }
  }

  async function carregarConflitosPaciente(idPac: string) {
    try {
      const confs = await gradeTerapeuticaService.getConflitosPorPaciente(idPac);
      setConflitos(confs);
    } catch (e) {
      console.error('Erro ao carregar conflitos:', e);
    }
  }

  const gradeVigente = grades.find((g) => g.status === 'VIGENTE');
  const propostaPendente = grades.find(
    (g) => g.status === 'PENDENTE_APROVACAO' || g.status === 'SUGESTAO_RESPONSAVEL' || g.status === 'RASCUNHO'
  );
  const historicoGrades = grades.filter((g) => g.idGradeTerapeutica !== gradeVigente?.idGradeTerapeutica);

  const pacienteAtual = pacientes.find((p) => p.idPaciente === idPacienteSelecionado);
  const nomePacienteAtual = pacienteAtual?.nomePessoa || pacienteAtual?.apelidoPaciente || 'Paciente';

  async function handleSalvarNovaProposta(dados: SaveGradeTerapeutica) {
    await gradeTerapeuticaService.criarProposta(dados);
    setSucesso('Proposta de grade terapêutica salva com sucesso!');
    if (dados.idPaciente && dados.idPaciente !== idPacienteSelecionado) {
      setIdPacienteSelecionado(dados.idPaciente);
    } else if (idPacienteSelecionado) {
      await carregarGradesPaciente(idPacienteSelecionado);
      await carregarConflitosPaciente(idPacienteSelecionado);
    }
  }

  async function handleEnviarParaAprovacao(idGrade: string) {
    try {
      setLoadingAcao(true);
      await gradeTerapeuticaService.enviarParaAprovacao(idGrade);
      setSucesso('Proposta enviada para o aplicativo do responsável!');
      await carregarGradesPaciente(idPacienteSelecionado);
    } catch (e: any) {
      setErro(e.response?.data?.message || e.message || 'Erro ao enviar proposta.');
    } finally {
      setLoadingAcao(false);
    }
  }

  async function handleAprovarPelaClinica(idGrade: string) {
    if (!confirm('Deseja aprovar e replicar a grade terapêutica imediatamente na agenda?')) return;
    try {
      setLoadingAcao(true);
      const gerados = await gradeTerapeuticaService.aprovarEReplicar(idGrade);
      setSucesso(`Grade aprovada com sucesso! ${gerados.length} compromissos foram replicados na agenda.`);
      await carregarGradesPaciente(idPacienteSelecionado);
      await carregarConflitosPaciente(idPacienteSelecionado);
    } catch (e: any) {
      setErro(e.response?.data?.message || e.message || 'Erro ao aprovar e replicar grade.');
    } finally {
      setLoadingAcao(false);
    }
  }

  async function handleConfirmarCancelamentoProfissional(idProf: string, dataCorte: string, motivo: string) {
    if (!gradeVigente) return;
    const qtd = await gradeTerapeuticaService.cancelarProfissional(gradeVigente.idGradeTerapeutica, {
      idProfissional: idProf,
      dataCorte,
      motivo,
    });
    setSucesso(`${qtd} compromissos futuros foram cancelados a partir de ${dataCorte}. Histórico passado preservado.`);
    await carregarGradesPaciente(idPacienteSelecionado);
  }

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <CalendarRange className="w-7 h-7 text-primary-600" />
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
              Grade Terapêutica Semanal
            </h1>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Programação fixa de terapias, aprovação com o responsável e replicação automática da agenda.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {conflitos.length > 0 && (
            <button
              onClick={() => setModalConflitos(true)}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-amber-50 hover:bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800 rounded-xl text-sm font-semibold transition shadow-sm"
            >
              <AlertTriangle className="w-4 h-4" />
              <span>{conflitos.length} {conflitos.length === 1 ? 'Choque' : 'Choques'} de Horário</span>
            </button>
          )}

          <button
            onClick={() => setModalNovaProposta(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-primary-600 hover:bg-primary-700 text-white rounded-xl text-sm font-semibold transition shadow-sm"
          >
            <Plus className="w-4 h-4" /> Nova Proposta de Grade
          </button>
        </div>
      </div>

      {/* Alertas */}
      {pacientes.length === 0 && !loading && (
        <div className="p-4 bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 rounded-xl text-amber-800 dark:text-amber-200 text-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-sm">
          <div className="flex items-center gap-2.5">
            <AlertTriangle className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0" />
            <span>Nenhum paciente vinculado a esta clínica foi encontrado. Cadastre um paciente para iniciar a grade semanal.</span>
          </div>
          <Link
            to="/admin/pacientes"
            className="px-3.5 py-1.5 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-xs font-semibold shrink-0 transition"
          >
            Cadastrar Paciente
          </Link>
        </div>
      )}
      {sucesso && (
        <div className="p-4 bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-800 rounded-xl text-emerald-800 dark:text-emerald-200 text-sm flex items-center justify-between">
          <span>{sucesso}</span>
          <button onClick={() => setSucesso(null)} className="text-xs font-bold underline">Fechar</button>
        </div>
      )}
      {erro && (
        <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-800 dark:text-red-200 text-sm flex items-center justify-between">
          <span>{erro}</span>
          <button onClick={() => setErro(null)} className="text-xs font-bold underline">Fechar</button>
        </div>
      )}

      {/* Barra de Filtro de Pacientes */}
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-4 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider shrink-0">
            Paciente Selecionado:
          </label>
          <select
            value={idPacienteSelecionado}
            onChange={(e) => setIdPacienteSelecionado(e.target.value)}
            disabled={pacientes.length === 0}
            className="w-full sm:w-80 px-3 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl text-sm font-medium focus:ring-2 focus:ring-primary-500 focus:outline-none"
          >
            {pacientes.length === 0 ? (
              <option value="">Nenhum paciente disponível</option>
            ) : (
              pacientes.map((p) => (
                <option key={p.idPaciente} value={p.idPaciente}>
                  {p.nomePessoa || p.apelidoPaciente || 'Paciente'}
                </option>
              ))
            )}
          </select>
        </div>

        <button
          onClick={() => idPacienteSelecionado && carregarGradesPaciente(idPacienteSelecionado)}
          disabled={loading}
          className="p-2 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition"
          title="Recarregar"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Proposta em Andamento / Sugestão do Responsável */}
      {propostaPendente && (
        <div className="bg-gradient-to-r from-amber-50 to-orange-50 dark:from-amber-950/30 dark:to-orange-950/30 border-2 border-amber-300 dark:border-amber-700 rounded-2xl p-5 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div className="flex items-center gap-2.5">
              <span className="p-2 bg-amber-100 dark:bg-amber-900/50 rounded-xl text-amber-700 dark:text-amber-300">
                <Sparkles className="w-5 h-5" />
              </span>
              <div>
                <h3 className="font-bold text-gray-900 dark:text-gray-100 text-base">
                  Proposta de Grade em Andamento (v{propostaPendente.numeroVersao})
                </h3>
                <p className="text-xs text-gray-600 dark:text-gray-400">
                  Vigência: {propostaPendente.dataInicioVigencia} até {propostaPendente.dataFimVigencia}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                STATUS_GRADE_LABELS[propostaPendente.status]?.bg
              } ${STATUS_GRADE_LABELS[propostaPendente.status]?.text}`}>
                {STATUS_GRADE_LABELS[propostaPendente.status]?.label}
              </span>

              {propostaPendente.status === 'RASCUNHO' && (
                <button
                  onClick={() => handleEnviarParaAprovacao(propostaPendente.idGradeTerapeutica)}
                  disabled={loadingAcao}
                  className="inline-flex items-center gap-1 px-3 py-1.5 bg-primary-600 hover:bg-primary-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
                >
                  <Send className="w-3.5 h-3.5" /> Enviar para Família
                </button>
              )}

              <button
                onClick={() => handleAprovarPelaClinica(propostaPendente.idGradeTerapeutica)}
                disabled={loadingAcao}
                className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
              >
                <CheckCircle2 className="w-3.5 h-3.5" /> Aprovar e Replicar Agenda
              </button>
            </div>
          </div>

          {/* Destaque para contraproposta da família */}
          {propostaPendente.status === 'SUGESTAO_RESPONSAVEL' && propostaPendente.justificativaSugestaoResponsavel && (
            <div className="p-3.5 bg-white dark:bg-gray-800 rounded-xl border border-orange-200 dark:border-orange-800 shadow-sm flex items-start gap-3">
              <MessageSquare className="w-5 h-5 text-orange-600 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold text-xs text-orange-800 dark:text-orange-300 block mb-0.5">
                  Contraproposta recebida do Responsável pelo DApp:
                </span>
                <p className="text-sm text-gray-800 dark:text-gray-200">
                  "{propostaPendente.justificativaSugestaoResponsavel}"
                </p>
              </div>
            </div>
          )}

          <GradeSemanalGrid itens={propostaPendente.itens} compact />
        </div>
      )}

      {/* Grade Terapêutica Vigente */}
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-6 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-gray-100 dark:border-gray-700/60 pb-4">
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-gray-900 dark:text-gray-100">
                Grade Vigente de {nomePacienteAtual}
              </h2>
              {gradeVigente && (
                <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 dark:bg-emerald-900/40 text-emerald-800 dark:text-emerald-300">
                  Ativa (v{gradeVigente.numeroVersao})
                </span>
              )}
            </div>
            {gradeVigente ? (
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Período: {gradeVigente.dataInicioVigencia} até {gradeVigente.dataFimVigencia} • {gradeVigente.itens.length} sessões semanais
              </p>
            ) : (
              <p className="text-xs text-gray-400 mt-1 italic">
                Nenhuma grade ativa para este paciente no momento.
              </p>
            )}
          </div>

          {gradeVigente && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => setModalCancelarProf(true)}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-red-50 hover:bg-red-100 dark:bg-red-900/20 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800 rounded-lg text-xs font-semibold transition"
                title="Descontinuar terapeuta e cancelar sessões futuras dele"
              >
                <UserMinus className="w-3.5 h-3.5" /> Cancelar Horários de Terapeuta
              </button>
            </div>
          )}
        </div>

        {gradeVigente ? (
          <GradeSemanalGrid itens={gradeVigente.itens} />
        ) : (
          <div className="py-12 text-center text-gray-500">
            <CalendarRange className="w-12 h-12 mx-auto mb-3 text-gray-400 opacity-60" />
            <p className="font-semibold text-base text-gray-800 dark:text-gray-200">Sem Grade Terapêutica Vigente</p>
            <p className="text-xs text-gray-500 mt-1 mb-4 max-w-sm mx-auto">
              {idPacienteSelecionado
                ? 'Clique no botão abaixo para montar e propor o quadro semanal deste paciente.'
                : 'Selecione um paciente ou inicie uma nova proposta de grade terapêutica.'}
            </p>
            <button
              onClick={() => setModalNovaProposta(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 bg-primary-600 hover:bg-primary-700 text-white rounded-xl text-xs font-semibold shadow-sm transition"
            >
              <Plus className="w-3.5 h-3.5" /> Criar Grade Terapêutica
            </button>
          </div>
        )}
      </div>

      {/* Histórico de Versões */}
      {historicoGrades.length > 0 && (
        <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="flex items-center gap-2 border-b border-gray-100 dark:border-gray-700/60 pb-3">
            <History className="w-5 h-5 text-gray-400" />
            <h3 className="font-bold text-gray-900 dark:text-gray-100 text-sm">
              Histórico de Versões e Propostas Anteriores ({historicoGrades.length})
            </h3>
          </div>

          <div className="divide-y divide-gray-100 dark:divide-gray-700">
            {historicoGrades.map((g) => (
              <div key={g.idGradeTerapeutica} className="py-3 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-xs text-gray-800 dark:text-gray-200">
                      Versão {g.numeroVersao}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-semibold ${
                      STATUS_GRADE_LABELS[g.status]?.bg
                    } ${STATUS_GRADE_LABELS[g.status]?.text}`}>
                      {STATUS_GRADE_LABELS[g.status]?.label}
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    Vigência: {g.dataInicioVigencia} até {g.dataFimVigencia} • {g.itens.length} sessões semanais
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Modais */}
      <NovaPropostaModal
        isOpen={modalNovaProposta}
        onClose={() => setModalNovaProposta(false)}
        onSalvar={handleSalvarNovaProposta}
        idPacientePreselecionado={idPacienteSelecionado}
      />

      <AvisosConflitoModal
        isOpen={modalConflitos}
        onClose={() => setModalConflitos(false)}
        conflitos={conflitos}
        nomePaciente={nomePacienteAtual}
      />

      {gradeVigente && (
        <CancelarProfissionalModal
          isOpen={modalCancelarProf}
          onClose={() => setModalCancelarProf(false)}
          grade={gradeVigente}
          onConfirmar={handleConfirmarCancelamentoProfissional}
        />
      )}
    </div>
  );
}
