package com.clinica.dailyautism.infrastructure.dto;


import lombok.Data;
import java.util.List;

@Data
public class AgendaResponsavelDTO {
    private final List<PacienteResumoDTO> pacientes;
    private final List<AgendaItemDTO> compromissos;
}