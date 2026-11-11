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
    private final ClinicaRepository clinicaRepository;
    private final ClinicaAccessHelper clinicaAccessHelper;
    private final ContratoParticularService contratoParticularService;

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
                        listaBase.sort(Comparator.comparing(Compromisso::getDataHoraCompromisso, Comparator.nullsLast(Comparator.reverseOrder())));
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

        return listaBase.stream()
                .filter(c -> pacienteId == null || pacienteId.isBlank() || (c.getPaciente() != null && pacienteId.equals(c.getPaciente().getIdPaciente())))
                .filter(c -> profissionalId == null || profissionalId.isBlank() || (c.getProfissional() != null && profissionalId.equals(c.getProfissional().getIdProf())))
                .filter(c -> status == null || c.getStatus() == status)
                .toList();
    }

    @Transactional
    public Compromisso updateCompromisso(String compromissoId, SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = loadCompromisso(compromissoId);
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
        StatusCompromisso statusAnterior = compromisso.getStatus();
        compromisso.setStatus(novoStatus);
        Compromisso saved = compromissoRepository.save(compromisso);
        contratoParticularService.processarAlteracaoStatusCompromisso(saved, statusAnterior, novoStatus);
        return saved;
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