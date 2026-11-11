package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.ClinicaPlanoSaude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicaPlanoSaudeDTO {
    private String idClinicaPlanoSaude;
    private String idClinica;
    private String nomeClinica;
    private String idPlanoSaude;
    private String nomePlano;
    private String operadora;
    private Integer diaCorteEnvio;
    private Integer prazoPagamentoDias;

    public static ClinicaPlanoSaudeDTO create(ClinicaPlanoSaude cps) {
        if (cps == null) return null;
        return ClinicaPlanoSaudeDTO.builder()
                .idClinicaPlanoSaude(cps.getIdClinicaPlanoSaude())
                .idClinica(cps.getClinica() != null ? cps.getClinica().getIdClinica() : null)
                .nomeClinica(cps.getClinica() != null ? cps.getClinica().getNomeClinica() : null)
                .idPlanoSaude(cps.getPlanoSaude() != null ? cps.getPlanoSaude().getIdPlanoSaude() : null)
                .nomePlano(cps.getPlanoSaude() != null ? cps.getPlanoSaude().getNomePlano() : null)
                .operadora(cps.getPlanoSaude() != null ? cps.getPlanoSaude().getOperadora() : null)
                .diaCorteEnvio(cps.getDiaCorteEnvio())
                .prazoPagamentoDias(cps.getPrazoPagamentoDias() != null ? cps.getPrazoPagamentoDias() : 30)
                .build();
    }
}
