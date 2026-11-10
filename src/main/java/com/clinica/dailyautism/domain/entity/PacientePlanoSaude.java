package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPacientePlanoSaude", callSuper = false)
@ToString(exclude = {"paciente", "planoSaude"})
@SQLRestriction("ativo = true")
public class PacientePlanoSaude extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPacientePlanoSaude;

    @ManyToOne
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idPlanoSaude", nullable = false)
    private PlanoSaude planoSaude;

    @Column(length = 50)
    private String numeroCarteirinha;

    private LocalDate validadeCarteirinha;

    @Column(length = 50)
    private String numeroGuia;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoLiberacaoGuia tipoLiberacao;

    private LocalDate dataInicioLiberacao;

    private LocalDate vencimentoLiberacao;

    private Integer quantidadeSessoesLiberadas;
}
