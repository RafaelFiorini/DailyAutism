package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.InactiveResourceConflictException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReativacaoUniqueServicesTest {

    @Mock private ClinicaRepository clinicaRepository;
    @Mock private UserRepository userRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PerfilRepository perfilRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private FuncionarioClinicaRepository funcionarioClinicaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private PlanoSaudeRepository planoSaudeRepository;
    @Mock private PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    @Mock private PacienteClinicaRepository pacienteClinicaRepository;
    @Mock private CompromissoRepository compromissoRepository;
    @Mock private ProfissionalPacienteRepository profissionalPacienteRepository;
    @Mock private PacientePlanoSaudeService pacientePlanoSaudeService;
    @Mock private EntityManager entityManager;
    @Mock private ProfissionalClinicaRepository profissionalClinicaRepository;
    @Mock private ResponsavelClinicaRepository responsavelClinicaRepository;

    @Test
    void clinicaDeveRetornarConflitoQuandoCnpjInativoEReativar() {
        ClinicaService service = new ClinicaService(clinicaRepository);

        Clinica inativa = Clinica.builder()
                .idClinica("clinica-1")
                .nomeClinica("Clínica Antiga")
                .cnpjClinica("12.345.678/0001-90")
                .build();
        inativa.desativar();

        when(clinicaRepository.findByCnpjAnyAtivo("12345678000190")).thenReturn(Optional.of(inativa));
        SaveClinicaDTO dto = new SaveClinicaDTO();
        dto.setNomeClinica("Clínica Nova");
        dto.setCnpjClinica("12.345.678/0001-90");

        InactiveResourceConflictException ex = assertThrows(
                InactiveResourceConflictException.class,
                () -> service.createClinica(dto)
        );
        assertEquals("CLINICA", ex.getResourceType());
        assertEquals("clinica-1", ex.getResourceId());

        when(clinicaRepository.findByIdAnyAtivo("clinica-1")).thenReturn(Optional.of(inativa));
        when(clinicaRepository.save(any(Clinica.class))).thenAnswer(inv -> inv.getArgument(0));
        Clinica reativada = service.reativarClinica("clinica-1");
        assertTrue(reativada.getAtivo());
    }

    @Test
    void userDeveRetornarConflitoQuandoEmailInativoEReativar() {
        UserService service = new UserService(
                userRepository, pessoaRepository, passwordEncoder, perfilRepository,
                responsavelRepository, profissionalRepository, clinicaRepository,
                funcionarioClinicaRepository, pacienteRepository
        );

        User inativo = User.builder()
                .idUser("user-1")
                .nomeUser("Maria")
                .emailUser("maria@clinica.com")
                .build();
        inativo.desativar();

        when(userRepository.findByEmailAnyAtivo("maria@clinica.com")).thenReturn(Optional.of(inativo));
        SaveUserFullDTO dto = new SaveUserFullDTO();
        dto.setNome("Maria Nova");
        dto.setEmail("maria@clinica.com");
        dto.setCpf("12345678901");

        InactiveResourceConflictException ex = assertThrows(
                InactiveResourceConflictException.class,
                () -> service.createFullUser(dto)
        );
        assertEquals("USUARIO", ex.getResourceType());
        assertEquals("user-1", ex.getResourceId());

        when(userRepository.findByIdAnyAtivo("user-1")).thenReturn(Optional.of(inativo));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserDTO reativado = service.reativarUser("user-1");
        assertEquals("user-1", reativado.getIdUser());
        assertTrue(inativo.getAtivo());
    }

    @Test
    void pacienteProfissionalEResponsavelDevemRetornarConflitoQuandoInativosEReativar() {
        Pessoa pessoa = Pessoa.builder()
                .idPessoa("pessoa-1")
                .nomePessoa("Lucas Silva")
                .CPFPessoa("12345678901")
                .build();

        // Paciente
        PacienteService pacienteService = new PacienteService(
                pacienteRepository, pessoaRepository, userRepository, responsavelRepository,
                planoSaudeRepository, clinicaRepository, pacientePlanoSaudeRepository,
                pacienteClinicaRepository, compromissoRepository, profissionalPacienteRepository,
                funcionarioClinicaRepository, pacientePlanoSaudeService, entityManager
        );
        Paciente pacInativo = Paciente.builder().idPaciente("pac-1").pessoaPaciente(pessoa).build();
        pacInativo.desativar();

        when(pessoaRepository.findByCPFPessoa("12345678901")).thenReturn(Optional.of(pessoa));
        when(pacienteRepository.findByPessoaIdAnyAtivo("pessoa-1")).thenReturn(Optional.of(pacInativo));

        SavePacienteDTO pacDto = new SavePacienteDTO();
        pacDto.setCPFPessoa("123.456.789-01");
        pacDto.setNomePessoa("Lucas Silva");

        InactiveResourceConflictException exPac = assertThrows(
                InactiveResourceConflictException.class,
                () -> pacienteService.createPaciente(pacDto, null)
        );
        assertEquals("PACIENTE", exPac.getResourceType());
        assertEquals("pac-1", exPac.getResourceId());

        when(pacienteRepository.findByIdAnyAtivo("pac-1")).thenReturn(Optional.of(pacInativo));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));
        assertTrue(pacienteService.reativarPaciente("pac-1", null).getAtivo());

        // Profissional
        ProfissionalService profissionalService = new ProfissionalService(
                profissionalRepository, pessoaRepository, userRepository, perfilRepository,
                passwordEncoder, profissionalClinicaRepository, clinicaRepository, funcionarioClinicaRepository
        );
        Profissional profInativo = Profissional.builder().idProf("prof-1").pessoaProf(pessoa).conselhoProf("CRP 123").build();
        profInativo.desativar();

        when(profissionalRepository.findByPessoaIdAnyAtivo("pessoa-1")).thenReturn(Optional.of(profInativo));
        SaveProfissionalDTO profDto = new SaveProfissionalDTO();
        profDto.setCPFPessoa("12345678901");
        profDto.setNomePessoa("Lucas Silva");
        profDto.setConselhoProf("CRP 123");

        InactiveResourceConflictException exProf = assertThrows(
                InactiveResourceConflictException.class,
                () -> profissionalService.createProfissional(profDto, null)
        );
        assertEquals("PROFISSIONAL", exProf.getResourceType());
        assertEquals("prof-1", exProf.getResourceId());

        when(profissionalRepository.findByIdAnyAtivo("prof-1")).thenReturn(Optional.of(profInativo));
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));
        assertTrue(profissionalService.reativarProfissional("prof-1", null).getAtivo());

        // Responsavel
        ResponsavelService responsavelService = new ResponsavelService(
                responsavelRepository, pessoaRepository, pacienteRepository, userRepository,
                perfilRepository, passwordEncoder, responsavelClinicaRepository,
                clinicaRepository, funcionarioClinicaRepository
        );
        Responsavel respInativo = Responsavel.builder().idResponsavel("resp-1").pessoaResponsavel(pessoa).build();
        respInativo.desativar();

        when(responsavelRepository.findByPessoaIdAnyAtivo("pessoa-1")).thenReturn(Optional.of(respInativo));
        SaveResponsavelDTO respDto = new SaveResponsavelDTO();
        respDto.setCPFPessoa("12345678901");
        respDto.setNomePessoa("Lucas Silva");

        InactiveResourceConflictException exResp = assertThrows(
                InactiveResourceConflictException.class,
                () -> responsavelService.createResponsavel(respDto, null)
        );
        assertEquals("RESPONSAVEL", exResp.getResourceType());
        assertEquals("resp-1", exResp.getResourceId());

        when(responsavelRepository.findByIdAnyAtivo("resp-1")).thenReturn(Optional.of(respInativo));
        when(responsavelRepository.save(any(Responsavel.class))).thenAnswer(inv -> inv.getArgument(0));
        assertTrue(responsavelService.reativarResponsavel("resp-1", null).getAtivo());
    }
}
