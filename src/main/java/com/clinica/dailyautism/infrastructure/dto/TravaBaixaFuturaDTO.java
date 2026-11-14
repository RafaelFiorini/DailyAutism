package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravaBaixaFuturaDTO {
    private boolean ativa;
    private String mensagem;
}
