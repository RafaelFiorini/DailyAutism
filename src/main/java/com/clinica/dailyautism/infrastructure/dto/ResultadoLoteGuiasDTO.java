package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoLoteGuiasDTO {
    private String idPlanoSaude;
    private String nomePlano;
    private int quantidadeGuias;
    private BigDecimal valorTotal;
    private LocalDate dataEnvio;
    private LocalDate dataPrevisaoPagamento;
    private int prazoPagamentoDias;
    private List<String> idsGuiasEnviadas;
}
