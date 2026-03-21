package com.clinica.dailyautism.infraestructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveTipoArquivoDTO {

    @NotNull(message = "O nome do tipo é obrigatório")
    private final String nomeTipo;

    private final String descricaoTipo;
}