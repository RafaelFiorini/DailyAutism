package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_log", indexes = {
    @Index(name = "idx_audit_data_hora", columnList = "dataHora"),
    @Index(name = "idx_audit_categoria", columnList = "categoria"),
    @Index(name = "idx_audit_acao", columnList = "acao"),
    @Index(name = "idx_audit_id_usuario", columnList = "idUsuario"),
    @Index(name = "idx_audit_entidade", columnList = "entidade, idEntidade")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idAuditoriaLog")
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idAuditoriaLog;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(length = 36)
    private String idUsuario;

    @Column(length = 150)
    private String nomeUsuario;

    @Column(length = 100)
    private String loginUsuario;

    @Column(length = 40)
    private String perfilUsuario;

    @Column(length = 36)
    private String idClinica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaAuditoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcaoAuditoria acao;

    @Column(length = 60)
    private String entidade;

    @Column(length = 100)
    private String idEntidade;

    @Column(length = 1000)
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String dadosAnteriores;

    @Column(columnDefinition = "TEXT")
    private String dadosNovos;

    @Column(length = 50)
    private String ipOrigem;

    @Column(length = 500)
    private String userAgent;
}
