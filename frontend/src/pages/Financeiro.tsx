import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { DollarSign, Wallet, Tag, FileCheck2, Loader2, AlertCircle, Building2 } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { financeiroService } from '../services/financeiroService';
import { pacienteService } from '../services/pacienteService';
import { compromissoService } from '../services/compromissoService';
import type {
  ClinicaPlanoPrazo,
  ContratoParticular,
  GuiaFaturamento,
  PacoteParticular,
  TabelaPreco,
} from '../types/financeiro';
import type { ClinicaOption, Paciente, PlanoSaudeOption } from '../types/paciente';
import type { TipoCompromisso } from '../types/compromisso';
import ContratosParticularesTab from '../components/ContratosParticularesTab';
import TabelaPrecosTab from '../components/TabelaPrecosTab';
import FaturamentoConveniosTab from '../components/FaturamentoConveniosTab';

type TabKey = 'particulares' | 'precos' | 'convenios';

export default function Financeiro() {
  const [searchParams] = useSearchParams();
  const { user, isAdmin, isAdminClinica, isFuncionarioClinica, isResponsavel } = useAuth();
  const isResponsavelOnly = isResponsavel && !isAdmin && !isFuncionarioClinica;
  const canManageContratos = isAdmin || isFuncionarioClinica;
  const canManagePrecos = isAdmin || isAdminClinica;
  const canManageConvenios = isAdmin || isFuncionarioClinica;
  const canSignGuias = isResponsavel || isAdmin;

  const initialTab = searchParams.get('tab') === 'convenios' ? 'convenios' : 'particulares';
  const [activeTab, setActiveTab] = useState<TabKey>(initialTab);
  const [selectedClinicaId, setSelectedClinicaId] = useState<string>(user?.idClinica || '');
  const [clinicas, setClinicas] = useState<ClinicaOption[]>([]);
  const [precos, setPrecos] = useState<TabelaPreco[]>([]);
  const [pacotes, setPacotes] = useState<PacoteParticular[]>([]);
  const [prazosConvenios, setPrazosConvenios] = useState<ClinicaPlanoPrazo[]>([]);
  const [contratos, setContratos] = useState<ContratoParticular[]>([]);
  const [guiasConvenio, setGuiasConvenio] = useState<GuiaFaturamento[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [planos, setPlanos] = useState<PlanoSaudeOption[]>([]);
  const [tiposCompromisso, setTiposCompromisso] = useState<TipoCompromisso[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadBaseAndData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const clinicaParam = !isResponsavelOnly && selectedClinicaId ? selectedClinicaId : undefined;
      const [ctrData, guiasData, pacData] = await Promise.all([
        financeiroService.listContratosParticulares(clinicaParam ? { clinicaId: clinicaParam } : undefined),
        financeiroService
          .listGuiasConvenio(clinicaParam ? { clinicaId: clinicaParam } : undefined)
          .catch(() => []),
        pacienteService.list().catch(() => []),
      ]);
      setContratos(ctrData);
      setGuiasConvenio(guiasData);
      setPacientes(pacData);

      if (!isResponsavelOnly) {
        const [clinicasList, precosData, pacotesData, prazosData, planosList, tiposList] =
          await Promise.all([
            pacienteService.listClinicas().catch(() => []),
            financeiroService.listPrecos(selectedClinicaId || undefined),
            financeiroService.listPacotes(selectedClinicaId || undefined),
            financeiroService.listPrazosConvenios(selectedClinicaId || undefined),
            pacienteService.listPlanos().catch(() => []),
            compromissoService.listTipos().catch(() => []),
          ]);
        setClinicas(clinicasList);
        if (!selectedClinicaId && clinicasList.length > 0 && !user?.idClinica) {
          setSelectedClinicaId(clinicasList[0].idClinica);
        }
        setPrecos(precosData);
        setPacotes(pacotesData);
        setPrazosConvenios(prazosData);
        setPlanos(planosList);
        setTiposCompromisso(tiposList);
      }
    } catch {
      setError('Não foi possível carregar os dados financeiros.');
    } finally {
      setLoading(false);
    }
  }, [isResponsavelOnly, selectedClinicaId, user?.idClinica]);

  useEffect(() => {
    loadBaseAndData();
  }, [loadBaseAndData]);

  const guiasPendentesAssinatura = useMemo(
    () => guiasConvenio.filter((g) => g.status === 'AGUARDANDO_ASSINATURA').length,
    [guiasConvenio]
  );

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
            <DollarSign className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              {isResponsavelOnly ? 'Financeiro & Pacotes' : 'Módulo Financeiro'}
            </h1>
            <p className="text-xs text-gray-500">
              {isResponsavelOnly
                ? 'Acompanhe os pacotes particulares pagos e o saldo de sessões por especialidade.'
                : 'Tabela de preços, combos particulares pré-pagos e controle por clínica.'}
            </p>
          </div>
        </div>

        {isAdmin && clinicas.length > 0 && (
          <div className="flex items-center gap-2">
            <Building2 className="h-4 w-4 text-gray-400" />
            <select
              value={selectedClinicaId}
              onChange={(e) => setSelectedClinicaId(e.target.value)}
              aria-label="Selecionar clínica"
              className="px-3 py-2 text-xs border border-gray-200 rounded-xl bg-white font-medium text-gray-700"
            >
              {clinicas.map((c) => (
                <option key={c.idClinica} value={c.idClinica}>
                  {c.nomeClinica}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Navegação por abas */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setActiveTab('particulares')}
          className={`px-4 py-2 rounded-xl text-sm font-semibold flex items-center gap-2 transition ${
            activeTab === 'particulares'
              ? 'bg-indigo-600 text-white shadow-sm'
              : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
          }`}
        >
          <Wallet className="h-4 w-4" />
          Particulares & Pacotes
        </button>

        {!isResponsavelOnly && (
          <button
            onClick={() => setActiveTab('precos')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold flex items-center gap-2 transition ${
              activeTab === 'precos'
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
            }`}
          >
            <Tag className="h-4 w-4" />
            Tabela de Preços & Combos
          </button>
        )}

        <button
          onClick={() => setActiveTab('convenios')}
          className={`px-4 py-2 rounded-xl text-sm font-semibold flex items-center gap-2 transition ${
            activeTab === 'convenios'
              ? 'bg-indigo-600 text-white shadow-sm'
              : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
          }`}
        >
          <FileCheck2 className="h-4 w-4" />
          Guias de Convênio
          {guiasPendentesAssinatura > 0 && (
            <span className="ml-1 px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-400 text-amber-950">
              {guiasPendentesAssinatura}
            </span>
          )}
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm flex items-center gap-2">
          <AlertCircle className="h-4 w-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="bg-white rounded-2xl border border-gray-200 p-12 flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
          <span className="text-sm text-gray-500">Carregando dados financeiros...</span>
        </div>
      ) : activeTab === 'particulares' ? (
        <ContratosParticularesTab
          clinicaId={selectedClinicaId || user?.idClinica || ''}
          canManage={canManageContratos}
          isResponsavelView={isResponsavelOnly}
          contratos={contratos}
          pacientes={pacientes}
          pacotes={pacotes}
          precos={precos}
          tiposCompromisso={tiposCompromisso}
          onRefresh={loadBaseAndData}
        />
      ) : activeTab === 'precos' && !isResponsavelOnly ? (
        <TabelaPrecosTab
          clinicaId={selectedClinicaId || user?.idClinica || ''}
          canManage={canManagePrecos}
          precos={precos}
          pacotes={pacotes}
          prazosConvenios={prazosConvenios}
          tiposCompromisso={tiposCompromisso}
          planos={planos}
          onRefresh={loadBaseAndData}
        />
      ) : (
        <FaturamentoConveniosTab
          clinicaId={selectedClinicaId || user?.idClinica || ''}
          canManage={canManageConvenios}
          canSign={canSignGuias}
          isResponsavelView={isResponsavelOnly}
          guias={guiasConvenio}
          pacientes={pacientes}
          planos={planos}
          onRefresh={loadBaseAndData}
        />
      )}
    </div>
  );
}
