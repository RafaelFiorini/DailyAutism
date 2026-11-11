package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Atendimento individual congelado dentro de uma guia de faturamento de convênio.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idGuiaItem", callSuper = false)
@ToString(exclude = {"guia", "compromisso"})
@SQLRestriction("ativo = true")
public class GuiaFaturamentoItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idGuiaItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idGuiaFaturamento", nullable = false)
    private GuiaFaturamentoConvenio guia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idCompromisso", nullable = false)
    private Compromisso compromisso;

    @Column(nullable = false)
    private LocalDateTime dataHoraAtendimento;

    @Column(nullable = false, length = 160)
    private String tipoCompromissoNome;

    @Column(length = 200)
    private String profissionalNome;

    @Column(length = 80)
    private String conselhoProfissional;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitarioConvenio;
}
