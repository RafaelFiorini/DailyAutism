package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CancelamentoENotificacaoCompromissoTest {

    @Mock private CompromissoRepository compromissoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock private PeriodicidadeRepository periodicidadeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private PacienteService pacienteService;
    @Mock private ProfissionalService profissionalService;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private ClinicaAccessHelper clinicaAccessHelper;
    @Mock private ContratoParticularService contratoParticularService;
    @Mock private ConfiguracaoSistemaService configuracaoSistemaService;
    @Mock private LancamentoRepasseService lancamentoRepasseService;
    @Mock private FuncionarioClinicaRepository funcionarioClinicaRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private CompromissoService compromissoService;

    private User userResp;
    private User userProf;
    private User userClinica;
    private Compromisso compromisso;

    @BeforeEach
    void setUp() {
        Pessoa pResp = Pessoa.builder().idPessoa("p-resp").nomePessoa("Rafael Pai").build();
        Pessoa pProf = Pessoa.builder().idPessoa("p-prof").nomePessoa("Dr. Rafael Psicólogo").build();
        Pessoa pClinica = Pessoa.builder().idPessoa("p-cli").nomePessoa("Ana Secretária").build();
        Pessoa pPac = Pessoa.builder().idPessoa("p-pac").nomePessoa("Enzo").build();

        userResp = User.builder().idUser("u-resp").nomeUser("Rafael Pai").emailUser("pai@teste.com").pessoa(pResp).build();
        userResp.setAtivo(true);
        userProf = User.builder().idUser("u-prof").nomeUser("Dr. Rafael Psicólogo").emailUser("prof@teste.com").pessoa(pProf).build();
        userProf.setAtivo(true);
        userClinica = User.builder().idUser("u-cli").nomeUser("Ana Secretária").emailUser("ana@clinica.com").pessoa(pClinica).build();
        userClinica.setAtivo(true);

        Profissional prof = Profissional.builder().idProf("prof-1").pessoaProf(pProf).build();
        pProf.setProfissional(prof);

        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Attenzione").build();
        FuncionarioClinica fc = FuncionarioClinica.builder().pessoa(pClinica).clinica(clinica).build();
        fc.setAtivo(true);

        Responsavel resp = Responsavel.builder().idResponsavel("resp-1").pessoaResponsavel(pResp).build();
        Paciente pac = Paciente.builder()
                .idPaciente("pac-1")
                .pessoaPaciente(pPac)
                .responsaveis(List.of(resp))
                .build();
        resp.setPacientes(List.of(pac));

        TipoCompromisso tipo = TipoCompromisso.builder().idTipoCompromisso("t-1").nomeTipo("Psicologia").build();

        compromisso = Compromisso.builder()
                .idCompromisso("comp-1")
                .tituloCompromisso("Psicologia")
                .dataHoraCompromisso(LocalDateTime.of(2026, 12, 10, 20, 0))
                .status(StatusCompromisso.AGENDADO)
                .paciente(pac)
                .profissional(prof)
                .clinica(clinica)
                .tipoCompromisso(tipo)
                .build();

        when(compromissoRepository.findById("comp-1")).thenReturn(Optional.of(compromisso));
        when(compromissoRepository.save(any(Compromisso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(responsavelRepository.findByPessoaResponsavel(pResp)).thenReturn(Optional.of(resp));
        when(profissionalRepository.findByPessoaProf(pProf)).thenReturn(Optional.of(prof));
        when(funcionarioClinicaRepository.findByClinicaIdClinica("cli-1")).thenReturn(List.of(fc));
        when(userRepository.findByPessoaIdAnyAtivo("p-prof")).thenReturn(Optional.of(userProf));
        when(userRepository.findByPessoaIdAnyAtivo("p-resp")).thenReturn(Optional.of(userResp));
        when(userRepository.findByPessoaIdAnyAtivo("p-cli")).thenReturn(Optional.of(userClinica));
    }

    @Test
    @DisplayName("Responsável cancela sessão com justificativa e notifica Profissional e Clínica")
    void responsavelCancelaENotificaProfissionalEClinica() {
        when(userRepository.findByEmailUserIgnoreCase("pai@teste.com")).thenReturn(Optional.of(userResp));

        Compromisso result = compromissoService.cancelarCompromissoComJustificativa(
                "comp-1", "Criança com febre hoje, avisando antecipadamente", "pai@teste.com"
        );

        assertEquals(StatusCompromisso.CANCELADO, result.getStatus());
        assertEquals("Criança com febre hoje, avisando antecipadamente", result.getMotivoCancelamento());
        assertEquals("Rafael Pai", result.getCanceladoPorNome());
        assertEquals("RESPONSAVEL", result.getCanceladoPorPerfil());
        assertNotNull(result.getCanceladoEm());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<User>> destCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(notificacaoService, times(1)).notificarUsuarios(
                destCaptor.capture(),
                eq("u-resp"),
                contains("pelo responsável"),
                contains("Criança com febre hoje"),
                eq(TipoNotificacao.CANCELAMENTO_SESSAO),
                eq("comp-1")
        );
        assertTrue(destCaptor.getValue().contains(userProf));
        assertTrue(destCaptor.getValue().contains(userClinica));
        verify(lancamentoRepasseService, times(1)).cancelarLancamentoPorCompromisso("comp-1");
    }

    @Test
    @DisplayName("Profissional cancela sessão com justificativa e notifica Responsável e Clínica")
    void profissionalCancelaENotificaResponsavelEClinica() {
        when(userRepository.findByEmailUserIgnoreCase("prof@teste.com")).thenReturn(Optional.of(userProf));

        Compromisso result = compromissoService.cancelarCompromissoComJustificativa(
                "comp-1", "Imprevisto médico do profissional", "prof@teste.com"
        );

        assertEquals(StatusCompromisso.CANCELADO, result.getStatus());
        assertEquals("PROFISSIONAL", result.getCanceladoPorPerfil());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<User>> destCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(notificacaoService, times(1)).notificarUsuarios(
                destCaptor.capture(),
                eq("u-prof"),
                contains("pelo profissional"),
                contains("Imprevisto médico"),
                eq(TipoNotificacao.CANCELAMENTO_SESSAO),
                eq("comp-1")
        );
        assertTrue(destCaptor.getValue().contains(userResp));
        assertTrue(destCaptor.getValue().contains(userClinica));
    }

    @Test
    @DisplayName("Usuário sem vínculo com a sessão recebe AccessDeniedException")
    void usuarioSemVinculoBloqueado() {
        User intruso = User.builder().idUser("u-intruso").emailUser("intruso@teste.com").build();
        when(userRepository.findByEmailUserIgnoreCase("intruso@teste.com")).thenReturn(Optional.of(intruso));

        assertThrows(AccessDeniedException.class, () ->
                compromissoService.cancelarCompromissoComJustificativa("comp-1", "Motivo qualquer", "intruso@teste.com")
        );
    }

    @Test
    @DisplayName("Sessão já realizada não pode ser cancelada")
    void sessaoRealizadaNaoPodeSerCancelada() {
        compromisso.setStatus(StatusCompromisso.REALIZADO);
        when(userRepository.findByEmailUserIgnoreCase("pai@teste.com")).thenReturn(Optional.of(userResp));

        assertThrows(IllegalStateException.class, () ->
                compromissoService.cancelarCompromissoComJustificativa("comp-1", "Motivo teste", "pai@teste.com")
        );
    }
}
