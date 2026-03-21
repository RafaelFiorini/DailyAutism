package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCompromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idTipoCompromisso;

    @Column(nullable = false, length = 100)
    private String nomeTipo;

    private String descricaoTipo;
}