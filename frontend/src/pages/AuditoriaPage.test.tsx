import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AuditoriaPage from './AuditoriaPage';
import { auditoriaService } from '../services/auditoriaService';
import type { PageAuditoria } from '../types/auditoria';

vi.mock('../services/auditoriaService', () => ({
  auditoriaService: {
    listar: vi.fn(),
    getById: vi.fn(),
    exportarCsv: vi.fn(),
  },
}));

const mockPageData: PageAuditoria = {
  content: [
    {
      idAuditoriaLog: 'audit-1',
      dataHora: '2026-11-18T14:30:00',
      idUsuario: 'user-admin',
      nomeUsuario: 'Administrador Geral',
      loginUsuario: 'admin@dailyautism.com',
      perfilUsuario: 'ADMIN',
      categoria: 'GRADE_TERAPEUTICA',
      categoriaDescricao: 'Grade Terapêutica',
      acao: 'APROVACAO',
      acaoDescricao: 'Aprovação',
      entidade: 'GradeTerapeutica',
      idEntidade: 'grade-100',
      descricao: 'Aprovou a grade terapêutica do paciente Pedro Lucas',
      ipOrigem: '192.168.1.15',
      userAgent: 'Chrome 120 / Windows 11',
    },
    {
      idAuditoriaLog: 'audit-2',
      dataHora: '2026-11-18T15:00:00',
      idUsuario: 'user-sec',
      nomeUsuario: 'Secretária Carla',
      loginUsuario: 'carla@clinica.com',
      perfilUsuario: 'SECRETARIA',
      categoria: 'AUTENTICACAO',
      categoriaDescricao: 'Autenticação e Acesso',
      acao: 'LOGIN_SUCESSO',
      acaoDescricao: 'Login Sucesso',
      entidade: 'Autenticacao',
      idEntidade: 'carla@clinica.com',
      descricao: 'Login efetuado com sucesso no sistema',
      ipOrigem: '200.180.50.1',
      userAgent: 'Firefox 122',
    },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 20,
  number: 0,
  first: true,
  last: true,
};

describe('AuditoriaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(auditoriaService.listar).mockResolvedValue(mockPageData);
    vi.mocked(auditoriaService.exportarCsv).mockResolvedValue(new Blob(['csv content'], { type: 'text/csv' }));
  });

  it('renderiza o cabeçalho, os filtros e a tabela com os registros de auditoria', async () => {
    render(<AuditoriaPage />);

    expect(screen.getByRole('heading', { name: /auditoria & logs de atividades/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/buscar por usuário, ação, descrição.../i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /exportar relatório csv/i })).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Administrador Geral')).toBeInTheDocument();
      expect(screen.getByText('Secretária Carla')).toBeInTheDocument();
      expect(screen.getAllByText('Grade Terapêutica').length).toBeGreaterThan(0);
      expect(screen.getByText('192.168.1.15')).toBeInTheDocument();
    });
  });

  it('permite filtrar por busca textual livre', async () => {
    render(<AuditoriaPage />);

    const inputBusca = screen.getByPlaceholderText(/buscar por usuário, ação, descrição.../i);
    fireEvent.change(inputBusca, { target: { value: 'Pedro Lucas' } });

    const btnFiltrar = screen.getByRole('button', { name: /filtrar/i });
    fireEvent.click(btnFiltrar);

    await waitFor(() => {
      expect(auditoriaService.listar).toHaveBeenCalledWith(
        expect.objectContaining({
          buscaTexto: 'Pedro Lucas',
        })
      );
    });
  });

  it('abre o modal de detalhes com informações completas ao clicar em Detalhes', async () => {
    render(<AuditoriaPage />);

    await waitFor(() => {
      expect(screen.getByText('Administrador Geral')).toBeInTheDocument();
    });

    const botoesDetalhes = await screen.findAllByRole('button', { name: /detalhes/i });
    fireEvent.click(botoesDetalhes[0]);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /detalhes do registro de auditoria/i })).toBeInTheDocument();
      expect(screen.getByText(/chrome 120 \/ windows 11/i)).toBeInTheDocument();
      expect(screen.getAllByText(/aprovou a grade terapêutica do paciente pedro lucas/i).length).toBeGreaterThan(0);
    });

    // Fecha o modal
    const btnFechar = screen.getByRole('button', { name: /fechar/i });
    fireEvent.click(btnFechar);

    await waitFor(() => {
      expect(screen.queryByRole('heading', { name: /detalhes do registro de auditoria/i })).not.toBeInTheDocument();
    });
  });
});
