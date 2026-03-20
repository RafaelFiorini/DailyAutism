package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Paciente;
import lombok.Data;

@Data
public class PacienteDTO {

    private final String idPaciente;
    private final String idPessoa;
    private final String nomePessoa;
    private final String apelidoPaciente;
    private final String planoPaciente;
    private final boolean isResponsavel;

    public static PacienteDTO create(Paciente paciente) {
        return new PacienteDTO(
                paciente.getIdPaciente(),
                paciente.getPessoaPaciente().getIdPessoa(),
                paciente.getPessoaPaciente().getNomePessoa(),
                paciente.getApelidoPaciente(),
                paciente.getPlanoPaciente(),
                paciente.isResponsavel()
        );
    }
}