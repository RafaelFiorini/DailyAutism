package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurarValorParticularDTO {

    @NotNull(message = "Valor da sessão particular é obrigatório")
    private BigDecimal valorSessao;
}
