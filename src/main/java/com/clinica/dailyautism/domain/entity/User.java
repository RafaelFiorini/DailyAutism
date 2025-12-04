package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;


@Entity
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idUser;

    @Column(nullable = false, length = 255)
    private String nomeUser;

    @Column(nullable = false, length = 255)
    private String emailUser;

    @Column(nullable = false)
    private String passwordUser;
}
