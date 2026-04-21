package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.TipoArquivo;
import lombok.Data;

@Data
public class TipoArquivoDTO {

    private final String idTipoArquivo;
    private final String nomeTipo;
    private final String descricaoTipo;

    public static TipoArquivoDTO create(TipoArquivo tipoArquivo) {
        return new TipoArquivoDTO(
                tipoArquivo.getIdTipoArquivo(),
                tipoArquivo.getNomeTipo(),
                tipoArquivo.getDescricaoTipo()
        );
    }
}