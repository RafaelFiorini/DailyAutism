package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveGradeTerapeuticaItemDTO {

    @NotNull(message = "O dia da semana é obrigatório")
    private DiaSemana diaSemana;

    @NotNull(message = "A hora de início é obrigatória")
    private LocalTime horaInicio;

    @NotNull(message = "A hora de término é obrigatória")
    private LocalTime horaFim;

    @NotNull(message = "O tipo de compromisso / especialidade é obrigatório")
    private String idTipoCompromisso;

    private String idProfissional;

    private String idPeriodicidade;

    private String sala;
}
