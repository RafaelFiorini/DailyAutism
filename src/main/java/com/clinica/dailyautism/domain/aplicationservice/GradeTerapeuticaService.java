package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.ProporAlteracaoGradeDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveGradeTerapeuticaDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveGradeTerapeuticaItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeTerapeuticaService {

    private final GradeTerapeuticaRepository gradeTerapeuticaRepository;
    private final GradeTerapeuticaItemRepository gradeTerapeuticaItemRepository;
    private final CompromissoRepository compromissoRepository;
    private final PacienteRepository pacienteRepository;
    private final ClinicaRepository clinicaRepository;
    private final UserRepository userRepository;
    private final PeriodicidadeRepository periodicidadeRepository;
    private final TipoCompromissoRepository tipoCompromissoRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional(readOnly = true)
    public GradeTerapeutica carregarGrade(String idGrade) {
        return gradeTerapeuticaRepository.findByIdWithItens(idGrade)
                .orElseThrow(() -> new IllegalArgumentException("Grade terapêutica não encontrada: " + idGrade));
    }

    @Transactional(readOnly = true)
    public List<GradeTerapeutica> listarPorPaciente(String idPaciente) {
        return gradeTerapeuticaRepository.findByPacienteIdPacienteOrderByNumeroVersaoDesc(idPaciente);
    }

    @Transactional(readOnly = true)
    public List<GradeTerapeutica> listarPorClinica(String idClinica) {
        return gradeTerapeuticaRepository.findByClinicaIdClinicaOrderByDataEnvioDesc(idClinica);
    }

    @Transactional
    public GradeTerapeutica salvarProposta(GradeTerapeutica grade, boolean enviarImediatamente) {
        if (grade.getNumeroVersao() == null) {
            grade.setNumeroVersao(1);
        }
        if (enviarImediatamente) {
            grade.setStatus(StatusGradeTerapeutica.PENDENTE_APROVACAO);
            grade.setDataEnvio(LocalDateTime.now());
        } else if (grade.getStatus() == null) {
            grade.setStatus(StatusGradeTerapeutica.RASCUNHO);
        }

        if (grade.getItens() != null) {
            for (GradeTerapeuticaItem item : grade.getItens()) {
                item.setGradeTerapeutica(grade);
            }
        }

        return gradeTerapeuticaRepository.save(grade);
    }

    @Transactional
    public GradeTerapeutica enviarParaAprovacao(String idGrade) {
        GradeTerapeutica grade = carregarGrade(idGrade);
        if (grade.getStatus() != StatusGradeTerapeutica.RASCUNHO && grade.getStatus() != StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL) {
            throw new IllegalStateException("A grade só pode ser enviada se estiver em rascunho ou com sugestão de alteração.");
        }
        grade.setStatus(StatusGradeTerapeutica.PENDENTE_APROVACAO);
        grade.setDataEnvio(LocalDateTime.now());
        return gradeTerapeuticaRepository.save(grade);
    }

    @Transactional
    public GradeTerapeutica registrarSugestaoResponsavel(String idGrade, String justificativa) {
        GradeTerapeutica grade = gradeTerapeuticaRepository.findById(idGrade)
                .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + idGrade));

        grade.setStatus(StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL);
        grade.setJustificativaSugestaoResponsavel(justificativa);
        grade.setDataResposta(LocalDateTime.now());
        return gradeTerapeuticaRepository.save(grade);
    }

    @Transactional
    public GradeTerapeutica rejeitar(String idGrade, String motivo) {
        GradeTerapeutica grade = carregarGrade(idGrade);
        grade.setStatus(StatusGradeTerapeutica.REJEITADA);
        grade.setMotivoRejeicao(motivo);
        grade.setDataResposta(LocalDateTime.now());
        return gradeTerapeuticaRepository.save(grade);
    }

    @Transactional
    public List<Compromisso> aprovarEReplicar(String idGrade, String emailUsuario) {
        GradeTerapeutica grade = carregarGrade(idGrade);

        User usuario = userRepository.findByEmailUser(emailUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + emailUsuario));

        // 1. Se for uma nova versão de grade, substituir a anterior e cancelar compromissos futuros dela
        if (grade.getIdGradeAnterior() != null && !grade.getIdGradeAnterior().isBlank()) {
            gradeTerapeuticaRepository.findById(grade.getIdGradeAnterior()).ifPresent(gradeAntiga -> {
                gradeAntiga.setStatus(StatusGradeTerapeutica.SUBSTITUIDA);
                gradeTerapeuticaRepository.save(gradeAntiga);

                LocalDateTime dataCorteHora = grade.getDataInicioVigencia().atStartOfDay();
                List<Compromisso> futurosGradeAntiga = compromissoRepository
                        .findByGradeTerapeuticaIdGradeTerapeuticaAndDataHoraCompromissoGreaterThanEqualOrderByDataHoraCompromissoAsc(
                                gradeAntiga.getIdGradeTerapeutica(), dataCorteHora
                        );

                for (Compromisso c : futurosGradeAntiga) {
                    if (c.getStatus() != StatusCompromisso.REALIZADO && c.getStatus() != StatusCompromisso.FALTOU) {
                        c.setStatus(StatusCompromisso.CANCELADO);
                        String obs = "Substituído pela grade terapêutica v" + grade.getNumeroVersao();
                        c.setDescricaoCompromisso(
                                c.getDescricaoCompromisso() == null ? obs : c.getDescricaoCompromisso() + " | " + obs
                        );
                    }
                }
                compromissoRepository.saveAll(futurosGradeAntiga);
            });
        }

        // 2. Resolver periodicidade semanal padrão
        Periodicidade periodicidadeSemanal = periodicidadeRepository.findAll().stream()
                .filter(p -> p.getNomePeriodicidade() != null && p.getNomePeriodicidade().toLowerCase().contains("semanal"))
                .findFirst()
                .orElseGet(() -> Periodicidade.builder()
                        .nomePeriodicidade("Semanal")
                        .diasIntervalo(7)
                        .build()
                );

        // 3. Motor de replicação semana a semana
        List<Compromisso> novosCompromissos = new ArrayList<>();
        LocalDate dataAtual = grade.getDataInicioVigencia();
        LocalDate dataFim = grade.getDataFimVigencia();
        LocalDateTime agora = LocalDateTime.now();

        while (!dataAtual.isAfter(dataFim)) {
            DayOfWeek dow = dataAtual.getDayOfWeek();

            if (grade.getItens() != null) {
                for (GradeTerapeuticaItem item : grade.getItens()) {
                    if (item.getDiaSemana() != null && item.getDiaSemana().getDayOfWeek() == dow) {
                        LocalDateTime dataHora = dataAtual.atTime(item.getHoraInicio());

                        // Não replica no passado remoto se já passou da hora atual
                        if (dataHora.isBefore(agora)) {
                            continue;
                        }

                        // Verifica choque de horário com a agenda do profissional
                        StatusCompromisso statusCompromisso = StatusCompromisso.AGENDADO;
                        if (item.getProfissional() != null) {
                            long conflitos = compromissoRepository.countConflitosProfissional(
                                    item.getProfissional().getIdProf(),
                                    dataHora,
                                    null
                            );
                            if (conflitos > 0) {
                                statusCompromisso = StatusCompromisso.PENDENTE_REAGENDAMENTO;
                            }
                        }

                        String titulo = "Atendimento Terapêutico";
                        if (item.getTipoCompromisso() != null) {
                            if (item.getTipoCompromisso().getDescricaoTipo() != null && !item.getTipoCompromisso().getDescricaoTipo().isBlank()) {
                                titulo = item.getTipoCompromisso().getDescricaoTipo();
                            } else if (item.getTipoCompromisso().getNomeTipo() != null && !item.getTipoCompromisso().getNomeTipo().isBlank()) {
                                titulo = item.getTipoCompromisso().getNomeTipo();
                            }
                        }

                        Compromisso compromisso = Compromisso.builder()
                                .tituloCompromisso(titulo)
                                .descricaoCompromisso("Atendimento gerado pela Grade Terapêutica v" + grade.getNumeroVersao())
                                .dataHoraCompromisso(dataHora)
                                .localCompromisso(item.getSala() != null ? item.getSala() : "Clínica")
                                .status(statusCompromisso)
                                .paciente(grade.getPaciente())
                                .tipoCompromisso(item.getTipoCompromisso())
                                .periodicidade(periodicidadeSemanal)
                                .profissional(item.getProfissional())
                                .clinica(grade.getClinica())
                                .criadoPor(usuario)
                                .gradeTerapeutica(grade)
                                .build();

                        novosCompromissos.add(compromisso);
                    }
                }
            }
            dataAtual = dataAtual.plusDays(1);
        }

        if (!novosCompromissos.isEmpty()) {
            compromissoRepository.saveAll(novosCompromissos);
        }

        grade.setStatus(StatusGradeTerapeutica.VIGENTE);
        grade.setDataResposta(LocalDateTime.now());
        gradeTerapeuticaRepository.save(grade);

        return novosCompromissos;
    }

    @Transactional
    public int cancelarCompromissosFuturosPorProfissional(
            String idGrade,
            String idProfissional,
            LocalDate dataCorte,
            String motivo
    ) {
        LocalDateTime dataCorteHora = dataCorte.atStartOfDay();
        List<Compromisso> futuros = compromissoRepository
                .findByGradeTerapeuticaIdGradeTerapeuticaAndDataHoraCompromissoGreaterThanEqualOrderByDataHoraCompromissoAsc(
                        idGrade, dataCorteHora
                );

        List<Compromisso> paraCancelar = new ArrayList<>();
        for (Compromisso c : futuros) {
            if (c.getProfissional() != null && idProfissional.equals(c.getProfissional().getIdProf())) {
                if (c.getStatus() != StatusCompromisso.REALIZADO && c.getStatus() != StatusCompromisso.FALTOU) {
                    c.setStatus(StatusCompromisso.CANCELADO);
                    String nota = motivo != null && !motivo.isBlank() ? motivo : "Cancelado: Profissional descontinuado";
                    c.setDescricaoCompromisso(
                            c.getDescricaoCompromisso() == null ? nota : c.getDescricaoCompromisso() + " | " + nota
                    );
                    paraCancelar.add(c);
                }
            }
        }

        if (!paraCancelar.isEmpty()) {
            compromissoRepository.saveAll(paraCancelar);
        }
        return paraCancelar.size();
    }

    @Transactional
    public GradeTerapeutica criarProposta(SaveGradeTerapeuticaDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado: " + dto.getIdPaciente()));

        Clinica clinica = null;
        if (dto.getIdClinica() != null && !dto.getIdClinica().isBlank()) {
            clinica = clinicaRepository.findById(dto.getIdClinica()).orElse(null);
        }

        GradeTerapeutica grade = GradeTerapeutica.builder()
                .paciente(paciente)
                .clinica(clinica)
                .numeroVersao(1)
                .tipoOrigemVigencia(dto.getTipoOrigemVigencia())
                .idOrigemVigencia(dto.getIdOrigemVigencia())
                .dataInicioVigencia(dto.getDataInicioVigencia())
                .dataFimVigencia(dto.getDataFimVigencia())
                .itens(new ArrayList<>())
                .build();

        if (dto.getItens() != null) {
            for (SaveGradeTerapeuticaItemDTO itemDTO : dto.getItens()) {
                TipoCompromisso tipo = tipoCompromissoRepository.findById(itemDTO.getIdTipoCompromisso())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de compromisso não encontrado: " + itemDTO.getIdTipoCompromisso()));

                Profissional prof = null;
                if (itemDTO.getIdProfissional() != null && !itemDTO.getIdProfissional().isBlank()) {
                    prof = profissionalRepository.findById(itemDTO.getIdProfissional()).orElse(null);
                }

                GradeTerapeuticaItem item = GradeTerapeuticaItem.builder()
                        .diaSemana(itemDTO.getDiaSemana())
                        .horaInicio(itemDTO.getHoraInicio())
                        .horaFim(itemDTO.getHoraFim())
                        .tipoCompromisso(tipo)
                        .profissional(prof)
                        .sala(itemDTO.getSala())
                        .build();

                grade.addItem(item);
            }
        }

        boolean enviar = Boolean.TRUE.equals(dto.getEnviarParaAprovacao());
        return salvarProposta(grade, enviar);
    }

    @Transactional
    public GradeTerapeutica proporAlteracao(String idGradeVigente, ProporAlteracaoGradeDTO dto) {
        GradeTerapeutica gradeVigente = carregarGrade(idGradeVigente);

        GradeTerapeutica novaVersao = GradeTerapeutica.builder()
                .paciente(gradeVigente.getPaciente())
                .clinica(gradeVigente.getClinica())
                .idGradeAnterior(gradeVigente.getIdGradeTerapeutica())
                .numeroVersao(gradeVigente.getNumeroVersao() + 1)
                .tipoOrigemVigencia(gradeVigente.getTipoOrigemVigencia())
                .idOrigemVigencia(gradeVigente.getIdOrigemVigencia())
                .dataInicioVigencia(dto.getDataInicioNovaGrade())
                .dataFimVigencia(gradeVigente.getDataFimVigencia())
                .status(StatusGradeTerapeutica.PENDENTE_APROVACAO)
                .dataEnvio(LocalDateTime.now())
                .itens(new ArrayList<>())
                .build();

        if (dto.getItens() != null) {
            for (SaveGradeTerapeuticaItemDTO itemDTO : dto.getItens()) {
                TipoCompromisso tipo = tipoCompromissoRepository.findById(itemDTO.getIdTipoCompromisso())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de compromisso não encontrado: " + itemDTO.getIdTipoCompromisso()));

                Profissional prof = null;
                if (itemDTO.getIdProfissional() != null && !itemDTO.getIdProfissional().isBlank()) {
                    prof = profissionalRepository.findById(itemDTO.getIdProfissional()).orElse(null);
                }

                novaVersao.addItem(GradeTerapeuticaItem.builder()
                        .diaSemana(itemDTO.getDiaSemana())
                        .horaInicio(itemDTO.getHoraInicio())
                        .horaFim(itemDTO.getHoraFim())
                        .tipoCompromisso(tipo)
                        .profissional(prof)
                        .sala(itemDTO.getSala())
                        .build());
            }
        }

        return gradeTerapeuticaRepository.save(novaVersao);
    }

    @Transactional(readOnly = true)
    public List<Compromisso> listarConflitosPendentes(String idGrade) {
        return compromissoRepository.findByGradeTerapeuticaIdGradeTerapeuticaAndStatusOrderByDataHoraCompromissoAsc(
                idGrade, StatusCompromisso.PENDENTE_REAGENDAMENTO
        );
    }

    @Transactional(readOnly = true)
    public List<Compromisso> listarConflitosPendentesPorPaciente(String idPaciente) {
        return compromissoRepository.findByPacienteIdPacienteAndStatusAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
                idPaciente,
                StatusCompromisso.PENDENTE_REAGENDAMENTO,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusYears(2)
        );
    }
}
