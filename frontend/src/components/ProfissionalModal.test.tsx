import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ProfissionalModal from './ProfissionalModal';
import { profissionalService } from '../services/profissionalService';

vi.mock('../services/profissionalService');

describe('ProfissionalModal', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onSuccess: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('does not render when isOpen is false', () => {
    render(<ProfissionalModal {...defaultProps} isOpen={false} />);
    expect(screen.queryByText(/cadastrar profissional/i)).not.toBeInTheDocument();
  });

  it('renders all form fields when isOpen is true', () => {
    render(<ProfissionalModal {...defaultProps} />);

    expect(screen.getByRole('heading', { name: /cadastrar novo profissional/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/nome completo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/cpf/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/registro no conselho/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/celular/i)).toBeInTheDocument();
  });

  it('validates required fields before submitting', async () => {
    render(<ProfissionalModal {...defaultProps} />);

    fireEvent.click(screen.getByRole('button', { name: /salvar profissional/i }));

    expect(await screen.findByText(/informe o nome completo/i)).toBeInTheDocument();
    expect(screen.getByText(/informe o cpf/i)).toBeInTheDocument();
    expect(screen.getByText(/informe o conselho profissional/i)).toBeInTheDocument();
    expect(profissionalService.create).not.toHaveBeenCalled();
  });

  it('submits valid data, calls onSuccess and closes', async () => {
    const mockCreated = {
      idProf: '123',
      idPessoa: 'p-123',
      nomeProfissional: 'Dra. Julia Santos',
      cpf: '12345678901',
      email: 'julia@clinica.com',
      telefone: '11987654321',
      conselhoProf: 'CRM/SP 54321',
    };
    vi.mocked(profissionalService.create).mockResolvedValueOnce(mockCreated);

    render(<ProfissionalModal {...defaultProps} />);

    fireEvent.change(screen.getByLabelText(/nome completo/i), { target: { value: 'Dra. Julia Santos' } });
    fireEvent.change(screen.getByLabelText(/cpf/i), { target: { value: '12345678901' } });
    fireEvent.change(screen.getByLabelText(/registro no conselho/i), { target: { value: 'CRM/SP 54321' } });
    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: 'julia@clinica.com' } });
    fireEvent.change(screen.getByLabelText(/celular/i), { target: { value: '11987654321' } });

    fireEvent.click(screen.getByRole('button', { name: /salvar profissional/i }));

    await waitFor(() => {
      expect(profissionalService.create).toHaveBeenCalledWith({
        nomePessoa: 'Dra. Julia Santos',
        CPFPessoa: '12345678901',
        conselhoProf: 'CRM/SP 54321',
        emailPessoa: 'julia@clinica.com',
        celularPessoa: '11987654321',
      });
      expect(defaultProps.onSuccess).toHaveBeenCalledWith(mockCreated);
      expect(defaultProps.onClose).toHaveBeenCalled();
    });
  });

  it('displays API error message when submission fails', async () => {
    vi.mocked(profissionalService.create).mockRejectedValueOnce(new Error('Erro ao salvar no banco'));

    render(<ProfissionalModal {...defaultProps} />);

    fireEvent.change(screen.getByLabelText(/nome completo/i), { target: { value: 'Dr. Teste' } });
    fireEvent.change(screen.getByLabelText(/cpf/i), { target: { value: '11122233344' } });
    fireEvent.change(screen.getByLabelText(/registro no conselho/i), { target: { value: 'CRP 12345' } });

    fireEvent.click(screen.getByRole('button', { name: /salvar profissional/i }));

    expect(await screen.findByText(/erro ao cadastrar profissional/i)).toBeInTheDocument();
  });
});

