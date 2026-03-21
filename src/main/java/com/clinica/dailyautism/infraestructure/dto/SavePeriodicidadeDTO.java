package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Periodicidade;
import lombok.Data;

@Data
public class PeriodicidadeDTO {

    private final String idPeriodicidade;
    private final String nomePeriodicidade;
    private final Integer diasIntervalo;
    private final String descricaoPeriodicidade;

    public static PeriodicidadeDTO create(Periodicidade periodicidade) {
        return new PeriodicidadeDTO(
                periodicidade.getIdPeriodicidade(),
                periodicidade.getNomePeriodicidade(),
                periodicidade.getDiasIntervalo(),
                periodicidade.getDescricaoPeriodicidade()
        );
    }
}