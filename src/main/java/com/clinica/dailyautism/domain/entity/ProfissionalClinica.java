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
public class ProfissionalClinica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idProfissionalClinica;

    @ManyToOne
    @JoinColumn(name = "idProf", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "idClinica", nullable = false)
    private Clinica clinica;

    private String diasAtendimento;

    private String horariosAtendimento;
}