package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveTipoCompromissoDTO {

    @NotNull(message = "O nome do tipo é obrigatório")
    private final String nomeTipo;

    private final String descricaoTipo;
}