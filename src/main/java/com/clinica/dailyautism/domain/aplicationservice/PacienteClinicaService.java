// PacienteClinicaService
package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infraestructure.dto.SavePacienteClinicaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PacienteClinicaService {

    private final PacienteClinicaRepository pacienteClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final ClinicaRepository clinicaRepository;

    @Transactional
    public PacienteClinica vincular(SavePacienteClinicaDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));
        Clinica clinica = clinicaRepository.findById(dto.getIdClinica())
                .orElseThrow(() -> new ClinicaNotFoundException(dto.getIdClinica()));

        PacienteClinica vinculo = PacienteClinica.builder()
                .paciente(paciente)
                .clinica(clinica)
                .dataInicioAtendimento(dto.getDataInicioAtendimento())
                .build();
        return pacienteClinicaRepository.save(vinculo);
    }

    public List<PacienteClinica> listarPorPaciente(String idPaciente) {
        return pacienteClinicaRepository.findByPacienteIdPaciente(idPaciente);
    }

    public List<PacienteClinica> listarPorClinica(String idClinica) {
        return pacienteClinicaRepository.findByClinicaIdClinica(idClinica);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        PacienteClinica vinculo = pacienteClinicaRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        pacienteClinicaRepository.save(vinculo);
    }
}