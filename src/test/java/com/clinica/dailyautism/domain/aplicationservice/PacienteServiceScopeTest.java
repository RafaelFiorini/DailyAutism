package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceScopeTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private UserRepository userRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private PlanoSaudeRepository planoSaudeRepository;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    @Mock private PacienteClinicaRepository pacienteClinicaRepository;
    @Mock private CompromissoRepository compromissoRepository;
    @Mock private ProfissionalPacienteRepository profissionalPacienteRepository;
    @Mock private ProfissionalClinicaRepository profissionalClinicaRepository;
    @Mock private FuncionarioClinicaRepository funcionarioClinicaRepository;
    @Mock private PacientePlanoSaudeService pacientePlanoSaudeService;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private PacienteService pacienteService;

    private Clinica clinicaAttenzione;
    private Pessoa pessoaAlexandra;
    private User userAlexandra;
    private Paciente paciente1;
    private Paciente paciente2;

    @BeforeEach
    void setUp() {
        clinicaAttenzione = Clinica.builder()
                .idClinica("clinica-1")
                .nomeClinica("Attenzione Saúde Mental")
                .build();

        pessoaAlexandra = Pessoa.builder()
                .idPessoa("pessoa-alexandra")
                .nomePessoa("Alexandra Borges Stertz")
                .CPFPessoa("11122233344")
                .build();

        userAlexandra = User.builder()
                .idUser("user-alexandra")
                .emailUser("alexandra@attenzione.com.br")
                .pessoa(pessoaAlexandra)
                .perfis(Set.of(
                        Perfil.builder().nome("ADMIN_CLINICA").build(),
                        Perfil.builder().nome("PROFISSIONAL").build()
                ))
                .build();

        paciente1 = Paciente.builder()
                .idPaciente("pac-1")
                .apelidoPaciente("Paciente 1")
                .ativo(true)
                .clinicas(List.of(
                        PacienteClinica.builder()
                                .clinica(clinicaAttenzione)
                                .build()
                ))
                .build();

        paciente2 = Paciente.builder()
                .idPaciente("pac-2")
                .apelidoPaciente("Paciente 2")
                .ativo(true)
                .clinicas(List.of(
                        PacienteClinica.builder()
                                .clinica(clinicaAttenzione)
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("Usuário com perfil ADMIN_CLINICA e PROFISSIONAL deve listar todos os pacientes da clínica mesmo sem compromisso")
    void testListPacientesForUser_AdminClinicaEProfissional_RetornaPacientesDaClinica() {
        when(userRepository.findByEmailUser("alexandra@attenzione.com.br")).thenReturn(Optional.of(userAlexandra));

        FuncionarioClinica vinculo = FuncionarioClinica.builder()
                .pessoa(pessoaAlexandra)
                .clinica(clinicaAttenzione)
                .build();
        when(funcionarioClinicaRepository.findByPessoa(pessoaAlexandra)).thenReturn(List.of(vinculo));
        when(pacienteRepository.findByClinicaIds(List.of("clinica-1"))).thenReturn(List.of(paciente1, paciente2));

        List<Paciente> result = pacienteService.listPacientesForUser("alexandra@attenzione.com.br");

        assertEquals(2, result.size());
        assertTrue(result.contains(paciente1));
        assertTrue(result.contains(paciente2));
        verify(pacienteRepository).findByClinicaIds(List.of("clinica-1"));
    }

    @Test
    @DisplayName("Usuário com perfil ADMIN_CLINICA e PROFISSIONAL deve carregar paciente da clínica com sucesso")
    void testLoadPacienteForUser_AdminClinicaEProfissional_Sucesso() {
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente1));
        when(userRepository.findByEmailUser("alexandra@attenzione.com.br")).thenReturn(Optional.of(userAlexandra));

        FuncionarioClinica vinculo = FuncionarioClinica.builder()
                .pessoa(pessoaAlexandra)
                .clinica(clinicaAttenzione)
                .build();
        when(funcionarioClinicaRepository.findByPessoa(pessoaAlexandra)).thenReturn(List.of(vinculo));

        Paciente loaded = pacienteService.loadPacienteForUser("pac-1", "alexandra@attenzione.com.br");

        assertNotNull(loaded);
        assertEquals("pac-1", loaded.getIdPaciente());
    }

    @Test
    @DisplayName("Usuário apenas PROFISSIONAL vinculado a clínica deve conseguir listar e visualizar pacientes da sua clínica")
    void testListPacientesForUser_ApenasProfissionalVinculadoAClinica_RetornaPacientesDaClinica() {
        Profissional prof = Profissional.builder()
                .idProf("prof-1")
                .pessoaProf(pessoaAlexandra)
                .build();
        pessoaAlexandra.setProfissional(prof);

        User userApenasProf = User.builder()
                .idUser("user-prof")
                .emailUser("roberto@clinica.com")
                .pessoa(pessoaAlexandra)
                .perfis(Set.of(Perfil.builder().nome("PROFISSIONAL").build()))
                .build();

        when(userRepository.findByEmailUser("roberto@clinica.com")).thenReturn(Optional.of(userApenasProf));
        when(compromissoRepository.findByCriadoPorIdUser("user-prof")).thenReturn(List.of());
        when(profissionalPacienteRepository.findByProfissionalIdProf("prof-1")).thenReturn(List.of());

        ProfissionalClinica pc = ProfissionalClinica.builder()
                .profissional(prof)
                .clinica(clinicaAttenzione)
                .build();
        when(profissionalClinicaRepository.findByProfissionalIdProf("prof-1")).thenReturn(List.of(pc));
        when(pacienteRepository.findByClinicaIds(List.of("clinica-1"))).thenReturn(List.of(paciente1, paciente2));
        when(pacienteRepository.findAllById(anySet())).thenReturn(List.of(paciente1, paciente2));

        List<Paciente> result = pacienteService.listPacientesForUser("roberto@clinica.com");

        assertEquals(2, result.size());
        assertTrue(result.contains(paciente1));
        assertTrue(result.contains(paciente2));
    }

    @Test
    @DisplayName("Usuário ADMIN_CLINICA tentando acessar paciente de outra clínica deve receber AccessDeniedException")
    void testLoadPacienteForUser_ClinicaDiferente_LancaAccessDenied() {
        Clinica outraClinica = Clinica.builder().idClinica("clinica-outra").build();
        Paciente pacienteOutraClinica = Paciente.builder()
                .idPaciente("pac-outro")
                .clinicas(List.of(PacienteClinica.builder().clinica(outraClinica).build()))
                .build();

        when(pacienteRepository.findById("pac-outro")).thenReturn(Optional.of(pacienteOutraClinica));
        when(userRepository.findByEmailUser("alexandra@attenzione.com.br")).thenReturn(Optional.of(userAlexandra));

        FuncionarioClinica vinculo = FuncionarioClinica.builder()
                .pessoa(pessoaAlexandra)
                .clinica(clinicaAttenzione)
                .build();
        when(funcionarioClinicaRepository.findByPessoa(pessoaAlexandra)).thenReturn(List.of(vinculo));

        assertThrows(AccessDeniedException.class, () ->
                pacienteService.loadPacienteForUser("pac-outro", "alexandra@attenzione.com.br")
        );
    }
}
