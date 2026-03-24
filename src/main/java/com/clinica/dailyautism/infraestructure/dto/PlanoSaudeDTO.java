package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.PlanoSaude;
import lombok.Data;

@Data
public class PlanoSaudeDTO {

    private final String idPlanoSaude;
    private final String nomePlano;
    private final String operadora;
    private final String ansReg;

    public static PlanoSaudeDTO create(PlanoSaude planoSaude) {
        return new PlanoSaudeDTO(
                planoSaude.getIdPlanoSaude(),
                planoSaude.getNomePlano(),
                planoSaude.getOperadora(),
                planoSaude.getAnsReg()
        );
    }
}