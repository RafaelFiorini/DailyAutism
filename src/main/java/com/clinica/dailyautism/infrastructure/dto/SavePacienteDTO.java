package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SavePacienteDTO {

    @NotNull(message = "O ID da pessoa é obrigatório")
    private final String idPessoa;

    private final String apelidoPaciente;

    private final boolean isResponsavel;

    // planoPaciente e vencimentoLiberacaoPlanoPaciente removidos
    // agora vivem em PacientePlanoSaude
}