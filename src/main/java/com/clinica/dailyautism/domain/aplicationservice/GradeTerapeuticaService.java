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
    private final AuditoriaService auditoriaService;

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

        GradeTerapeutica salva = gradeTerapeuticaRepository.save(grade);
        auditoriaService.registrarAcaoAsync(
                null,
                "Sistema/Clínica",
                "clinica@dailyautism.com",
                "SECRETARIA",
                salva.getClinica() != null ? salva.getClinica().getIdClinica() : null,
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.CRIACAO,
                "GradeTerapeutica",
                salva.getIdGradeTerapeutica(),
                "Criou/atualizou proposta de grade terapêutica v" + salva.getNumeroVersao() + " (status: " + salva.getStatus() + ")",
                null,
                null,
                null,
                null
        );
        return salva;
    }

    @Transactional
    public GradeTerapeutica enviarParaAprovacao(String idGrade) {
        GradeTerapeutica grade = carregarGrade(idGrade);
        if (grade.getStatus() != StatusGradeTerapeutica.RASCUNHO && grade.getStatus() != StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL) {
            throw new IllegalStateException("A grade só pode ser enviada se estiver em rascunho ou com sugestão de alteração.");
        }
        grade.setStatus(StatusGradeTerapeutica.PENDENTE_APROVACAO);
        grade.setDataEnvio(LocalDateTime.now());
        GradeTerapeutica salva = gradeTerapeuticaRepository.save(grade);
        auditoriaService.registrarAcaoAsync(
                null,
                "Secretaria",
                "secretaria@dailyautism.com",
                "SECRETARIA",
                salva.getClinica() != null ? salva.getClinica().getIdClinica() : null,
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.EDICAO,
                "GradeTerapeutica",
                salva.getIdGradeTerapeutica(),
                "Enviou grade terapêutica v" + salva.getNumeroVersao() + " para aprovação do responsável",
                null,
                null,
                null,
                null
        );
        return salva;
    }

    @Transactional
    public GradeTerapeutica registrarSugestaoResponsavel(String idGrade, String justificativa) {
        GradeTerapeutica grade = gradeTerapeuticaRepository.findById(idGrade)
                .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + idGrade));

        grade.setStatus(StatusGradeTerapeutica.SUGESTAO_RESPONSAVEL);
        grade.setJustificativaSugestaoResponsavel(justificativa);
        grade.setDataResposta(LocalDateTime.now());
        GradeTerapeutica salva = gradeTerapeuticaRepository.save(grade);
        auditoriaService.registrarAcaoAsync(
                null,
                "Responsável (Família)",
                "responsavel@dapp.dailyautism.com",
                "RESPONSAVEL",
                salva.getClinica() != null ? salva.getClinica().getIdClinica() : null,
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.EDICAO,
                "GradeTerapeutica",
                salva.getIdGradeTerapeutica(),
                "Responsável enviou contraproposta na grade v" + salva.getNumeroVersao() + ": " + justificativa,
                null,
                null,
                null,
                null
        );
        return salva;
    }

    @Transactional
    public GradeTerapeutica rejeitar(String idGrade, String motivo) {
        GradeTerapeutica grade = carregarGrade(idGrade);
        grade.setStatus(StatusGradeTerapeutica.REJEITADA);
        grade.setMotivoRejeicao(motivo);
        grade.setDataResposta(LocalDateTime.now());
        GradeTerapeutica salva = gradeTerapeuticaRepository.save(grade);
        auditoriaService.registrarAcaoAsync(
                null,
                "Responsável (Família)",
                "responsavel@dapp.dailyautism.com",
                "RESPONSAVEL",
                salva.getClinica() != null ? salva.getClinica().getIdClinica() : null,
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.REJEICAO,
                "GradeTerapeutica",
                salva.getIdGradeTerapeutica(),
                "Responsável rejeitou a proposta de grade v" + salva.getNumeroVersao() + ". Motivo: " + motivo,
                null,
                null,
                null,
                null
        );
        return salva;
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

        // 2. Resolver periodicidade semanal padrão de fallback
        Periodicidade periodicidadeSemanal = periodicidadeRepository.findAll().stream()
                .filter(p -> p.getNomePeriodicidade() != null && p.getNomePeriodicidade().toLowerCase().contains("semanal"))
                .findFirst()
                .orElseGet(() -> Periodicidade.builder()
                        .nomePeriodicidade("Semanal")
                        .diasIntervalo(7)
                        .build()
                );

        // 3. Motor de replicação por item de grade respeitando sua periodicidade específica
        List<Compromisso> novosCompromissos = new ArrayList<>();
        LocalDate dataInicioVigencia = grade.getDataInicioVigencia();
        LocalDate dataFimVigencia = grade.getDataFimVigencia();
        LocalDateTime agora = LocalDateTime.now();

        if (grade.getItens() != null) {
            for (GradeTerapeuticaItem item : grade.getItens()) {
                if (item.getDiaSemana() == null) continue;

                // 3.1 Encontra a primeira data válida para este dia da semana a partir de dataInicioVigencia
                LocalDate dataOcorrencia = dataInicioVigencia;
                while (dataOcorrencia.getDayOfWeek() != item.getDiaSemana().getDayOfWeek() && !dataOcorrencia.isAfter(dataFimVigencia)) {
                    dataOcorrencia = dataOcorrencia.plusDays(1);
                }

                // 3.2 Determina o intervalo de avanço da periodicidade do item
                Periodicidade periodicidadeItem = item.getPeriodicidade() != null ? item.getPeriodicidade() : periodicidadeSemanal;
                String nomePer = periodicidadeItem.getNomePeriodicidade() != null ? periodicidadeItem.getNomePeriodicidade().toLowerCase() : "";
                Integer diasIntervalo = periodicidadeItem.getDiasIntervalo();

                boolean isQuinzenal = nomePer.contains("quinzenal") || (diasIntervalo != null && diasIntervalo == 14);
                boolean isMensal = nomePer.contains("mensal") || (diasIntervalo != null && diasIntervalo >= 28);
                boolean isAvulsa = nomePer.contains("avulsa") || (diasIntervalo != null && diasIntervalo == 0);

                // 3.3 Replica as sessões do item até o fim da vigência
                while (!dataOcorrencia.isAfter(dataFimVigencia)) {
                    LocalDateTime dataHora = dataOcorrencia.atTime(item.getHoraInicio());

                    // Não replica no passado remoto
                    if (!dataHora.isBefore(agora)) {
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
                                .descricaoCompromisso("Atendimento gerado pela Grade Terapêutica v" + grade.getNumeroVersao() + " (" + periodicidadeItem.getNomePeriodicidade() + ")")
                                .dataHoraCompromisso(dataHora)
                                .localCompromisso(item.getSala() != null ? item.getSala() : "Clínica")
                                .status(statusCompromisso)
                                .paciente(grade.getPaciente())
                                .tipoCompromisso(item.getTipoCompromisso())
                                .periodicidade(periodicidadeItem)
                                .profissional(item.getProfissional())
                                .clinica(grade.getClinica())
                                .criadoPor(usuario)
                                .gradeTerapeutica(grade)
                                .build();

                        novosCompromissos.add(compromisso);
                    }

                    // Avança conforme a periodicidade configurada
                    if (isAvulsa) {
                        break;
                    } else if (isQuinzenal) {
                        dataOcorrencia = dataOcorrencia.plusWeeks(2); // De 15 em 15 dias (14 dias, mantendo o mesmo dia da semana)
                    } else if (isMensal) {
                        dataOcorrencia = dataOcorrencia.plusWeeks(4); // Mensal (4 semanas, mantendo o mesmo dia da semana)
                    } else {
                        // Semanal (ou intervalo customizado em dias)
                        int salto = (diasIntervalo != null && diasIntervalo > 0) ? diasIntervalo : 7;
                        dataOcorrencia = dataOcorrencia.plusDays(salto);
                    }
                }
            }
        }

        if (!novosCompromissos.isEmpty()) {
            compromissoRepository.saveAll(novosCompromissos);
        }

        grade.setStatus(StatusGradeTerapeutica.VIGENTE);
        grade.setDataResposta(LocalDateTime.now());
        gradeTerapeuticaRepository.save(grade);

        auditoriaService.registrarAcaoAsync(
                usuario.getIdUser(),
                usuario.getNomeUser(),
                usuario.getEmailUser(),
                "RESPONSAVEL",
                grade.getClinica() != null ? grade.getClinica().getIdClinica() : null,
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.APROVACAO,
                "GradeTerapeutica",
                grade.getIdGradeTerapeutica(),
                "Aprovou grade terapêutica v" + grade.getNumeroVersao() + " com replicação de " + novosCompromissos.size() + " compromissos até " + grade.getDataFimVigencia(),
                null,
                null,
                null,
                null
        );

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

        auditoriaService.registrarAcaoAsync(
                null,
                "Secretaria / Gestão",
                "clinica@dailyautism.com",
                "SECRETARIA",
                null,
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.CANCELAMENTO,
                "GradeTerapeutica",
                idGrade,
                "Cancelou " + paraCancelar.size() + " compromissos futuros do profissional a partir de " + dataCorte + " (" + motivo + ")",
                null,
                null,
                null,
                null
        );

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

                Periodicidade periodicidade = null;
                if (itemDTO.getIdPeriodicidade() != null && !itemDTO.getIdPeriodicidade().isBlank()) {
                    periodicidade = periodicidadeRepository.findById(itemDTO.getIdPeriodicidade()).orElse(null);
                }

                GradeTerapeuticaItem item = GradeTerapeuticaItem.builder()
                        .diaSemana(itemDTO.getDiaSemana())
                        .horaInicio(itemDTO.getHoraInicio())
                        .horaFim(itemDTO.getHoraFim())
                        .tipoCompromisso(tipo)
                        .profissional(prof)
                        .periodicidade(periodicidade)
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

                Periodicidade periodicidade = null;
                if (itemDTO.getIdPeriodicidade() != null && !itemDTO.getIdPeriodicidade().isBlank()) {
                    periodicidade = periodicidadeRepository.findById(itemDTO.getIdPeriodicidade()).orElse(null);
                }

                novaVersao.addItem(GradeTerapeuticaItem.builder()
                        .diaSemana(itemDTO.getDiaSemana())
                        .horaInicio(itemDTO.getHoraInicio())
                        .horaFim(itemDTO.getHoraFim())
                        .tipoCompromisso(tipo)
                        .profissional(prof)
                        .periodicidade(periodicidade)
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
