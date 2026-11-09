import { useState, useEffect, useCallback, useContext, type FormEvent } from 'react';
import {
  X,
  HeartPulse,
  Edit3,
  Loader2,
  AlertCircle,
  Building2,
  CreditCard,
  UserCheck,
  Users,
  RefreshCw,
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { pacienteService } from '../services/pacienteService';
import type { Paciente, SavePacienteInput, ClinicaOption, PlanoSaudeOption, ResponsavelOption } from '../types/paciente';

interface PacienteModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (paciente: Paciente) => void;
  initialData?: Paciente | null;
  hasOutroResponsavel?: boolean;
}

export default function PacienteModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
  hasOutroResponsavel,
}: PacienteModalProps) {
  const auth = useContext(AuthContext);
  const user = auth?.user;
  const userIsResponsavel = Boolean(auth?.isResponsavel);
  const isEditing = Boolean(initialData);
  const jaTemOutroResponsavel = Boolean(hasOutroResponsavel || initialData?.hasOutroResponsavel);

  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [apelido, setApelido] = useState('');
  const [datanasc, setDatanasc] = useState('');
  const [rg, setRg] = useState('');
  const [endereco, setEndereco] = useState('');
  const [celular, setCelular] = useState('');
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [isProprioResponsavel, setIsProprioResponsavel] = useState(false);
  const [idResponsavel, setIdResponsavel] = useState('');

  // Plano de Saúde e Clínica
  const [idPlanoSaude, setIdPlanoSaude] = useState('');
  const [numeroCarteirinha, setNumeroCarteirinha] = useState('');
  const [idClinica, setIdClinica] = useState('');

  const [planosOptions, setPlanosOptions] = useState<PlanoSaudeOption[]>([]);
  const [clinicasOptions, setClinicasOptions] = useState<ClinicaOption[]>([]);
  const [responsaveisOptions, setResponsaveisOptions] = useState<ResponsavelOption[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(false);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const formatCPF = (value: string) => {
    const raw = value.replace(/\D/g, '').slice(0, 11);
    return raw
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  };

  const formatPhone = (value: string) => {
    const raw = value.replace(/\D/g, '').slice(0, 11);
    if (raw.length <= 10) {
      return raw.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    }
    return raw.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
  };

  const loadOptions = useCallback(async () => {
    setLoadingOptions(true);
    try {
      const [planos, clinicas, resps] = await Promise.all([
        pacienteService.listPlanos(),
        pacienteService.listClinicas(),
        pacienteService.listResponsaveis(),
      ]);
      setPlanosOptions(planos);
      setClinicasOptions(clinicas);
      setResponsaveisOptions(resps);
    } catch {
      // Ignora erros de carregamento secundário
    } finally {
      setLoadingOptions(false);
    }
  }, []);

  useEffect(() => {
    if (!isOpen) return;
    loadOptions();

    if (initialData) {
      setNome(initialData.nomePessoa || '');
      setCpf(initialData.cpf ? formatCPF(initialData.cpf) : '');
      setApelido(initialData.apelidoPaciente || '');
      setDatanasc(initialData.datanasc || '');
      setRg(initialData.rg || '');
      setEndereco(initialData.endereco || '');
      setCelular(initialData.celular ? formatPhone(initialData.celular) : '');
      setTelefone(initialData.telefone ? formatPhone(initialData.telefone) : '');
      setEmail(initialData.email || '');
      setIsProprioResponsavel(jaTemOutroResponsavel ? false : Boolean(initialData.isResponsavel));
      setIdResponsavel(initialData.idResponsavel || '');
      setIdPlanoSaude(initialData.idPlanoSaude || '');
      setNumeroCarteirinha(initialData.numeroCarteirinha || '');
      setIdClinica(initialData.idClinica || '');
    } else {
      setNome('');
      setCpf('');
      setApelido('');
      setDatanasc('');
      setRg('');
      setEndereco('');
      setCelular('');
      setTelefone('');
      setEmail('');
      setIsProprioResponsavel(false);
      setIdResponsavel(userIsResponsavel && user?.idResponsavel ? user.idResponsavel : '');
      setIdPlanoSaude('');
      setNumeroCarteirinha('');
      setIdClinica('');
    }
    setErrors({});
    setApiError('');
  }, [isOpen, initialData, loadOptions, jaTemOutroResponsavel, userIsResponsavel, user]);

  if (!isOpen) return null;

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!nome.trim()) {
      newErrors.nome = 'Informe o nome completo do paciente';
    }
    const cleanCpf = cpf.replace(/\D/g, '');
    if (!cleanCpf) {
      newErrors.cpf = 'Informe o CPF do paciente';
    } else if (cleanCpf.length !== 11) {
      newErrors.cpf = 'CPF deve conter 11 dígitos';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setApiError('');

    if (!validate()) return;

    setSubmitting(true);
    try {
      const cleanCpf = cpf.replace(/\D/g, '');
      const payload: SavePacienteInput = {
        nomePessoa: nome.trim(),
        CPFPessoa: cleanCpf,
        cpf: cleanCpf,
        apelidoPaciente: apelido.trim() || undefined,
        datanascPessoa: datanasc || undefined,
        RGPessoa: rg.trim() || undefined,
        rg: rg.trim() || undefined,
        enderecoPessoa: endereco.trim() || undefined,
        telefonePessoa: telefone.trim() || undefined,
        celularPessoa: celular.trim() || undefined,
        emailPessoa: email.trim() || undefined,
        isResponsavel: isProprioResponsavel,
        idResponsavel: !isProprioResponsavel && idResponsavel ? idResponsavel : undefined,
        idPlanoSaude: idPlanoSaude || undefined,
        numeroCarteirinha: numeroCarteirinha.trim() || undefined,
        idClinica: idClinica || undefined,
      };

      const result = isEditing && initialData
        ? await pacienteService.update(initialData.idPaciente, payload)
        : await pacienteService.create(payload);

      onSuccess(result);
      onClose();
    } catch (err: unknown) {
      let msg = isEditing ? 'Erro ao atualizar paciente.' : 'Erro ao cadastrar paciente.';
      if (err && typeof err === 'object' && 'response' in err) {
        const res = (err as { response?: { data?: { errorMessage?: string; message?: string; details?: string[] } } }).response;
        if (res?.data) {
          if (Array.isArray(res.data.details) && res.data.details.length > 0) {
            msg = res.data.details.join(', ');
          } else if (res.data.errorMessage) {
            msg = res.data.errorMessage;
          } else if (res.data.message) {
            msg = res.data.message;
          }
        }
      }
      setApiError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden animate-in fade-in zoom-in-95 duration-150 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center">
              {isEditing ? <Edit3 className="h-5 w-5" /> : <HeartPulse className="h-5 w-5" />}
            </div>
            <div>
              <h3 className="font-bold text-gray-900 text-lg">
                {isEditing ? 'Editar Paciente' : 'Cadastrar Novo Paciente'}
              </h3>
              <p className="text-xs text-gray-500">
                {isEditing ? 'Atualize os dados cadastrais do paciente' : 'Perfil para acompanhamento e agendamentos'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 rounded-lg p-1.5 hover:bg-gray-100 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto flex-1">
          {apiError && (
            <div className="flex items-center gap-2 p-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
              <span>{apiError}</span>
            </div>
          )}

          {/* Nome e Apelido */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Nome Completo *
              </label>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Ex: Gabriel Henrique da Silva"
                className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                  errors.nome ? 'border-red-500 bg-red-50' : 'border-gray-200'
                }`}
              />
              {errors.nome && <p className="text-xs text-red-600 mt-1">{errors.nome}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Apelido
              </label>
              <input
                type="text"
                value={apelido}
                onChange={(e) => setApelido(e.target.value)}
                placeholder="Ex: Biel, Gabi"
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          {/* CPF e Data de Nascimento */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                CPF *
              </label>
              <input
                type="text"
                value={cpf}
                onChange={(e) => setCpf(formatCPF(e.target.value))}
                disabled={isEditing}
                placeholder="000.000.000-00"
                className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                  isEditing ? 'bg-gray-100 text-gray-500 cursor-not-allowed' : ''
                } ${errors.cpf ? 'border-red-500 bg-red-50' : 'border-gray-200'}`}
              />
              {errors.cpf && <p className="text-xs text-red-600 mt-1">{errors.cpf}</p>}
              {isEditing && (
                <p className="text-[11px] text-gray-400 mt-0.5">O documento não pode ser alterado.</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Data de Nascimento
              </label>
              <input
                type="date"
                value={datanasc}
                onChange={(e) => setDatanasc(e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          {/* RG e Contato */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                RG
              </label>
              <input
                type="text"
                value={rg}
                onChange={(e) => setRg(e.target.value)}
                placeholder="Registro Geral"
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Celular / WhatsApp
              </label>
              <input
                type="text"
                value={celular}
                onChange={(e) => setCelular(formatPhone(e.target.value))}
                placeholder="(00) 00000-0000"
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Telefone Fixo
              </label>
              <input
                type="text"
                value={telefone}
                onChange={(e) => setTelefone(formatPhone(e.target.value))}
                placeholder="(00) 0000-0000"
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
              E-mail
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="exemplo@email.com"
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          {/* Endereço */}
          <div>
            <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">
              Endereço Completo
            </label>
            <input
              type="text"
              value={endereco}
              onChange={(e) => setEndereco(e.target.value)}
              placeholder="Rua, número, complemento, bairro, cidade - UF"
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          {/* Seção Responsável Legal */}
          <div className="border border-gray-200 rounded-xl p-3 bg-gray-50/40 space-y-2.5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4 text-indigo-600" />
                <span className="text-xs font-semibold text-gray-800 uppercase tracking-wide">
                  Responsável Legal
                </span>
              </div>
              <button
                type="button"
                onClick={loadOptions}
                disabled={loadingOptions}
                title="Atualizar opções"
                className="text-gray-400 hover:text-indigo-600 p-1 rounded-md hover:bg-gray-100 transition-colors"
              >
                <RefreshCw className={`h-3.5 w-3.5 ${loadingOptions ? 'animate-spin text-indigo-600' : ''}`} />
              </button>
            </div>

            {/* Checkbox: Paciente é seu próprio responsável (somente se não tiver outro responsável pré-vinculado) */}
            {!jaTemOutroResponsavel && (
              <div className="bg-white p-2.5 rounded-lg border border-gray-200">
                <label className="flex items-start gap-2.5 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={isProprioResponsavel}
                    onChange={(e) => {
                      const checked = e.target.checked;
                      setIsProprioResponsavel(checked);
                      if (checked) {
                        setIdResponsavel('');
                      }
                    }}
                    className="mt-0.5 h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <div className="text-xs">
                    <span className="font-semibold text-gray-800 flex items-center gap-1.5">
                      <UserCheck className="h-3.5 w-3.5 text-indigo-600" />
                      O paciente também é seu próprio responsável legal?
                    </span>
                    <span className="text-gray-500 block text-[11px] mt-0.5">
                      Marque caso o paciente seja maior de idade e responda por si mesmo.
                    </span>
                  </div>
                </label>
              </div>
            )}

            {/* Caso o paciente tenha outro responsável (criança/adolescente/dependente) */}
            {!isProprioResponsavel && (
              <div>
                <label className="block text-[11px] font-medium text-gray-700 mb-1">
                  Selecione o Responsável Legal
                </label>
                <select
                  value={idResponsavel}
                  onChange={(e) => setIdResponsavel(e.target.value)}
                  disabled={loadingOptions}
                  className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                >
                  <option value="">Selecione um responsável cadastrado...</option>
                  {userIsResponsavel && user?.idResponsavel && (
                    <option value={user.idResponsavel}>
                      Eu mesmo ({user.nomeUser || user.emailUser})
                    </option>
                  )}
                  {responsaveisOptions.map((resp) => {
                    if (userIsResponsavel && user?.idResponsavel && resp.idResponsavel === user.idResponsavel) {
                      return null;
                    }
                    return (
                      <option key={resp.idResponsavel} value={resp.idResponsavel}>
                        {resp.nome} {resp.cpf ? `(CPF: ${resp.cpf})` : ''}
                      </option>
                    );
                  })}
                </select>
                <p className="text-[10px] text-gray-400 mt-1 italic">
                  Caso o responsável ainda não esteja na lista, cadastre-o no menu Responsáveis e clique no botão de atualizar ao lado.
                </p>
              </div>
            )}
          </div>

          {/* Espera: Plano de Saúde */}
          <div className="border border-gray-200 rounded-xl p-3 bg-gray-50/40 space-y-2.5">
            <div className="flex items-center gap-2">
              <CreditCard className="h-4 w-4 text-indigo-600" />
              <span className="text-xs font-semibold text-gray-800 uppercase tracking-wide">
                Plano de Saúde (Espera de Integração)
              </span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-medium text-gray-600 mb-1">
                  Selecione o Plano
                </label>
                <select
                  value={idPlanoSaude}
                  onChange={(e) => setIdPlanoSaude(e.target.value)}
                  disabled={loadingOptions}
                  className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                >
                  <option value="">Nenhum / Particular</option>
                  {planosOptions.map((plano) => (
                    <option key={plano.idPlanoSaude} value={plano.idPlanoSaude}>
                      {plano.nomePlano} {plano.operadora ? `(${plano.operadora})` : ''}
                    </option>
                  ))}
                </select>
                {planosOptions.length === 0 && !loadingOptions && (
                  <p className="text-[10px] text-gray-400 mt-1 italic">
                    Nenhum plano cadastrado ainda no sistema.
                  </p>
                )}
              </div>

              <div>
                <label className="block text-[11px] font-medium text-gray-600 mb-1">
                  Nº da Carteirinha
                </label>
                <input
                  type="text"
                  value={numeroCarteirinha}
                  onChange={(e) => setNumeroCarteirinha(e.target.value)}
                  placeholder="Ex: 00123456789"
                  className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                />
              </div>
            </div>
          </div>

          {/* Espera: Clínica */}
          <div className="border border-gray-200 rounded-xl p-3 bg-gray-50/40 space-y-2.5">
            <div className="flex items-center gap-2">
              <Building2 className="h-4 w-4 text-indigo-600" />
              <span className="text-xs font-semibold text-gray-800 uppercase tracking-wide">
                Clínica de Atendimento (Espera de Integração)
              </span>
            </div>
            <div>
              <label className="block text-[11px] font-medium text-gray-600 mb-1">
                Selecione a Clínica
              </label>
              <select
                value={idClinica}
                onChange={(e) => setIdClinica(e.target.value)}
                disabled={loadingOptions}
                className="w-full px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
              >
                <option value="">Nenhuma / Não vinculada</option>
                {clinicasOptions.map((clinica) => (
                  <option key={clinica.idClinica} value={clinica.idClinica}>
                    {clinica.nomeClinica}
                  </option>
                ))}
              </select>
              {clinicasOptions.length === 0 && !loadingOptions && (
                <p className="text-[10px] text-gray-400 mt-1 italic">
                  Nenhuma clínica cadastrada ainda no sistema.
                </p>
              )}
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100 flex-shrink-0">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 rounded-lg transition-colors flex items-center gap-2"
            >
              {submitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  {isEditing ? 'Atualizando...' : 'Salvando...'}
                </>
              ) : (
                isEditing ? 'Salvar Alterações' : 'Salvar Paciente'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
