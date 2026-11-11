import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AssinarGuiaModal from './AssinarGuiaModal';
import { financeiroService } from '../services/financeiroService';
import type { GuiaFaturamento } from '../types/financeiro';

vi.mock('../services/financeiroService', () => ({
  financeiroService: {
    assinarGuia: vi.fn(),
    downloadGuiaPdf: vi.fn(),
  },
}));

const mockGuia: GuiaFaturamento = {
  idGuiaFaturamento: 'g-1',
  idClinica: 'cli-1',
  nomeClinica: 'Attenzione',
  idPaciente: 'pac-1',
  nomePaciente: 'Lorenzo Lima',
  idPlanoSaude: 'pl-1',
  nomePlanoSaude: 'Unimed Litoral',
  numeroLoteGuia: 'FAT-202611-0001',
  competenciaInicio: '2026-11-01',
  competenciaFim: '2026-11-30',
  valorTotal: 140,
  status: 'AGUARDANDO_ASSINATURA',
  itens: [
    {
      idGuiaItem: 'it-1',
      idCompromisso: 'cmp-1',
      dataHoraAtendimento: '2026-11-05T10:00:00',
      tipoCompromissoNome: 'Terapia Ocupacional',
      profissionalNome: 'Dra. Camila',
      conselhoProfissional: 'CRP 08/21450',
      valorUnitarioConvenio: 70,
    },
    {
      idGuiaItem: 'it-2',
      idCompromisso: 'cmp-2',
      dataHoraAtendimento: '2026-11-12T10:00:00',
      tipoCompromissoNome: 'Terapia Ocupacional',
      profissionalNome: 'Dra. Camila',
      conselhoProfissional: 'CRP 08/21450',
      valorUnitarioConvenio: 70,
    },
  ],
};

describe('AssinarGuiaModal', () => {
  const onClose = vi.fn();
  const onSigned = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('valida aceite e senha antes de enviar assinatura', async () => {
    render(<AssinarGuiaModal isOpen={true} guia={mockGuia} onClose={onClose} onSigned={onSigned} />);

    expect(screen.getByText(/FAT-202611-0001/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /Assinar Eletronicamente/i }));
    await waitFor(() => {
      expect(screen.getByText(/Confirme que conferiu os atendimentos/i)).toBeInTheDocument();
    });
    expect(financeiroService.assinarGuia).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('checkbox'));
    fireEvent.click(screen.getByRole('button', { name: /Assinar Eletronicamente/i }));
    await waitFor(() => {
      expect(screen.getByText(/Informe sua senha de acesso/i)).toBeInTheDocument();
    });
    expect(financeiroService.assinarGuia).not.toHaveBeenCalled();
  });

  it('assina guia com sucesso quando confirmado com senha', async () => {
    vi.mocked(financeiroService.assinarGuia).mockResolvedValueOnce({
      ...mockGuia,
      status: 'ASSINADA',
      codigoVerificador: 'DA-1234',
    });

    render(<AssinarGuiaModal isOpen={true} guia={mockGuia} onClose={onClose} onSigned={onSigned} />);

    fireEvent.click(screen.getByRole('checkbox'));
    fireEvent.change(screen.getByPlaceholderText(/Digite sua senha atual/i), {
      target: { value: '41100011102' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Assinar Eletronicamente/i }));

    await waitFor(() => {
      expect(financeiroService.assinarGuia).toHaveBeenCalledWith('g-1', '41100011102');
      expect(onSigned).toHaveBeenCalled();
      expect(onClose).toHaveBeenCalled();
    });
  });
});
