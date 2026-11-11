package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cota contratada e consumida de uma especialidade específica dentro de um contrato particular.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idContratoItem", callSuper = false)
@ToString(exclude = {"contrato", "tipoCompromisso", "movimentacoes"})
@SQLRestriction("ativo = true")
public class ContratoParticularItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idContratoItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idContratoParticular", nullable = false)
    private ContratoParticular contrato;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTipoCompromisso", nullable = false)
    private TipoCompromisso tipoCompromisso;

    @Column(nullable = false)
    private Integer quantidadeContratada;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidadeConsumida = 0;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorUnitarioReferencia;

    @Builder.Default
    @OneToMany(mappedBy = "contratoItem", cascade = CascadeType.ALL)
    private List<MovimentacaoContratoParticular> movimentacoes = new ArrayList<>();

    public int getQuantidadeRestante() {
        int contratada = quantidadeContratada != null ? quantidadeContratada : 0;
        int consumida = quantidadeConsumida != null ? quantidadeConsumida : 0;
        return Math.max(0, contratada - consumida);
    }
}
