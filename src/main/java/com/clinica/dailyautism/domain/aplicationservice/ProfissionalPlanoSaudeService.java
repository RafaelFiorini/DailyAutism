// ProfissionalPlanoSaudeService
package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infraestructure.dto.SaveProfissionalPlanoSaudeDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfissionalPlanoSaudeService {

    private final ProfissionalPlanoSaudeRepository profissionalPlanoSaudeRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PlanoSaudeRepository planoSaudeRepository;

    @Transactional
    public ProfissionalPlanoSaude vincular(SaveProfissionalPlanoSaudeDTO dto) {
        Profissional profissional = profissionalRepository.findById(dto.getIdProf())
                .orElseThrow(() -> new ProfissionalNotFoundException(dto.getIdProf()));
        PlanoSaude plano = planoSaudeRepository.findById(dto.getIdPlanoSaude())
                .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()));

        ProfissionalPlanoSaude vinculo = ProfissionalPlanoSaude.builder()
                .profissional(profissional)
                .planoSaude(plano)
                .numeroCredenciamento(dto.getNumeroCredenciamento())
                .build();
        return profissionalPlanoSaudeRepository.save(vinculo);
    }

    public List<ProfissionalPlanoSaude> listarPorProfissional(String idProf) {
        return profissionalPlanoSaudeRepository.findByProfissionalIdProf(idProf);
    }

    public List<ProfissionalPlanoSaude> listarPorPlano(String idPlanoSaude) {
        return profissionalPlanoSaudeRepository.findByPlanoSaudeIdPlanoSaude(idPlanoSaude);
    }

    @Transactional
    public ProfissionalPlanoSaude atualizar(String idVinculo, SaveProfissionalPlanoSaudeDTO dto) {
        ProfissionalPlanoSaude vinculo = profissionalPlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.setNumeroCredenciamento(dto.getNumeroCredenciamento());
        return profissionalPlanoSaudeRepository.save(vinculo);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        ProfissionalPlanoSaude vinculo = profissionalPlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        profissionalPlanoSaudeRepository.save(vinculo);
    }
}