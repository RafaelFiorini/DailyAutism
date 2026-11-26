package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Notificacao;
import com.clinica.dailyautism.domain.entity.TipoNotificacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoDTO {

    private String idNotificacao;
    private String titulo;
    private String mensagem;
    private TipoNotificacao tipo;
    private String idReferencia;
    private Boolean lida;
    private LocalDateTime lidaEm;
    private LocalDateTime criadoEm;

    public static NotificacaoDTO create(Notificacao n) {
        return NotificacaoDTO.builder()
                .idNotificacao(n.getIdNotificacao())
                .titulo(n.getTitulo())
                .mensagem(n.getMensagem())
                .tipo(n.getTipo())
                .idReferencia(n.getIdReferencia())
                .lida(Boolean.TRUE.equals(n.getLida()))
                .lidaEm(n.getLidaEm())
                .criadoEm(n.getCriadoEm())
                .build();
    }
}
