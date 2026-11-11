package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssinarGuiaDTO {

    @NotBlank(message = "Informe sua senha para assinar a guia eletronicamente")
    private String senhaConfirmacao;
}
