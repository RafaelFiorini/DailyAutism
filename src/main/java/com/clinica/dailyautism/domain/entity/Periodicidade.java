package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SQLRestriction("ativo = true")
public class Periodicidade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPeriodicidade;

    @Column(nullable = false, length = 100)
    private String nomePeriodicidade;

    @Column
    private Integer diasIntervalo;

    private String descricaoPeriodicidade;
}