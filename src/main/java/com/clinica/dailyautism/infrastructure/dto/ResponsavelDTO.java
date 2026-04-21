package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Responsavel;
import lombok.Data;

@Data
public class ResponsavelDTO {

    private final String idResponsavel;
    private final String idPessoa;
    private final String nomeResponsavel;

    public static ResponsavelDTO create(Responsavel responsavel) {
        return new ResponsavelDTO(
                responsavel.getIdResponsavel(),
                responsavel.getPessoaResponsavel().getIdPessoa(),
                responsavel.getPessoaResponsavel().getNomePessoa()
        );
    }
}