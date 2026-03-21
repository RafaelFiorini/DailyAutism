package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.TipoCompromisso;
import lombok.Data;

@Data
public class TipoCompromissoDTO {

    private final String idTipoCompromisso;
    private final String nomeTipo;
    private final String descricaoTipo;

    public static TipoCompromissoDTO create(TipoCompromisso tipoCompromisso) {
        return new TipoCompromissoDTO(
                tipoCompromisso.getIdTipoCompromisso(),
                tipoCompromisso.getNomeTipo(),
                tipoCompromisso.getDescricaoTipo()
        );
    }
}