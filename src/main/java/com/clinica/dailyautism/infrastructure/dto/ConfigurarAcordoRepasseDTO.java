package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.TipoRepasseProfissional;
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
public class ConfigurarAcordoRepasseDTO {

    @NotNull(message = "Tipo de repasse é obrigatório")
    private TipoRepasseProfissional tipoRepasse;

    @NotNull(message = "Valor de repasse é obrigatório")
    private BigDecimal valorRepasse;
}
