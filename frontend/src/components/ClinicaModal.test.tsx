import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ClinicaModal from './ClinicaModal';
import { clinicaService } from '../services/clinicaService';

vi.mock('../services/clinicaService', () => ({
  clinicaService: {
    create: vi.fn(),
    update: vi.fn(),
    list: vi.fn(),
  },
}));

describe('ClinicaModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('não renderiza quando isOpen é false', () => {
    render(<ClinicaModal isOpen={false} onClose={vi.fn()} onSuccess={vi.fn()} />);
    expect(screen.queryByText(/Nova Clínica/i)).not.toBeInTheDocument();
  });

  it('renderiza os campos obrigatórios quando aberto no modo de criação', () => {
    render(<ClinicaModal isOpen={true} onClose={vi.fn()} onSuccess={vi.fn()} />);

    expect(screen.getByRole('heading', { name: /Nova Clínica/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Clínica Integração/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/00.000.000\/0000-00/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Cadastrar Clínica/i })).toBeInTheDocument();
  });

  it('valida campos obrigatórios antes do envio', async () => {
    render(<ClinicaModal isOpen={true} onClose={vi.fn()} onSuccess={vi.fn()} />);

    // Tentar submeter sem preencher
    fireEvent.click(screen.getByRole('button', { name: /Cadastrar Clínica/i }));

    // Inputs possuem atributo required do HTML5
    expect(clinicaService.create).not.toHaveBeenCalled();
  });

  it('submete dados válidos para clinicaService.create', async () => {
    const mockSuccess = vi.fn();
    const mockClose = vi.fn();
    vi.mocked(clinicaService.create).mockResolvedValueOnce({
      idClinica: 'new-1',
      nomeClinica: 'Clínica Vida',
      cnpjClinica: '12345678000199',
    });

    render(<ClinicaModal isOpen={true} onClose={mockClose} onSuccess={mockSuccess} />);

    fireEvent.change(screen.getByPlaceholderText(/Clínica Integração/i), {
      target: { value: 'Clínica Vida' },
    });
    fireEvent.change(screen.getByPlaceholderText(/00.000.000\/0000-00/i), {
      target: { value: '12345678000199' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Cadastrar Clínica/i }));

    await waitFor(() => {
      expect(clinicaService.create).toHaveBeenCalledWith(
        expect.objectContaining({
          nomeClinica: 'Clínica Vida',
          cnpjClinica: '12345678000199',
        })
      );
      expect(mockSuccess).toHaveBeenCalled();
      expect(mockClose).toHaveBeenCalled();
    });
  });
});
