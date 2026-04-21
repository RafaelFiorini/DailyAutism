// SavePacientePlanoSaudeDTO
package com.clinica.dailyautism.infrastructure.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SavePacientePlanoSaudeDTO {
    private String idPaciente;
    private String idPlanoSaude;
    private String numeroCarteirinha;
    private LocalDate validadeCarteirinha;
    private LocalDate vencimentoLiberacao;
}