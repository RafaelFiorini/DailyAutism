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
public class ProporAlteracaoGradeDTO {

    @NotNull(message = "A data de início da nova grade (data de corte) é obrigatória")
    private LocalDate dataInicioNovaGrade;

    private LocalDate dataFimNovaGrade;

    private Boolean enviarParaAprovacao;

    @NotEmpty(message = "A nova proposta precisa conter ao menos um horário semanal")
    @Valid
    @Builder.Default
    private List<SaveGradeTerapeuticaItemDTO> itens = new ArrayList<>();
}
