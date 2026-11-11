package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.TabelaPrecoClinica;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabelaPrecoDTO {
    private String idTabelaPreco;
    private String idClinica;
    private String nomeClinica;
    private String idTipoCompromisso;
    private String nomeTipoCompromisso;
    private String idPlanoSaude;
    private String nomePlanoSaude;
    private BigDecimal valorSessao;
    private boolean particular;

    public static TabelaPrecoDTO create(TabelaPrecoClinica entity) {
        if (entity == null) return null;
        return TabelaPrecoDTO.builder()
                .idTabelaPreco(entity.getIdTabelaPreco())
                .idClinica(entity.getClinica() != null ? entity.getClinica().getIdClinica() : null)
                .nomeClinica(entity.getClinica() != null ? entity.getClinica().getNomeClinica() : null)
                .idTipoCompromisso(entity.getTipoCompromisso() != null ? entity.getTipoCompromisso().getIdTipoCompromisso() : null)
                .nomeTipoCompromisso(entity.getTipoCompromisso() != null ? entity.getTipoCompromisso().getNomeTipo() : null)
                .idPlanoSaude(entity.getPlanoSaude() != null ? entity.getPlanoSaude().getIdPlanoSaude() : null)
                .nomePlanoSaude(entity.getPlanoSaude() != null ? entity.getPlanoSaude().getNomePlano() : "Particular")
                .valorSessao(entity.getValorSessao())
                .particular(entity.getPlanoSaude() == null)
                .build();
    }
}
