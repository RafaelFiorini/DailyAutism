package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoElegivelDTO {
    private String idCompromisso;
    private String tituloCompromisso;
    private LocalDateTime dataHoraAtendimento;
    private String idTipoCompromisso;
    private String nomeTipoCompromisso;
    private String nomeProfissional;
    private String conselhoProfissional;
    private BigDecimal valorUnitarioConvenio;
}
