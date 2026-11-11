package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveTabelaPrecoDTO {

    private String idClinica;

    @NotBlank(message = "A especialidade é obrigatória")
    private String idTipoCompromisso;

    /** Nulo ou vazio indica preço Particular padrão da clínica. */
    private String idPlanoSaude;

    @NotNull(message = "O valor da sessão é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor da sessão deve ser maior que zero")
    private BigDecimal valorSessao;
}
