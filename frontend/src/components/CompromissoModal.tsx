import { useState, useEffect } from 'react';
import { X, Calendar, Loader2, AlertCircle, Clock, MapPin, User, Tag, CalendarClock } from 'lucide-react';
import { compromissoService } from '../services/compromissoService';
import { pacienteService } from '../services/pacienteService';
import { periodicidadeService } from '../services/periodicidadeService';
import type { Compromisso, SaveCompromissoInput, TipoCompromisso } from '../types/compromisso';
import type { Paciente } from '../types/paciente';
import type { Periodicidade } from '../types/periodicidade';

interface CompromissoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (compromisso: Compromisso) => void;
  initialData?: Compromisso | null;
}

export default function CompromissoModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
}: CompromissoModalProps) {
  const [tituloCompromisso, setTituloCompromisso] = useState('');
  const [descricaoCompromisso, setDescricaoCompromisso] = useState('');
  const [dataHoraCompromisso, setDataHoraCompromisso] = useState('');
  const [localCompromisso, setLocalCompromisso] = useState('');
  const [idPaciente, setIdPaciente] = useState('');
  const [idTipoCompromisso, setIdTipoCompromisso] = useState('');
  const [idPeriodicidade, setIdPeriodicidade] = useState('');

  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [tipos, setTipos] = useState<TipoCompromisso[]>([]);
  const [periodicidades, setPeriodicidades] = useState<Periodicidade[]>([]);

  const [loadingData, setLoadingData] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Carrega opções de paciente, tipo e periodicidade ao abrir
  useEffect(() => {
    if (isOpen) {
      setLoadingData(true);
      Promise.all([
        pacienteService.list().catch(() => []),
        compromissoService.listTipos().catch(() => []),
        periodicidadeService.list().catch(() => []),
      ])
        .then(([pacList, tiposList, periodList]) => {
          setPacientes(pacList);
          setTipos(tiposList);
          setPeriodicidades(periodList);

          // Se for novo compromisso e houver tipos/periodicidades, pré-seleciona padrão se estiver vazio
          if (!initialData) {
            if (periodList.length > 0 && !idPeriodicidade) {
              const semanal = periodList.find((p) => p.nomePeriodicidade.toLowerCase().includes('semanal'));
              setIdPeriodicidade(semanal ? semanal.idPeriodicidade : periodList[0].idPeriodicidade);
            }
          }
        })
        .finally(() => setLoadingData(false));
    }
  }, [isOpen, initialData]);

  // Preenche dados ao editar ou reseta ao criar novo
  useEffect(() => {
    if (initialData) {
      setTituloCompromisso(initialData.tituloCompromisso || '');
      setDescricaoCompromisso(initialData.descricaoCompromisso || '');
      // Converte dataHora para valor de input datetime-local: "YYYY-MM-DDTHH:mm"
      if (initialData.dataHoraCompromisso) {
        const d = new Date(initialData.dataHoraCompromisso);
        if (!isNaN(d.getTime())) {
          const pad = (n: number) => String(n).padStart(2, '0');
          const formatted = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
          setDataHoraCompromisso(formatted);
        } else {
          setDataHoraCompromisso(initialData.dataHoraCompromisso.substring(0, 16));
        }
      } else {
        setDataHoraCompromisso('');
      }
      setLocalCompromisso(initialData.localCompromisso || '');
      setIdPaciente(initialData.idPaciente || '');
      setIdTipoCompromisso(initialData.idTipoCompromisso || '');
      setIdPeriodicidade(initialData.idPeriodicidade || '');
    } else {
      setTituloCompromisso('');
      setDescricaoCompromisso('');
      // Valor padrão: amanhã às 09:00
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(9, 0, 0, 0);
      const pad = (n: number) => String(n).padStart(2, '0');
      setDataHoraCompromisso(`${tomorrow.getFullYear()}-${pad(tomorrow.getMonth() + 1)}-${pad(tomorrow.getDate())}T09:00`);
      setLocalCompromisso('');
      setIdPaciente('');
      setIdTipoCompromisso('');
      setIdPeriodicidade('');
    }
    setError('');
  }, [initialData, isOpen]);

  // Ao trocar o tipo de compromisso, se o título estiver vazio ou for o nome de outro tipo, atualiza o título
  const handleTipoChange = (novoIdTipo: string) => {
    setIdTipoCompromisso(novoIdTipo);
    const tipoEncontrado = tipos.find((t) => t.idTipoCompromisso === novoIdTipo);
    if (tipoEncontrado) {
      if (!tituloCompromisso || tipos.some((t) => t.nomeTipo === tituloCompromisso)) {
        setTituloCompromisso(tipoEncontrado.nomeTipo);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!tituloCompromisso.trim()) {
      setError('O título do compromisso é obrigatório.');
      return;
    }
    if (!idPaciente) {
      setError('Selecione um paciente.');
      return;
    }
    if (!idTipoCompromisso) {
      setError('Selecione o tipo de atendimento.');
      return;
    }
    if (!idPeriodicidade) {
      setError('Selecione a periodicidade.');
      return;
    }
    if (!dataHoraCompromisso) {
      setError('Selecione a data e horário do compromisso.');
      return;
    }

    // Formata datetime para ISO com segundos se necessário
    let formattedDateTime = dataHoraCompromisso;
    if (formattedDateTime.length === 16) {
      formattedDateTime += ':00';
    }

    const payload: SaveCompromissoInput = {
      tituloCompromisso: tituloCompromisso.trim(),
      descricaoCompromisso: descricaoCompromisso.trim() || undefined,
      dataHoraCompromisso: formattedDateTime,
      localCompromisso: localCompromisso.trim() || undefined,
      idPaciente,
      idTipoCompromisso,
      idPeriodicidade,
    };

    setSaving(true);
    try {
      let result: Compromisso;
      if (initialData) {
        result = await compromissoService.update(initialData.idCompromisso, payload);
      } else {
        result = await compromissoService.create(payload);
      }
      onSuccess(result);
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Erro ao salvar agendamento. Verifique os dados e tente novamente.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-lg w-full overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Calendar className="w-4 h-4" />
            </div>
            <h3 className="font-semibold text-gray-900">
              {initialData ? 'Editar Agendamento' : 'Novo Agendamento'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-500 rounded-lg p-1 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-sm text-red-600">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {loadingData ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-gray-500">
              <Loader2 className="w-6 h-6 animate-spin text-indigo-600" />
              <span className="text-sm">Carregando dados...</span>
            </div>
          ) : (
            <>
              {/* Paciente */}
              <div>
                <label
                  htmlFor="idPaciente"
                  className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1.5"
                >
                  <User className="w-4 h-4 text-gray-400" />
                  Paciente *
                </label>
                <select
                  id="idPaciente"
                  value={idPaciente}
                  onChange={(e) => setIdPaciente(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm bg-white"
                  required
                >
                  <option value="">Selecione o paciente...</option>
                  {pacientes.map((p) => (
                    <option key={p.idPaciente} value={p.idPaciente}>
                      {p.nomePessoa || p.apelidoPaciente || 'Paciente sem nome'}
                    </option>
                  ))}
                </select>
                {pacientes.length === 0 && (
                  <p className="text-xs text-amber-600 mt-1">
                    Nenhum paciente disponível cadastrado para este perfil.
                  </p>
                )}
              </div>

              {/* Tipo de Atendimento / Especialidade */}
              <div>
                <label
                  htmlFor="idTipoCompromisso"
                  className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1.5"
                >
                  <Tag className="w-4 h-4 text-gray-400" />
                  Tipo de Atendimento / Especialidade *
                </label>
                <select
                  id="idTipoCompromisso"
                  value={idTipoCompromisso}
                  onChange={(e) => handleTipoChange(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm bg-white"
                  required
                >
                  <option value="">Selecione a especialidade...</option>
                  {tipos.map((t) => (
                    <option key={t.idTipoCompromisso} value={t.idTipoCompromisso}>
                      {t.nomeTipo}
                    </option>
                  ))}
                </select>
              </div>

              {/* Título do Agendamento */}
              <div>
                <label
                  htmlFor="tituloCompromisso"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  Título do Agendamento *
                </label>
                <input
                  id="tituloCompromisso"
                  type="text"
                  value={tituloCompromisso}
                  onChange={(e) => setTituloCompromisso(e.target.value)}
                  placeholder="Ex: Sessão Semanal de Fonoaudiologia"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm"
                  required
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Data e Horário */}
                <div>
                  <label
                    htmlFor="dataHoraCompromisso"
                    className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1.5"
                  >
                    <Clock className="w-4 h-4 text-gray-400" />
                    Data e Horário *
                  </label>
                  <input
                    id="dataHoraCompromisso"
                    type="datetime-local"
                    value={dataHoraCompromisso}
                    onChange={(e) => setDataHoraCompromisso(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm"
                    required
                  />
                </div>

                {/* Periodicidade */}
                <div>
                  <label
                    htmlFor="idPeriodicidade"
                    className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1.5"
                  >
                    <CalendarClock className="w-4 h-4 text-gray-400" />
                    Periodicidade *
                  </label>
                  <select
                    id="idPeriodicidade"
                    value={idPeriodicidade}
                    onChange={(e) => setIdPeriodicidade(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm bg-white"
                    required
                  >
                    <option value="">Selecione a periodicidade...</option>
                    {periodicidades.map((p) => (
                      <option key={p.idPeriodicidade} value={p.idPeriodicidade}>
                        {p.nomePeriodicidade} {p.diasIntervalo && p.diasIntervalo > 0 ? `(${p.diasIntervalo}d)` : '(Única)'}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Local */}
              <div>
                <label
                  htmlFor="localCompromisso"
                  className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1.5"
                >
                  <MapPin className="w-4 h-4 text-gray-400" />
                  Local / Sala (opcional)
                </label>
                <input
                  id="localCompromisso"
                  type="text"
                  value={localCompromisso}
                  onChange={(e) => setLocalCompromisso(e.target.value)}
                  placeholder="Ex: Consultório 03, Sala de Psicomotricidade ou Online"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm"
                />
              </div>

              {/* Descrição / Observações */}
              <div>
                <label
                  htmlFor="descricaoCompromisso"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  Observações / Detalhes (opcional)
                </label>
                <textarea
                  id="descricaoCompromisso"
                  value={descricaoCompromisso}
                  onChange={(e) => setDescricaoCompromisso(e.target.value)}
                  rows={2}
                  placeholder="Orientações prévias, materiais a trazer ou foco da sessão..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm"
                />
              </div>
            </>
          )}

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving || loadingData}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors disabled:opacity-50 flex items-center gap-2 shadow-sm"
            >
              {saving && <Loader2 className="w-4 h-4 animate-spin" />}
              {initialData ? 'Salvar Alterações' : 'Criar Agendamento'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
