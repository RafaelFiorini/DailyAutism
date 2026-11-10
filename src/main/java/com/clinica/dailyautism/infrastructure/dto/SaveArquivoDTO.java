package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveArquivoDTO {

    private String nomeArquivo;

    private String descricao;

    @NotNull(message = "A data do arquivo é obrigatória")
    private LocalDate dataArquivo;

    @NotNull(message = "A extensão do arquivo é obrigatória")
    private String extensaoArquivo;

    private String arquivoBase64;

    private String urlArquivo;

    @NotNull(message = "O ID do paciente é obrigatório")
    private String idPaciente;

    @NotNull(message = "O ID do tipo de arquivo é obrigatório")
    private String idTipoArquivo;

    private String idUser;

    private Boolean compartilhadoComResponsavel;

    private Boolean compartilhadoComClinica;

    private String idClinicaCompartilhada;

    private List<String> idsProfissionaisCompartilhados;
}
