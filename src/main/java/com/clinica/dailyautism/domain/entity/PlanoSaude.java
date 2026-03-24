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
public class PlanoSaude extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPlanoSaude;

    @Column(nullable = false, length = 200)
    private String nomePlano;

    @Column(nullable = false, length = 200)
    private String operadora;

    @Column(length = 10)
    private String ansReg;
}