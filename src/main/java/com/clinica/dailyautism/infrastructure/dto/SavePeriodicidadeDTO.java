package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SavePeriodicidadeDTO {

    @NotNull(message = "O nome da periodicidade é obrigatório")
    private final String nomePeriodicidade;

    //@NotNull(message = "O intervalo em dias é obrigatório")
    private final Integer diasIntervalo;

    private final String descricaoPeriodicidade;
}