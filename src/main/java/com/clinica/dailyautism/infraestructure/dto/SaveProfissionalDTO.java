package com.clinica.dailyautism.infraestructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveProfissionalDTO {

    @NotNull(message = "O ID da pessoa é obrigatório")
    private final String idPessoa;

    private final String conselhoProf;

   // private final boolean ativoProf;
}