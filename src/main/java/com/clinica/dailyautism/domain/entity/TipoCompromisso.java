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
public class TipoCompromisso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idTipoCompromisso;

    @Column(nullable = false, length = 100)
    private String nomeTipo;

    private String descricaoTipo;
}