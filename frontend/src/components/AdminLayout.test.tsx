import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import AdminLayout from './AdminLayout';
import * as useAuthModule from '../hooks/useAuth';

vi.mock('../hooks/useAuth');

describe('AdminLayout Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renderiza o link "Meu Financeiro" apontando para /admin/meu-financeiro para o perfil PROFISSIONAL', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: 'prof-1', nomeUser: 'Dra. Ana Psicóloga', perfis: ['PROFISSIONAL'] } as any,
      token: 'jwt-token',
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
    });

    render(
      <MemoryRouter>
        <AdminLayout />
      </MemoryRouter>
    );

    const meuFinanceiroLink = screen.getByRole('link', { name: /meu financeiro/i });
    expect(meuFinanceiroLink).toBeInTheDocument();
    expect(meuFinanceiroLink).toHaveAttribute('href', '/admin/meu-financeiro');

    // Não deve exibir link para /admin/financeiro institucional
    const links = screen.getAllByRole('link');
    const hrefs = links.map((l) => l.getAttribute('href'));
    expect(hrefs).not.toContain('/admin/financeiro');
  });

  it('renderiza o link "Financeiro" institucional apontando para /admin/financeiro para ADMIN', () => {
    vi.spyOn(useAuthModule, 'useAuth').mockReturnValue({
      user: { idUser: 'admin-1', nomeUser: 'Super Admin', perfis: ['ADMIN'] } as any,
      token: 'jwt-token',
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

    render(
      <MemoryRouter>
        <AdminLayout />
      </MemoryRouter>
    );

    const financeiroLink = screen.getByRole('link', { name: /^financeiro$/i });
    expect(financeiroLink).toBeInTheDocument();
    expect(financeiroLink).toHaveAttribute('href', '/admin/financeiro');
  });
});
