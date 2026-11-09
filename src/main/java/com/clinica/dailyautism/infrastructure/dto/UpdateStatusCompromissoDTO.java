package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.StatusCompromisso;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusCompromissoDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusCompromisso status;
}
