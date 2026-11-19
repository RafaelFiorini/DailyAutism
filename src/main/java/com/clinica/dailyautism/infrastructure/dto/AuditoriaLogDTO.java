package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.AcaoAuditoria;
import com.clinica.dailyautism.domain.entity.AuditoriaLog;
import com.clinica.dailyautism.domain.entity.CategoriaAuditoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLogDTO {

    private String idAuditoriaLog;
    private LocalDateTime dataHora;
    private String idUsuario;
    private String nomeUsuario;
    private String loginUsuario;
    private String perfilUsuario;
    private String idClinica;
    private CategoriaAuditoria categoria;
    private String categoriaDescricao;
    private AcaoAuditoria acao;
    private String acaoDescricao;
    private String entidade;
    private String idEntidade;
    private String descricao;
    private String dadosAnteriores;
    private String dadosNovos;
    private String ipOrigem;
    private String userAgent;

    public static AuditoriaLogDTO fromEntity(AuditoriaLog log) {
        if (log == null) return null;
        return AuditoriaLogDTO.builder()
                .idAuditoriaLog(log.getIdAuditoriaLog())
                .dataHora(log.getDataHora())
                .idUsuario(log.getIdUsuario())
                .nomeUsuario(log.getNomeUsuario())
                .loginUsuario(log.getLoginUsuario())
                .perfilUsuario(log.getPerfilUsuario())
                .idClinica(log.getIdClinica())
                .categoria(log.getCategoria())
                .categoriaDescricao(log.getCategoria() != null ? log.getCategoria().getDescricao() : null)
                .acao(log.getAcao())
                .acaoDescricao(log.getAcao() != null ? log.getAcao().getDescricao() : null)
                .entidade(log.getEntidade())
                .idEntidade(log.getIdEntidade())
                .descricao(log.getDescricao())
                .dadosAnteriores(log.getDadosAnteriores())
                .dadosNovos(log.getDadosNovos())
                .ipOrigem(log.getIpOrigem())
                .userAgent(log.getUserAgent())
                .build();
    }
}
