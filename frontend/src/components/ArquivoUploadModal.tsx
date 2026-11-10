import { useState, useEffect, useContext } from 'react';
import {
  X,
  Upload,
  FileText,
  Loader2,
  AlertCircle,
  Lock,
  Share2,
  User,
  Tag,
  Calendar,
  Building2,
  Briefcase,
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { arquivoService } from '../services/arquivoService';
import { pacienteService } from '../services/pacienteService';
import type { Arquivo, OpcoesCompartilhamento, TipoArquivo } from '../types/arquivo';
import type { Paciente } from '../types/paciente';

interface ArquivoUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (arquivo: Arquivo) => void;
  defaultPacienteId?: string;
}

const ALLOWED_EXTS = ['pdf', 'doc', 'docx', 'txt', 'odt', 'rtf'];

function getTodayISO(): string {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function toPascalSlug(text?: string): string {
  if (!text) return '';
  return text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9\s]/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join('');
}

export default function ArquivoUploadModal({
  isOpen,
  onClose,
  onSuccess,
  defaultPacienteId,
}: ArquivoUploadModalProps) {
  const auth = useContext(AuthContext);
  const isResponsavel = auth?.isResponsavel ?? false;

  const [idPaciente, setIdPaciente] = useState('');
  const [idTipoArquivo, setIdTipoArquivo] = useState('');
  const [dataArquivo, setDataArquivo] = useState(getTodayISO());
  const [descricao, setDescricao] = useState('');

  const [fileNameOriginal, setFileNameOriginal] = useState('');
  const [extensaoArquivo, setExtensaoArquivo] = useState('');
  const [arquivoBase64, setArquivoBase64] = useState('');

  // Compartilhamento inicial (padrão: bloqueado / privado ao autor)
  const [compartilhadoComResponsavel, setCompartilhadoComResponsavel] = useState(false);
  const [compartilhadoComClinica, setCompartilhadoComClinica] = useState(false);
  const [idClinicaCompartilhada, setIdClinicaCompartilhada] = useState('');
  const [idsProfissionais, setIdsProfissionais] = useState<string[]>([]);

  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [tipos, setTipos] = useState<TipoArquivo[]>([]);
  const [opcoesComp, setOpcoesComp] = useState<OpcoesCompartilhamento>({
    clinicas: [],
    profissionais: [],
  });

  const [loadingInit, setLoadingInit] = useState(false);
  const [loadingOpcoes, setLoadingOpcoes] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isOpen) return;
    setLoadingInit(true);
    Promise.all([
      pacienteService.list().catch(() => []),
      arquivoService.listTipos().catch(() => []),
    ])
      .then(([pacList, tipoList]) => {
        setPacientes(pacList);
        setTipos(tipoList);
        if (defaultPacienteId) {
          setIdPaciente(defaultPacienteId);
        } else if (pacList.length === 1) {
          setIdPaciente(pacList[0].idPaciente);
        }
      })
      .finally(() => setLoadingInit(false));

    // Reset fields
    setIdPaciente(defaultPacienteId || '');
    setIdTipoArquivo('');
    setDataArquivo(getTodayISO());
    setDescricao('');
    setFileNameOriginal('');
    setExtensaoArquivo('');
    setArquivoBase64('');
    setCompartilhadoComResponsavel(false);
    setCompartilhadoComClinica(false);
    setIdClinicaCompartilhada('');
    setIdsProfissionais([]);
    setError('');
  }, [isOpen, defaultPacienteId]);

  useEffect(() => {
    if (!isOpen || !idPaciente) {
      setOpcoesComp({ clinicas: [], profissionais: [] });
      return;
    }
    setLoadingOpcoes(true);
    arquivoService
      .opcoesCompartilhamento(idPaciente)
      .then((opts) => {
        setOpcoesComp(opts);
        if (opts.clinicas.length === 1) {
          setIdClinicaCompartilhada(opts.clinicas[0].idClinica);
        }
      })
      .catch(() => setOpcoesComp({ clinicas: [], profissionais: [] }))
      .finally(() => setLoadingOpcoes(false));
  }, [isOpen, idPaciente]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError('');
    const file = e.target.files?.[0];
    if (!file) return;

    const parts = file.name.split('.');
    const ext = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : '';
    if (!ALLOWED_EXTS.includes(ext)) {
      setError('Formato inválido. Selecione um arquivo PDF, DOC, DOCX, TXT, ODT ou RTF.');
      e.target.value = '';
      return;
    }

    if (file.size > 15 * 1024 * 1024) {
      setError('O arquivo excede o tamanho máximo permitido de 15 MB.');
      e.target.value = '';
      return;
    }

    setFileNameOriginal(file.name);
    setExtensaoArquivo(ext);

    const reader = new FileReader();
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : '';
      const base64Only = result.includes(',') ? result.split(',')[1] : result;
      setArquivoBase64(base64Only);
    };
    reader.onerror = () => {
      setError('Erro ao ler o arquivo selecionado.');
    };
    reader.readAsDataURL(file);
  };

  const toggleProfissional = (idProf: string) => {
    setIdsProfissionais((prev) =>
      prev.includes(idProf) ? prev.filter((id) => id !== idProf) : [...prev, idProf]
    );
  };

  // Preview do nome renomeado
  const pacienteSelecionado = pacientes.find((p) => p.idPaciente === idPaciente);
  const tipoSelecionado = tipos.find((t) => t.idTipoArquivo === idTipoArquivo);
  const previewNomePadronizado = (() => {
    const tipoSlug = toPascalSlug(tipoSelecionado?.nomeTipo) || 'TipoArquivo';
    const pacSlug = toPascalSlug(pacienteSelecionado?.nomePessoa) || 'NomePaciente';
    const ext = extensaoArquivo || 'pdf';
    return `${tipoSlug}_${pacSlug}_DataHora.${ext}`;
  })();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!idPaciente) {
      setError('Selecione o paciente.');
      return;
    }
    if (!idTipoArquivo) {
      setError('Selecione o tipo de documento.');
      return;
    }
    if (!dataArquivo) {
      setError('Informe a data do documento.');
      return;
    }
    if (!arquivoBase64 || !extensaoArquivo) {
      setError('Selecione um arquivo válido para enviar.');
      return;
    }

    setSaving(true);
    try {
      const created = await arquivoService.create({
        idPaciente,
        idTipoArquivo,
        dataArquivo,
        extensaoArquivo,
        arquivoBase64,
        descricao: descricao.trim() || undefined,
        compartilhadoComResponsavel,
        compartilhadoComClinica,
        idClinicaCompartilhada:
          compartilhadoComClinica && idClinicaCompartilhada ? idClinicaCompartilhada : undefined,
        idsProfissionaisCompartilhados: idsProfissionais,
      });
      onSuccess(created);
      onClose();
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.errorMessage ||
        'Erro ao enviar arquivo. Verifique os dados e tente novamente.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl max-w-xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/70">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Upload className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900 text-base">Enviar Documento do Paciente</h3>
              <p className="text-xs text-gray-500">
                Laudos, receitas, acompanhamentos e resultados de exames (PDF/DOC/DOCX)
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 p-1.5 rounded-lg hover:bg-gray-100"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-sm text-red-700">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {loadingInit ? (
            <div className="py-10 flex flex-col items-center justify-center gap-2 text-gray-500">
              <Loader2 className="w-6 h-6 animate-spin text-indigo-600" />
              <span className="text-sm">Carregando opções...</span>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label
                    htmlFor="upload-paciente"
                    className="block text-xs font-semibold uppercase text-gray-700 mb-1 flex items-center gap-1"
                  >
                    <User className="w-3.5 h-3.5 text-gray-400" />
                    Paciente *
                  </label>
                  <select
                    id="upload-paciente"
                    value={idPaciente}
                    onChange={(e) => setIdPaciente(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="">Selecione o paciente...</option>
                    {pacientes.map((p) => (
                      <option key={p.idPaciente} value={p.idPaciente}>
                        {p.nomePessoa || p.apelidoPaciente}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label
                    htmlFor="upload-tipo"
                    className="block text-xs font-semibold uppercase text-gray-700 mb-1 flex items-center gap-1"
                  >
                    <Tag className="w-3.5 h-3.5 text-gray-400" />
                    Tipo de Documento *
                  </label>
                  <select
                    id="upload-tipo"
                    value={idTipoArquivo}
                    onChange={(e) => setIdTipoArquivo(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="">Selecione o tipo...</option>
                    {tipos.map((t) => (
                      <option key={t.idTipoArquivo} value={t.idTipoArquivo}>
                        {t.nomeTipo}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label
                    htmlFor="upload-data"
                    className="block text-xs font-semibold uppercase text-gray-700 mb-1 flex items-center gap-1"
                  >
                    <Calendar className="w-3.5 h-3.5 text-gray-400" />
                    Data do Documento *
                  </label>
                  <input
                    id="upload-data"
                    type="date"
                    value={dataArquivo}
                    onChange={(e) => setDataArquivo(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <div>
                  <label
                    htmlFor="upload-file"
                    className="block text-xs font-semibold uppercase text-gray-700 mb-1 flex items-center gap-1"
                  >
                    <FileText className="w-3.5 h-3.5 text-gray-400" />
                    Arquivo (PDF, DOC, DOCX, TXT) *
                  </label>
                  <input
                    id="upload-file"
                    type="file"
                    accept=".pdf,.doc,.docx,.txt,.odt,.rtf"
                    onChange={handleFileChange}
                    required={!arquivoBase64}
                    className="w-full text-xs text-gray-600 file:mr-3 file:py-2 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 border border-gray-300 rounded-lg p-1"
                  />
                  {fileNameOriginal && (
                    <span className="text-[11px] text-emerald-700 mt-1 block truncate">
                      Selecionado: {fileNameOriginal}
                    </span>
                  )}
                </div>
              </div>

              {/* Preview do nome padronizado */}
              <div className="p-2.5 bg-gray-50 border border-gray-200 rounded-lg text-xs text-gray-600">
                <span className="font-semibold text-gray-700">Nome ao salvar (padronizado): </span>
                <span className="font-mono text-indigo-700 break-all">{previewNomePadronizado}</span>
              </div>

              <div>
                <label
                  htmlFor="upload-desc"
                  className="block text-xs font-semibold uppercase text-gray-700 mb-1"
                >
                  Observações / Resumo (opcional)
                </label>
                <textarea
                  id="upload-desc"
                  rows={2}
                  value={descricao}
                  onChange={(e) => setDescricao(e.target.value)}
                  placeholder="Ex: Laudo de retorno trimestral ou resultado de hemograma..."
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              {/* Controle de Acesso / Privacidade */}
              <div className="p-4 bg-indigo-50/50 border border-indigo-200 rounded-xl space-y-3">
                <div className="flex items-start gap-2">
                  <Lock className="w-4 h-4 text-indigo-600 mt-0.5 shrink-0" />
                  <div>
                    <h4 className="text-xs font-bold text-indigo-950 uppercase tracking-wide flex items-center gap-1.5">
                      Controle de Acesso e Liberação
                    </h4>
                    <p className="text-[11px] text-indigo-800/80 mt-0.5">
                      Por padrão, o arquivo fica <strong>restrito apenas a você</strong> para revisão. Marque abaixo caso já queira liberar agora:
                    </p>
                  </div>
                </div>

                {!isResponsavel && (
                  <label className="flex items-center gap-2.5 p-2 bg-white rounded-lg border border-indigo-100 cursor-pointer text-xs">
                    <input
                      type="checkbox"
                      checked={compartilhadoComResponsavel}
                      onChange={(e) => setCompartilhadoComResponsavel(e.target.checked)}
                      className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                    />
                    <span className="font-medium text-gray-800">
                      Liberar visualização para o Responsável Legal do paciente
                    </span>
                  </label>
                )}

                <div className="p-2.5 bg-white rounded-lg border border-indigo-100 space-y-2">
                  <label className="flex items-center gap-2.5 cursor-pointer text-xs">
                    <input
                      type="checkbox"
                      checked={compartilhadoComClinica}
                      onChange={(e) => setCompartilhadoComClinica(e.target.checked)}
                      className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                    />
                    <span className="font-medium text-gray-800 flex items-center gap-1.5">
                      <Building2 className="w-3.5 h-3.5 text-teal-600" />
                      Liberar para toda a Clínica (admin, recepção e profissionais da unidade)
                    </span>
                  </label>

                  {compartilhadoComClinica && (
                    <div className="pl-6">
                      {loadingOpcoes ? (
                        <span className="text-[11px] text-gray-500">Carregando clínicas...</span>
                      ) : opcoesComp.clinicas.length > 0 ? (
                        <select
                          value={idClinicaCompartilhada}
                          onChange={(e) => setIdClinicaCompartilhada(e.target.value)}
                          className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg bg-white"
                        >
                          <option value="">Todas as clínicas vinculadas ao paciente</option>
                          {opcoesComp.clinicas.map((c) => (
                            <option key={c.idClinica} value={c.idClinica}>
                              {c.nomeClinica}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <p className="text-[11px] text-gray-400">
                          Será liberado para as clínicas em que o paciente estiver vinculado.
                        </p>
                      )}
                    </div>
                  )}
                </div>

                {/* Liberar para profissionais específicos */}
                <div className="p-2.5 bg-white rounded-lg border border-indigo-100 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-medium text-gray-800 flex items-center gap-1.5">
                      <Briefcase className="w-3.5 h-3.5 text-purple-600" />
                      Liberar para Profissionais Específicos
                    </span>
                    <span className="text-[11px] text-gray-500">
                      {idsProfissionais.length} selecionado(s)
                    </span>
                  </div>

                  {!idPaciente ? (
                    <p className="text-[11px] text-gray-400 italic">
                      Selecione um paciente primeiro para ver os profissionais vinculados.
                    </p>
                  ) : loadingOpcoes ? (
                    <span className="text-[11px] text-gray-500">Carregando profissionais...</span>
                  ) : opcoesComp.profissionais.length === 0 ? (
                    <p className="text-[11px] text-gray-400 italic">
                      Nenhum profissional disponível para seleção direta.
                    </p>
                  ) : (
                    <div className="max-h-28 overflow-y-auto divide-y divide-gray-100 border border-gray-100 rounded-lg p-1">
                      {opcoesComp.profissionais.map((prof) => (
                        <label
                          key={prof.idProf}
                          className="flex items-center gap-2 p-1.5 hover:bg-gray-50 rounded cursor-pointer text-xs"
                        >
                          <input
                            type="checkbox"
                            checked={idsProfissionais.includes(prof.idProf)}
                            onChange={() => toggleProfissional(prof.idProf)}
                            className="h-3.5 w-3.5 rounded border-gray-300 text-indigo-600"
                          />
                          <span className="text-gray-800 font-medium">{prof.nomeProfissional}</span>
                          {prof.conselhoProf && (
                            <span className="text-[10px] text-gray-400 font-mono">
                              ({prof.conselhoProf})
                            </span>
                          )}
                        </label>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </>
          )}

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving || loadingInit}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-2 shadow-sm"
            >
              {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Share2 className="w-4 h-4" />}
              Enviar Documento
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
