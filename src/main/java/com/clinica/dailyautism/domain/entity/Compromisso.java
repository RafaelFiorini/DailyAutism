package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.domain.entity.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SQLRestriction("ativo = true")
public class Compromisso extends BaseEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCompromisso status;

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