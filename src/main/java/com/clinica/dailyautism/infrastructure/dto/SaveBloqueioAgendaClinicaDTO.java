package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.TipoBloqueioAgenda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveBloqueioAgendaClinicaDTO {

    private String idClinica;

    @NotBlank(message = "Informe o título do feriado ou recesso.")
    private String titulo;

    @Builder.Default
    private TipoBloqueioAgenda tipo = TipoBloqueioAgenda.FERIADO;

    @NotNull(message = "Informe a data de início.")
    private LocalDate dataInicio;

    @NotNull(message = "Informe a data de término.")
    private LocalDate dataFim;

    /** Opcional: se não informado, bloqueia o dia inteiro. */
    private LocalTime horaInicio;
    private LocalTime horaFim;

    private String observacao;

    /** Se true, suspende (cancela) automaticamente os agendamentos existentes no período preservando o saldo de sessões. */
    @Builder.Default
    private Boolean suspenderAgendamentosExistentes = true;
}
