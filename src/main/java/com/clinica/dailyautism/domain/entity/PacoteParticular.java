package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pacote ou combo particular ofertado por uma clínica com desconto.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPacote", callSuper = false)
@ToString(exclude = {"clinica", "itens"})
@SQLRestriction("ativo = true")
public class PacoteParticular extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPacote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClinica", nullable = false)
    private Clinica clinica;

    @Column(nullable = false, length = 150)
    private String nomePacote;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotalPacote;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorSemDescontoCalculado;

    private Integer validadeMesesSugerida;

    @Builder.Default
    @OneToMany(mappedBy = "pacote", cascade = CascadeType.ALL)
    private List<PacoteParticularItem> itens = new ArrayList<>();
}
