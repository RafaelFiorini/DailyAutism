import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Clinicas from './Clinicas';
import { clinicaService } from '../services/clinicaService';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../services/clinicaService', () => ({
  clinicaService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../hooks/useAuth');

const mockClinicas = [
  {
    idClinica: 'c1',
    nomeClinica: 'Clínica Somar',
    cnpjClinica: '12345678000199',
    telefoneClinica: '1133334444',
    whatsappClinica: '11999998888',
    emailClinica: 'contato@somar.com',
    enderecoClinica: 'Av. Paulista, 1000 - Bela Vista, São Paulo - SP',
  },
  {
    idClinica: 'c2',
    nomeClinica: 'Instituto Crescer',
    cnpjClinica: '98765432000111',
    telefoneClinica: '2122223333',
    whatsappClinica: '21988887777',
    emailClinica: 'atendimento@crescer.com',
    enderecoClinica: 'Rua das Flores, 50 - Botafogo, Rio de Janeiro - RJ',
  },
];

describe('Clinicas Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('exibe tela de acesso restrito se o usuário não for ADMIN', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: '1', nomeUser: 'Pai', emailUser: 'pai@test.com', perfis: ['RESPONSAVEL'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: false,
      isResponsavel: true,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    render(<Clinicas />);
    expect(screen.getByText('Acesso Restrito')).toBeInTheDocument();
  });

  it('renderiza lista de clínicas para ADMIN', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: 'admin', nomeUser: 'Admin', emailUser: 'admin@dailyautism.com', perfis: ['ADMIN'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: true,
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    vi.mocked(clinicaService.list).mockResolvedValueOnce(mockClinicas);

    render(<Clinicas />);

    await waitFor(() => {
      expect(screen.getByText('Clínica Somar')).toBeInTheDocument();
      expect(screen.getByText('Instituto Crescer')).toBeInTheDocument();
      expect(screen.getByText('12.345.678/0001-99')).toBeInTheDocument();
      expect(screen.getByText('98.765.432/0001-11')).toBeInTheDocument();
    });
  });

  it('filtra clínicas pela busca', async () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: 'admin', nomeUser: 'Admin', emailUser: 'admin@dailyautism.com', perfis: ['ADMIN'] },
      token: 'tok',
      isAuthenticated: true,
      isAdmin: true,
      isResponsavel: false,
      isProfissional: false,
      isFuncionarioClinica: false,
      isAdminClinica: false,
      loading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
    });

    vi.mocked(clinicaService.list).mockResolvedValueOnce(mockClinicas);

    render(<Clinicas />);

    await waitFor(() => {
      expect(screen.getByText('Clínica Somar')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar por nome, CNPJ/i);
    fireEvent.change(searchInput, { target: { value: 'Crescer' } });

    expect(screen.queryByText('Clínica Somar')).not.toBeInTheDocument();
    expect(screen.getByText('Instituto Crescer')).toBeInTheDocument();
  });
});
