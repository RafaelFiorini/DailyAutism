package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.domain.entity.security.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idCompromisso;

    @Column(nullable = false, length = 200)
    private String tituloCompromisso;

    private String descricaoCompromisso;

    @Column(nullable = false)
    private LocalDateTime dataHoraCompromisso;

    private String localCompromisso;

    private boolean aprovado;

    @ManyToOne
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idTipoCompromisso", nullable = false)
    private TipoCompromisso tipoCompromisso;

    @ManyToOne
    @JoinColumn(name = "idPeriodicidade", nullable = false)
    private Periodicidade periodicidade;

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User criadoPor;
}