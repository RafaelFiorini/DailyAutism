package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Preço unitário por especialidade dentro de uma clínica.
 * Quando planoSaude for null, representa o valor Particular padrão da clínica.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idTabelaPreco", callSuper = false)
@ToString(exclude = {"clinica", "tipoCompromisso", "planoSaude"})
@SQLRestriction("ativo = true")
public class TabelaPrecoClinica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idTabelaPreco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClinica", nullable = false)
    private Clinica clinica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTipoCompromisso", nullable = false)
    private TipoCompromisso tipoCompromisso;

    @ManyToOne
    @JoinColumn(name = "idPlanoSaude")
    private PlanoSaude planoSaude;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorSessao;
}
