package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.PacientePlanoSaude;
import com.clinica.dailyautism.domain.entity.TipoLiberacaoGuia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacientePlanoSaudeDTO {

    private String idPacientePlanoSaude;
    private String idPaciente;
    private String nomePaciente;
    private String idPlanoSaude;
    private String nomePlano;
    private String operadora;
    private String numeroCarteirinha;
    private LocalDate validadeCarteirinha;
    private String numeroGuia;
    private TipoLiberacaoGuia tipoLiberacao;
    private LocalDate dataInicioLiberacao;
    private LocalDate vencimentoLiberacao;
    private Integer quantidadeSessoesLiberadas;
    private Long diasParaVencimento;
    private String statusGuia;

    public static Long calcularDiasParaVencimento(LocalDate vencimento) {
        if (vencimento == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), vencimento);
    }

    public static String calcularStatusGuia(LocalDate vencimento, Integer quantidadeSessoes) {
        if (vencimento != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), vencimento);
            if (dias < 0) {
                return "VENCIDA";
            }
            if (dias <= 15) {
                return "VENCENDO_EM_BREVE";
            }
            return "ATIVA";
        }
        if (quantidadeSessoes != null && quantidadeSessoes > 0) {
            return "ATIVA";
        }
        return "SEM_GUIA";
    }

    public static PacientePlanoSaudeDTO create(PacientePlanoSaude pps) {
        if (pps == null) return null;

        Long dias = calcularDiasParaVencimento(pps.getVencimentoLiberacao());
        String status = calcularStatusGuia(pps.getVencimentoLiberacao(), pps.getQuantidadeSessoesLiberadas());

        String idPac = pps.getPaciente() != null ? pps.getPaciente().getIdPaciente() : null;
        String nomePac = (pps.getPaciente() != null && pps.getPaciente().getPessoaPaciente() != null)
                ? pps.getPaciente().getPessoaPaciente().getNomePessoa()
                : (pps.getPaciente() != null ? pps.getPaciente().getApelidoPaciente() : null);

        String idPlano = pps.getPlanoSaude() != null ? pps.getPlanoSaude().getIdPlanoSaude() : null;
        String nomePlano = pps.getPlanoSaude() != null ? pps.getPlanoSaude().getNomePlano() : null;
        String operadora = pps.getPlanoSaude() != null ? pps.getPlanoSaude().getOperadora() : null;

        return PacientePlanoSaudeDTO.builder()
                .idPacientePlanoSaude(pps.getIdPacientePlanoSaude())
                .idPaciente(idPac)
                .nomePaciente(nomePac)
                .idPlanoSaude(idPlano)
                .nomePlano(nomePlano)
                .operadora(operadora)
                .numeroCarteirinha(pps.getNumeroCarteirinha())
                .validadeCarteirinha(pps.getValidadeCarteirinha())
                .numeroGuia(pps.getNumeroGuia())
                .tipoLiberacao(pps.getTipoLiberacao() != null ? pps.getTipoLiberacao() : TipoLiberacaoGuia.PERIODO)
                .dataInicioLiberacao(pps.getDataInicioLiberacao())
                .vencimentoLiberacao(pps.getVencimentoLiberacao())
                .quantidadeSessoesLiberadas(pps.getQuantidadeSessoesLiberadas())
                .diasParaVencimento(dias)
                .statusGuia(status)
                .build();
    }
}
