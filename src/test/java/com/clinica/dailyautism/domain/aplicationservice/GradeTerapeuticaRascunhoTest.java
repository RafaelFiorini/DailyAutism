package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SaveGradeTerapeuticaDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveGradeTerapeuticaItemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeTerapeuticaRascunhoTest {

    @Mock private GradeTerapeuticaRepository gradeTerapeuticaRepository;
    @Mock private GradeTerapeuticaItemRepository gradeTerapeuticaItemRepository;
    @Mock private CompromissoRepository compromissoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private UserRepository userRepository;
    @Mock private PeriodicidadeRepository periodicidadeRepository;
    @Mock private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private PacienteClinicaRepository pacienteClinicaRepository;
    @Mock private BloqueioAgendaClinicaRepository bloqueioAgendaClinicaRepository;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private GradeTerapeuticaService service;

    @Test
    void atualizarProposta_editaRascunhoExistenteSemDuplicar() {
        GradeTerapeutica rascunho = GradeTerapeutica.builder()
                .idGradeTerapeutica("g-1")
                .numeroVersao(1)
                .status(StatusGradeTerapeutica.RASCUNHO)
                .dataInicioVigencia(LocalDate.of(2026, 11, 16))
                .dataFimVigencia(LocalDate.of(2026, 12, 31))
                .itens(new ArrayList<>())
                .build();

        TipoCompromisso tipo = TipoCompromisso.builder().idTipoCompromisso("t-1").nomeTipo("Psicologia").build();
        when(gradeTerapeuticaRepository.findByIdWithItens("g-1")).thenReturn(Optional.of(rascunho));
        when(tipoCompromissoRepository.findById("t-1")).thenReturn(Optional.of(tipo));
        when(gradeTerapeuticaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SaveGradeTerapeuticaItemDTO itemDTO = new SaveGradeTerapeuticaItemDTO();
        itemDTO.setDiaSemana(DiaSemana.SEGUNDA);
        itemDTO.setHoraInicio(LocalTime.of(14, 0));
        itemDTO.setHoraFim(LocalTime.of(15, 0));
        itemDTO.setIdTipoCompromisso("t-1");

        SaveGradeTerapeuticaDTO dto = new SaveGradeTerapeuticaDTO();
        dto.setIdPaciente("p-1");
        dto.setDataInicioVigencia(LocalDate.of(2026, 11, 20));
        dto.setDataFimVigencia(LocalDate.of(2027, 1, 31));
        dto.setEnviarParaAprovacao(false);
        dto.setItens(List.of(itemDTO));

        GradeTerapeutica atualizada = service.atualizarProposta("g-1", dto);

        assertEquals("g-1", atualizada.getIdGradeTerapeutica());
        assertEquals(StatusGradeTerapeutica.RASCUNHO, atualizada.getStatus());
        assertEquals(1, atualizada.getItens().size());
        assertEquals(LocalDate.of(2026, 11, 20), atualizada.getDataInicioVigencia());
    }

    @Test
    void excluirProposta_inativaRascunhoEBloqueiaGradeVigente() {
        GradeTerapeutica rascunho = GradeTerapeutica.builder()
                .idGradeTerapeutica("g-rasc")
                .numeroVersao(1)
                .status(StatusGradeTerapeutica.RASCUNHO)
                .build();
        rascunho.setAtivo(true);

        when(gradeTerapeuticaRepository.findByIdWithItens("g-rasc")).thenReturn(Optional.of(rascunho));
        service.excluirProposta("g-rasc");
        assertFalse(rascunho.getAtivo());
        verify(gradeTerapeuticaRepository).save(rascunho);

        GradeTerapeutica vigente = GradeTerapeutica.builder()
                .idGradeTerapeutica("g-vig")
                .numeroVersao(1)
                .status(StatusGradeTerapeutica.VIGENTE)
                .build();
        when(gradeTerapeuticaRepository.findByIdWithItens("g-vig")).thenReturn(Optional.of(vigente));
        assertThrows(IllegalStateException.class, () -> service.excluirProposta("g-vig"));
    }

    @Test
    void aprovarEReplicar_limpaRascunhosAntigosDoMesmoPaciente() {
        Paciente paciente = Paciente.builder().idPaciente("p-1").build();
        User usuario = User.builder().idUser("u-1").nomeUser("Ana").emailUser("ana@clinica.com").build();

        GradeTerapeutica gradeNova = GradeTerapeutica.builder()
                .idGradeTerapeutica("g-nova")
                .paciente(paciente)
                .numeroVersao(1)
                .status(StatusGradeTerapeutica.RASCUNHO)
                .dataInicioVigencia(LocalDate.now().plusDays(1))
                .dataFimVigencia(LocalDate.now().plusDays(10))
                .itens(new ArrayList<>())
                .build();
        gradeNova.setAtivo(true);

        GradeTerapeutica rascunhoAntigo = GradeTerapeutica.builder()
                .idGradeTerapeutica("g-antigo")
                .paciente(paciente)
                .numeroVersao(1)
                .status(StatusGradeTerapeutica.RASCUNHO)
                .itens(new ArrayList<>())
                .build();
        rascunhoAntigo.setAtivo(true);

        when(gradeTerapeuticaRepository.findByIdWithItens("g-nova")).thenReturn(Optional.of(gradeNova));
        when(userRepository.findByEmailUser("ana@clinica.com")).thenReturn(Optional.of(usuario));
        when(periodicidadeRepository.findAll()).thenReturn(List.of());
        when(gradeTerapeuticaRepository.findByPacienteIdPacienteOrderByNumeroVersaoDesc("p-1"))
                .thenReturn(List.of(gradeNova, rascunhoAntigo));

        service.aprovarEReplicar("g-nova", "ana@clinica.com");

        assertEquals(StatusGradeTerapeutica.VIGENTE, gradeNova.getStatus());
        assertTrue(gradeNova.getAtivo());
        assertFalse(rascunhoAntigo.getAtivo());
        verify(gradeTerapeuticaRepository).saveAll(List.of(rascunhoAntigo));
    }
}
