package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idLancamento", callSuper = false)
@ToString(exclude = {"profissional", "clinica", "paciente", "compromisso"})
@SQLRestriction("ativo = true")
public class LancamentoRepasseProfissional extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idLancamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idProf", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "idClinica")
    private Clinica clinica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idCompromisso")
    private Compromisso compromisso;

    @Column(nullable = false)
    private LocalDate dataCompetencia;

    @Column(nullable = false)
    private LocalDate dataAtendimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOrigemLancamento tipoOrigem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorBruto;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentualRepasse;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorLiquidoRepasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLancamentoRepasse statusPagamento;

    private LocalDate dataPagamento;

    @Column(length = 500)
    private String observacoes;
}
