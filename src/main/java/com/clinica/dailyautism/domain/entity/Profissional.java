package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data

public class Profissional  {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idProf;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pessoaProf", referencedColumnName = "idPessoa")
    private Pessoa pessoaProf;

    private String conselhoProf;

    private boolean ativoProf;



}
