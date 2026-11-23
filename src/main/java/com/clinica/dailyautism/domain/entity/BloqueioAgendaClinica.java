package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.domain.entity.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bloqueio_agenda_clinica")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idBloqueio", callSuper = false)
@ToString(exclude = {"clinica", "criadoPor"})
@SQLRestriction("ativo = true")
public class BloqueioAgendaClinica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idBloqueio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_clinica", referencedColumnName = "idClinica", nullable = false)
    private Clinica clinica;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TipoBloqueioAgenda tipo = TipoBloqueioAgenda.FERIADO;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    /** Se nulos, o bloqueio vale para o dia inteiro. */
    private LocalTime horaInicio;
    private LocalTime horaFim;

    @Column(length = 500)
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "criado_por_id_user", referencedColumnName = "idUser")
    private User criadoPor;

    /**
     * Verifica se uma data/hora específica cai dentro deste bloqueio/recesso da clínica.
     */
    public boolean conflitaCom(LocalDateTime dataHora) {
        if (dataHora == null || dataInicio == null || dataFim == null) {
            return false;
        }
        LocalDate data = dataHora.toLocalDate();
        if (data.isBefore(dataInicio) || data.isAfter(dataFim)) {
            return false;
        }
        // Dia inteiro
        if (horaInicio == null || horaFim == null) {
            return true;
        }
        LocalTime hora = dataHora.toLocalTime();
        return !hora.isBefore(horaInicio) && !hora.isAfter(horaFim);
    }
}
