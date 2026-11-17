package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.GradeTerapeuticaService;
import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.infrastructure.dto.*;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeTerapeuticaRestResourceTest {

    @Mock
    private GradeTerapeuticaService gradeTerapeuticaService;

    @InjectMocks
    private GradeTerapeuticaRestResource gradeTerapeuticaRestResource;

    private GradeTerapeutica grade;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        Pessoa pessoa = Pessoa.builder().nomePessoa("Lucas Silva").build();
        paciente = new Paciente();
        paciente.setIdPaciente("pac-10");
        paciente.setPessoaPaciente(pessoa);

        grade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-10")
                .paciente(paciente)
                .numeroVersao(1)
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 6, 30))
                .status(StatusGradeTerapeutica.RASCUNHO)
                .itens(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Deve criar proposta de grade e retornar 201 Created")
    void deveCriarProposta() {
        SaveGradeTerapeuticaDTO saveDTO = SaveGradeTerapeuticaDTO.builder()
                .idPaciente("pac-10")
                .dataInicioVigencia(LocalDate.of(2026, 1, 1))
                .dataFimVigencia(LocalDate.of(2026, 6, 30))
                .build();

        when(gradeTerapeuticaService.criarProposta(any(SaveGradeTerapeuticaDTO.class))).thenReturn(grade);

        ResponseEntity<GradeTerapeuticaDTO> response = gradeTerapeuticaRestResource.criarProposta(saveDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("grade-10", response.getBody().getIdGradeTerapeutica());
        assertEquals("Lucas Silva", response.getBody().getNomePaciente());
    }

    @Test
    @DisplayName("Deve carregar grade por ID e retornar 200 OK")
    void deveCarregarGrade() {
        when(gradeTerapeuticaService.carregarGrade("grade-10")).thenReturn(grade);

        ResponseEntity<GradeTerapeuticaDTO> response = gradeTerapeuticaRestResource.carregarGrade("grade-10");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("grade-10", response.getBody().getIdGradeTerapeutica());
    }

    @Test
    @DisplayName("Deve aprovar e replicar compromissos retornando 200 OK")
    void deveAprovarEReplicar() {
        UserDetailsImpl user = mock(UserDetailsImpl.class);
        when(user.getUsername()).thenReturn("resp@clinica.com");

        Compromisso c1 = Compromisso.builder()
                .idCompromisso("cmp-1")
                .tituloCompromisso("Psicologia ABA")
                .dataHoraCompromisso(LocalDateTime.of(2026, 1, 5, 14, 0))
                .status(StatusCompromisso.AGENDADO)
                .paciente(paciente)
                .build();

        when(gradeTerapeuticaService.aprovarEReplicar("grade-10", "resp@clinica.com"))
                .thenReturn(List.of(c1));

        ResponseEntity<List<CompromissoDTO>> response = gradeTerapeuticaRestResource.aprovarEReplicar("grade-10", user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("cmp-1", response.getBody().get(0).getIdCompromisso());
    }

    @Test
    @DisplayName("Deve registrar sugestão do responsável retornando 200 OK")
    void deveSugerirAlteracao() {
        SugestaoGradeDTO sugestaoDTO = SugestaoGradeDTO.builder()
                .justificativa("Horário inviável na quarta-feira")
                .build();

        grade.setStatus(StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL);
        grade.setJustificativaSugestaoResponsavel("Horário inviável na quarta-feira");

        when(gradeTerapeuticaService.registrarSugestaoResponsavel("grade-10", "Horário inviável na quarta-feira"))
                .thenReturn(grade);

        ResponseEntity<GradeTerapeuticaDTO> response = gradeTerapeuticaRestResource.sugerirAlteracao("grade-10", sugestaoDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL, response.getBody().getStatus());
        assertEquals("Horário inviável na quarta-feira", response.getBody().getJustificativaSugestaoResponsavel());
    }

    @Test
    @DisplayName("Deve cancelar compromissos futuros de profissional que saiu")
    void deveCancelarProfissional() {
        CancelarProfissionalGradeDTO dto = CancelarProfissionalGradeDTO.builder()
                .idProfissional("prof-1")
                .dataCorte(LocalDate.of(2026, 2, 1))
                .motivo("Profissional saiu da clínica")
                .build();

        when(gradeTerapeuticaService.cancelarCompromissosFuturosPorProfissional(
                eq("grade-10"), eq("prof-1"), eq(LocalDate.of(2026, 2, 1)), eq("Profissional saiu da clínica")
        )).thenReturn(5);

        ResponseEntity<Integer> response = gradeTerapeuticaRestResource.cancelarProfissional("grade-10", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody());
    }
}
