package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SugestaoGradeDTO {

    @NotBlank(message = "A justificativa ou contraproposta é obrigatória")
    private String justificativa;
}
