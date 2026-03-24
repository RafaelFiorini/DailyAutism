package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.ProfissionalPaciente;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.ProfissionalNotFoundException;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalPacienteRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalRepository;
import com.clinica.dailyautism.infraestructure.dto.SaveProfissionalPacienteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfissionalPacienteService {

    private final ProfissionalPacienteRepository profissionalPacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PacienteRepository pacienteRepository;

    @Transactional
    public ProfissionalPaciente vincular(SaveProfissionalPacienteDTO dto) {
        Profissional profissional = profissionalRepository.findById(dto.getIdProf())
                .orElseThrow(() -> new ProfissionalNotFoundException(dto.getIdProf()));

        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));

        if (profissionalPacienteRepository.existsByProfissionalIdProfAndPacienteIdPaciente(
                dto.getIdProf(), dto.getIdPaciente())) {
            throw new RuntimeException("Vínculo já existe entre este profissional e paciente");
        }

        ProfissionalPaciente vinculo = ProfissionalPaciente.builder()
                .profissional(profissional)
                .paciente(paciente)
                .anamnese(dto.isAnamnese())
                .build();

        return profissionalPacienteRepository.save(vinculo);
    }

    public List<ProfissionalPaciente> listarPorProfissional(String idProf) {
        return profissionalPacienteRepository.findByProfissionalIdProf(idProf);
    }

    public List<ProfissionalPaciente> listarPorPaciente(String idPaciente) {
        return profissionalPacienteRepository.findByPacienteIdPaciente(idPaciente);
    }

    @Transactional
    public ProfissionalPaciente atualizarAnamnese(String idVinculo, boolean anamnese) {
        ProfissionalPaciente vinculo = profissionalPacienteRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.setAnamnese(anamnese);
        return profissionalPacienteRepository.save(vinculo);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        ProfissionalPaciente vinculo = profissionalPacienteRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        profissionalPacienteRepository.save(vinculo);
    }
}