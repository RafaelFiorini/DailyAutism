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
public class Profissional extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idProf;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pessoaProf", referencedColumnName = "idPessoa")
    private Pessoa pessoaProf;

    private String conselhoProf;

    // ativoProf REMOVIDO — substituído pelo campo ativo da BaseEntity
}