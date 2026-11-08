import { useState, FormEvent } from 'react';
import {
  X,
  UserCheck,
  Stethoscope,
  Users,
  Lock,
  Mail,
  User,
  CreditCard,
  Phone,
  Calendar,
  MapPin,
  AlertCircle,
  Loader2,
  Plus,
  Trash2,
  CheckCircle2,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import type { PacienteInicialInput } from '../types/auth';

interface RegisterModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function RegisterModal({ isOpen, onClose }: RegisterModalProps) {
  const { register } = useAuth();

  const [tipo, setTipo] = useState<'RESPONSAVEL' | 'PROFISSIONAL'>('RESPONSAVEL');
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmSenha, setConfirmSenha] = useState('');
  const [telefone, setTelefone] = useState('');
  const [celular, setCelular] = useState('');
  const [datanasc, setDatanasc] = useState('');
  const [endereco, setEndereco] = useState('');

  // Profissional
  const [conselhoProf, setConselhoProf] = useState('');

  // Responsável
  const [isPacienteTambem, setIsPacienteTambem] = useState(false);
  const [pacientes, setPacientes] = useState<PacienteInicialInput[]>([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleFormatCpf = (value: string) => {
    const raw = value.replace(/\D/g, '').slice(0, 11);
    if (raw.length <= 3) return raw;
    if (raw.length <= 6) return `${raw.slice(0, 3)}.${raw.slice(3)}`;
    if (raw.length <= 9) return `${raw.slice(0, 3)}.${raw.slice(3, 6)}.${raw.slice(6)}`;
    return `${raw.slice(0, 3)}.${raw.slice(3, 6)}.${raw.slice(6, 9)}-${raw.slice(9)}`;
  };

  const handleAddPaciente = () => {
    setPacientes([...pacientes, { nome: '', cpf: '', apelido: '', datanasc: '' }]);
  };

  const handleRemovePaciente = (index: number) => {
    setPacientes(pacientes.filter((_, i) => i !== index));
  };

  const handlePacienteChange = (index: number, field: keyof PacienteInicialInput, val: string) => {
    const updated = [...pacientes];
    if (field === 'cpf') {
      updated[index] = { ...updated[index], [field]: handleFormatCpf(val) };
    } else {
      updated[index] = { ...updated[index], [field]: val };
    }
    setPacientes(updated);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!nome.trim()) {
      setError('Informe o seu nome completo.');
      return;
    }

    const cleanCpf = cpf.replace(/\D/g, '');
    if (cleanCpf.length !== 11) {
      setError('CPF inválido. Deve conter 11 dígitos.');
      return;
    }

    if (!email.trim() || !email.includes('@')) {
      setError('Informe um e-mail válido.');
      return;
    }

    if (senha.length < 6) {
      setError('A senha deve ter no mínimo 6 caracteres.');
      return;
    }

    if (senha !== confirmSenha) {
      setError('As senhas não coincidem.');
      return;
    }

    if (tipo === 'PROFISSIONAL' && !conselhoProf.trim()) {
      setError('Informe o registro no conselho profissional (ex: CRM, CRP, CREFITO).');
      return;
    }

    if (tipo === 'RESPONSAVEL') {
      for (let i = 0; i < pacientes.length; i++) {
        const p = pacientes[i];
        if (!p.nome.trim()) {
          setError(`Informe o nome do paciente #${i + 1}.`);
          return;
        }
        const pCpf = p.cpf.replace(/\D/g, '');
        if (pCpf.length !== 11) {
          setError(`CPF do paciente #${i + 1} deve conter 11 dígitos.`);
          return;
        }
      }
    }

    setLoading(true);
    try {
      await register({
        tipo,
        nome: nome.trim(),
        cpf: cleanCpf,
        email: email.trim().toLowerCase(),
        senha,
        telefone: telefone.replace(/\D/g, '') || undefined,
        celular: celular.replace(/\D/g, '') || undefined,
        datanasc: datanasc || undefined,
        endereco: endereco.trim() || undefined,
        conselhoProf: tipo === 'PROFISSIONAL' ? conselhoProf.trim() : undefined,
        isPacienteTambem: tipo === 'RESPONSAVEL' ? isPacienteTambem : false,
        pacientes:
          tipo === 'RESPONSAVEL'
            ? pacientes.map((p) => ({
                ...p,
                cpf: p.cpf.replace(/\D/g, ''),
              }))
            : undefined,
      });

      onClose();
    } catch (err: any) {
      const msg =
        err?.response?.data?.errorMessage ||
        err?.response?.data?.message ||
        'Não foi possível realizar o cadastro. Verifique os dados ou tente novamente.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-gradient-to-r from-indigo-50 to-blue-50">
          <div>
            <h2 className="text-xl font-bold text-gray-900">Primeiro Acesso / Cadastre-se</h2>
            <p className="text-xs text-gray-500 mt-0.5">
              Crie seu acesso ao DailyAutism para gerenciar pacientes ou atendimentos.
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-white/80 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          {error && (
            <div className="p-3.5 rounded-lg bg-red-50 border border-red-200 flex items-start gap-2.5 text-red-700 text-sm">
              <AlertCircle className="h-5 w-5 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form id="register-form" onSubmit={handleSubmit} className="space-y-6">
            {/* Escolha do Perfil */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-2">
                Qual é o seu perfil de acesso? *
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setTipo('RESPONSAVEL')}
                  className={`p-4 rounded-xl border text-left flex items-start gap-3 transition-all ${
                    tipo === 'RESPONSAVEL'
                      ? 'border-indigo-600 bg-indigo-50/70 ring-2 ring-indigo-500/20'
                      : 'border-gray-200 hover:border-gray-300 bg-white'
                  }`}
                >
                  <div
                    className={`p-2 rounded-lg ${
                      tipo === 'RESPONSAVEL'
                        ? 'bg-indigo-600 text-white'
                        : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    <Users className="h-5 w-5" />
                  </div>
                  <div>
                    <div className="font-semibold text-gray-900 text-sm flex items-center gap-1.5">
                      Sou Responsável
                      {tipo === 'RESPONSAVEL' && (
                        <CheckCircle2 className="h-4 w-4 text-indigo-600" />
                      )}
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      Pai, mãe, tutor ou o próprio paciente em acompanhamento.
                    </p>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => setTipo('PROFISSIONAL')}
                  className={`p-4 rounded-xl border text-left flex items-start gap-3 transition-all ${
                    tipo === 'PROFISSIONAL'
                      ? 'border-indigo-600 bg-indigo-50/70 ring-2 ring-indigo-500/20'
                      : 'border-gray-200 hover:border-gray-300 bg-white'
                  }`}
                >
                  <div
                    className={`p-2 rounded-lg ${
                      tipo === 'PROFISSIONAL'
                        ? 'bg-indigo-600 text-white'
                        : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    <Stethoscope className="h-5 w-5" />
                  </div>
                  <div>
                    <div className="font-semibold text-gray-900 text-sm flex items-center gap-1.5">
                      Sou Profissional
                      {tipo === 'PROFISSIONAL' && (
                        <CheckCircle2 className="h-4 w-4 text-indigo-600" />
                      )}
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      Médico, psicólogo, terapeuta ocupacional, fonoaudiólogo, etc.
                    </p>
                  </div>
                </button>
              </div>
            </div>

            {/* Dados Pessoais */}
            <div className="space-y-4">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">
                Dados Pessoais
              </h3>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Nome Completo *
                </label>
                <div className="relative">
                  <User className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    required
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    placeholder="Ex: Maria dos Santos"
                    className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    CPF * (11 dígitos)
                  </label>
                  <div className="relative">
                    <CreditCard className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      required
                      value={cpf}
                      onChange={(e) => setCpf(handleFormatCpf(e.target.value))}
                      placeholder="000.000.000-00"
                      maxLength={14}
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Data de Nascimento
                  </label>
                  <div className="relative">
                    <Calendar className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="date"
                      value={datanasc}
                      onChange={(e) => setDatanasc(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              </div>

              {/* Registro no Conselho (apenas profissional) */}
              {tipo === 'PROFISSIONAL' && (
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Registro no Conselho (CRM, CRP, CREFITO, etc.) *
                  </label>
                  <div className="relative">
                    <Stethoscope className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      required
                      value={conselhoProf}
                      onChange={(e) => setConselhoProf(e.target.value)}
                      placeholder="Ex: CRP 06/12345"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Celular / WhatsApp
                  </label>
                  <div className="relative">
                    <Phone className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="tel"
                      value={celular}
                      onChange={(e) => setCelular(e.target.value)}
                      placeholder="(00) 00000-0000"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Telefone Fixo
                  </label>
                  <div className="relative">
                    <Phone className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="tel"
                      value={telefone}
                      onChange={(e) => setTelefone(e.target.value)}
                      placeholder="(00) 0000-0000"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Endereço</label>
                <div className="relative">
                  <MapPin className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    value={endereco}
                    onChange={(e) => setEndereco(e.target.value)}
                    placeholder="Rua, número, complemento, bairro, cidade"
                    className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>
            </div>

            {/* Dados de Acesso */}
            <div className="space-y-4">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">
                Dados de Acesso
              </h3>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">E-mail *</label>
                <div className="relative">
                  <Mail className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="seuemail@exemplo.com"
                    className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Senha de Acesso * (mínimo 6)
                  </label>
                  <div className="relative">
                    <Lock className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="password"
                      required
                      value={senha}
                      onChange={(e) => setSenha(e.target.value)}
                      placeholder="••••••••"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Confirmar Senha *
                  </label>
                  <div className="relative">
                    <Lock className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input
                      type="password"
                      required
                      value={confirmSenha}
                      onChange={(e) => setConfirmSenha(e.target.value)}
                      placeholder="••••••••"
                      className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Se Responsável: Pacientes vinculados */}
            {tipo === 'RESPONSAVEL' && (
              <div className="space-y-4 pt-2 border-t border-gray-100">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider">
                      Pacientes Vinculados
                    </h3>
                    <p className="text-xs text-gray-500">
                      Você pode se auto-vincular ou já cadastrar seus dependentes agora.
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={handleAddPaciente}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-lg transition-colors"
                  >
                    <Plus className="h-3.5 w-3.5" />
                    Adicionar Dependente
                  </button>
                </div>

                <label className="flex items-start gap-2.5 p-3 rounded-lg bg-gray-50 border border-gray-200 cursor-pointer hover:bg-gray-100/70 transition-colors">
                  <input
                    type="checkbox"
                    checked={isPacienteTambem}
                    onChange={(e) => setIsPacienteTambem(e.target.checked)}
                    className="mt-0.5 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <div>
                    <div className="text-xs font-semibold text-gray-900 flex items-center gap-1">
                      <UserCheck className="h-3.5 w-3.5 text-indigo-600" />
                      Eu também sou o paciente (estou em acompanhamento)
                    </div>
                    <p className="text-[11px] text-gray-500 mt-0.5">
                      Cria automaticamente um cadastro de paciente vinculado a você com os mesmos dados.
                    </p>
                  </div>
                </label>

                {pacientes.map((p, idx) => (
                  <div
                    key={idx}
                    className="p-3.5 bg-gray-50 rounded-xl border border-gray-200 space-y-3"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-gray-700">
                        Paciente #{idx + 1}
                      </span>
                      <button
                        type="button"
                        onClick={() => handleRemovePaciente(idx)}
                        className="text-red-500 hover:text-red-700 p-1"
                        title="Remover"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                      <input
                        type="text"
                        placeholder="Nome do Paciente *"
                        value={p.nome}
                        onChange={(e) => handlePacienteChange(idx, 'nome', e.target.value)}
                        className="w-full px-3 py-1.5 text-xs border border-gray-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                      />
                      <input
                        type="text"
                        placeholder="CPF do Paciente *"
                        value={p.cpf}
                        maxLength={14}
                        onChange={(e) => handlePacienteChange(idx, 'cpf', e.target.value)}
                        className="w-full px-3 py-1.5 text-xs border border-gray-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                      />
                      <input
                        type="text"
                        placeholder="Apelido (opcional)"
                        value={p.apelido || ''}
                        onChange={(e) => handlePacienteChange(idx, 'apelido', e.target.value)}
                        className="w-full px-3 py-1.5 text-xs border border-gray-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                      />
                      <input
                        type="date"
                        value={p.datanasc || ''}
                        onChange={(e) => handlePacienteChange(idx, 'datanasc', e.target.value)}
                        className="w-full px-3 py-1.5 text-xs border border-gray-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </form>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-100 bg-gray-50 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-4 py-2 text-xs font-semibold text-gray-700 hover:bg-gray-200 rounded-lg transition-colors"
          >
            Cancelar
          </button>
          <button
            type="submit"
            form="register-form"
            disabled={loading}
            className="inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 rounded-lg shadow-sm transition-colors"
          >
            {loading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Criando Cadastro...
              </>
            ) : (
              'Concluir Cadastro'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
