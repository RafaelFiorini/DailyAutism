package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.PlanoSaude;
import com.clinica.dailyautism.domain.exception.PlanoSaudeNotFoundException;
import com.clinica.dailyautism.domain.repository.PlanoSaudeRepository;
import com.clinica.dailyautism.infraestructure.dto.SavePlanoSaudeDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PlanoSaudeService {

    private final PlanoSaudeRepository planoSaudeRepository;

    @Transactional
    public PlanoSaude createPlanoSaude(SavePlanoSaudeDTO dto) {
        PlanoSaude plano = PlanoSaude.builder()
                .nomePlano(dto.getNomePlano())
                .operadora(dto.getOperadora())
                .ansReg(dto.getAnsReg())
                .build();
        return planoSaudeRepository.save(plano);
    }

    public PlanoSaude loadPlanoSaude(String planoId) {
        return planoSaudeRepository.findById(planoId)
                .orElseThrow(() -> new PlanoSaudeNotFoundException(planoId));
    }

    public List<PlanoSaude> listPlanosSaude() {
        return planoSaudeRepository.findAll();
    }

    @Transactional
    public PlanoSaude updatePlanoSaude(String planoId, SavePlanoSaudeDTO dto) {
        PlanoSaude plano = loadPlanoSaude(planoId);
        plano.setNomePlano(dto.getNomePlano());
        plano.setOperadora(dto.getOperadora());
        plano.setAnsReg(dto.getAnsReg());
        return planoSaudeRepository.save(plano);
    }

    @Transactional
    public void deletePlanoSaude(String planoId) {
        PlanoSaude plano = planoSaudeRepository.findById(planoId)
                .orElseThrow(() -> new PlanoSaudeNotFoundException(planoId));
        plano.desativar();
        planoSaudeRepository.save(plano);
    }
}