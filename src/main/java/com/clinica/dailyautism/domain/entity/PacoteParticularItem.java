package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Item discriminando a quantidade de sessões por especialidade em um pacote particular.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPacoteItem", callSuper = false)
@ToString(exclude = {"pacote", "tipoCompromisso"})
@SQLRestriction("ativo = true")
public class PacoteParticularItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPacoteItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPacote", nullable = false)
    private PacoteParticular pacote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTipoCompromisso", nullable = false)
    private TipoCompromisso tipoCompromisso;

    @Column(nullable = false)
    private Integer quantidadeSessoes;
}
