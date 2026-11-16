package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoFinanceiroProfissionalDTO {

    private String competencia; // YYYY-MM
    private BigDecimal totalFaturado;
    private BigDecimal totalRecebido;
    private BigDecimal totalPendente;
    private int quantidadeTotalSessoes;
    private BigDecimal valorSessaoParticularPadrao;
    private List<TotalPorOrigemDTO> totaisPorOrigem;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalPorOrigemDTO {
        private String idOrigem; // idClinica ou "PARTICULAR"
        private String nomeOrigem;
        private boolean isParticular;
        private BigDecimal total;
        private BigDecimal totalRecebido;
        private BigDecimal totalPendente;
        private int quantidadeSessoes;
    }
}
