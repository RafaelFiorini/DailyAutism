package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelarCompromissoDTO {

    @NotBlank(message = "Informe o motivo/justificativa do cancelamento.")
    @Size(min = 5, max = 500, message = "A justificativa deve ter entre 5 e 500 caracteres.")
    private String motivo;
}
