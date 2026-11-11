package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GerarGuiaFaturamentoDTO {

    private String idClinica;

    @NotBlank(message = "O paciente é obrigatório")
    private String idPaciente;

    @NotBlank(message = "O plano de saúde é obrigatório")
    private String idPlanoSaude;

    private String idResponsavel;

    @NotNull(message = "A data inicial da competência é obrigatória")
    private LocalDate competenciaInicio;

    @NotNull(message = "A data final da competência é obrigatória")
    private LocalDate competenciaFim;

    /** Se informado, seleciona apenas os compromissos específicos dentre os elegíveis. */
    private List<String> idsCompromissos;

    private String observacoes;

    private Boolean liberarParaAssinatura;
}
