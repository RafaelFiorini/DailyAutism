import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import App from './App';
import { authService } from './services/authService';

vi.mock('./services/authService');

describe('App Routing Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('renders Login page by default when unauthenticated', async () => {
    render(<App />);
    expect(await screen.findByRole('heading', { name: /painel administrativo/i })).toBeInTheDocument();
  });

  it('redirects to /admin dashboard when valid admin token exists', async () => {
    localStorage.setItem('dailyautism_token', 'valid-admin-token');
    vi.mocked(authService.getMe).mockResolvedValueOnce({
      idUser: 'admin-1',
      nomeUser: 'Dra. Ana Paula (Admin)',
      emailUser: 'admin@clinica.com',
      perfis: ['ADMIN'],
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getAllByText(/Dra\. Ana Paula/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/visão geral/i).length).toBeGreaterThan(0);
    });
  });
});
