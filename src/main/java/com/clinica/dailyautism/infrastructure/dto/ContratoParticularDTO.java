package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoParticularDTO {

    private String idContratoParticular;
    private String idClinica;
    private String nomeClinica;
    private String idPaciente;
    private String nomePaciente;
    private String idResponsavel;
    private String nomeResponsavel;
    private String idPacoteOrigem;
    private String nomePacoteOrigem;
    private String descricao;
    private ModalidadeContratoParticular modalidade;
    private LocalDate dataInicio;
    private LocalDate dataFimPrevista;
    private BigDecimal valorCobrado;
    private StatusPagamentoContrato statusPagamento;
    private LocalDate dataPagamento;
    private String formaPagamento;
    private StatusContratoParticular statusContrato;
    private int totalSessoesContratadas;
    private int totalSessoesConsumidas;
    private int totalSessoesRestantes;
    private List<ContratoItemDTO> itens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContratoItemDTO {
        private String idContratoItem;
        private String idTipoCompromisso;
        private String nomeTipoCompromisso;
        private Integer quantidadeContratada;
        private Integer quantidadeConsumida;
        private Integer quantidadeRestante;
        private BigDecimal valorUnitarioReferencia;
        private List<MovimentacaoDTO> movimentacoes;

        public static ContratoItemDTO create(ContratoParticularItem item) {
            List<MovimentacaoDTO> movs = item.getMovimentacoes() == null
                    ? List.of()
                    : item.getMovimentacoes().stream()
                            .filter(m -> m.getAtivo() == null || m.getAtivo())
                            .sorted((a, b) -> b.getDataMovimentacao().compareTo(a.getDataMovimentacao()))
                            .map(MovimentacaoDTO::create)
                            .toList();

            return ContratoItemDTO.builder()
                    .idContratoItem(item.getIdContratoItem())
                    .idTipoCompromisso(item.getTipoCompromisso() != null ? item.getTipoCompromisso().getIdTipoCompromisso() : null)
                    .nomeTipoCompromisso(item.getTipoCompromisso() != null ? item.getTipoCompromisso().getNomeTipo() : null)
                    .quantidadeContratada(item.getQuantidadeContratada())
                    .quantidadeConsumida(item.getQuantidadeConsumida())
                    .quantidadeRestante(item.getQuantidadeRestante())
                    .valorUnitarioReferencia(item.getValorUnitarioReferencia())
                    .movimentacoes(movs)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimentacaoDTO {
        private String idMovimentacao;
        private String idCompromisso;
        private String tituloCompromisso;
        private TipoMovimentacaoContrato tipoMovimentacao;
        private Integer quantidade;
        private LocalDateTime dataMovimentacao;
        private String observacao;

        public static MovimentacaoDTO create(MovimentacaoContratoParticular mov) {
            return MovimentacaoDTO.builder()
                    .idMovimentacao(mov.getIdMovimentacao())
                    .idCompromisso(mov.getCompromisso() != null ? mov.getCompromisso().getIdCompromisso() : null)
                    .tituloCompromisso(mov.getCompromisso() != null ? mov.getCompromisso().getTituloCompromisso() : null)
                    .tipoMovimentacao(mov.getTipoMovimentacao())
                    .quantidade(mov.getQuantidade())
                    .dataMovimentacao(mov.getDataMovimentacao())
                    .observacao(mov.getObservacao())
                    .build();
        }
    }

    public static ContratoParticularDTO create(ContratoParticular contrato) {
        if (contrato == null) return null;

        List<ContratoItemDTO> itensDto = contrato.getItens() == null
                ? List.of()
                : contrato.getItens().stream()
                        .filter(i -> i.getAtivo() == null || i.getAtivo())
                        .map(ContratoItemDTO::create)
                        .toList();

        int contratadas = itensDto.stream().mapToInt(i -> i.getQuantidadeContratada() != null ? i.getQuantidadeContratada() : 0).sum();
        int consumidas = itensDto.stream().mapToInt(i -> i.getQuantidadeConsumida() != null ? i.getQuantidadeConsumida() : 0).sum();
        int restantes = itensDto.stream().mapToInt(i -> i.getQuantidadeRestante() != null ? i.getQuantidadeRestante() : 0).sum();

        String nomePac = (contrato.getPaciente() != null && contrato.getPaciente().getPessoaPaciente() != null)
                ? contrato.getPaciente().getPessoaPaciente().getNomePessoa()
                : (contrato.getPaciente() != null ? contrato.getPaciente().getApelidoPaciente() : null);

        String nomeResp = (contrato.getResponsavel() != null && contrato.getResponsavel().getPessoaResponsavel() != null)
                ? contrato.getResponsavel().getPessoaResponsavel().getNomePessoa()
                : null;

        return ContratoParticularDTO.builder()
                .idContratoParticular(contrato.getIdContratoParticular())
                .idClinica(contrato.getClinica() != null ? contrato.getClinica().getIdClinica() : null)
                .nomeClinica(contrato.getClinica() != null ? contrato.getClinica().getNomeClinica() : null)
                .idPaciente(contrato.getPaciente() != null ? contrato.getPaciente().getIdPaciente() : null)
                .nomePaciente(nomePac)
                .idResponsavel(contrato.getResponsavel() != null ? contrato.getResponsavel().getIdResponsavel() : null)
                .nomeResponsavel(nomeResp)
                .idPacoteOrigem(contrato.getPacoteOrigem() != null ? contrato.getPacoteOrigem().getIdPacote() : null)
                .nomePacoteOrigem(contrato.getPacoteOrigem() != null ? contrato.getPacoteOrigem().getNomePacote() : null)
                .descricao(contrato.getDescricao())
                .modalidade(contrato.getModalidade())
                .dataInicio(contrato.getDataInicio())
                .dataFimPrevista(contrato.getDataFimPrevista())
                .valorCobrado(contrato.getValorCobrado())
                .statusPagamento(contrato.getStatusPagamento())
                .dataPagamento(contrato.getDataPagamento())
                .formaPagamento(contrato.getFormaPagamento())
                .statusContrato(contrato.getStatusContrato())
                .totalSessoesContratadas(contratadas)
                .totalSessoesConsumidas(consumidas)
                .totalSessoesRestantes(restantes)
                .itens(itensDto)
                .build();
    }
}
