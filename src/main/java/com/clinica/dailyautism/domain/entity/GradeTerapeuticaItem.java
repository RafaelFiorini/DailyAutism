package com.clinica.dailyautism.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idGradeTerapeuticaItem", callSuper = false)
@SQLRestriction("ativo = true")
public class GradeTerapeuticaItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idGradeTerapeuticaItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idGradeTerapeutica", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private GradeTerapeutica gradeTerapeutica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @ManyToOne
    @JoinColumn(name = "idTipoCompromisso", nullable = false)
    private TipoCompromisso tipoCompromisso;

    @ManyToOne
    @JoinColumn(name = "idProfissional")
    private Profissional profissional;

    @Column(length = 100)
    private String sala;
}
