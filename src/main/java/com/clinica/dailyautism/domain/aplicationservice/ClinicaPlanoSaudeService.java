// ClinicaPlanoSaudeService
package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infraestructure.dto.SaveClinicaPlanoSaudeDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ClinicaPlanoSaudeService {

    private final ClinicaPlanoSaudeRepository clinicaPlanoSaudeRepository;
    private final ClinicaRepository clinicaRepository;
    private final PlanoSaudeRepository planoSaudeRepository;

    @Transactional
    public ClinicaPlanoSaude vincular(SaveClinicaPlanoSaudeDTO dto) {
        Clinica clinica = clinicaRepository.findById(dto.getIdClinica())
                .orElseThrow(() -> new ClinicaNotFoundException(dto.getIdClinica()));
        PlanoSaude plano = planoSaudeRepository.findById(dto.getIdPlanoSaude())
                .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()));

        ClinicaPlanoSaude vinculo = ClinicaPlanoSaude.builder()
                .clinica(clinica)
                .planoSaude(plano)
                .build();
        return clinicaPlanoSaudeRepository.save(vinculo);
    }

    public List<ClinicaPlanoSaude> listarPorClinica(String idClinica) {
        return clinicaPlanoSaudeRepository.findByClinicaIdClinica(idClinica);
    }

    public List<ClinicaPlanoSaude> listarPorPlano(String idPlanoSaude) {
        return clinicaPlanoSaudeRepository.findByPlanoSaudeIdPlanoSaude(idPlanoSaude);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        ClinicaPlanoSaude vinculo = clinicaPlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        clinicaPlanoSaudeRepository.save(vinculo);
    }
}