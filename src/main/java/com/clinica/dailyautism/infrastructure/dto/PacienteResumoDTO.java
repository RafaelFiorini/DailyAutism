package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Paciente;
import lombok.Data;

@Data
public class PacienteResumoDTO {
    private final String idPaciente;
    private final String nomePaciente;
    private final String apelidoPaciente;

    public static PacienteResumoDTO create(Paciente p) {
        return new PacienteResumoDTO(
                p.getIdPaciente(),
                p.getPessoaPaciente().getNomePessoa(),
                p.getApelidoPaciente()
        );
    }
}