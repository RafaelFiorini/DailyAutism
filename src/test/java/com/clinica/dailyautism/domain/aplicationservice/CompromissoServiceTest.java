package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.AprovarCompromissoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompromissoServiceTest {

    @Mock private CompromissoRepository compromissoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock private PeriodicidadeRepository periodicidadeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private PacienteService pacienteService;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private ClinicaAccessHelper clinicaAccessHelper;
    @Mock private ContratoParticularService contratoParticularService;

    @InjectMocks
    private CompromissoService compromissoService;

    @Test
    void deveRetornarErroClaroAoAprovarAgendamentoComDataPassada() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        Compromisso antigo = Compromisso.builder()
                .idCompromisso("cmp-old")
                .dataHoraCompromisso(ontem.atTime(13, 30))
                .status(StatusCompromisso.AGUARDANDO_APROVACAO)
                .build();

        when(compromissoRepository.findById("cmp-old")).thenReturn(Optional.of(antigo));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> compromissoService.aprovarCompromisso("cmp-old", new AprovarCompromissoDTO(null))
        );
        assertTrue(ex.getMessage().contains("data anterior a hoje"));
        assertTrue(ex.getMessage().contains("Avise a clínica"));
        verify(compromissoRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroClaroQuandoGuiaEstiverVencidaAntesDaSessao() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        LocalDate ontem = LocalDate.now().minusDays(1);
        PacientePlanoSaude guiaVencida = PacientePlanoSaude.builder()
                .vencimentoLiberacao(ontem)
                .build();
        guiaVencida.setAtivo(true);

        Paciente paciente = Paciente.builder()
                .idPaciente("pac-1")
                .planos(List.of(guiaVencida))
                .build();

        Compromisso futuro = Compromisso.builder()
                .idCompromisso("cmp-fut")
                .dataHoraCompromisso(amanha.atTime(10, 0))
                .paciente(paciente)
                .status(StatusCompromisso.AGUARDANDO_APROVACAO)
                .build();

        when(compromissoRepository.findById("cmp-fut")).thenReturn(Optional.of(futuro));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> compromissoService.aprovarCompromisso("cmp-fut", new AprovarCompromissoDTO(null))
        );
        assertTrue(ex.getMessage().contains("venceu em"));
        verify(compromissoRepository, never()).save(any());
    }
}
