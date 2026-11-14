import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import AgendaClinicaTimeline from './AgendaClinicaTimeline';
import type { Compromisso } from '../types/compromisso';

describe('AgendaClinicaTimeline Component', () => {
  const selectedDate = '2026-11-13';
  const onChangeDate = vi.fn();
  const onUpdateStatus = vi.fn();

  const mockCompromissos: Compromisso[] = [
    {
      idCompromisso: 'c1',
      dataHoraCompromisso: '2026-11-13T08:30:00',
      tituloCompromisso: 'Psicologia ABA',
      status: 'AGENDADO',
      idPaciente: 'pac-1',
      nomePaciente: 'Julia Vitória',
      idProfissional: 'prof-1',
      nomeProfissional: 'Dra. Camila Ribeiro',
      nomeTipoCompromisso: 'Psicologia / ABA',
      localCompromisso: 'Sala 01',
    },
    {
      idCompromisso: 'c2',
      dataHoraCompromisso: '2026-11-13T08:30:00',
      tituloCompromisso: 'Terapia Ocupacional',
      status: 'REALIZADO',
      idPaciente: 'pac-2',
      nomePaciente: 'Matheus Mendes',
      idProfissional: 'prof-2',
      nomeProfissional: 'Ediane Terapeuta',
      nomeTipoCompromisso: 'Terapia Ocupacional',
      localCompromisso: 'Sala 02',
    },
    {
      idCompromisso: 'c3',
      dataHoraCompromisso: '2026-11-13T10:00:00',
      tituloCompromisso: 'Fonoaudiologia',
      status: 'AGENDADO',
      idPaciente: 'pac-3',
      nomePaciente: 'Bernardo Rocha',
      idProfissional: 'prof-3',
      nomeProfissional: 'Dra. Beatriz Campos',
      nomeTipoCompromisso: 'Fonoaudiologia',
      localCompromisso: 'Sala 03',
    },
    {
      idCompromisso: 'c4_outro_dia',
      dataHoraCompromisso: '2026-11-14T09:00:00',
      tituloCompromisso: 'Fonoaudiologia',
      status: 'AGENDADO',
      idPaciente: 'pac-4',
      nomePaciente: 'Alice Rocha',
    },
  ] as unknown as Compromisso[];

  it('renderiza os horários em sequência e as caixinhas de atendimento com paciente e profissional', () => {
    render(
      <MemoryRouter>
        <AgendaClinicaTimeline
          selectedDate={selectedDate}
          onChangeDate={onChangeDate}
          compromissos={mockCompromissos}
          loading={false}
          onUpdateStatus={onUpdateStatus}
        />
      </MemoryRouter>
    );

    // Horários agrupados
    expect(screen.getByText('08:30')).toBeInTheDocument();
    expect(screen.getByText('10:00')).toBeInTheDocument();

    // Contador de simultâneos
    expect(screen.getByText('2 sessões simultâneas')).toBeInTheDocument();
    expect(screen.getByText('1 sessão programada')).toBeInTheDocument();

    // Caixinhas do horário 08:30
    expect(screen.getByText('Julia Vitória')).toBeInTheDocument();
    expect(screen.getByText('Dra. Camila Ribeiro')).toBeInTheDocument();
    expect(screen.getByText('Matheus Mendes')).toBeInTheDocument();
    expect(screen.getByText('Ediane Terapeuta')).toBeInTheDocument();

    // Caixinha do horário 10:00
    expect(screen.getByText('Bernardo Rocha')).toBeInTheDocument();
    expect(screen.getByText('Dra. Beatriz Campos')).toBeInTheDocument();

    // Não deve exibir compromisso de outro dia
    expect(screen.queryByText('Alice Rocha')).not.toBeInTheDocument();

    // Contadores do topo
    expect(screen.getByText('Total no Dia')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument(); // total no dia
  });

  it('permite acionar botões de ação rápida para atendimentos agendados', () => {
    render(
      <MemoryRouter>
        <AgendaClinicaTimeline
          selectedDate={selectedDate}
          onChangeDate={onChangeDate}
          compromissos={mockCompromissos}
          loading={false}
          onUpdateStatus={onUpdateStatus}
        />
      </MemoryRouter>
    );

    const btnRealizado = screen.getAllByRole('button', { name: /Realizado/i })[0];
    fireEvent.click(btnRealizado);
    expect(onUpdateStatus).toHaveBeenCalledWith('c1', 'REALIZADO');

    const btnFalta = screen.getAllByRole('button', { name: /Falta/i })[0];
    fireEvent.click(btnFalta);
    expect(onUpdateStatus).toHaveBeenCalledWith('c1', 'FALTOU');
  });

  it('navega entre os dias ao clicar nos botões de anterior e próximo', () => {
    render(
      <MemoryRouter>
        <AgendaClinicaTimeline
          selectedDate={selectedDate}
          onChangeDate={onChangeDate}
          compromissos={mockCompromissos}
          loading={false}
        />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByTitle('Dia anterior'));
    expect(onChangeDate).toHaveBeenCalledWith('2026-11-12');

    fireEvent.click(screen.getByTitle('Próximo dia'));
    expect(onChangeDate).toHaveBeenCalledWith('2026-11-14');
  });

  it('exibe estado amigável quando não há atendimentos no dia', () => {
    render(
      <MemoryRouter>
        <AgendaClinicaTimeline
          selectedDate="2026-11-15"
          onChangeDate={onChangeDate}
          compromissos={mockCompromissos}
          loading={false}
        />
      </MemoryRouter>
    );

    expect(screen.getByText('Nenhum atendimento programado para este dia')).toBeInTheDocument();
  });
});
