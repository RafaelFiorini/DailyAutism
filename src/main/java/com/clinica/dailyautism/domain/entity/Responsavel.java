package com.clinica.dailyautism.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Responsavel {

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
    private List<Paciente> pacientes;
}