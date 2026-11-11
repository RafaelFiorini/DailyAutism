package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Guia consolidada de faturamento de atendimentos realizados via plano de saúde.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idGuiaFaturamento", callSuper = false)
@ToString(exclude = {"clinica", "paciente", "planoSaude", "responsavel", "itens"})
@SQLRestriction("ativo = true")
public class GuiaFaturamentoConvenio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idGuiaFaturamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClinica", nullable = false)
    private Clinica clinica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPlanoSaude", nullable = false)
    private PlanoSaude planoSaude;

    @ManyToOne
    @JoinColumn(name = "idResponsavel")
    private Responsavel responsavel;

    @Column(nullable = false, length = 40)
    private String numeroLoteGuia;

    @Column(length = 80)
    private String numeroCarteirinha;

    @Column(length = 80)
    private String numeroGuiaOperadora;

    @Column(nullable = false)
    private LocalDate competenciaInicio;

    @Column(nullable = false)
    private LocalDate competenciaFim;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusGuiaFaturamento status = StatusGuiaFaturamento.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProvedorAssinatura provedorAssinatura;

    @Column(length = 36)
    private String assinadoPorUserId;

    @Column(length = 200)
    private String assinadoPorNome;

    @Column(length = 20)
    private String assinadoPorCpf;

    private LocalDateTime dataHoraAssinatura;

    @Column(length = 64)
    private String ipAssinatura;

    @Column(length = 32)
    private String codigoVerificador;

    @Column(length = 64)
    private String hashPdfOriginal;

    @Column(length = 64)
    private String hashPdfAssinado;

    private LocalDate dataEnvioConvenio;

    private LocalDate dataPrevisaoPagamento;

    private LocalDate dataPagamento;

    @Column(length = 255)
    private String caminhoPdf;

    @Column(length = 500)
    private String observacoes;

    @Builder.Default
    @OneToMany(mappedBy = "guia", cascade = CascadeType.ALL)
    private List<GuiaFaturamentoItem> itens = new ArrayList<>();
}
