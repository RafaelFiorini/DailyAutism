package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Arquivo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArquivoDownloadDTO {
    private String idArquivo;
    private String nomeArquivo;
    private String extensaoArquivo;
    private String arquivoBase64;

    public static ArquivoDownloadDTO create(Arquivo arquivo) {
        return new ArquivoDownloadDTO(
                arquivo.getIdArquivo(),
                arquivo.getNomeArquivo(),
                arquivo.getExtensaoArquivo(),
                arquivo.getArquivoBase64()
        );
    }
}
