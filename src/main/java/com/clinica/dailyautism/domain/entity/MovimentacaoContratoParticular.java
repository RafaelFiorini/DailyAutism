package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Histórico auditável de abatimentos e estornos de sessões em um item de contrato particular.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idMovimentacao", callSuper = false)
@ToString(exclude = {"contratoItem", "compromisso"})
@SQLRestriction("ativo = true")
public class MovimentacaoContratoParticular extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idMovimentacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idContratoItem", nullable = false)
    private ContratoParticularItem contratoItem;

    @ManyToOne
    @JoinColumn(name = "idCompromisso")
    private Compromisso compromisso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoContrato tipoMovimentacao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao;

    @Column(length = 255)
    private String observacao;
}
