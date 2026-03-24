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
public class ProfissionalPlanoSaude extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idProfissionalPlanoSaude;

    @ManyToOne
    @JoinColumn(name = "idProf", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "idPlanoSaude", nullable = false)
    private PlanoSaude planoSaude;

    private String numeroCredenciamento;
}