import { useState, useEffect } from 'react';
import {
  X,
  Share2,
  Loader2,
  AlertCircle,
  Lock,
  Building2,
  Briefcase,
  UserCheck,
} from 'lucide-react';
import { arquivoService } from '../services/arquivoService';
import type { Arquivo, OpcoesCompartilhamento } from '../types/arquivo';

interface ArquivoCompartilharModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updated: Arquivo) => void;
  arquivo: Arquivo | null;
}

export default function ArquivoCompartilharModal({
  isOpen,
  onClose,
  onSuccess,
  arquivo,
}: ArquivoCompartilharModalProps) {
  const [compartilhadoComResponsavel, setCompartilhadoComResponsavel] = useState(false);
  const [compartilhadoComClinica, setCompartilhadoComClinica] = useState(false);
  const [idClinicaCompartilhada, setIdClinicaCompartilhada] = useState('');
  const [idsProfissionais, setIdsProfissionais] = useState<string[]>([]);

  const [opcoes, setOpcoes] = useState<OpcoesCompartilhamento>({
    clinicas: [],
    profissionais: [],
  });
  const [loadingOpcoes, setLoadingOpcoes] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isOpen || !arquivo) return;

    setCompartilhadoComResponsavel(Boolean(arquivo.compartilhadoComResponsavel));
    setCompartilhadoComClinica(Boolean(arquivo.compartilhadoComClinica));
    setIdClinicaCompartilhada(arquivo.idClinicaCompartilhada || '');
    setIdsProfissionais(
      arquivo.profissionaisCompartilhados
        ? arquivo.profissionaisCompartilhados.map((p) => p.idProf)
        : []
    );
    setError('');

    setLoadingOpcoes(true);
    arquivoService
      .opcoesCompartilhamento(arquivo.idPaciente)
      .then((res) => setOpcoes(res))
      .catch(() => setOpcoes({ clinicas: [], profissionais: [] }))
      .finally(() => setLoadingOpcoes(false));
  }, [isOpen, arquivo]);

  const toggleProfissional = (idProf: string) => {
    setIdsProfissionais((prev) =>
      prev.includes(idProf) ? prev.filter((id) => id !== idProf) : [...prev, idProf]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!arquivo) return;
    setError('');
    setSaving(true);

    try {
      const updated = await arquivoService.compartilhar(arquivo.idArquivo, {
        compartilhadoComResponsavel,
        compartilhadoComClinica,
        idClinicaCompartilhada:
          compartilhadoComClinica && idClinicaCompartilhada ? idClinicaCompartilhada : undefined,
        idsProfissionaisCompartilhados: idsProfissionais,
      });
      onSuccess(updated);
      onClose();
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.errorMessage ||
          'Erro ao atualizar compartilhamento do arquivo.'
      );
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen || !arquivo) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl max-w-lg w-full overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/70">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Share2 className="w-4 h-4" />
            </div>
            <div className="min-w-0">
              <h3 className="font-semibold text-gray-900 text-base">Gerenciar Acesso ao Arquivo</h3>
              <p className="text-xs text-gray-500 truncate max-w-xs">{arquivo.nomeArquivo}</p>
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
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-sm text-red-700">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-start gap-2 text-xs text-amber-800">
            <Lock className="w-4 h-4 text-amber-600 mt-0.5 shrink-0" />
            <span>
              Se nenhuma opção abaixo estiver marcada, o documento permanece{' '}
              <strong>privado apenas para você</strong> (e administradores gerais).
            </span>
          </div>

          {loadingOpcoes ? (
            <div className="py-8 flex flex-col items-center justify-center gap-2 text-gray-500">
              <Loader2 className="w-6 h-6 animate-spin text-indigo-600" />
              <span className="text-xs">Carregando clínicas e profissionais do paciente...</span>
            </div>
          ) : (
            <div className="space-y-3">
              {/* Responsável */}
              <label className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl border border-gray-200 cursor-pointer hover:bg-indigo-50/30 transition-colors">
                <input
                  type="checkbox"
                  checked={compartilhadoComResponsavel}
                  onChange={(e) => setCompartilhadoComResponsavel(e.target.checked)}
                  className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                />
                <UserCheck className="w-4 h-4 text-indigo-600 shrink-0" />
                <div className="text-xs">
                  <span className="font-semibold text-gray-900 block">
                    Responsável Legal de {arquivo.nomePaciente}
                  </span>
                  <span className="text-gray-500">
                    Permite que os pais/tutores visualizem e baixem o documento.
                  </span>
                </div>
              </label>

              {/* Clínica */}
              <div className="p-3 bg-gray-50 rounded-xl border border-gray-200 space-y-2.5">
                <label className="flex items-center gap-3 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={compartilhadoComClinica}
                    onChange={(e) => setCompartilhadoComClinica(e.target.checked)}
                    className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <Building2 className="w-4 h-4 text-teal-600 shrink-0" />
                  <div className="text-xs">
                    <span className="font-semibold text-gray-900 block">
                      Liberar para a Clínica
                    </span>
                    <span className="text-gray-500">
                      Todos os profissionais, recepção e admin da clínica selecionada terão acesso.
                    </span>
                  </div>
                </label>

                {compartilhadoComClinica && opcoes.clinicas.length > 0 && (
                  <div className="pl-7">
                    <select
                      value={idClinicaCompartilhada}
                      onChange={(e) => setIdClinicaCompartilhada(e.target.value)}
                      className="w-full px-3 py-1.5 text-xs border border-gray-300 rounded-lg bg-white"
                    >
                      <option value="">Todas as clínicas em que o paciente é atendido</option>
                      {opcoes.clinicas.map((c) => (
                        <option key={c.idClinica} value={c.idClinica}>
                          {c.nomeClinica}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
              </div>

              {/* Profissionais específicos */}
              <div className="p-3 bg-gray-50 rounded-xl border border-gray-200 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-gray-900 flex items-center gap-2">
                    <Briefcase className="w-4 h-4 text-purple-600" />
                    Profissionais Específicos
                  </span>
                  <span className="text-[11px] text-gray-500">
                    {idsProfissionais.length} selecionado(s)
                  </span>
                </div>

                {opcoes.profissionais.length === 0 ? (
                  <p className="text-xs text-gray-400 italic">
                    Nenhum profissional disponível para seleção direta.
                  </p>
                ) : (
                  <div className="max-h-36 overflow-y-auto bg-white border border-gray-200 rounded-lg divide-y divide-gray-100 p-1">
                    {opcoes.profissionais.map((prof) => (
                      <label
                        key={prof.idProf}
                        className="flex items-center gap-2.5 p-2 hover:bg-gray-50 rounded cursor-pointer text-xs"
                      >
                        <input
                          type="checkbox"
                          checked={idsProfissionais.includes(prof.idProf)}
                          onChange={() => toggleProfissional(prof.idProf)}
                          className="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        />
                        <span className="font-medium text-gray-800">{prof.nomeProfissional}</span>
                        {prof.conselhoProf && (
                          <span className="text-[11px] text-gray-400 font-mono">
                            ({prof.conselhoProf})
                          </span>
                        )}
                      </label>
                    ))}
                  </div>
                )}
              </div>
            </div>
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
              disabled={saving || loadingOpcoes}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-2 shadow-sm"
            >
              {saving && <Loader2 className="w-4 h-4 animate-spin" />}
              Salvar Permissões
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
