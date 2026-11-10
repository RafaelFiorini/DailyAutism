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
                .criadoPor(user)
                .build();

        return compromissoRepository.save(compromisso);
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
        compromisso.setStatus(novoStatus);
        return compromissoRepository.save(compromisso);
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
        original.setStatus(StatusCompromisso.AGENDADO);
        compromissoRepository.save(original);

        // descobre a data fim
        LocalDate dataFim = resolverDataFim(original, dto.getDataFim());

        if (dataFim == null) {
            return List.of(original); // sem replicação
        }

        // replica
        List<Compromisso> replicas = new ArrayList<>();
        int intervalo = original.getPeriodicidade().getDiasIntervalo();
        if (intervalo == 0) {
            return List.of(original);
        }
        LocalDateTime proxima = original.getDataHoraCompromisso().plusDays(intervalo);

        while (!proxima.toLocalDate().isAfter(dataFim)) {
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
                    .criadoPor(original.getCriadoPor())
                    .build();
            replicas.add(replica);
            proxima = proxima.plusDays(intervalo);
        }

        compromissoRepository.saveAll(replicas);
        return replicas;
    }

    private LocalDate resolverDataFim(Compromisso compromisso, LocalDate dataFimManual) {
        // tenta pegar do plano de saúde do paciente primeiro
        if (compromisso.getPaciente().getPlanos() != null &&
                !compromisso.getPaciente().getPlanos().isEmpty()) {
            return compromisso.getPaciente().getPlanos()
                    .stream()
                    .map(PacientePlanoSaude::getVencimentoLiberacao)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(dataFimManual);
        }
        return dataFimManual;
    }
}