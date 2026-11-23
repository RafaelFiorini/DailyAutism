package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.BloqueioAgendaClinica;
import com.clinica.dailyautism.domain.entity.TipoBloqueioAgenda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueioAgendaClinicaDTO {

    private String idBloqueio;
    private String idClinica;
    private String nomeClinica;
    private String titulo;
    private TipoBloqueioAgenda tipo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String observacao;
    private String criadoPorNome;
    private Integer agendamentosSuspensos;

    public static BloqueioAgendaClinicaDTO create(BloqueioAgendaClinica entity) {
        return create(entity, null);
    }

    public static BloqueioAgendaClinicaDTO create(BloqueioAgendaClinica entity, Integer agendamentosSuspensos) {
        return BloqueioAgendaClinicaDTO.builder()
                .idBloqueio(entity.getIdBloqueio())
                .idClinica(entity.getClinica() != null ? entity.getClinica().getIdClinica() : null)
                .nomeClinica(entity.getClinica() != null ? entity.getClinica().getNomeClinica() : null)
                .titulo(entity.getTitulo())
                .tipo(entity.getTipo())
                .dataInicio(entity.getDataInicio())
                .dataFim(entity.getDataFim())
                .horaInicio(entity.getHoraInicio())
                .horaFim(entity.getHoraFim())
                .observacao(entity.getObservacao())
                .criadoPorNome(entity.getCriadoPor() != null ? entity.getCriadoPor().getNomeUser() : null)
                .agendamentosSuspensos(agendamentosSuspensos)
                .build();
    }
}
