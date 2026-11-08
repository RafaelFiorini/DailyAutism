import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import PacienteModal from './PacienteModal';
import { pacienteService } from '../services/pacienteService';

vi.mock('../services/pacienteService', () => ({
  pacienteService: {
    create: vi.fn(),
    update: vi.fn(),
    listPlanos: vi.fn().mockResolvedValue([]),
    listClinicas: vi.fn().mockResolvedValue([]),
  },
}));

describe('PacienteModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('não renderiza quando isOpen é false', () => {
    const { container } = render(
      <PacienteModal isOpen={false} onClose={mockOnClose} onSuccess={mockOnSuccess} />
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('renderiza corretamente no modo de criação', async () => {
    render(<PacienteModal isOpen={true} onClose={mockOnClose} onSuccess={mockOnSuccess} />);

    expect(screen.getByText('Cadastrar Novo Paciente')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Ex: Gabriel Henrique da Silva')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Salvar Paciente/i })).toBeInTheDocument();
    expect(screen.getByText(/O paciente também é seu próprio responsável legal/i)).toBeInTheDocument();
  });

  it('exibe erro de validação quando o nome ou CPF estão vazios', async () => {
    render(<PacienteModal isOpen={true} onClose={mockOnClose} onSuccess={mockOnSuccess} />);

    fireEvent.click(screen.getByRole('button', { name: /Salvar Paciente/i }));

    await waitFor(() => {
      expect(screen.getByText('Informe o nome completo do paciente')).toBeInTheDocument();
      expect(screen.getByText('Informe o CPF do paciente')).toBeInTheDocument();
    });
    expect(pacienteService.create).not.toHaveBeenCalled();
  });

  it('cria paciente com sucesso quando preenchido corretamente com CPF', async () => {
    vi.mocked(pacienteService.create).mockResolvedValueOnce({
      idPaciente: 'pac-1',
      nomePessoa: 'Gabriel Silva',
      cpf: '12345678901',
      apelidoPaciente: 'Biel',
      isResponsavel: true,
    });

    render(<PacienteModal isOpen={true} onClose={mockOnClose} onSuccess={mockOnSuccess} />);

    fireEvent.change(screen.getByPlaceholderText('Ex: Gabriel Henrique da Silva'), {
      target: { value: 'Gabriel Silva' },
    });
    fireEvent.change(screen.getByPlaceholderText('000.000.000-00'), {
      target: { value: '12345678901' },
    });
    fireEvent.change(screen.getByPlaceholderText('Ex: Biel, Gabi'), {
      target: { value: 'Biel' },
    });

    // Marca toggle de isResponsavel
    const checkboxResponsavel = screen.getByLabelText(/O paciente também é seu próprio responsável legal/i);
    fireEvent.click(checkboxResponsavel);

    fireEvent.click(screen.getByRole('button', { name: /Salvar Paciente/i }));

    await waitFor(() => {
      expect(pacienteService.create).toHaveBeenCalledWith(
        expect.objectContaining({
          nomePessoa: 'Gabriel Silva',
          cpf: '12345678901',
          apelidoPaciente: 'Biel',
          isResponsavel: true,
        })
      );
      expect(mockOnSuccess).toHaveBeenCalledWith(
        expect.objectContaining({ idPaciente: 'pac-1' })
      );
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it('não exibe a pergunta de próprio responsável se hasOutroResponsavel for true', async () => {
    render(
      <PacienteModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
        hasOutroResponsavel={true}
      />
    );

    expect(
      screen.queryByText(/O paciente também é seu próprio responsável legal/i)
    ).not.toBeInTheDocument();
  });

  it('não exibe a pergunta de próprio responsável se initialData tiver hasOutroResponsavel = true', async () => {
    const existingComResponsavel = {
      idPaciente: 'pac-456',
      nomePessoa: 'Enzo Gabriel',
      cpf: '11122233344',
      hasOutroResponsavel: true,
      responsaveis: ['Maria Gabriel'],
    };

    render(
      <PacienteModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
        initialData={existingComResponsavel}
      />
    );

    expect(
      screen.queryByText(/O paciente também é seu próprio responsável legal/i)
    ).not.toBeInTheDocument();
  });

  it('renderiza campos preenchidos e atualiza no modo de edição', async () => {
    const existing = {
      idPaciente: 'pac-123',
      nomePessoa: 'Davi Lucas',
      apelidoPaciente: 'Davi',
      cpf: '12345678901',
      isResponsavel: true,
    };

    vi.mocked(pacienteService.update).mockResolvedValueOnce({
      ...existing,
      nomePessoa: 'Davi Lucas Atualizado',
    });

    render(
      <PacienteModal
        isOpen={true}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
        initialData={existing}
      />
    );

    expect(screen.getByText('Editar Paciente')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Davi Lucas')).toBeInTheDocument();
    expect(screen.getByDisplayValue('123.456.789-01')).toBeDisabled();

    fireEvent.change(screen.getByDisplayValue('Davi Lucas'), {
      target: { value: 'Davi Lucas Atualizado' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Salvar Alterações/i }));

    await waitFor(() => {
      expect(pacienteService.update).toHaveBeenCalledWith(
        'pac-123',
        expect.objectContaining({
          nomePessoa: 'Davi Lucas Atualizado',
        })
      );
      expect(mockOnSuccess).toHaveBeenCalled();
    });
  });
});
