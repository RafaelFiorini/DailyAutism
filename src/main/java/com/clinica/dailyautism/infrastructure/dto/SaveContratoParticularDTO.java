package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.ModalidadeContratoParticular;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveContratoParticularDTO {

    private String idClinica;

    @NotBlank(message = "O paciente é obrigatório")
    private String idPaciente;

    private String idResponsavel;

    private String idPacote;

    /** Usado para multiplicar combos mensais por N meses (ex.: 2 ou 4 meses). */
    private Integer multiplicadorMeses;

    private String descricao;

    private ModalidadeContratoParticular modalidade;

    private LocalDate dataInicio;

    private LocalDate dataFimPrevista;

    private BigDecimal valorCobrado;

    private Boolean pagoNaContratacao;

    private LocalDate dataPagamento;

    private String formaPagamento;

    @Valid
    private List<SaveContratoItemDTO> itens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveContratoItemDTO {
        @NotBlank(message = "A especialidade do item é obrigatória")
        private String idTipoCompromisso;

        @NotNull(message = "A quantidade contratada é obrigatória")
        @Min(value = 1, message = "A quantidade contratada deve ser ao menos 1")
        private Integer quantidadeContratada;
    }
}
