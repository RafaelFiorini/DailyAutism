package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SQLRestriction("ativo = true")
public class Pessoa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPessoa;

    @Column(nullable = false, length = 255)
    private String nomePessoa;

    @Column(nullable = false, length = 11)
    private String CPFPessoa;

    @Column(nullable = false)
    private LocalDate datanascPessoa;

    @Column(nullable = false)
    private String RGPessoa;

    private String enderecoPessoa;

    private String telefonePessoa;

    private String celularPessoa;

    @Column(nullable = false, length = 255)
    private String emailPessoa;

    @OneToOne(mappedBy = "pessoaResponsavel")
    private Responsavel responsavel;

    @OneToOne(mappedBy = "pessoaProf")
    private Profissional profissional;
}