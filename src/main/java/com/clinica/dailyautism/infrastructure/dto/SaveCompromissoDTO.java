package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SaveCompromissoDTO {

    @NotNull(message = "O título do compromisso é obrigatório")
    private final String tituloCompromisso;

    private final String descricaoCompromisso;

    @NotNull(message = "A data e hora do compromisso são obrigatórias")
    private final LocalDateTime dataHoraCompromisso;

    private final String localCompromisso;

    @NotNull(message = "O ID do paciente é obrigatório")
    private final String idPaciente;

    @NotNull(message = "O ID do tipo de compromisso é obrigatório")
    private final String idTipoCompromisso;

    @NotNull(message = "O ID da periodicidade é obrigatório")
    private final String idPeriodicidade;

    @NotNull(message = "O ID do usuário é obrigatório")
    private final String idUser;
}