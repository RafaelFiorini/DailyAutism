package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.LancamentoRepasseProfissional;
import com.clinica.dailyautism.domain.entity.StatusLancamentoRepasse;
import com.clinica.dailyautism.domain.entity.TipoOrigemLancamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoRepasseDTO {

    private String idLancamento;
    private String idProfissional;
    private String nomeProfissional;
    private String idClinica;
    private String nomeClinica;
    private String idPaciente;
    private String nomePaciente;
    private String idCompromisso;
    private String tituloCompromisso;
    private LocalDate dataCompetencia;
    private LocalDate dataAtendimento;
    private TipoOrigemLancamento tipoOrigem;
    private BigDecimal valorBruto;
    private BigDecimal percentualRepasse;
    private BigDecimal valorLiquidoRepasse;
    private StatusLancamentoRepasse statusPagamento;
    private LocalDate dataPagamento;
    private String observacoes;

    public static LancamentoRepasseDTO create(LancamentoRepasseProfissional entity) {
        if (entity == null) return null;

        String nomeProf = entity.getProfissional() != null && entity.getProfissional().getPessoaProf() != null
                ? entity.getProfissional().getPessoaProf().getNomePessoa() : null;

        String nomeClinica = entity.getClinica() != null ? entity.getClinica().getNomeClinica() : "Particular Direto";

        String nomePac = entity.getPaciente() != null && entity.getPaciente().getPessoaPaciente() != null
                ? entity.getPaciente().getPessoaPaciente().getNomePessoa() : null;

        String titComp = entity.getCompromisso() != null ? entity.getCompromisso().getTituloCompromisso() : null;

        return LancamentoRepasseDTO.builder()
                .idLancamento(entity.getIdLancamento())
                .idProfissional(entity.getProfissional() != null ? entity.getProfissional().getIdProf() : null)
                .nomeProfissional(nomeProf)
                .idClinica(entity.getClinica() != null ? entity.getClinica().getIdClinica() : null)
                .nomeClinica(nomeClinica)
                .idPaciente(entity.getPaciente() != null ? entity.getPaciente().getIdPaciente() : null)
                .nomePaciente(nomePac)
                .idCompromisso(entity.getCompromisso() != null ? entity.getCompromisso().getIdCompromisso() : null)
                .tituloCompromisso(titComp)
                .dataCompetencia(entity.getDataCompetencia())
                .dataAtendimento(entity.getDataAtendimento())
                .tipoOrigem(entity.getTipoOrigem())
                .valorBruto(entity.getValorBruto())
                .percentualRepasse(entity.getPercentualRepasse())
                .valorLiquidoRepasse(entity.getValorLiquidoRepasse())
                .statusPagamento(entity.getStatusPagamento())
                .dataPagamento(entity.getDataPagamento())
                .observacoes(entity.getObservacoes())
                .build();
    }
}
