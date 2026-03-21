package com.clinica.dailyautism.infraestructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SaveArquivoDTO {

    @NotNull(message = "O nome do arquivo é obrigatório")
    private final String nomeArquivo;

    @NotNull(message = "A data do arquivo é obrigatória")
    private final LocalDate dataArquivo;

    @NotNull(message = "A extensão do arquivo é obrigatória")
    private final String extensaoArquivo;

    private final String arquivoBase64;

    private final String urlArquivo;

    @NotNull(message = "O ID do paciente é obrigatório")
    private final String idPaciente;

    @NotNull(message = "O ID do tipo de arquivo é obrigatório")
    private final String idTipoArquivo;

    @NotNull(message = "O ID do usuário é obrigatório")
    private final String idUser;
}