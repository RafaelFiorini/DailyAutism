package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SQLRestriction("ativo = true")
public class Responsavel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idResponsavel;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pessoaResponsavel", referencedColumnName = "idPessoa")
    private Pessoa pessoaResponsavel;

    @ManyToMany
    @JoinTable(
            name = "responsavel_paciente",
            joinColumns = @JoinColumn(name = "idResponsavel"),
            inverseJoinColumns = @JoinColumn(name = "idPaciente")
    )
    @Builder.Default
    private List<Paciente> pacientes = new ArrayList<>();
}