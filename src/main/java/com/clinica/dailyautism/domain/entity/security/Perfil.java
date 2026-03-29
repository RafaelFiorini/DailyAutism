package com.clinica.dailyautism.domain.entity.security;


import com.clinica.dailyautism.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "perfis")
@SQLRestriction("ativo = true")
@EqualsAndHashCode(callSuper = false)
public class Perfil extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPerfil;

    @Column(nullable = false, unique = true, length = 30)
    private String nome; // "ADMIN", "RESPONSAVEL", "PROFISSIONAL", "CLINICA"

    // Na entidade Perfil
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "acao_perfil",
            joinColumns = @JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @JoinColumn(name = "id_acao")
    )
    @Builder.Default
    private Set<Acao> acoes = new HashSet<>();
}