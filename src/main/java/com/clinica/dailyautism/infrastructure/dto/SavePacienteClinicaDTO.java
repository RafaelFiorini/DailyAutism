// SavePacienteClinicaDTO
package com.clinica.dailyautism.infrastructure.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SavePacienteClinicaDTO {
    private String idPaciente;
    private String idClinica;
    private LocalDate dataInicioAtendimento;
}