package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompartilharArquivoDTO {

    private Boolean compartilhadoComResponsavel;
    private Boolean compartilhadoComClinica;
    private String idClinicaCompartilhada;
    private List<String> idsProfissionaisCompartilhados;
}
