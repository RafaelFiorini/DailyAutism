package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.BloqueioAgendaClinicaRepository;
import com.clinica.dailyautism.domain.repository.CompromissoRepository;
import com.clinica.dailyautism.infrastructure.dto.BloqueioAgendaClinicaDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveBloqueioAgendaClinicaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloqueioAgendaClinicaServiceTest {

    @Mock private BloqueioAgendaClinicaRepository bloqueioRepository;
    @Mock private CompromissoRepository compromissoRepository;
    @Mock private ClinicaAccessHelper clinicaAccessHelper;
    @Mock private ContratoParticularService contratoParticularService;
    @Mock private LancamentoRepasseService lancamentoRepasseService;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private BloqueioAgendaClinicaService service;

    private Clinica clinicaA;
    private User userAdminClinica;

    @BeforeEach
    void setUp() {
        clinicaA = Clinica.builder()
                .idClinica("clinica-A")
                .nomeClinica("Attenzione Saúde Mental")
                .build();

        userAdminClinica = User.builder()
                .idUser("user-1")
                .nomeUser("Alexandra")
                .emailUser("alexandra@attenzione.com.br")
                .build();
    }

    @Test
    @DisplayName("Criar bloqueio de Véspera de Natal suspende agendamentos do período e preserva realizados")
    void criarBloqueioSuspendeAgendadosEPreservaRealizados() {
        when(clinicaAccessHelper.validarEAcessarClinica(null, "alexandra@attenzione.com.br")).thenReturn(clinicaA);
        when(clinicaAccessHelper.loadUserOrNull("alexandra@attenzione.com.br")).thenReturn(userAdminClinica);
        when(bloqueioRepository.save(any(BloqueioAgendaClinica.class))).thenAnswer(inv -> {
            BloqueioAgendaClinica b = inv.getArgument(0);
            b.setIdBloqueio("bloq-1");
            return b;
        });

        Compromisso agendado = Compromisso.builder()
                .idCompromisso("comp-1")
                .dataHoraCompromisso(LocalDateTime.of(2026, 12, 24, 20, 0))
                .status(StatusCompromisso.AGENDADO)
                .clinica(clinicaA)
                .build();

        Compromisso realizado = Compromisso.builder()
                .idCompromisso("comp-2")
                .dataHoraCompromisso(LocalDateTime.of(2026, 12, 24, 10, 0))
                .status(StatusCompromisso.REALIZADO)
                .clinica(clinicaA)
                .build();

        when(compromissoRepository.findByClinicaIdClinicaAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
                eq("clinica-A"), any(), any()
        )).thenReturn(List.of(agendado, realizado));

        SaveBloqueioAgendaClinicaDTO dto = SaveBloqueioAgendaClinicaDTO.builder()
                .titulo("Véspera de Natal")
                .tipo(TipoBloqueioAgenda.RECESSO)
                .dataInicio(LocalDate.of(2026, 12, 24))
                .dataFim(LocalDate.of(2026, 12, 24))
                .suspenderAgendamentosExistentes(true)
                .build();

        BloqueioAgendaClinicaDTO result = service.criarBloqueio(dto, "alexandra@attenzione.com.br");

        assertEquals("bloq-1", result.getIdBloqueio());
        assertEquals(1, result.getAgendamentosSuspensos());
        assertEquals(StatusCompromisso.CANCELADO, agendado.getStatus());
        assertTrue(agendado.getDescricaoCompromisso().contains("saldo de sessões preservado"));
        assertEquals(StatusCompromisso.REALIZADO, realizado.getStatus());
        verify(lancamentoRepasseService, times(1)).cancelarLancamentoPorCompromisso("comp-1");
        verify(lancamentoRepasseService, never()).cancelarLancamentoPorCompromisso("comp-2");
    }

    @Test
    @DisplayName("Bloqueio por turno (12:00 às 23:59) suspende apenas atendimentos do turno bloqueado")
    void bloqueioParcialPorTurnoSuspendeApenasHorarioBloqueado() {
        when(clinicaAccessHelper.validarEAcessarClinica(null, "alexandra@attenzione.com.br")).thenReturn(clinicaA);
        when(clinicaAccessHelper.loadUserOrNull("alexandra@attenzione.com.br")).thenReturn(userAdminClinica);
        when(bloqueioRepository.save(any(BloqueioAgendaClinica.class))).thenAnswer(inv -> {
            BloqueioAgendaClinica b = inv.getArgument(0);
            b.setIdBloqueio("bloq-2");
            return b;
        });

        Compromisso manha = Compromisso.builder()
                .idCompromisso("comp-manha")
                .dataHoraCompromisso(LocalDateTime.of(2026, 12, 24, 9, 0))
                .status(StatusCompromisso.AGENDADO)
                .clinica(clinicaA)
                .build();

        Compromisso noite = Compromisso.builder()
                .idCompromisso("comp-noite")
                .dataHoraCompromisso(LocalDateTime.of(2026, 12, 24, 20, 0))
                .status(StatusCompromisso.AGENDADO)
                .clinica(clinicaA)
                .build();

        when(compromissoRepository.findByClinicaIdClinicaAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
                eq("clinica-A"), any(), any()
        )).thenReturn(List.of(manha, noite));

        SaveBloqueioAgendaClinicaDTO dto = SaveBloqueioAgendaClinicaDTO.builder()
                .titulo("Expediente reduzido - Véspera de Natal")
                .tipo(TipoBloqueioAgenda.PONTO_FACULTATIVO)
                .dataInicio(LocalDate.of(2026, 12, 24))
                .dataFim(LocalDate.of(2026, 12, 24))
                .horaInicio(LocalTime.of(12, 0))
                .horaFim(LocalTime.of(23, 59))
                .suspenderAgendamentosExistentes(true)
                .build();

        BloqueioAgendaClinicaDTO result = service.criarBloqueio(dto, "alexandra@attenzione.com.br");

        assertEquals(1, result.getAgendamentosSuspensos());
        assertEquals(StatusCompromisso.AGENDADO, manha.getStatus());
        assertEquals(StatusCompromisso.CANCELADO, noite.getStatus());
    }

    @Test
    @DisplayName("Remover bloqueio desativa o registro com soft-delete")
    void removerBloqueioDesativaRegistro() {
        BloqueioAgendaClinica bloq = BloqueioAgendaClinica.builder()
                .idBloqueio("bloq-1")
                .titulo("Feriado")
                .clinica(clinicaA)
                .dataInicio(LocalDate.of(2026, 12, 25))
                .dataFim(LocalDate.of(2026, 12, 25))
                .build();
        bloq.setAtivo(true);

        when(bloqueioRepository.findById("bloq-1")).thenReturn(Optional.of(bloq));
        when(clinicaAccessHelper.validarEAcessarClinica("clinica-A", "alexandra@attenzione.com.br")).thenReturn(clinicaA);
        when(clinicaAccessHelper.loadUserOrNull("alexandra@attenzione.com.br")).thenReturn(userAdminClinica);

        service.removerBloqueio("bloq-1", "alexandra@attenzione.com.br");

        assertFalse(bloq.getAtivo());
        verify(bloqueioRepository, times(1)).save(bloq);
    }
}
