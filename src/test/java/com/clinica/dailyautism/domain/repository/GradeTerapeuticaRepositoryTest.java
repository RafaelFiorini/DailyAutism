package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeTerapeuticaRepositoryTest {

    @Mock
    private GradeTerapeuticaRepository gradeTerapeuticaRepository;

    @Mock
    private GradeTerapeuticaItemRepository gradeTerapeuticaItemRepository;

    @Mock
    private CompromissoRepository compromissoRepository;

    @Test
    @DisplayName("Deve construir agregado GradeTerapeutica com itens e manter integridade")
    void deveConstruirAgregadoGradeTerapeuticaComItens() {
        Paciente paciente = new Paciente();
        paciente.setIdPaciente("pac-1");

        Clinica clinica = new Clinica();
        clinica.setIdClinica("cli-1");

        TipoCompromisso tipoPsico = new TipoCompromisso();
        tipoPsico.setIdTipoCompromisso("tipo-psico");

        Profissional prof = new Profissional();
        prof.setIdProf("prof-1");

        GradeTerapeutica grade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-1")
                .paciente(paciente)
                .clinica(clinica)
                .numeroVersao(1)
                .tipoOrigemVigencia("GUIA_CONVENIO")
                .idOrigemVigencia("guia-123")
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 6, 30))
                .status(StatusGradeTerapeutica.RASCUNHO)
                .build();

        GradeTerapeuticaItem item1 = GradeTerapeuticaItem.builder()
                .idGradeTerapeuticaItem("item-1")
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(14, 0))
                .horaFim(LocalTime.of(15, 0))
                .tipoCompromisso(tipoPsico)
                .profissional(prof)
                .sala("Sala 01")
                .build();

        grade.addItem(item1);

        assertEquals(1, grade.getItens().size());
        assertEquals(grade, item1.getGradeTerapeutica());
        assertEquals(DiaSemana.SEGUNDA, item1.getDiaSemana());
        assertEquals(DayOfWeek.MONDAY, item1.getDiaSemana().getDayOfWeek());

        grade.removeItem(item1);
        assertEquals(0, grade.getItens().size());
        assertNull(item1.getGradeTerapeutica());
    }

    @Test
    @DisplayName("Deve converter corretamente entre DayOfWeek e DiaSemana")
    void deveConverterEntreDayOfWeekEDiaSemana() {
        assertEquals(DiaSemana.SEGUNDA, DiaSemana.fromDayOfWeek(DayOfWeek.MONDAY));
        assertEquals(DiaSemana.TERCA, DiaSemana.fromDayOfWeek(DayOfWeek.TUESDAY));
        assertEquals(DiaSemana.QUARTA, DiaSemana.fromDayOfWeek(DayOfWeek.WEDNESDAY));
        assertEquals(DiaSemana.QUINTA, DiaSemana.fromDayOfWeek(DayOfWeek.THURSDAY));
        assertEquals(DiaSemana.SEXTA, DiaSemana.fromDayOfWeek(DayOfWeek.FRIDAY));
        assertEquals(DiaSemana.SABADO, DiaSemana.fromDayOfWeek(DayOfWeek.SATURDAY));
        assertEquals(DiaSemana.DOMINGO, DiaSemana.fromDayOfWeek(DayOfWeek.SUNDAY));
    }

    @Test
    @DisplayName("Deve simular persistência e busca no repositório mockado")
    void devePersistirEBuscarGrade() {
        GradeTerapeutica grade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-99")
                .numeroVersao(1)
                .status(StatusGradeTerapeutica.PENDENTE_APROVACAO)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .build();

        when(gradeTerapeuticaRepository.findById("grade-99")).thenReturn(Optional.of(grade));
        when(gradeTerapeuticaRepository.findByPacienteIdPacienteOrderByNumeroVersaoDesc("pac-1"))
                .thenReturn(List.of(grade));

        Optional<GradeTerapeutica> encontrada = gradeTerapeuticaRepository.findById("grade-99");
        assertTrue(encontrada.isPresent());
        assertEquals(StatusGradeTerapeutica.PENDENTE_APROVACAO, encontrada.get().getStatus());

        List<GradeTerapeutica> historico = gradeTerapeuticaRepository.findByPacienteIdPacienteOrderByNumeroVersaoDesc("pac-1");
        assertFalse(historico.isEmpty());
        assertEquals(1, historico.size());
    }
}
