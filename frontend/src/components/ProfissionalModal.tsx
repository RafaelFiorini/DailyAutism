import { useState, useEffect, FormEvent } from 'react';
import { X, UserPlus, Edit3, Loader2, AlertCircle, Building2 } from 'lucide-react';
import { profissionalService } from '../services/profissionalService';
import { clinicaService } from '../services/clinicaService';
import type { Profissional, SaveProfissionalInput } from '../types/profissional';
import type { Clinica } from '../types/clinica';
import { extractApiErrorMessage, extractInactiveConflict, type InactiveConflictInfo } from '../utils/apiError';
import InactiveConflictAlert from './InactiveConflictAlert';

interface ProfissionalModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (item: Profissional) => void;
  initialData?: Profissional | null;
}

export default function ProfissionalModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
}: ProfissionalModalProps) {
  const isEditing = Boolean(initialData);

  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [conselho, setConselho] = useState('');
  const [email, setEmail] = useState('');
  const [celular, setCelular] = useState('');
  const [datanasc, setDatanasc] = useState('');
  const [rg, setRg] = useState('');
  const [endereco, setEndereco] = useState('');
  const [idClinica, setIdClinica] = useState('');
  const [clinicas, setClinicas] = useState<Clinica[]>([]);
  const [loadingClinicas, setLoadingClinicas] = useState(false);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [reactivating, setReactivating] = useState(false);
  const [inactiveConflict, setInactiveConflict] = useState<InactiveConflictInfo | null>(null);
  const [pendingPayload, setPendingPayload] = useState<SaveProfissionalInput | null>(null);

  useEffect(() => {
    if (isOpen) {
      setLoadingClinicas(true);
      clinicaService
        .list()
        .then((data) => setClinicas(data))
        .catch(() => setClinicas([]))
        .finally(() => setLoadingClinicas(false));

      if (initialData) {
        setNome(initialData.nomeProfissional || '');
        setCpf(initialData.cpf || '');
        setConselho(initialData.conselhoProf || '');
        setEmail(initialData.email || '');
        setCelular(initialData.telefone || '');
        setDatanasc(initialData.datanasc || '');
        setRg(initialData.rg || '');
        setEndereco(initialData.endereco || '');
        setIdClinica(initialData.idClinica || '');
      } else {
        setNome('');
        setCpf('');
        setConselho('');
        setEmail('');
        setCelular('');
        setDatanasc('');
        setRg('');
        setEndereco('');
        setIdClinica('');
      }
      setErrors({});
      setApiError('');
      setInactiveConflict(null);
      setPendingPayload(null);
    }
  }, [isOpen, initialData]);

  if (!isOpen) return null;

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!nome.trim()) newErrors.nome = 'Informe o nome completo';
    if (!cpf.trim()) newErrors.cpf = 'Informe o CPF';
    if (!conselho.trim()) newErrors.conselho = 'Informe o conselho profissional';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setApiError('');
    setInactiveConflict(null);

    if (!validate()) return;

    setSubmitting(true);
    try {
      const cleanCpf = cpf.replace(/\D/g, '');
      const payload: SaveProfissionalInput = {
        nomePessoa: nome.trim(),
        CPFPessoa: cleanCpf,
        cpf: cleanCpf,
        conselhoProf: conselho.trim(),
        emailPessoa: email.trim() || undefined,
        celularPessoa: celular.trim() || undefined,
        datanascPessoa: datanasc || undefined,
        RGPessoa: rg.trim() || undefined,
        rg: rg.trim() || undefined,
        enderecoPessoa: endereco.trim() || undefined,
        idClinica: idClinica || undefined,
      };

      setPendingPayload(payload);
      const result = isEditing && initialData
        ? await profissionalService.update(initialData.idProf, payload)
        : await profissionalService.create(payload);

      onSuccess(result);
      onClose();
    } catch (err: unknown) {
      const conflict = extractInactiveConflict(err);
      if (conflict) {
        setInactiveConflict(conflict);
      } else {
        setApiError(
          extractApiErrorMessage(err, isEditing ? 'Erro ao atualizar profissional.' : 'Erro ao cadastrar profissional.')
        );
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleReactivateExisting = async () => {
    if (!inactiveConflict) return;
    setReactivating(true);
    setApiError('');
    try {
      let reactivated = await profissionalService.reativar(inactiveConflict.resourceId);
      if (pendingPayload) {
        reactivated = await profissionalService.update(inactiveConflict.resourceId, pendingPayload);
      }
      onSuccess(reactivated);
      onClose();
    } catch (err: unknown) {
      setApiError(extractApiErrorMessage(err, 'Não foi possível reativar o profissional.'));
    } finally {
      setReactivating(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden animate-in fade-in zoom-in-95 duration-150 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center">
              {isEditing ? <Edit3 className="h-5 w-5" /> : <UserPlus className="h-5 w-5" />}
            </div>
            <div>
              <h3 className="font-bold text-gray-900 text-lg">
                {isEditing ? 'Editar Profissional' : 'Cadastrar Novo Profissional'}
              </h3>
              <p className="text-xs text-gray-500">
                {isEditing
                  ? 'Atualize os dados cadastrais e de contato'
                  : 'Médico, Psicólogo, Terapeuta Ocupacional, etc.'}
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
          <InactiveConflictAlert
            conflict={inactiveConflict}
            onReactivate={handleReactivateExisting}
            loading={reactivating}
          />
          {apiError && (
            <div className="p-3 rounded-lg bg-red-50 border border-red-200 flex items-center gap-2 text-red-700 text-sm">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
              <span>{apiError}</span>
            </div>
          )}

          {/* Nome */}
          <div>
            <label htmlFor="nome" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
              Nome Completo *
            </label>
            <input
              id="nome"
              type="text"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Ex: Dra. Ana Paula Costa"
              className={`w-full px-3 py-2 text-sm border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors ${
                errors.nome ? 'border-red-400 bg-red-50/30' : 'border-gray-300'
              }`}
            />
            {errors.nome && <p className="text-xs text-red-600 mt-1">{errors.nome}</p>}
          </div>

          {/* Grid CPF e Conselho */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="cpf" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                CPF *
              </label>
              <input
                id="cpf"
                type="text"
                value={cpf}
                onChange={(e) => setCpf(e.target.value)}
                placeholder="000.000.000-00"
                maxLength={14}
                disabled={isEditing}
                className={`w-full px-3 py-2 text-sm border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors ${
                  errors.cpf ? 'border-red-400 bg-red-50/30' : 'border-gray-300'
                } ${isEditing ? 'bg-gray-100 text-gray-500 cursor-not-allowed' : ''}`}
              />
              {errors.cpf && <p className="text-xs text-red-600 mt-1">{errors.cpf}</p>}
            </div>

            <div>
              <label htmlFor="conselho" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Registro no Conselho *
              </label>
              <input
                id="conselho"
                type="text"
                value={conselho}
                onChange={(e) => setConselho(e.target.value)}
                placeholder="Ex: CRM/SP 123456"
                className={`w-full px-3 py-2 text-sm border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors ${
                  errors.conselho ? 'border-red-400 bg-red-50/30' : 'border-gray-300'
                }`}
              />
              {errors.conselho && <p className="text-xs text-red-600 mt-1">{errors.conselho}</p>}
            </div>
          </div>

          {/* Grid Contatos */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="email" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                E-mail
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="profissional@clinica.com"
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label htmlFor="celular" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Celular / WhatsApp
              </label>
              <input
                id="celular"
                type="text"
                value={celular}
                onChange={(e) => setCelular(e.target.value)}
                placeholder="(11) 99999-9999"
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* RG e Data de Nascimento */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="rg" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                RG
              </label>
              <input
                id="rg"
                type="text"
                value={rg}
                onChange={(e) => setRg(e.target.value)}
                placeholder="00.000.000-0"
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
              />
            </div>
            <div>
              <label htmlFor="datanasc" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
                Data de Nascimento
              </label>
              <input
                id="datanasc"
                type="date"
                value={datanasc}
                onChange={(e) => setDatanasc(e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* Endereço */}
          <div>
            <label htmlFor="endereco" className="block text-xs font-semibold uppercase text-gray-700 mb-1">
              Endereço
            </label>
            <input
              id="endereco"
              type="text"
              value={endereco}
              onChange={(e) => setEndereco(e.target.value)}
              placeholder="Rua, número, bairro, cidade - UF"
              className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
            />
          </div>

          {/* Clínica de Atendimento */}
          <div className="pt-2">
            <label htmlFor="clinica" className="block text-xs font-semibold uppercase text-gray-700 mb-1 flex items-center gap-1.5">
              <Building2 className="h-3.5 w-3.5 text-indigo-600" />
              Clínica de Atendimento
            </label>
            <select
              id="clinica"
              value={idClinica}
              onChange={(e) => setIdClinica(e.target.value)}
              disabled={loadingClinicas}
              className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors bg-white disabled:bg-gray-100"
            >
              <option value="">Selecione uma clínica (opcional)</option>
              {clinicas.map((c) => (
                <option key={c.idClinica} value={c.idClinica}>
                  {c.nomeClinica}
                </option>
              ))}
            </select>
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
                isEditing ? 'Salvar Alterações' : 'Salvar Profissional'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
