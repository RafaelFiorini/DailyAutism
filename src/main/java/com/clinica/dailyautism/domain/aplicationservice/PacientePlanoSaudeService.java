// PacientePlanoSaudeService
package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infraestructure.dto.SavePacientePlanoSaudeDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PacientePlanoSaudeService {

    private final PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    private final PacienteRepository pacienteRepository;
    private final PlanoSaudeRepository planoSaudeRepository;

    @Transactional
    public PacientePlanoSaude vincular(SavePacientePlanoSaudeDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));
        PlanoSaude plano = planoSaudeRepository.findById(dto.getIdPlanoSaude())
                .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()));

        PacientePlanoSaude vinculo = PacientePlanoSaude.builder()
                .paciente(paciente)
                .planoSaude(plano)
                .numeroCarteirinha(dto.getNumeroCarteirinha())
                .validadeCarteirinha(dto.getValidadeCarteirinha())
                .vencimentoLiberacao(dto.getVencimentoLiberacao())
                .build();
        return pacientePlanoSaudeRepository.save(vinculo);
    }

    public List<PacientePlanoSaude> listarPorPaciente(String idPaciente) {
        return pacientePlanoSaudeRepository.findByPacienteIdPaciente(idPaciente);
    }

    public List<PacientePlanoSaude> listarPorPlano(String idPlanoSaude) {
        return pacientePlanoSaudeRepository.findByPlanoSaudeIdPlanoSaude(idPlanoSaude);
    }

    @Transactional
    public PacientePlanoSaude atualizar(String idVinculo, SavePacientePlanoSaudeDTO dto) {
        PacientePlanoSaude vinculo = pacientePlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.setNumeroCarteirinha(dto.getNumeroCarteirinha());
        vinculo.setValidadeCarteirinha(dto.getValidadeCarteirinha());
        vinculo.setVencimentoLiberacao(dto.getVencimentoLiberacao());
        return pacientePlanoSaudeRepository.save(vinculo);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        PacientePlanoSaude vinculo = pacientePlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        pacientePlanoSaudeRepository.save(vinculo);
    }
}