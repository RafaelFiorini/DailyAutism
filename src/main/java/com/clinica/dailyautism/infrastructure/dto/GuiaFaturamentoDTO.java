package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.GuiaFaturamentoConvenio;
import com.clinica.dailyautism.domain.entity.GuiaFaturamentoItem;
import com.clinica.dailyautism.domain.entity.ProvedorAssinatura;
import com.clinica.dailyautism.domain.entity.StatusGuiaFaturamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaFaturamentoDTO {

    private String idGuiaFaturamento;
    private String idClinica;
    private String nomeClinica;
    private String idPaciente;
    private String nomePaciente;
    private String idPlanoSaude;
    private String nomePlanoSaude;
    private String idResponsavel;
    private String nomeResponsavel;
    private String numeroLoteGuia;
    private String numeroCarteirinha;
    private String numeroGuiaOperadora;
    private LocalDate competenciaInicio;
    private LocalDate competenciaFim;
    private BigDecimal valorTotal;
    private StatusGuiaFaturamento status;
    private ProvedorAssinatura provedorAssinatura;
    private String assinadoPorNome;
    private String assinadoPorCpf;
    private LocalDateTime dataHoraAssinatura;
    private String codigoVerificador;
    private String hashPdfOriginal;
    private String hashPdfAssinado;
    private LocalDate dataEnvioConvenio;
    private LocalDate dataPrevisaoPagamento;
    private LocalDate dataPagamento;
    private String observacoes;
    private List<GuiaFaturamentoItemDTO> itens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuiaFaturamentoItemDTO {
        private String idGuiaItem;
        private String idCompromisso;
        private LocalDateTime dataHoraAtendimento;
        private String tipoCompromissoNome;
        private String profissionalNome;
        private String conselhoProfissional;
        private BigDecimal valorUnitarioConvenio;

        public static GuiaFaturamentoItemDTO create(GuiaFaturamentoItem item) {
            return GuiaFaturamentoItemDTO.builder()
                    .idGuiaItem(item.getIdGuiaItem())
                    .idCompromisso(item.getCompromisso() != null ? item.getCompromisso().getIdCompromisso() : null)
                    .dataHoraAtendimento(item.getDataHoraAtendimento())
                    .tipoCompromissoNome(item.getTipoCompromissoNome())
                    .profissionalNome(item.getProfissionalNome())
                    .conselhoProfissional(item.getConselhoProfissional())
                    .valorUnitarioConvenio(item.getValorUnitarioConvenio())
                    .build();
        }
    }

    public static GuiaFaturamentoDTO create(GuiaFaturamentoConvenio guia) {
        if (guia == null) return null;

        List<GuiaFaturamentoItemDTO> itensDto = guia.getItens() == null
                ? List.of()
                : guia.getItens().stream()
                        .filter(i -> i.getAtivo() == null || i.getAtivo())
                        .sorted(Comparator.comparing(GuiaFaturamentoItem::getDataHoraAtendimento))
                        .map(GuiaFaturamentoItemDTO::create)
                        .toList();

        String nomePac = (guia.getPaciente() != null && guia.getPaciente().getPessoaPaciente() != null)
                ? guia.getPaciente().getPessoaPaciente().getNomePessoa()
                : (guia.getPaciente() != null ? guia.getPaciente().getApelidoPaciente() : null);

        String nomeResp = (guia.getResponsavel() != null && guia.getResponsavel().getPessoaResponsavel() != null)
                ? guia.getResponsavel().getPessoaResponsavel().getNomePessoa()
                : null;

        return GuiaFaturamentoDTO.builder()
                .idGuiaFaturamento(guia.getIdGuiaFaturamento())
                .idClinica(guia.getClinica() != null ? guia.getClinica().getIdClinica() : null)
                .nomeClinica(guia.getClinica() != null ? guia.getClinica().getNomeClinica() : null)
                .idPaciente(guia.getPaciente() != null ? guia.getPaciente().getIdPaciente() : null)
                .nomePaciente(nomePac)
                .idPlanoSaude(guia.getPlanoSaude() != null ? guia.getPlanoSaude().getIdPlanoSaude() : null)
                .nomePlanoSaude(guia.getPlanoSaude() != null ? guia.getPlanoSaude().getNomePlano() : null)
                .idResponsavel(guia.getResponsavel() != null ? guia.getResponsavel().getIdResponsavel() : null)
                .nomeResponsavel(nomeResp)
                .numeroLoteGuia(guia.getNumeroLoteGuia())
                .numeroCarteirinha(guia.getNumeroCarteirinha())
                .numeroGuiaOperadora(guia.getNumeroGuiaOperadora())
                .competenciaInicio(guia.getCompetenciaInicio())
                .competenciaFim(guia.getCompetenciaFim())
                .valorTotal(guia.getValorTotal())
                .status(guia.getStatus())
                .provedorAssinatura(guia.getProvedorAssinatura())
                .assinadoPorNome(guia.getAssinadoPorNome())
                .assinadoPorCpf(guia.getAssinadoPorCpf())
                .dataHoraAssinatura(guia.getDataHoraAssinatura())
                .codigoVerificador(guia.getCodigoVerificador())
                .hashPdfOriginal(guia.getHashPdfOriginal())
                .hashPdfAssinado(guia.getHashPdfAssinado())
                .dataEnvioConvenio(guia.getDataEnvioConvenio())
                .dataPrevisaoPagamento(guia.getDataPrevisaoPagamento())
                .dataPagamento(guia.getDataPagamento())
                .observacoes(guia.getObservacoes())
                .itens(itensDto)
                .build();
    }
}
