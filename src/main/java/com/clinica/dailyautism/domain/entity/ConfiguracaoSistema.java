package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "configuracao_sistema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoSistema {

    @Id
    @Column(nullable = false, length = 100)
    private String chave;

    @Column(nullable = false, length = 255)
    private String valor;

    @Column(length = 255)
    private String descricao;
}
