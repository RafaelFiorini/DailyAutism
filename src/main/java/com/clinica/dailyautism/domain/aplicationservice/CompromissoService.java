package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.CompromissoNotFoundException;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PeriodicidadeNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.AgendaItemDTO;
import com.clinica.dailyautism.infrastructure.dto.AgendaResponsavelDTO;
import com.clinica.dailyautism.infrastructure.dto.PacienteResumoDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveCompromissoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional
    public Compromisso createCompromisso(SaveCompromissoDTO saveCompromissoDTO) {
        Paciente paciente = pacienteRepository.findById(saveCompromissoDTO.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(saveCompromissoDTO.getIdPaciente()));

        TipoCompromisso tipoCompromisso = tipoCompromissoRepository.findById(saveCompromissoDTO.getIdTipoCompromisso())
                .orElseThrow(() -> new TipoCompromissoNotFoundException("Tipo de compromisso não encontrado: " + saveCompromissoDTO.getIdTipoCompromisso()));

        Periodicidade periodicidade = periodicidadeRepository.findById(saveCompromissoDTO.getIdPeriodicidade())
                .orElseThrow(() -> new PeriodicidadeNotFoundException("Periodicidade não encontrada: " + saveCompromissoDTO.getIdPeriodicidade()));

        User user = userRepository.findById(saveCompromissoDTO.getIdUser())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + saveCompromissoDTO.getIdUser()));

        Compromisso compromisso = Compromisso.builder()
                .tituloCompromisso(saveCompromissoDTO.getTituloCompromisso())
                .descricaoCompromisso(saveCompromissoDTO.getDescricaoCompromisso())
                .dataHoraCompromisso(saveCompromissoDTO.getDataHoraCompromisso())
                .localCompromisso(saveCompromissoDTO.getLocalCompromisso())
                .status(StatusCompromisso.AGENDADO) // sempre começa como não aprovado
                .paciente(paciente)
                .tipoCompromisso(tipoCompromisso)
                .periodicidade(periodicidade)
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

    @Transactional
    public Compromisso updateCompromisso(String compromissoId, SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = loadCompromisso(compromissoId);
        compromisso.setTituloCompromisso(saveCompromissoDTO.getTituloCompromisso());
        compromisso.setDescricaoCompromisso(saveCompromissoDTO.getDescricaoCompromisso());
        compromisso.setDataHoraCompromisso(saveCompromissoDTO.getDataHoraCompromisso());
        compromisso.setLocalCompromisso(saveCompromissoDTO.getLocalCompromisso());
        return compromissoRepository.save(compromisso);
    }

    @Transactional
    public Compromisso aprovarCompromisso(String compromissoId) {
        Compromisso compromisso = loadCompromisso(compromissoId);
        compromisso.setStatus(StatusCompromisso.AGENDADO);
        return compromissoRepository.save(compromisso);
    }

    @Transactional
    public void deleteCompromisso(String compromissoId) {
        Compromisso compromisso = compromissoRepository.findById(compromissoId)
                .orElseThrow(() -> new CompromissoNotFoundException("Compromisso não encontrado: " + compromissoId));
        compromisso.desativar();
        compromissoRepository.save(compromisso);
    }
//    public List<AgendaItemDTO> agendaResponsavel(String responsavelId, LocalDateTime de, LocalDateTime ate) {
//        Responsavel responsavel = responsavelRepository.findById(responsavelId)
//                .orElseThrow(() -> new RuntimeException("Responsável não encontrado: " + responsavelId));
//
//        List<String> idsPacientes = responsavel.getPacientes().stream()
//                .map(Paciente::getIdPaciente)
//                .toList();
//
//        return compromissoRepository
//                .findByPacienteIdPacienteInAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(idsPacientes, de, ate)
//                .stream()
//                .map(AgendaItemDTO::create)
//                .toList();
//    }

    public List<AgendaItemDTO> agendaProfissional(String profissionalId, LocalDateTime de, LocalDateTime ate) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado: " + profissionalId));

        List<String> idsPacientes = profissional.getPacientes().stream()
                .map(ProfissionalPaciente::getPaciente)
                .map(Paciente::getIdPaciente)
                .toList();

        return compromissoRepository
                .findByPacienteIdPacienteInAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(idsPacientes, de, ate)
                .stream()
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
}