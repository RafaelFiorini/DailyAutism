package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.CompromissoNotFoundException;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PeriodicidadeNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;
    private final PacienteRepository pacienteRepository;
    private final TipoCompromissoRepository tipoCompromissoRepository;
    private final PeriodicidadeRepository periodicidadeRepository;
    private final UserRepository userRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ResponsavelRepository responsavelRepository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;
    private final ClinicaRepository clinicaRepository;
    private final ClinicaAccessHelper clinicaAccessHelper;
    private final ContratoParticularService contratoParticularService;
    private final ConfiguracaoSistemaService configuracaoSistemaService;
    private final LancamentoRepasseService lancamentoRepasseService;
    private final FuncionarioClinicaRepository funcionarioClinicaRepository;
    private final NotificacaoService notificacaoService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Compromisso createCompromisso(SaveCompromissoDTO saveCompromissoDTO, String emailDoToken) {
        Paciente paciente = pacienteRepository.findById(saveCompromissoDTO.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(saveCompromissoDTO.getIdPaciente()));

        TipoCompromisso tipoCompromisso = tipoCompromissoRepository.findById(saveCompromissoDTO.getIdTipoCompromisso())
                .orElseThrow(() -> new TipoCompromissoNotFoundException("Tipo de compromisso não encontrado: " + saveCompromissoDTO.getIdTipoCompromisso()));

        Periodicidade periodicidade = periodicidadeRepository.findById(saveCompromissoDTO.getIdPeriodicidade())
                .orElseThrow(() -> new PeriodicidadeNotFoundException("Periodicidade não encontrada: " + saveCompromissoDTO.getIdPeriodicidade()));

        User user = userRepository.findByEmailUser(emailDoToken)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + emailDoToken));

        Profissional profissional = null;
        if (saveCompromissoDTO.getIdProfissional() != null && !saveCompromissoDTO.getIdProfissional().isBlank()) {
            profissional = profissionalRepository.findById(saveCompromissoDTO.getIdProfissional().trim())
                    .orElse(null);
        }

        if (profissional != null && saveCompromissoDTO.getDataHoraCompromisso() != null) {
            validarConflitoHorarioProfissional(profissional.getIdProf(), saveCompromissoDTO.getDataHoraCompromisso(), null);
        }

        Clinica clinica = resolverClinicaCriacao(saveCompromissoDTO.getIdClinica(), user, paciente, profissional);

        Compromisso compromisso = Compromisso.builder()
                .tituloCompromisso(saveCompromissoDTO.getTituloCompromisso())
                .descricaoCompromisso(saveCompromissoDTO.getDescricaoCompromisso())
                .dataHoraCompromisso(saveCompromissoDTO.getDataHoraCompromisso())
                .localCompromisso(saveCompromissoDTO.getLocalCompromisso())
                .status(StatusCompromisso.AGUARDANDO_APROVACAO)
                .paciente(paciente)
                .tipoCompromisso(tipoCompromisso)
                .periodicidade(periodicidade)
                .profissional(profissional)
                .clinica(clinica)
                .criadoPor(user)
                .build();

        return compromissoRepository.save(compromisso);
    }

    private Clinica resolverClinicaCriacao(String idClinicaInformado, User user, Paciente paciente, Profissional profissional) {
        if (idClinicaInformado != null && !idClinicaInformado.isBlank()) {
            return clinicaRepository.findById(idClinicaInformado.trim()).orElse(null);
        }
        List<String> clinicasUsuario = clinicaAccessHelper.listarIdsClinicasDoUsuario(user);
        if (!clinicasUsuario.isEmpty()) {
            return clinicaRepository.findById(clinicasUsuario.get(0)).orElse(null);
        }
        Compromisso temp = Compromisso.builder().paciente(paciente).profissional(profissional).build();
        return clinicaAccessHelper.resolverClinicaDoCompromisso(temp);
    }

    public Compromisso loadCompromisso(String compromissoId) {
        return compromissoRepository.findById(compromissoId)
                .orElseThrow(() -> new CompromissoNotFoundException("Compromisso não encontrado: " + compromissoId));
    }

    public List<Compromisso> listCompromissosByPaciente(String pacienteId) {
        return compromissoRepository.findByPacienteIdPacienteOrderByDataHoraCompromissoAsc(pacienteId);
    }

    public List<Compromisso> listCompromissosForUser(String email, String pacienteId, String profissionalId, StatusCompromisso status) {
        List<Compromisso> listaBase;

        if (email == null || email.isBlank()) {
            listaBase = compromissoRepository.findAllByOrderByDataHoraCompromissoDesc();
        } else {
            User user = userRepository.findByEmailUser(email).orElse(null);
            if (user == null) {
                listaBase = compromissoRepository.findAllByOrderByDataHoraCompromissoDesc();
            } else {
                boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                        .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
                if (isAdmin) {
                    listaBase = compromissoRepository.findAllByOrderByDataHoraCompromissoDesc();
                } else {
                    boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                            .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome()) || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome()) || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));

                    if (isClinica) {
                        Set<Compromisso> unicos = new LinkedHashSet<>();

                        // 1. Compromissos dos pacientes da clínica
                        List<Paciente> pacientesPermitidos = pacienteService.listPacientesForUser(email);
                        List<String> pacIds = pacientesPermitidos.stream().map(Paciente::getIdPaciente).toList();
                        if (!pacIds.isEmpty()) {
                            unicos.addAll(compromissoRepository.findByPacienteIdPacienteInOrderByDataHoraCompromissoDesc(pacIds));
                        }

                        // 2. Compromissos dos profissionais da clínica
                        List<Profissional> profissionaisPermitidos = profissionalService.listProfissionaisForUser(email);
                        for (Profissional prof : profissionaisPermitidos) {
                            if (prof.getIdProf() != null) {
                                unicos.addAll(compromissoRepository.findByProfissionalIdProfOrderByDataHoraCompromissoDesc(prof.getIdProf()));
                            }
                        }

                        // 3. Compromissos criados pelo usuário
                        unicos.addAll(compromissoRepository.findByCriadoPorIdUser(user.getIdUser()));

                        listaBase = new ArrayList<>(unicos);
                        listaBase.sort(Comparator.comparing(Compromisso::getDataHoraCompromisso, Comparator.nullsLast(Comparator.naturalOrder())));
                    } else {
                        boolean isProfissional = user.getPerfis() != null && user.getPerfis().stream()
                                .anyMatch(p -> "PROFISSIONAL".equalsIgnoreCase(p.getNome()));

                        if (isProfissional && user.getPessoa() != null && user.getPessoa().getProfissional() != null) {
                            String idProf = user.getPessoa().getProfissional().getIdProf();
                            Set<Compromisso> unicos = new LinkedHashSet<>();
                            unicos.addAll(compromissoRepository.findByProfissionalIdProfOrderByDataHoraCompromissoDesc(idProf));

                            List<Paciente> pacientesPermitidos = pacienteService.listPacientesForUser(email);
                            List<String> pacIds = pacientesPermitidos.stream().map(Paciente::getIdPaciente).toList();
                            if (!pacIds.isEmpty()) {
                                unicos.addAll(compromissoRepository.findByPacienteIdPacienteInOrderByDataHoraCompromissoDesc(pacIds));
                            }
                            unicos.addAll(compromissoRepository.findByCriadoPorIdUser(user.getIdUser()));
                            listaBase = new ArrayList<>(unicos);
                            listaBase.sort(Comparator.comparing(Compromisso::getDataHoraCompromisso, Comparator.nullsLast(Comparator.naturalOrder())));
                        } else {
                            List<Paciente> pacientesPermitidos = pacienteService.listPacientesForUser(email);
                            if (pacientesPermitidos.isEmpty()) {
                                listaBase = List.of();
                            } else {
                                List<String> ids = pacientesPermitidos.stream()
                                        .map(Paciente::getIdPaciente)
                                        .toList();
                                listaBase = compromissoRepository.findByPacienteIdPacienteInOrderByDataHoraCompromissoDesc(ids);
                            }
                        }
                    }
                }
            }
        }

        return listaBase.stream()
                .filter(c -> pacienteId == null || pacienteId.isBlank() || (c.getPaciente() != null && pacienteId.equals(c.getPaciente().getIdPaciente())))
                .filter(c -> profissionalId == null || profissionalId.isBlank() || (c.getProfissional() != null && profissionalId.equals(c.getProfissional().getIdProf())))
                .filter(c -> status == null || c.getStatus() == status)
                .sorted(Comparator.comparing(Compromisso::getDataHoraCompromisso, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public Compromisso updateCompromisso(String compromissoId, SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = loadCompromisso(compromissoId);

        String idProfAtualizado = saveCompromissoDTO.getIdProfissional() != null
                ? (saveCompromissoDTO.getIdProfissional().isBlank() ? null : saveCompromissoDTO.getIdProfissional().trim())
                : (compromisso.getProfissional() != null ? compromisso.getProfissional().getIdProf() : null);

        LocalDateTime dataHoraAtualizada = saveCompromissoDTO.getDataHoraCompromisso() != null
                ? saveCompromissoDTO.getDataHoraCompromisso()
                : compromisso.getDataHoraCompromisso();

        validarConflitoHorarioProfissional(idProfAtualizado, dataHoraAtualizada, compromisso.getIdCompromisso());

        compromisso.setTituloCompromisso(saveCompromissoDTO.getTituloCompromisso());
        compromisso.setDescricaoCompromisso(saveCompromissoDTO.getDescricaoCompromisso());
        compromisso.setDataHoraCompromisso(saveCompromissoDTO.getDataHoraCompromisso());
        compromisso.setLocalCompromisso(saveCompromissoDTO.getLocalCompromisso());

        if (saveCompromissoDTO.getIdPaciente() != null && !saveCompromissoDTO.getIdPaciente().isBlank()) {
            Paciente paciente = pacienteRepository.findById(saveCompromissoDTO.getIdPaciente())
                    .orElseThrow(() -> new PacienteNotFoundException(saveCompromissoDTO.getIdPaciente()));
            compromisso.setPaciente(paciente);
        }

        if (saveCompromissoDTO.getIdTipoCompromisso() != null && !saveCompromissoDTO.getIdTipoCompromisso().isBlank()) {
            TipoCompromisso tipo = tipoCompromissoRepository.findById(saveCompromissoDTO.getIdTipoCompromisso())
                    .orElseThrow(() -> new TipoCompromissoNotFoundException("Tipo de compromisso não encontrado: " + saveCompromissoDTO.getIdTipoCompromisso()));
            compromisso.setTipoCompromisso(tipo);
        }

        if (saveCompromissoDTO.getIdPeriodicidade() != null && !saveCompromissoDTO.getIdPeriodicidade().isBlank()) {
            Periodicidade periodicidade = periodicidadeRepository.findById(saveCompromissoDTO.getIdPeriodicidade())
                    .orElseThrow(() -> new PeriodicidadeNotFoundException("Periodicidade não encontrada: " + saveCompromissoDTO.getIdPeriodicidade()));
            compromisso.setPeriodicidade(periodicidade);
        }

        if (saveCompromissoDTO.getIdProfissional() != null) {
            if (saveCompromissoDTO.getIdProfissional().isBlank()) {
                compromisso.setProfissional(null);
            } else {
                Profissional prof = profissionalRepository.findById(saveCompromissoDTO.getIdProfissional().trim())
                        .orElse(null);
                compromisso.setProfissional(prof);
            }
        }

        return compromissoRepository.save(compromisso);
    }

    @Transactional
    public Compromisso updateStatus(String compromissoId, StatusCompromisso novoStatus) {
        Compromisso compromisso = loadCompromisso(compromissoId);
        validarBaixaSessaoFutura(compromisso, novoStatus);
        StatusCompromisso statusAnterior = compromisso.getStatus();
        compromisso.setStatus(novoStatus);
        Compromisso saved = compromissoRepository.save(compromisso);
        contratoParticularService.processarAlteracaoStatusCompromisso(saved, statusAnterior, novoStatus);

        if (novoStatus == StatusCompromisso.REALIZADO) {
            lancamentoRepasseService.gerarOuAtualizarLancamentoPorCompromisso(saved);
        } else if (novoStatus == StatusCompromisso.CANCELADO || novoStatus == StatusCompromisso.FALTOU || novoStatus == StatusCompromisso.REJEITADO) {
            lancamentoRepasseService.cancelarLancamentoPorCompromisso(saved.getIdCompromisso());
        }
        return saved;
    }

    @Transactional
    public Compromisso cancelarCompromissoComJustificativa(String compromissoId, String motivo, String emailUsuario) {
        if (motivo == null || motivo.trim().length() < 5) {
            throw new IllegalArgumentException("Informe uma justificativa com pelo menos 5 caracteres para cancelar a sessão.");
        }

        Compromisso compromisso = loadCompromisso(compromissoId);

        if (compromisso.getStatus() == StatusCompromisso.REALIZADO) {
            throw new IllegalStateException("Não é possível cancelar uma sessão que já foi realizada.");
        }
        if (compromisso.getStatus() == StatusCompromisso.CANCELADO) {
            throw new IllegalStateException("Esta sessão já está cancelada.");
        }

        User usuario = userRepository.findByEmailUserIgnoreCase(emailUsuario != null ? emailUsuario.trim() : "")
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + emailUsuario));

        boolean isAdmin = clinicaAccessHelper.isAdmin(usuario);
        boolean isStaffClinica = false;
        if (clinicaAccessHelper.isStaffClinica(usuario) && compromisso.getClinica() != null) {
            List<String> clinicasUsuario = clinicaAccessHelper.listarIdsClinicasDoUsuario(usuario);
            isStaffClinica = clinicasUsuario.contains(compromisso.getClinica().getIdClinica());
        }

        boolean isProfissionalSessao = false;
        if (compromisso.getProfissional() != null && usuario.getPessoa() != null) {
            Profissional profUser = usuario.getPessoa().getProfissional() != null
                    ? usuario.getPessoa().getProfissional()
                    : profissionalRepository.findByPessoaProf(usuario.getPessoa()).orElse(null);
            if (profUser != null && profUser.getIdProf().equals(compromisso.getProfissional().getIdProf())) {
                isProfissionalSessao = true;
            }
        }

        boolean isResponsavelPaciente = false;
        if (compromisso.getPaciente() != null && usuario.getPessoa() != null) {
            Responsavel resp = responsavelRepository.findByPessoaResponsavel(usuario.getPessoa()).orElse(null);
            if (resp != null && resp.getPacientes() != null) {
                String idPac = compromisso.getPaciente().getIdPaciente();
                isResponsavelPaciente = resp.getPacientes().stream()
                        .anyMatch(p -> p != null && idPac.equals(p.getIdPaciente()));
            }
        }

        if (!isAdmin && !isStaffClinica && !isProfissionalSessao && !isResponsavelPaciente) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não tem permissão para cancelar esta sessão."
            );
        }

        String perfilCancelador = isResponsavelPaciente && !isProfissionalSessao && !isStaffClinica && !isAdmin
                ? "RESPONSAVEL"
                : isProfissionalSessao && !isStaffClinica && !isAdmin
                ? "PROFISSIONAL"
                : isStaffClinica
                ? "CLINICA"
                : "ADMIN";

        String motivoLimpo = motivo.trim();
        StatusCompromisso statusAnterior = compromisso.getStatus();
        compromisso.setStatus(StatusCompromisso.CANCELADO);
        compromisso.setMotivoCancelamento(motivoLimpo);
        compromisso.setCanceladoPorNome(usuario.getNomeUser());
        compromisso.setCanceladoPorPerfil(perfilCancelador);
        compromisso.setCanceladoEm(LocalDateTime.now());

        Compromisso saved = compromissoRepository.save(compromisso);
        contratoParticularService.processarAlteracaoStatusCompromisso(saved, statusAnterior, StatusCompromisso.CANCELADO);
        lancamentoRepasseService.cancelarLancamentoPorCompromisso(saved.getIdCompromisso());

        // Notificar todos os envolvidos (profissional, responsáveis e gestão da clínica), exceto quem cancelou
        notificarEnvolvidosCancelamento(saved, usuario, perfilCancelador, motivoLimpo);

        if (auditoriaService != null) {
            auditoriaService.registrarAcaoAsync(
                    usuario,
                    CategoriaAuditoria.CLINICO,
                    AcaoAuditoria.EXCLUSAO,
                    "Compromisso",
                    saved.getIdCompromisso(),
                    "Sessão cancelada por " + perfilCancelador + " (" + usuario.getNomeUser() + "). Motivo: " + motivoLimpo,
                    null,
                    null,
                    null
            );
        }

        return saved;
    }

    private void notificarEnvolvidosCancelamento(Compromisso compromisso, User autor, String perfilAutor, String motivo) {
        if (notificacaoService == null) {
            return;
        }
        List<User> destinatarios = new ArrayList<>();

        // 1. Profissional da sessão
        if (compromisso.getProfissional() != null && compromisso.getProfissional().getPessoaProf() != null) {
            userRepository.findByPessoaIdAnyAtivo(compromisso.getProfissional().getPessoaProf().getIdPessoa())
                    .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                    .ifPresent(destinatarios::add);
        }

        // 2. Responsáveis pelo paciente
        if (compromisso.getPaciente() != null && compromisso.getPaciente().getResponsaveis() != null) {
            for (Responsavel resp : compromisso.getPaciente().getResponsaveis()) {
                if (resp != null && resp.getPessoaResponsavel() != null) {
                    userRepository.findByPessoaIdAnyAtivo(resp.getPessoaResponsavel().getIdPessoa())
                            .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                            .ifPresent(destinatarios::add);
                }
            }
        }

        // 3. Funcionários / Administradores da clínica da sessão
        if (compromisso.getClinica() != null && funcionarioClinicaRepository != null) {
            List<FuncionarioClinica> funcs = funcionarioClinicaRepository.findByClinicaIdClinica(compromisso.getClinica().getIdClinica());
            for (FuncionarioClinica fc : funcs) {
                if (fc != null && fc.getPessoa() != null && (fc.getAtivo() == null || fc.getAtivo())) {
                    userRepository.findByPessoaIdAnyAtivo(fc.getPessoa().getIdPessoa())
                            .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                            .ifPresent(destinatarios::add);
                }
            }
        }

        String nomePaciente = (compromisso.getPaciente() != null && compromisso.getPaciente().getPessoaPaciente() != null)
                ? compromisso.getPaciente().getPessoaPaciente().getNomePessoa()
                : (compromisso.getPaciente() != null ? compromisso.getPaciente().getApelidoPaciente() : "Paciente");
        String especialidade = compromisso.getTipoCompromisso() != null
                ? compromisso.getTipoCompromisso().getNomeTipo()
                : compromisso.getTituloCompromisso();
        String dataFormatada = compromisso.getDataHoraCompromisso() != null
                ? compromisso.getDataHoraCompromisso().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))
                : "";

        String papelTexto = switch (perfilAutor) {
            case "RESPONSAVEL" -> "pelo responsável";
            case "PROFISSIONAL" -> "pelo profissional";
            case "CLINICA" -> "pela clínica";
            default -> "pelo administrador";
        };

        String titulo = "Sessão cancelada " + papelTexto;
        String mensagem = String.format(
                "%s (%s) em %s foi cancelada por %s. Motivo: %s",
                nomePaciente,
                especialidade,
                dataFormatada,
                autor.getNomeUser(),
                motivo
        );

        notificacaoService.notificarUsuarios(
                destinatarios,
                autor.getIdUser(),
                titulo,
                mensagem,
                TipoNotificacao.CANCELAMENTO_SESSAO,
                compromisso.getIdCompromisso()
        );
    }

//    @Transactional
//    public Compromisso aprovarCompromisso(String compromissoId) {
//        Compromisso compromisso = loadCompromisso(compromissoId);
//        compromisso.setStatus(StatusCompromisso.AGENDADO);
//        return compromissoRepository.save(compromisso);
//    }

    @Transactional
    public void deleteCompromisso(String compromissoId) {
        Compromisso compromisso = compromissoRepository.findById(compromissoId)
                .orElseThrow(() -> new CompromissoNotFoundException("Compromisso não encontrado: " + compromissoId));
        compromisso.desativar();
        compromissoRepository.save(compromisso);
    }
    @Transactional
    public Compromisso rejeitarCompromisso(String compromissoId) {
        Compromisso compromisso = loadCompromisso(compromissoId);
        compromisso.setStatus(StatusCompromisso.REJEITADO);
        return compromissoRepository.save(compromisso);
    }

    public List<AgendaItemDTO> agendaProfissional(String profissionalId, LocalDateTime de, LocalDateTime ate) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado: " + profissionalId));

        Set<Compromisso> unicos = new LinkedHashSet<>();
        unicos.addAll(compromissoRepository.findByProfissionalIdProfAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(profissionalId, de, ate));

        if (profissional.getPacientes() != null) {
            List<String> idsPacientes = profissional.getPacientes().stream()
                    .map(ProfissionalPaciente::getPaciente)
                    .filter(Objects::nonNull)
                    .map(Paciente::getIdPaciente)
                    .toList();

            if (!idsPacientes.isEmpty()) {
                unicos.addAll(compromissoRepository
                        .findByPacienteIdPacienteInAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(idsPacientes, de, ate));
            }
        }

        return unicos.stream()
                .sorted(Comparator.comparing(Compromisso::getDataHoraCompromisso))
                .map(AgendaItemDTO::create)
                .toList();
    }
    public AgendaResponsavelDTO agendaResponsavel(String responsavelId, LocalDateTime de, LocalDateTime ate) {
        Responsavel responsavel = responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado: " + responsavelId));

        List<Paciente> pacientes = responsavel.getPacientes();

        List<PacienteResumoDTO> pacientesDTO = pacientes.stream()
                .map(PacienteResumoDTO::create)
                .toList();

        if (pacientes.isEmpty()) {
            return new AgendaResponsavelDTO(pacientesDTO, List.of());
        }

        List<String> idsPacientes = pacientes.stream()
                .map(Paciente::getIdPaciente)
                .toList();

        List<AgendaItemDTO> compromissos = compromissoRepository
                .findByPacienteIdPacienteInAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(idsPacientes, de, ate)
                .stream()
                .map(AgendaItemDTO::create)
                .toList();

        return new AgendaResponsavelDTO(pacientesDTO, compromissos);
    }
    @Transactional
    public List<Compromisso> aprovarCompromisso(String compromissoId, AprovarCompromissoDTO dto) {
        Compromisso original = loadCompromisso(compromissoId);

        if (original.getDataHoraCompromisso() != null
                && original.getDataHoraCompromisso().toLocalDate().isBefore(LocalDate.now())) {
            String dataBr = original.getDataHoraCompromisso().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new IllegalArgumentException(
                    "Não é possível aprovar um agendamento com data anterior a hoje (" + dataBr
                            + "). Avise a clínica para cadastrar uma nova data."
            );
        }

        Paciente paciente = original.getPaciente();
        PacientePlanoSaude guiaAtiva = resolverGuiaAtiva(paciente);
        LocalDate vencimentoPadrao = guiaAtiva != null
                ? guiaAtiva.getVencimentoLiberacao()
                : (paciente != null ? paciente.getVencimentoLiberacao() : null);
        LocalDate dataFim = dto.getDataFim() != null ? dto.getDataFim() : vencimentoPadrao;

        if (dataFim != null
                && original.getDataHoraCompromisso() != null
                && dataFim.isBefore(original.getDataHoraCompromisso().toLocalDate())) {
            String vencBr = dataFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new IllegalArgumentException(
                    "A guia ou limite de sessões do paciente venceu em " + vencBr
                            + ". Entre em contato com a clínica para atualizar antes de aprovar."
            );
        }

        original.setStatus(StatusCompromisso.AGENDADO);
        compromissoRepository.save(original);

        int intervalo = original.getPeriodicidade() != null && original.getPeriodicidade().getDiasIntervalo() != null
                ? original.getPeriodicidade().getDiasIntervalo()
                : 0;
        if (intervalo <= 0) {
            return List.of(original);
        }

        TipoLiberacaoGuia tipoAtivo = guiaAtiva != null
                ? guiaAtiva.getTipoLiberacao()
                : (paciente != null ? paciente.getTipoLiberacao() : null);
        Integer qtdSessoesAtiva = guiaAtiva != null
                ? guiaAtiva.getQuantidadeSessoesLiberadas()
                : (paciente != null ? paciente.getQuantidadeSessoesLiberadas() : null);
        Integer qtdLiberada = (tipoAtivo == TipoLiberacaoGuia.QUANTIDADE
                && qtdSessoesAtiva != null
                && qtdSessoesAtiva > 0)
                ? qtdSessoesAtiva
                : null;

        // Se não há nem data fim nem quantidade de sessões, aprova apenas o original
        if (dataFim == null && (qtdLiberada == null || qtdLiberada <= 1)) {
            return List.of(original);
        }

        List<Compromisso> replicas = new ArrayList<>();
        LocalDateTime proxima = original.getDataHoraCompromisso().plusDays(intervalo);
        int sessoesGeradas = 1; // original já é a primeira sessão
        int limiteSeguranca = 365; // proteção contra loops infinitos caso data muito distante

        while (replicas.size() < limiteSeguranca) {
            if (qtdLiberada != null && sessoesGeradas >= qtdLiberada) {
                break;
            }
            if (dataFim != null && proxima.toLocalDate().isAfter(dataFim)) {
                break;
            }
            if (qtdLiberada == null && dataFim == null) {
                break;
            }

            Compromisso replica = Compromisso.builder()
                    .tituloCompromisso(original.getTituloCompromisso())
                    .descricaoCompromisso(original.getDescricaoCompromisso())
                    .dataHoraCompromisso(proxima)
                    .localCompromisso(original.getLocalCompromisso())
                    .status(StatusCompromisso.AGENDADO)
                    .paciente(original.getPaciente())
                    .tipoCompromisso(original.getTipoCompromisso())
                    .periodicidade(original.getPeriodicidade())
                    .profissional(original.getProfissional())
                    .clinica(original.getClinica())
                    .criadoPor(original.getCriadoPor())
                    .build();
            replicas.add(replica);
            sessoesGeradas++;
            proxima = proxima.plusDays(intervalo);
        }

        if (!replicas.isEmpty()) {
            compromissoRepository.saveAll(replicas);
        }

        List<Compromisso> resultado = new ArrayList<>();
        resultado.add(original);
        resultado.addAll(replicas);
        return resultado;
    }

    private void validarConflitoHorarioProfissional(String idProfissional, LocalDateTime dataHoraCompromisso, String idCompromissoIgnorado) {
        if (idProfissional == null || idProfissional.isBlank() || dataHoraCompromisso == null) {
            return;
        }

        long conflitos = compromissoRepository.countConflitosProfissional(
                idProfissional.trim(),
                dataHoraCompromisso,
                idCompromissoIgnorado
        );

        if (conflitos > 0) {
            throw new IllegalArgumentException(
                    "O profissional já possui um compromisso agendado neste mesmo dia e horário."
            );
        }
    }

    private void validarBaixaSessaoFutura(Compromisso compromisso, StatusCompromisso novoStatus) {
        if (novoStatus == null || compromisso == null || compromisso.getDataHoraCompromisso() == null) {
            return;
        }

        // Cancelamento ou rejeição são explicitamente permitidos antes da data
        if (novoStatus == StatusCompromisso.CANCELADO || novoStatus == StatusCompromisso.REJEITADO) {
            return;
        }

        // Se o status for de execução/baixa da sessão (REALIZADO ou FALTOU)
        if (novoStatus == StatusCompromisso.REALIZADO || novoStatus == StatusCompromisso.FALTOU) {
            LocalDate dataSessao = compromisso.getDataHoraCompromisso().toLocalDate();
            LocalDate hoje = LocalDate.now();

            if (dataSessao.isAfter(hoje) && configuracaoSistemaService.isTravaBaixaSessoesFuturasAtiva()) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                throw new IllegalArgumentException(
                        "Não é permitido dar baixa em sessões futuras antes da data do atendimento (Data da sessão: "
                                + dataSessao.format(fmt)
                                + "). A trava pode ser desativada pelo administrador do sistema para realização de testes."
                );
            }
        }
    }

    private PacientePlanoSaude resolverGuiaAtiva(Paciente paciente) {
        if (paciente == null || paciente.getPlanos() == null || paciente.getPlanos().isEmpty()) {
            return null;
        }
        return paciente.getPlanos().stream()
                .filter(p -> p.getAtivo() == null || p.getAtivo())
                .filter(p -> p.getVencimentoLiberacao() != null || (p.getQuantidadeSessoesLiberadas() != null && p.getQuantidadeSessoesLiberadas() > 0))
                .max(Comparator.comparing(
                        p -> p.getVencimentoLiberacao() != null ? p.getVencimentoLiberacao() : LocalDate.MIN
                ))
                .orElse(null);
    }
}