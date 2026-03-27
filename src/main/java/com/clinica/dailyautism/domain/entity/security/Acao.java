package com.clinica.dailyautism.domain.entity.security;


import com.clinica.dailyautism.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "acoes")
@SQLRestriction("ativo = true")
@EqualsAndHashCode(callSuper = false)
public class Acao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAcao;

    @Column(nullable = false, unique = true, length = 60)
    private String nome; // ex: "CRIAR_PACIENTE", "VER_ARQUIVO"

    @Column(length = 40)
    private String recurso; // ex: "PACIENTE", "ARQUIVO"

    @Column(length = 20)
    private String tipo; // ex: "LEITURA", "ESCRITA", "EXCLUSAO"
}