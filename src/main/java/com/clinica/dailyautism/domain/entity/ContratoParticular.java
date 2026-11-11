package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrato / fechamento particular pré-pago de um paciente em uma clínica.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idContratoParticular", callSuper = false)
@ToString(exclude = {"clinica", "paciente", "responsavel", "pacoteOrigem", "itens"})
@SQLRestriction("ativo = true")
public class ContratoParticular extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idContratoParticular;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClinica", nullable = false)
    private Clinica clinica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idResponsavel")
    private Responsavel responsavel;

    @ManyToOne
    @JoinColumn(name = "idPacote")
    private PacoteParticular pacoteOrigem;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModalidadeContratoParticular modalidade;

    @Column(nullable = false)
    private LocalDate dataInicio;

    private LocalDate dataFimPrevista;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorCobrado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPagamentoContrato statusPagamento = StatusPagamentoContrato.PENDENTE;

    private LocalDate dataPagamento;

    @Column(length = 40)
    private String formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusContratoParticular statusContrato = StatusContratoParticular.ATIVO;

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContratoParticularItem> itens = new ArrayList<>();
}
