import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Arquivos from './Arquivos';
import { AuthContext, AuthContextType } from '../context/AuthContext';
import { arquivoService } from '../services/arquivoService';
import { pacienteService } from '../services/pacienteService';
import type { Arquivo } from '../types/arquivo';

vi.mock('../services/arquivoService', () => ({
  arquivoService: {
    list: vi.fn(),
    listTipos: vi.fn(),
    opcoesCompartilhamento: vi.fn().mockResolvedValue({ clinicas: [], profissionais: [] }),
    download: vi.fn(),
    triggerBrowserDownload: vi.fn(),
    delete: vi.fn(),
    compartilhar: vi.fn(),
  },
}));

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    list: vi.fn(),
  },
}));

const mockArquivos: Arquivo[] = [
  {
    idArquivo: 'a1',
    nomeArquivo: 'LaudoMedico_LucasSilva_20261110_100000.pdf',
    descricao: 'Laudo neuropediátrico atualizado',
    dataArquivo: '2026-11-10',
    dataUpload: '2026-11-10',
    extensaoArquivo: 'pdf',
    idPaciente: 'pac1',
    nomePaciente: 'Lucas Silva',
    idTipoArquivo: 't1',
    nomeTipoArquivo: 'Laudo Médico',
    idUser: 'u1',
    nomeUploadadoPor: 'Dr. Marcos',
    compartilhadoComResponsavel: false,
    compartilhadoComClinica: false,
    profissionaisCompartilhados: [],
  },
  {
    idArquivo: 'a2',
    nomeArquivo: 'ResultadoDeExame_LucasSilva_20261105_090000.docx',
    descricao: 'Exame auditivo',
    dataArquivo: '2026-11-05',
    dataUpload: '2026-11-06',
    extensaoArquivo: 'docx',
    idPaciente: 'pac1',
    nomePaciente: 'Lucas Silva',
    idTipoArquivo: 't2',
    nomeTipoArquivo: 'Resultado de Exame',
    idUser: 'u2',
    nomeUploadadoPor: 'Viviane Silva',
    compartilhadoComResponsavel: false,
    compartilhadoComClinica: true,
    nomeClinicaCompartilhada: 'Attenzione',
    profissionaisCompartilhados: [],
  },
];

function renderWithAuth(userOverrides?: Partial<AuthContextType>) {
  const authValue: AuthContextType = {
    user: { idUser: 'u1', nomeUser: 'Dr. Marcos', emailUser: 'marcos@test.com', perfis: ['PROFISSIONAL'] },
    token: 'tok',
    isAuthenticated: true,
    isAdmin: false,
    isResponsavel: false,
    isProfissional: true,
    isFuncionarioClinica: false,
    isAdminClinica: false,
    loading: false,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    ...userOverrides,
  };

  return render(
    <MemoryRouter>
      <AuthContext.Provider value={authValue}>
        <Arquivos />
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe('Arquivos Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(arquivoService.list).mockResolvedValue(mockArquivos);
    vi.mocked(pacienteService.list).mockResolvedValue([
      { idPaciente: 'pac1', nomePessoa: 'Lucas Silva' } as any,
    ]);
    vi.mocked(arquivoService.listTipos).mockResolvedValue([
      { idTipoArquivo: 't1', nomeTipo: 'Laudo Médico' },
      { idTipoArquivo: 't2', nomeTipo: 'Resultado de Exame' },
    ]);
  });

  it('renderiza arquivos listando badges de privacidade e liberação', async () => {
    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('LaudoMedico_LucasSilva_20261110_100000.pdf')).toBeInTheDocument();
      expect(screen.getByText('ResultadoDeExame_LucasSilva_20261105_090000.docx')).toBeInTheDocument();
      expect(screen.getByText('Privado (Restrito ao autor)')).toBeInTheDocument();
      expect(screen.getByText('Clínica Attenzione')).toBeInTheDocument();
    });
  });

  it('permite filtrar por tipo de arquivo', async () => {
    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('LaudoMedico_LucasSilva_20261110_100000.pdf')).toBeInTheDocument();
    });

    const selectTipo = screen.getByLabelText('Filtrar por tipo de documento');
    fireEvent.change(selectTipo, { target: { value: 't2' } });

    expect(screen.queryByText('LaudoMedico_LucasSilva_20261110_100000.pdf')).not.toBeInTheDocument();
    expect(screen.getByText('ResultadoDeExame_LucasSilva_20261105_090000.docx')).toBeInTheDocument();
  });

  it('exibe controles de compartilhamento e exclusão apenas para o autor do arquivo (ou Admin)', async () => {
    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText('LaudoMedico_LucasSilva_20261110_100000.pdf')).toBeInTheDocument();
    });

    // u1 é autor do arquivo a1, mas não do a2 -> deve haver apenas 1 botão de gerenciar acesso e 2 de download
    expect(screen.getAllByTitle('Baixar Documento')).toHaveLength(2);
    expect(screen.getAllByTitle('Gerenciar Acesso / Liberar')).toHaveLength(1);
    expect(screen.getAllByTitle('Excluir Documento')).toHaveLength(1);
  });
});
