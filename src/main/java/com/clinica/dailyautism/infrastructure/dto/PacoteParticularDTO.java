package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.PacoteParticular;
import com.clinica.dailyautism.domain.entity.PacoteParticularItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacoteParticularDTO {

    private String idPacote;
    private String idClinica;
    private String nomeClinica;
    private String nomePacote;
    private String descricao;
    private BigDecimal valorTotalPacote;
    private BigDecimal valorSemDescontoCalculado;
    private Integer validadeMesesSugerida;
    private Integer totalSessoes;
    private List<PacoteParticularItemDTO> itens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacoteParticularItemDTO {
        private String idPacoteItem;
        private String idTipoCompromisso;
        private String nomeTipoCompromisso;
        private Integer quantidadeSessoes;

        public static PacoteParticularItemDTO create(PacoteParticularItem item) {
            return PacoteParticularItemDTO.builder()
                    .idPacoteItem(item.getIdPacoteItem())
                    .idTipoCompromisso(item.getTipoCompromisso() != null ? item.getTipoCompromisso().getIdTipoCompromisso() : null)
                    .nomeTipoCompromisso(item.getTipoCompromisso() != null ? item.getTipoCompromisso().getNomeTipo() : null)
                    .quantidadeSessoes(item.getQuantidadeSessoes())
                    .build();
        }
    }

    public static PacoteParticularDTO create(PacoteParticular pacote) {
        if (pacote == null) return null;
        List<PacoteParticularItemDTO> itensDto = pacote.getItens() == null
                ? List.of()
                : pacote.getItens().stream()
                        .filter(i -> i.getAtivo() == null || i.getAtivo())
                        .map(PacoteParticularItemDTO::create)
                        .toList();
        int totalSessoes = itensDto.stream()
                .mapToInt(i -> i.getQuantidadeSessoes() != null ? i.getQuantidadeSessoes() : 0)
                .sum();

        return PacoteParticularDTO.builder()
                .idPacote(pacote.getIdPacote())
                .idClinica(pacote.getClinica() != null ? pacote.getClinica().getIdClinica() : null)
                .nomeClinica(pacote.getClinica() != null ? pacote.getClinica().getNomeClinica() : null)
                .nomePacote(pacote.getNomePacote())
                .descricao(pacote.getDescricao())
                .valorTotalPacote(pacote.getValorTotalPacote())
                .valorSemDescontoCalculado(pacote.getValorSemDescontoCalculado())
                .validadeMesesSugerida(pacote.getValidadeMesesSugerida())
                .totalSessoes(totalSessoes)
                .itens(itensDto)
                .build();
    }
}
