package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnviarLoteGuiasDTO {
    private String idClinica;
    private String idPlanoSaude;
    private List<String> idsGuias;
    private LocalDate dataEnvio;
}
