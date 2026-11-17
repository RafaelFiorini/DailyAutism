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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeTerapeuticaServiceTest {

    @Mock private GradeTerapeuticaRepository gradeTerapeuticaRepository;
    @Mock private GradeTerapeuticaItemRepository gradeTerapeuticaItemRepository;
    @Mock private CompromissoRepository compromissoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private UserRepository userRepository;
    @Mock private PeriodicidadeRepository periodicidadeRepository;
    @Mock private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private GradeTerapeuticaService gradeTerapeuticaService;

    private Paciente paciente;
    private Clinica clinica;
    private User usuario;
    private Periodicidade periodicidadeSemanal;
    private TipoCompromisso tipoPsico;
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        Pessoa pessoaPaciente = Pessoa.builder()
                .nomePessoa("Pedro Lucas")
                .build();

        paciente = new Paciente();
        paciente.setIdPaciente("pac-1");
        paciente.setPessoaPaciente(pessoaPaciente);

        clinica = new Clinica();
        clinica.setIdClinica("cli-1");

        usuario = new User();
        usuario.setIdUser("usr-1");
        usuario.setEmailUser("secretaria@clinica.com");

        periodicidadeSemanal = Periodicidade.builder()
                .idPeriodicidade("per-sem")
                .nomePeriodicidade("Semanal")
                .diasIntervalo(7)
                .build();

        tipoPsico = new TipoCompromisso();
        tipoPsico.setIdTipoCompromisso("tipo-psico");
        tipoPsico.setNomeTipo("Psicologia ABA");
        tipoPsico.setDescricaoTipo("Psicologia ABA");

        Pessoa pessoaProf = Pessoa.builder()
                .nomePessoa("Dra. Amanda")
                .build();

        profissional = new Profissional();
        profissional.setIdProf("prof-1");
        profissional.setPessoaProf(pessoaProf);
    }

    @Test
    @DisplayName("1. Deve registrar sugestão do responsável sem replicar agenda e alterar status para SUGESTAO_RESPONSAVEL")
    void deveRegistrarSugestaoResponsavel() {
        GradeTerapeutica grade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-1")
                .status(StatusGradeTerapeutica.PENDENTE_APROVACAO)
                .build();

        when(gradeTerapeuticaRepository.findById("grade-1")).thenReturn(Optional.of(grade));
        when(gradeTerapeuticaRepository.save(any(GradeTerapeutica.class))).thenAnswer(i -> i.getArgument(0));

        GradeTerapeutica resultado = gradeTerapeuticaService.registrarSugestaoResponsavel(
                "grade-1",
                "Gostaria de mudar a sessão de terça das 14h para 16h pois Pedro tem natação."
        );

        assertEquals(StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL, resultado.getStatus());
        assertEquals("Gostaria de mudar a sessão de terça das 14h para 16h pois Pedro tem natação.",
                resultado.getJustificativaSugestaoResponsavel());
        assertNotNull(resultado.getDataResposta());
        verify(compromissoRepository, never()).save(any(Compromisso.class));
    }

    @Test
    @DisplayName("2. Deve aprovar e replicar agenda gerando compromissos AGENDADO semana a semana")
    void deveAprovarEReplicarAgendaSemConflito() {
        LocalDate inicio = LocalDate.now().plusDays(1); // futuro
        // Ajusta início para uma segunda-feira próxima
        while (inicio.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            inicio = inicio.plusDays(1);
        }
        LocalDate fim = inicio.plusWeeks(3); // 4 semanas no total

        GradeTerapeutica grade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-1")
                .paciente(paciente)
                .clinica(clinica)
                .numeroVersao(1)
                .dataInicioVigencia(inicio)
                .dataFimVigencia(fim)
                .status(StatusGradeTerapeutica.PENDENTE_APROVACAO)
                .itens(new ArrayList<>())
                .build();

        GradeTerapeuticaItem item = GradeTerapeuticaItem.builder()
                .idGradeTerapeuticaItem("item-1")
                .gradeTerapeutica(grade)
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(14, 0))
                .horaFim(LocalTime.of(15, 0))
                .tipoCompromisso(tipoPsico)
                .profissional(profissional)
                .sala("Sala 1")
                .build();
        grade.getItens().add(item);

        when(gradeTerapeuticaRepository.findByIdWithItens("grade-1")).thenReturn(Optional.of(grade));
        when(gradeTerapeuticaRepository.save(any(GradeTerapeutica.class))).thenAnswer(i -> i.getArgument(0));
        when(periodicidadeRepository.findAll()).thenReturn(List.of(periodicidadeSemanal));
        when(userRepository.findByEmailUser("secretaria@clinica.com")).thenReturn(Optional.of(usuario));
        when(compromissoRepository.countConflitosProfissional(eq("prof-1"), any(LocalDateTime.class), isNull()))
                .thenReturn(0L);

        List<Compromisso> gerados = gradeTerapeuticaService.aprovarEReplicar("grade-1", "secretaria@clinica.com");

        assertEquals(4, gerados.size());
        for (Compromisso c : gerados) {
            assertEquals(StatusCompromisso.AGENDADO, c.getStatus());
            assertEquals("Pedro Lucas", c.getPaciente().getPessoaPaciente().getNomePessoa());
            assertEquals(grade, c.getGradeTerapeutica());
            assertEquals(14, c.getDataHoraCompromisso().getHour());
        }
        assertEquals(StatusGradeTerapeutica.VIGENTE, grade.getStatus());
        verify(compromissoRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("3. Deve marcar como PENDENTE_REAGENDAMENTO compromissos que colidirem com agenda do profissional")
    void deveMarcarPendenteReagendamentoQuandoHouverConflito() {
        LocalDate inicio = LocalDate.now().plusDays(1);
        while (inicio.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            inicio = inicio.plusDays(1);
        }
        LocalDate fim = inicio.plusWeeks(1); // 2 semanas

        GradeTerapeutica grade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-choque")
                .paciente(paciente)
                .clinica(clinica)
                .numeroVersao(1)
                .dataInicioVigencia(inicio)
                .dataFimVigencia(fim)
                .status(StatusGradeTerapeutica.PENDENTE_APROVACAO)
                .itens(new ArrayList<>())
                .build();

        GradeTerapeuticaItem item = GradeTerapeuticaItem.builder()
                .idGradeTerapeuticaItem("item-1")
                .gradeTerapeutica(grade)
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(10, 0))
                .horaFim(LocalTime.of(11, 0))
                .tipoCompromisso(tipoPsico)
                .profissional(profissional)
                .build();
        grade.getItens().add(item);

        when(gradeTerapeuticaRepository.findByIdWithItens("grade-choque")).thenReturn(Optional.of(grade));
        when(gradeTerapeuticaRepository.save(any(GradeTerapeutica.class))).thenAnswer(i -> i.getArgument(0));
        when(periodicidadeRepository.findAll()).thenReturn(List.of(periodicidadeSemanal));
        when(userRepository.findByEmailUser("secretaria@clinica.com")).thenReturn(Optional.of(usuario));

        // 1ª semana tem choque (retorna 1), 2ª semana não tem (retorna 0)
        LocalDateTime choqueDataHora = inicio.atTime(10, 0);
        when(compromissoRepository.countConflitosProfissional("prof-1", choqueDataHora, null)).thenReturn(1L);
        when(compromissoRepository.countConflitosProfissional("prof-1", inicio.plusWeeks(1).atTime(10, 0), null)).thenReturn(0L);

        List<Compromisso> gerados = gradeTerapeuticaService.aprovarEReplicar("grade-choque", "secretaria@clinica.com");

        assertEquals(2, gerados.size());
        assertEquals(StatusCompromisso.PENDENTE_REAGENDAMENTO, gerados.get(0).getStatus());
        assertEquals(StatusCompromisso.AGENDADO, gerados.get(1).getStatus());
    }

    @Test
    @DisplayName("4. Deve substituir grade antiga cancelando futuros a partir da data de corte e preservando passados")
    void deveSubstituirGradeECancelarFuturosPreservandoPassados() {
        LocalDate dataCorte = LocalDate.now().plusWeeks(2);

        GradeTerapeutica gradeAntiga = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-v1")
                .status(StatusGradeTerapeutica.VIGENTE)
                .numeroVersao(1)
                .build();

        GradeTerapeutica novaGrade = GradeTerapeutica.builder()
                .idGradeTerapeutica("grade-v2")
                .paciente(paciente)
                .clinica(clinica)
                .idGradeAnterior("grade-v1")
                .numeroVersao(2)
                .dataInicioVigencia(dataCorte)
                .dataFimVigencia(dataCorte.plusWeeks(2))
                .status(StatusGradeTerapeutica.PENDENTE_APROVACAO)
                .itens(new ArrayList<>())
                .build();

        Compromisso futuroGradeAntiga = Compromisso.builder()
                .idCompromisso("cmp-futuro")
                .dataHoraCompromisso(dataCorte.atTime(14, 0))
                .status(StatusCompromisso.AGENDADO)
                .gradeTerapeutica(gradeAntiga)
                .build();

        when(gradeTerapeuticaRepository.findByIdWithItens("grade-v2")).thenReturn(Optional.of(novaGrade));
        when(gradeTerapeuticaRepository.findById("grade-v1")).thenReturn(Optional.of(gradeAntiga));
        when(gradeTerapeuticaRepository.save(any(GradeTerapeutica.class))).thenAnswer(i -> i.getArgument(0));
        when(periodicidadeRepository.findAll()).thenReturn(List.of(periodicidadeSemanal));
        when(userRepository.findByEmailUser("secretaria@clinica.com")).thenReturn(Optional.of(usuario));
        when(compromissoRepository.findByGradeTerapeuticaIdGradeTerapeuticaAndDataHoraCompromissoGreaterThanEqualOrderByDataHoraCompromissoAsc(
                eq("grade-v1"), any(LocalDateTime.class)))
                .thenReturn(List.of(futuroGradeAntiga));

        gradeTerapeuticaService.aprovarEReplicar("grade-v2", "secretaria@clinica.com");

        assertEquals(StatusGradeTerapeutica.SUBSTITUIDA, gradeAntiga.getStatus());
        assertEquals(StatusCompromisso.CANCELADO, futuroGradeAntiga.getStatus());
        assertTrue(futuroGradeAntiga.getDescricaoCompromisso().contains("Substituído pela grade"));
    }

    @Test
    @DisplayName("5. Deve cancelar compromissos futuros de um profissional que saiu da clínica a partir da data de corte")
    void deveCancelarCompromissosFuturosDeProfissionalQueSaiu() {
        LocalDate dataCorte = LocalDate.now().plusDays(3);

        Compromisso futuro1 = Compromisso.builder()
                .idCompromisso("cmp-futuro-1")
                .dataHoraCompromisso(dataCorte.atTime(15, 0))
                .status(StatusCompromisso.AGENDADO)
                .profissional(profissional)
                .build();

        Compromisso futuro2 = Compromisso.builder()
                .idCompromisso("cmp-futuro-2")
                .dataHoraCompromisso(dataCorte.plusWeeks(1).atTime(15, 0))
                .status(StatusCompromisso.PENDENTE_REAGENDAMENTO)
                .profissional(profissional)
                .build();

        when(compromissoRepository.findByGradeTerapeuticaIdGradeTerapeuticaAndDataHoraCompromissoGreaterThanEqualOrderByDataHoraCompromissoAsc(
                eq("grade-1"), any(LocalDateTime.class)))
                .thenReturn(List.of(futuro1, futuro2));

        int cancelados = gradeTerapeuticaService.cancelarCompromissosFuturosPorProfissional(
                "grade-1",
                "prof-1",
                dataCorte,
                "Profissional desligou-se da clínica e não haverá reposição deste horário"
        );

        assertEquals(2, cancelados);
        assertEquals(StatusCompromisso.CANCELADO, futuro1.getStatus());
        assertEquals(StatusCompromisso.CANCELADO, futuro2.getStatus());
        assertTrue(futuro1.getDescricaoCompromisso().contains("Profissional desligou-se"));
        verify(compromissoRepository).saveAll(anyList());
    }
}
