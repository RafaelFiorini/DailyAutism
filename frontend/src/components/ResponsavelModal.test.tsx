import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ResponsavelModal from './ResponsavelModal';
import { responsavelService } from '../services/responsavelService';

vi.mock('../services/responsavelService');

describe('ResponsavelModal', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onSuccess: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(responsavelService.listPacientes).mockResolvedValue([
      { idPaciente: 'pac-1', nomePessoa: 'Lucas Silva', apelidoPaciente: 'Luquinhas' },
      { idPaciente: 'pac-2', nomePessoa: 'Beatriz Costa' },
    ]);
  });

  it('does not render when isOpen is false', () => {
    render(<ResponsavelModal {...defaultProps} isOpen={false} />);
    expect(screen.queryByText(/cadastrar novo responsável/i)).toBeNull();
  });

  it('renders all form fields when isOpen is true', async () => {
    render(<ResponsavelModal {...defaultProps} />);

    expect(screen.getByText(/cadastrar novo responsável/i)).toBeDefined();
    expect(screen.getByLabelText(/nome completo/i)).toBeDefined();
    expect(screen.getByLabelText(/cpf/i)).toBeDefined();
    expect(screen.getByLabelText(/e-mail/i)).toBeDefined();
    expect(screen.getByLabelText(/celular/i)).toBeDefined();
    expect(screen.getByLabelText(/rg/i)).toBeDefined();
    expect(screen.getByLabelText(/data de nascimento/i)).toBeDefined();
    expect(screen.getByLabelText(/endereço/i)).toBeDefined();
    expect(screen.getByText(/este responsável também é paciente em acompanhamento/i)).toBeDefined();

    await waitFor(() => {
      expect(screen.getByText('Lucas Silva')).toBeDefined();
      expect(screen.getByText('Beatriz Costa')).toBeDefined();
    });
  });

  it('validates required fields before submitting', async () => {
    render(<ResponsavelModal {...defaultProps} />);

    fireEvent.click(screen.getByRole('button', { name: /salvar responsável/i }));

    expect(screen.getByText(/informe o nome completo/i)).toBeDefined();
    expect(screen.getByText(/informe o cpf/i)).toBeDefined();
    expect(responsavelService.create).not.toHaveBeenCalled();
  });

  it('submits valid data, including isPacienteTambem and selected patients', async () => {
    const mockCreated = {
      idResponsavel: 'resp-1',
      idPessoa: 'p-1',
      nomeResponsavel: 'Mariana Lima',
      cpf: '12345678901',
      email: 'mariana@email.com',
      telefone: '11987654321',
      isPaciente: true,
      pacientes: [{ idPaciente: 'pac-1', nomePaciente: 'Lucas Silva' }],
    };
    vi.mocked(responsavelService.create).mockResolvedValueOnce(mockCreated);

    render(<ResponsavelModal {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByText('Lucas Silva')).toBeDefined();
    });

    fireEvent.change(screen.getByLabelText(/nome completo/i), { target: { value: 'Mariana Lima' } });
    fireEvent.change(screen.getByLabelText(/cpf/i), { target: { value: '12345678901' } });
    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: 'mariana@email.com' } });
    fireEvent.change(screen.getByLabelText(/celular/i), { target: { value: '11987654321' } });

    // Check "também é paciente"
    fireEvent.click(screen.getByText(/este responsável também é paciente em acompanhamento/i));

    // Check patient Lucas Silva
    fireEvent.click(screen.getByText('Lucas Silva'));

    fireEvent.click(screen.getByRole('button', { name: /salvar responsável/i }));

    await waitFor(() => {
      expect(responsavelService.create).toHaveBeenCalledWith(
        expect.objectContaining({
          nomePessoa: 'Mariana Lima',
          CPFPessoa: '12345678901',
          cpf: '12345678901',
          emailPessoa: 'mariana@email.com',
          celularPessoa: '11987654321',
          isPacienteTambem: true,
          pacienteIds: ['pac-1'],
        })
      );
      expect(defaultProps.onSuccess).toHaveBeenCalledWith(mockCreated);
      expect(defaultProps.onClose).toHaveBeenCalled();
    });
  });

  it('displays API error message when submission fails', async () => {
    vi.mocked(responsavelService.create).mockRejectedValueOnce({
      response: { data: { errorMessage: 'Esta pessoa já está cadastrada como responsável.' } },
    });

    render(<ResponsavelModal {...defaultProps} />);

    fireEvent.change(screen.getByLabelText(/nome completo/i), { target: { value: 'Mariana Lima' } });
    fireEvent.change(screen.getByLabelText(/cpf/i), { target: { value: '12345678901' } });

    fireEvent.click(screen.getByRole('button', { name: /salvar responsável/i }));

    await waitFor(() => {
      expect(screen.getByText('Esta pessoa já está cadastrada como responsável.')).toBeDefined();
    });
  });
});
