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
public class Clinica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idClinica;

    @Column(nullable = false, length = 200)
    private String nomeClinica;

    @Column(nullable = false, length = 14, unique = true)
    private String cnpjClinica;

    private String enderecoClinica;

    private String telefoneClinica;

    private String whatsappClinica;

    private String emailClinica;
}