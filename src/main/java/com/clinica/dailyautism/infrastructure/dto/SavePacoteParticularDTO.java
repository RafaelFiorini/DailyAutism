package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavePacoteParticularDTO {

    private String idClinica;

    @NotBlank(message = "O nome do pacote é obrigatório")
    private String nomePacote;

    private String descricao;

    @NotNull(message = "O valor total do pacote é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor total do pacote deve ser maior que zero")
    private BigDecimal valorTotalPacote;

    private Integer validadeMesesSugerida;

    @Valid
    @NotEmpty(message = "Informe ao menos uma especialidade e quantidade de sessões para o pacote")
    private List<SavePacoteItemDTO> itens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePacoteItemDTO {
        @NotBlank(message = "A especialidade do item é obrigatória")
        private String idTipoCompromisso;

        @NotNull(message = "A quantidade de sessões é obrigatória")
        @Min(value = 1, message = "A quantidade de sessões deve ser ao menos 1")
        private Integer quantidadeSessoes;
    }
}
