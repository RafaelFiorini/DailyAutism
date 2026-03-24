package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SQLRestriction("ativo = true")
public class Paciente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPaciente;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pessoaPaciente", referencedColumnName = "idPessoa")
    private Pessoa pessoaPaciente;

    private String apelidoPaciente;

    @OneToMany(mappedBy = "paciente")
    private List<PacientePlanoSaude> planos;

    @OneToMany(mappedBy = "paciente")
    private List<PacienteClinica> clinicas;

    private boolean isResponsavel;

    @ManyToMany(mappedBy = "pacientes")
    private List<Responsavel> responsaveis;

    @OneToMany(mappedBy = "paciente")
    private List<ProfissionalPaciente> profissionais;
}