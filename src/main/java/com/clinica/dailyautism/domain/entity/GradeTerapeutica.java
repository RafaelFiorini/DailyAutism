package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idGradeTerapeutica", callSuper = false)
@SQLRestriction("ativo = true")
public class GradeTerapeutica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idGradeTerapeutica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idClinica")
    private Clinica clinica;

    @Column(length = 36)
    private String idGradeAnterior;

    @Column(nullable = false)
    @Builder.Default
    private Integer numeroVersao = 1;

    @Column(length = 30)
    private String tipoOrigemVigencia; // GUIA_CONVENIO, CONTRATO_PARTICULAR

    @Column(length = 36)
    private String idOrigemVigencia;

    @Column(nullable = false)
    private LocalDate dataInicioVigencia;

    @Column(nullable = false)
    private LocalDate dataFimVigencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusGradeTerapeutica status;

    @Column(columnDefinition = "TEXT")
    private String justificativaSugestaoResponsavel;

    @Column(columnDefinition = "TEXT")
    private String motivoRejeicao;

    private LocalDateTime dataEnvio;

    private LocalDateTime dataResposta;

    @OneToMany(mappedBy = "gradeTerapeutica", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GradeTerapeuticaItem> itens = new ArrayList<>();

    public void addItem(GradeTerapeuticaItem item) {
        if (this.itens == null) {
            this.itens = new ArrayList<>();
        }
        item.setGradeTerapeutica(this);
        this.itens.add(item);
    }

    public void removeItem(GradeTerapeuticaItem item) {
        if (this.itens != null) {
            this.itens.remove(item);
            item.setGradeTerapeutica(null);
        }
    }
}
