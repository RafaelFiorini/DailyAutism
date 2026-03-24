package com.clinica.dailyautism.infraestructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SavePlanoSaudeDTO {

    @NotNull(message = "O nome do plano é obrigatório")
    private String nomePlano;

    @NotNull(message = "A operadora é obrigatória")
    private String operadora;

    private String ansReg;
}