import { useState, useEffect } from 'react';
import { X, Building2, Loader2, AlertCircle } from 'lucide-react';
import { clinicaService } from '../services/clinicaService';
import type { Clinica, SaveClinicaInput } from '../types/clinica';
import { extractApiErrorMessage, extractInactiveConflict, type InactiveConflictInfo } from '../utils/apiError';
import InactiveConflictAlert from './InactiveConflictAlert';

interface ClinicaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (clinica: Clinica) => void;
  initialData?: Clinica | null;
}

export default function ClinicaModal({
  isOpen,
  onClose,
  onSuccess,
  initialData,
}: ClinicaModalProps) {
  const [nomeClinica, setNomeClinica] = useState('');
  const [cnpjClinica, setCnpjClinica] = useState('');
  const [telefoneClinica, setTelefoneClinica] = useState('');
  const [whatsappClinica, setWhatsappClinica] = useState('');
  const [emailClinica, setEmailClinica] = useState('');
  const [enderecoClinica, setEnderecoClinica] = useState('');

  const [loading, setLoading] = useState(false);
  const [reactivating, setReactivating] = useState(false);
  const [error, setError] = useState('');
  const [inactiveConflict, setInactiveConflict] = useState<InactiveConflictInfo | null>(null);
  const [pendingPayload, setPendingPayload] = useState<SaveClinicaInput | null>(null);

  useEffect(() => {
    if (initialData) {
      setNomeClinica(initialData.nomeClinica || '');
      setCnpjClinica(formatCnpj(initialData.cnpjClinica || ''));
      setTelefoneClinica(initialData.telefoneClinica || '');
      setWhatsappClinica(initialData.whatsappClinica || '');
      setEmailClinica(initialData.emailClinica || '');
      setEnderecoClinica(initialData.enderecoClinica || '');
    } else {
      setNomeClinica('');
      setCnpjClinica('');
      setTelefoneClinica('');
      setWhatsappClinica('');
      setEmailClinica('');
      setEnderecoClinica('');
    }
    setError('');
    setInactiveConflict(null);
    setPendingPayload(null);
  }, [initialData, isOpen]);

  const formatCnpj = (value: string) => {
    const raw = value.replace(/\D/g, '').slice(0, 14);
    if (raw.length <= 2) return raw;
    if (raw.length <= 5) return `${raw.slice(0, 2)}.${raw.slice(2)}`;
    if (raw.length <= 8) return `${raw.slice(0, 2)}.${raw.slice(2, 5)}.${raw.slice(5)}`;
    if (raw.length <= 12) return `${raw.slice(0, 2)}.${raw.slice(2, 5)}.${raw.slice(5, 8)}/${raw.slice(8)}`;
    return `${raw.slice(0, 2)}.${raw.slice(2, 5)}.${raw.slice(5, 8)}/${raw.slice(8, 12)}-${raw.slice(12)}`;
  };

  const handleCnpjChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCnpjClinica(formatCnpj(e.target.value));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setInactiveConflict(null);

    const rawCnpj = cnpjClinica.replace(/\D/g, '');

    if (!nomeClinica.trim()) {
      setError('Informe o nome da clínica.');
      return;
    }

    if (rawCnpj.length !== 14) {
      setError('Informe um CNPJ válido com 14 dígitos.');
      return;
    }

    const payload: SaveClinicaInput = {
      nomeClinica: nomeClinica.trim(),
      cnpjClinica: rawCnpj,
      telefoneClinica: telefoneClinica.trim() || undefined,
      whatsappClinica: whatsappClinica.trim() || undefined,
      emailClinica: emailClinica.trim() || undefined,
      enderecoClinica: enderecoClinica.trim() || undefined,
    };

    setLoading(true);
    setPendingPayload(payload);
    try {
      let saved: Clinica;
      if (initialData?.idClinica) {
        saved = await clinicaService.update(initialData.idClinica, payload);
      } else {
        saved = await clinicaService.create(payload);
      }
      onSuccess(saved);
      onClose();
    } catch (err: unknown) {
      const conflict = extractInactiveConflict(err);
      if (conflict) {
        setInactiveConflict(conflict);
      } else {
        setError(
          extractApiErrorMessage(err, 'Erro ao salvar clínica. Verifique se o CNPJ já está cadastrado.')
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const handleReactivateExisting = async () => {
    if (!inactiveConflict) return;
    setReactivating(true);
    setError('');
    try {
      let reactivated = await clinicaService.reativar(inactiveConflict.resourceId);
      if (pendingPayload) {
        reactivated = await clinicaService.update(inactiveConflict.resourceId, pendingPayload);
      }
      onSuccess(reactivated);
      onClose();
    } catch (err: unknown) {
      setError(extractApiErrorMessage(err, 'Não foi possível reativar a clínica.'));
    } finally {
      setReactivating(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-gray-100 animate-in fade-in zoom-in duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
          <div className="flex items-center gap-2 text-indigo-900 font-semibold text-lg">
            <Building2 className="h-5 w-5 text-indigo-600" />
            <h2>{initialData ? 'Editar Clínica' : 'Nova Clínica'}</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <InactiveConflictAlert
            conflict={inactiveConflict}
            onReactivate={handleReactivateExisting}
            loading={reactivating}
          />
          {error && (
            <div className="flex items-center gap-2 p-3 text-sm text-red-600 bg-red-50 rounded-xl border border-red-100">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <p>{error}</p>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              Nome / Razão Social *
            </label>
            <input
              type="text"
              required
              value={nomeClinica}
              onChange={(e) => setNomeClinica(e.target.value)}
              placeholder="Ex: Clínica Integração e Desenvolvimento"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              CNPJ *
            </label>
            <input
              type="text"
              required
              value={cnpjClinica}
              onChange={handleCnpjChange}
              placeholder="00.000.000/0000-00"
              maxLength={18}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition font-mono"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                Telefone
              </label>
              <input
                type="text"
                value={telefoneClinica}
                onChange={(e) => setTelefoneClinica(e.target.value)}
                placeholder="(00) 0000-0000"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                WhatsApp
              </label>
              <input
                type="text"
                value={whatsappClinica}
                onChange={(e) => setWhatsappClinica(e.target.value)}
                placeholder="(00) 90000-0000"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              E-mail de Contato
            </label>
            <input
              type="email"
              value={emailClinica}
              onChange={(e) => setEmailClinica(e.target.value)}
              placeholder="contato@clinica.com.br"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
              Endereço Completo
            </label>
            <textarea
              rows={2}
              value={enderecoClinica}
              onChange={(e) => setEnderecoClinica(e.target.value)}
              placeholder="Rua, Número, Bairro, Cidade - UF, CEP"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          {/* Footer buttons */}
          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors shadow-sm"
            >
              {loading && <Loader2 className="h-4 w-4 animate-spin" />}
              {initialData ? 'Salvar Alterações' : 'Cadastrar Clínica'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
