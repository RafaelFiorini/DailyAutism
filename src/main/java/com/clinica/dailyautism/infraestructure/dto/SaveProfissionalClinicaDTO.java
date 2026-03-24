// SaveProfissionalClinicaDTO
package com.clinica.dailyautism.infraestructure.dto;

import lombok.Data;

@Data
public class SaveProfissionalClinicaDTO {
    private String idProf;
    private String idClinica;
    private String diasAtendimento;
    private String horariosAtendimento;
}