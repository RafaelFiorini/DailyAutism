package com.clinica.dailyautism.infraestructure.dto;

import lombok.Data;

@Data
public class SaveProfissionalPacienteDTO {
    private String idProf;
    private String idPaciente;
    private boolean anamnese;
}