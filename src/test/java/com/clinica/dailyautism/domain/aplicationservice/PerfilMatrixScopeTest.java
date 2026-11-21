package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suíte de regressão da Matriz de Perfis (Isolados e Mesclados).
 * Valida que nenhum perfil ou sobreposição de perfis entra em conflito
 * nos escopos de Pacientes, Profissionais e Acesso a Clínicas.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerfilMatrixScopeTest {

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
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private PerfilRepository perfilRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PacientePlanoSaudeService pacientePlanoSaudeService;
    @Mock private EntityManager entityManager;

    private ClinicaAccessHelper clinicaAccessHelper;
    private PacienteService pacienteService;
    private ProfissionalService profissionalService;

    private Clinica clinicaA;
    private Clinica clinicaB;
    private Paciente pacClinicaA;
    private Paciente pacClinicaB;
    private Paciente pacFilhoResponsavel;
    private Paciente pacAtendidoProfissional;

    private Pessoa pessoaUsuario;
    private Profissional profUsuario;
    private Profissional profColegaClinicaA;

    @BeforeEach
    void setUp() {
        clinicaAccessHelper = new ClinicaAccessHelper(
                userRepository,
                funcionarioClinicaRepository,
                clinicaRepository,
                responsavelRepository,
                profissionalClinicaRepository,
                profissionalRepository
        );

        pacienteService = new PacienteService(
                pacienteRepository,
                pessoaRepository,
                userRepository,
                responsavelRepository,
                planoSaudeRepository,
                clinicaRepository,
                pacientePlanoSaudeRepository,
                pacienteClinicaRepository,
                compromissoRepository,
                profissionalPacienteRepository,
                profissionalClinicaRepository,
                funcionarioClinicaRepository,
                pacientePlanoSaudeService,
                clinicaAccessHelper,
                entityManager
        );

        profissionalService = new ProfissionalService(
                profissionalRepository,
                pessoaRepository,
                userRepository,
                perfilRepository,
                passwordEncoder,
                profissionalClinicaRepository,
                clinicaRepository,
                funcionarioClinicaRepository
        );

        clinicaA = Clinica.builder().idClinica("clinica-A").nomeClinica("Attenzione Saúde Mental").build();
        clinicaB = Clinica.builder().idClinica("clinica-B").nomeClinica("Clínica Concorrente").build();

        pacClinicaA = Paciente.builder()
                .idPaciente("pac-clinica-A")
                .apelidoPaciente("Paciente Clínica A")
                .clinicas(List.of(PacienteClinica.builder().clinica(clinicaA).build()))
                .build();
        pacClinicaA.setAtivo(true);

        pacClinicaB = Paciente.builder()
                .idPaciente("pac-clinica-B")
                .apelidoPaciente("Paciente Clínica B")
                .clinicas(List.of(PacienteClinica.builder().clinica(clinicaB).build()))
                .build();
        pacClinicaB.setAtivo(true);

        pacFilhoResponsavel = Paciente.builder()
                .idPaciente("pac-filho")
                .apelidoPaciente("Filho do Responsável")
                .clinicas(List.of(PacienteClinica.builder().clinica(clinicaB).build()))
                .build();
        pacFilhoResponsavel.setAtivo(true);

        pacAtendidoProfissional = Paciente.builder()
                .idPaciente("pac-atendido")
                .apelidoPaciente("Paciente Particular do Profissional")
                .clinicas(List.of())
                .build();
        pacAtendidoProfissional.setAtivo(true);

        pessoaUsuario = Pessoa.builder()
                .idPessoa("pessoa-user")
                .nomePessoa("Usuário Teste")
                .CPFPessoa("11122233344")
                .build();

        profUsuario = Profissional.builder()
                .idProf("prof-user")
                .pessoaProf(pessoaUsuario)
                .conselhoProf("CRP 06/12345")
                .build();
        profUsuario.setAtivo(true);

        Pessoa pessoaColega = Pessoa.builder()
                .idPessoa("pessoa-colega")
                .nomePessoa("Dr. Colega")
                .CPFPessoa("55566677788")
                .build();
        profColegaClinicaA = Profissional.builder()
                .idProf("prof-colega")
                .pessoaProf(pessoaColega)
                .conselhoProf("CREFONO 2-9999")
                .build();
        profColegaClinicaA.setAtivo(true);

        // Repositórios padrão
        Map<String, Paciente> pacMap = Map.of(
                pacClinicaA.getIdPaciente(), pacClinicaA,
                pacClinicaB.getIdPaciente(), pacClinicaB,
                pacFilhoResponsavel.getIdPaciente(), pacFilhoResponsavel,
                pacAtendidoProfissional.getIdPaciente(), pacAtendidoProfissional
        );

        when(pacienteRepository.findAll()).thenReturn(List.of(pacClinicaA, pacClinicaB, pacFilhoResponsavel, pacAtendidoProfissional));
        when(pacienteRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(pacMap.get(inv.getArgument(0))));
        when(pacienteRepository.findAllById(any())).thenAnswer(inv -> {
            Iterable<String> ids = inv.getArgument(0);
            List<Paciente> found = new ArrayList<>();
            for (String id : ids) {
                if (pacMap.containsKey(id)) found.add(pacMap.get(id));
            }
            return found;
        });
        when(pacienteRepository.findByClinicaIds(List.of("clinica-A"))).thenReturn(List.of(pacClinicaA));
        when(clinicaRepository.findById("clinica-A")).thenReturn(Optional.of(clinicaA));
        when(clinicaRepository.findAll()).thenReturn(List.of(clinicaA, clinicaB));
        when(clinicaRepository.count()).thenReturn(2L);
    }

    private User buildUser(String email, String... perfis) {
        Set<Perfil> set = new LinkedHashSet<>();
        for (String p : perfis) {
            set.add(Perfil.builder().nome(p).build());
        }
        User u = User.builder()
                .idUser("user-" + email)
                .emailUser(email)
                .pessoa(pessoaUsuario)
                .perfis(set)
                .build();
        when(userRepository.findByEmailUser(email)).thenReturn(Optional.of(u));
        return u;
    }

    private void vincularComoFuncionarioClinicaA() {
        FuncionarioClinica fc = FuncionarioClinica.builder()
                .pessoa(pessoaUsuario)
                .clinica(clinicaA)
                .cargo("Gestor")
                .build();
        fc.setAtivo(true);
        when(funcionarioClinicaRepository.findByPessoa(pessoaUsuario)).thenReturn(List.of(fc));
        when(funcionarioClinicaRepository.findByClinicaIdClinica("clinica-A")).thenReturn(List.of(fc));
    }

    private void vincularComoProfissional() {
        pessoaUsuario.setProfissional(profUsuario);
        when(profissionalRepository.findByPessoaProf(pessoaUsuario)).thenReturn(Optional.of(profUsuario));

        ProfissionalPaciente pp = ProfissionalPaciente.builder()
                .profissional(profUsuario)
                .paciente(pacAtendidoProfissional)
                .build();
        pp.setAtivo(true);
        when(profissionalPacienteRepository.findByProfissionalIdProf("prof-user")).thenReturn(List.of(pp));
        when(profissionalPacienteRepository.existsByProfissionalIdProfAndPacienteIdPaciente("prof-user", "pac-atendido")).thenReturn(true);
    }

    private void vincularProfissionalAClinicaA() {
        ProfissionalClinica pc = ProfissionalClinica.builder()
                .profissional(profUsuario)
                .clinica(clinicaA)
                .build();
        pc.setAtivo(true);
        when(profissionalClinicaRepository.findByProfissionalIdProf("prof-user")).thenReturn(List.of(pc));
        when(profissionalClinicaRepository.findProfissionaisByClinicaIds(List.of("clinica-A")))
                .thenReturn(List.of(profUsuario, profColegaClinicaA));
    }

    private void vincularComoResponsavel() {
        Responsavel resp = Responsavel.builder()
                .idResponsavel("resp-user")
                .pessoaResponsavel(pessoaUsuario)
                .pacientes(List.of(pacFilhoResponsavel))
                .build();
        when(responsavelRepository.findByPessoaResponsavel(pessoaUsuario)).thenReturn(Optional.of(resp));
    }

    // =========================================================================
    // 1. PERFIS ISOLADOS - PACIENTES
    // =========================================================================
    @Nested
    @DisplayName("Perfis Isolados - Escopo de Pacientes")
    class PerfisIsoladosPacientes {

        @Test
        @DisplayName("ADMIN geral lista e carrega todos os pacientes")
        void adminGeral() {
            buildUser("admin@dailyautism.com", "ADMIN");
            List<Paciente> lista = pacienteService.listPacientesForUser("admin@dailyautism.com");
            assertEquals(4, lista.size());
            assertEquals(pacClinicaB, pacienteService.loadPacienteForUser("pac-clinica-B", "admin@dailyautism.com"));
        }

        @Test
        @DisplayName("ADMIN_CLINICA isolado vê pacientes da sua clínica e bloqueia clínica externa")
        void adminClinicaIsolado() {
            buildUser("admin.cli@clinica.com", "ADMIN_CLINICA");
            vincularComoFuncionarioClinicaA();

            List<Paciente> lista = pacienteService.listPacientesForUser("admin.cli@clinica.com");
            assertEquals(1, lista.size());
            assertTrue(lista.contains(pacClinicaA));

            assertEquals(pacClinicaA, pacienteService.loadPacienteForUser("pac-clinica-A", "admin.cli@clinica.com"));
            assertThrows(AccessDeniedException.class,
                    () -> pacienteService.loadPacienteForUser("pac-clinica-B", "admin.cli@clinica.com"));
        }

        @Test
        @DisplayName("FUNCIONARIO_CLINICA (Secretária) isolada vê pacientes da clínica e bloqueia externa")
        void secretariaClinicaIsolada() {
            buildUser("secretaria@clinica.com", "FUNCIONARIO_CLINICA");
            vincularComoFuncionarioClinicaA();

            List<Paciente> lista = pacienteService.listPacientesForUser("secretaria@clinica.com");
            assertEquals(1, lista.size());
            assertTrue(lista.contains(pacClinicaA));

            assertEquals(pacClinicaA, pacienteService.loadPacienteForUser("pac-clinica-A", "secretaria@clinica.com"));
            assertThrows(AccessDeniedException.class,
                    () -> pacienteService.loadPacienteForUser("pac-clinica-B", "secretaria@clinica.com"));
        }

        @Test
        @DisplayName("PROFISSIONAL isolado vê pacientes atendidos e da clínica onde atua")
        void profissionalIsolado() {
            buildUser("prof@clinica.com", "PROFISSIONAL");
            vincularComoProfissional();
            vincularProfissionalAClinicaA();

            List<Paciente> lista = pacienteService.listPacientesForUser("prof@clinica.com");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(pacClinicaA));
            assertTrue(lista.contains(pacAtendidoProfissional));

            assertEquals(pacAtendidoProfissional, pacienteService.loadPacienteForUser("pac-atendido", "prof@clinica.com"));
            assertEquals(pacClinicaA, pacienteService.loadPacienteForUser("pac-clinica-A", "prof@clinica.com"));
            assertThrows(AccessDeniedException.class,
                    () -> pacienteService.loadPacienteForUser("pac-clinica-B", "prof@clinica.com"));
        }

        @Test
        @DisplayName("RESPONSAVEL isolado vê apenas seus filhos/pacientes e bloqueia demais")
        void responsavelIsolado() {
            buildUser("mae@familia.com", "RESPONSAVEL");
            vincularComoResponsavel();

            List<Paciente> lista = pacienteService.listPacientesForUser("mae@familia.com");
            assertEquals(1, lista.size());
            assertTrue(lista.contains(pacFilhoResponsavel));

            assertEquals(pacFilhoResponsavel, pacienteService.loadPacienteForUser("pac-filho", "mae@familia.com"));
            assertThrows(AccessDeniedException.class,
                    () -> pacienteService.loadPacienteForUser("pac-clinica-A", "mae@familia.com"));
        }
    }

    // =========================================================================
    // 2. PERFIS MESCLADOS (SOBREPOSIÇÃO) - PACIENTES
    // =========================================================================
    @Nested
    @DisplayName("Perfis Mesclados (Sobreposição Aditiva) - Escopo de Pacientes")
    class PerfisMescladosPacientes {

        @Test
        @DisplayName("ADMIN_CLINICA + PROFISSIONAL: vê união dos pacientes da clínica e pacientes particulares atendidos")
        void adminClinicaEProfissional() {
            buildUser("alexandra@attenzione.com.br", "ADMIN_CLINICA", "PROFISSIONAL");
            vincularComoFuncionarioClinicaA();
            vincularComoProfissional();
            vincularProfissionalAClinicaA();

            List<Paciente> lista = pacienteService.listPacientesForUser("alexandra@attenzione.com.br");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(pacClinicaA));
            assertTrue(lista.contains(pacAtendidoProfissional));

            assertEquals(pacClinicaA, pacienteService.loadPacienteForUser("pac-clinica-A", "alexandra@attenzione.com.br"));
            assertEquals(pacAtendidoProfissional, pacienteService.loadPacienteForUser("pac-atendido", "alexandra@attenzione.com.br"));
            assertThrows(AccessDeniedException.class,
                    () -> pacienteService.loadPacienteForUser("pac-clinica-B", "alexandra@attenzione.com.br"));
        }

        @Test
        @DisplayName("ADMIN_CLINICA + RESPONSAVEL: vê pacientes da clínica + seu próprio filho mesmo se de outra clínica")
        void adminClinicaEResponsavel() {
            buildUser("gestora.mae@clinica.com", "ADMIN_CLINICA", "RESPONSAVEL");
            vincularComoFuncionarioClinicaA();
            vincularComoResponsavel();

            List<Paciente> lista = pacienteService.listPacientesForUser("gestora.mae@clinica.com");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(pacClinicaA));
            assertTrue(lista.contains(pacFilhoResponsavel));

            assertEquals(pacClinicaA, pacienteService.loadPacienteForUser("pac-clinica-A", "gestora.mae@clinica.com"));
            assertEquals(pacFilhoResponsavel, pacienteService.loadPacienteForUser("pac-filho", "gestora.mae@clinica.com"));
        }

        @Test
        @DisplayName("ADMIN_CLINICA + PROFISSIONAL + RESPONSAVEL: união completa dos 3 escopos sem conflito")
        void adminClinicaProfissionalEResponsavel() {
            buildUser("tripla@clinica.com", "ADMIN_CLINICA", "PROFISSIONAL", "RESPONSAVEL");
            vincularComoFuncionarioClinicaA();
            vincularComoProfissional();
            vincularProfissionalAClinicaA();
            vincularComoResponsavel();

            List<Paciente> lista = pacienteService.listPacientesForUser("tripla@clinica.com");
            assertEquals(3, lista.size());
            assertTrue(lista.contains(pacClinicaA));
            assertTrue(lista.contains(pacAtendidoProfissional));
            assertTrue(lista.contains(pacFilhoResponsavel));

            assertEquals(pacClinicaA, pacienteService.loadPacienteForUser("pac-clinica-A", "tripla@clinica.com"));
            assertEquals(pacAtendidoProfissional, pacienteService.loadPacienteForUser("pac-atendido", "tripla@clinica.com"));
            assertEquals(pacFilhoResponsavel, pacienteService.loadPacienteForUser("pac-filho", "tripla@clinica.com"));
            assertThrows(AccessDeniedException.class,
                    () -> pacienteService.loadPacienteForUser("pac-clinica-B", "tripla@clinica.com"));
        }

        @Test
        @DisplayName("FUNCIONARIO_CLINICA + PROFISSIONAL: união clínica + atendimentos")
        void secretariaEProfissional() {
            buildUser("sec.prof@clinica.com", "FUNCIONARIO_CLINICA", "PROFISSIONAL");
            vincularComoFuncionarioClinicaA();
            vincularComoProfissional();

            List<Paciente> lista = pacienteService.listPacientesForUser("sec.prof@clinica.com");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(pacClinicaA));
            assertTrue(lista.contains(pacAtendidoProfissional));
        }

        @Test
        @DisplayName("FUNCIONARIO_CLINICA + RESPONSAVEL: união clínica + filho sob responsabilidade")
        void secretariaEResponsavel() {
            buildUser("sec.mae@clinica.com", "FUNCIONARIO_CLINICA", "RESPONSAVEL");
            vincularComoFuncionarioClinicaA();
            vincularComoResponsavel();

            List<Paciente> lista = pacienteService.listPacientesForUser("sec.mae@clinica.com");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(pacClinicaA));
            assertTrue(lista.contains(pacFilhoResponsavel));
        }

        @Test
        @DisplayName("PROFISSIONAL + RESPONSAVEL: vê pacientes atendidos + seu próprio filho")
        void profissionalEResponsavel() {
            buildUser("terapeuta.mae@clinica.com", "PROFISSIONAL", "RESPONSAVEL");
            vincularComoProfissional();
            vincularComoResponsavel();

            List<Paciente> lista = pacienteService.listPacientesForUser("terapeuta.mae@clinica.com");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(pacAtendidoProfissional));
            assertTrue(lista.contains(pacFilhoResponsavel));

            assertEquals(pacAtendidoProfissional, pacienteService.loadPacienteForUser("pac-atendido", "terapeuta.mae@clinica.com"));
            assertEquals(pacFilhoResponsavel, pacienteService.loadPacienteForUser("pac-filho", "terapeuta.mae@clinica.com"));
        }

        @Test
        @DisplayName("PACIENTE + RESPONSAVEL: vê paciente sob responsabilidade sem bloqueio")
        void pacienteEResponsavel() {
            buildUser("pac.resp@familia.com", "PACIENTE", "RESPONSAVEL");
            vincularComoResponsavel();

            List<Paciente> lista = pacienteService.listPacientesForUser("pac.resp@familia.com");
            assertEquals(1, lista.size());
            assertTrue(lista.contains(pacFilhoResponsavel));
        }
    }

    // =========================================================================
    // 3. ESCOPO DE PROFISSIONAIS & AUTO-CURA DE CLÍNICA
    // =========================================================================
    @Nested
    @DisplayName("Perfis Isolados e Mesclados - Escopo de Profissionais e Clínicas")
    class EscopoProfissionaisEClinicas {

        @Test
        @DisplayName("ADMIN_CLINICA + PROFISSIONAL sem registro em Profissional auto-cria entidade e vincula à clínica")
        void adminClinicaEProfissionalAutoCura() {
            buildUser("alexandra.nova@attenzione.com.br", "ADMIN_CLINICA", "PROFISSIONAL");
            vincularComoFuncionarioClinicaA();
            when(profissionalRepository.findByPessoaProf(pessoaUsuario)).thenReturn(Optional.empty());
            when(profissionalClinicaRepository.findProfissionaisByClinicaIds(List.of("clinica-A")))
                    .thenReturn(List.of(profColegaClinicaA));
            when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> {
                Profissional saved = inv.getArgument(0);
                saved.setIdProf("prof-auto-gerado");
                return saved;
            });

            List<Profissional> lista = profissionalService.listProfissionaisForUser("alexandra.nova@attenzione.com.br");
            assertEquals(2, lista.size());
            assertTrue(lista.stream().anyMatch(p -> "prof-colega".equals(p.getIdProf())));
            assertTrue(lista.stream().anyMatch(p -> "prof-auto-gerado".equals(p.getIdProf())));
            verify(profissionalRepository, times(1)).save(any(Profissional.class));
            verify(profissionalClinicaRepository, atLeastOnce()).save(any(ProfissionalClinica.class));
        }

        @Test
        @DisplayName("FUNCIONARIO_CLINICA (Secretária) lista todos os profissionais da clínica")
        void secretariaListaProfissionaisDaClinica() {
            buildUser("secretaria@clinica.com", "FUNCIONARIO_CLINICA");
            vincularComoFuncionarioClinicaA();
            when(profissionalClinicaRepository.findProfissionaisByClinicaIds(List.of("clinica-A")))
                    .thenReturn(List.of(profUsuario, profColegaClinicaA));

            List<Profissional> lista = profissionalService.listProfissionaisForUser("secretaria@clinica.com");
            assertEquals(2, lista.size());
        }

        @Test
        @DisplayName("PROFISSIONAL + RESPONSAVEL lista a si mesmo e colegas da clínica")
        void profissionalEResponsavelListaColegas() {
            buildUser("prof.resp@clinica.com", "PROFISSIONAL", "RESPONSAVEL");
            vincularComoProfissional();
            vincularProfissionalAClinicaA();

            List<Profissional> lista = profissionalService.listProfissionaisForUser("prof.resp@clinica.com");
            assertEquals(2, lista.size());
            assertTrue(lista.contains(profUsuario));
            assertTrue(lista.contains(profColegaClinicaA));
        }

        @Test
        @DisplayName("ClinicaAccessHelper resolve clínica única como fallback se gestor estiver sem vínculo explícito")
        void clinicaAccessHelperFallbackClinicaUnica() {
            User gestorSemVinculo = buildUser("gestor.novo@attenzione.com.br", "ADMIN_CLINICA");
            when(funcionarioClinicaRepository.findByPessoa(pessoaUsuario)).thenReturn(List.of());
            when(clinicaRepository.findAll()).thenReturn(List.of(clinicaA));

            List<String> ids = clinicaAccessHelper.listarIdsClinicasDoUsuario(gestorSemVinculo);
            assertEquals(List.of("clinica-A"), ids);
            verify(funcionarioClinicaRepository, times(1)).save(any(FuncionarioClinica.class));
        }
    }
}
