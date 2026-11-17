package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveGradeTerapeuticaDTO {

    @NotNull(message = "O paciente é obrigatório")
    private String idPaciente;

    private String idClinica;

    private String tipoOrigemVigencia; // GUIA_CONVENIO ou CONTRATO_PARTICULAR

    private String idOrigemVigencia;

    @NotNull(message = "A data de início de vigência é obrigatória")
    private LocalDate dataInicioVigencia;

    @NotNull(message = "A data final de vigência é obrigatória")
    private LocalDate dataFimVigencia;

    private Boolean enviarParaAprovacao;

    @NotEmpty(message = "A grade terapêutica precisa conter ao menos um horário semanal")
    @Valid
    @Builder.Default
    private List<SaveGradeTerapeuticaItemDTO> itens = new ArrayList<>();
}
