package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarProfissionalGradeDTO {

    @NotBlank(message = "O ID do profissional é obrigatório")
    private String idProfissional;

    @NotNull(message = "A data de corte para cancelamento é obrigatória")
    private LocalDate dataCorte;

    private String motivo;
}
