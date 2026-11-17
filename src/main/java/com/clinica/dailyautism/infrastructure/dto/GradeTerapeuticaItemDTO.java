package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.DiaSemana;
import com.clinica.dailyautism.domain.entity.GradeTerapeuticaItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeTerapeuticaItemDTO {

    private String idGradeTerapeuticaItem;
    private DiaSemana diaSemana;
    private String diaSemanaDescricao;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String idTipoCompromisso;
    private String nomeTipoCompromisso;
    private String idProfissional;
    private String nomeProfissional;
    private String sala;

    public static GradeTerapeuticaItemDTO fromEntity(GradeTerapeuticaItem item) {
        if (item == null) return null;

        String nomeTipo = null;
        if (item.getTipoCompromisso() != null) {
            nomeTipo = item.getTipoCompromisso().getDescricaoTipo() != null
                    ? item.getTipoCompromisso().getDescricaoTipo()
                    : item.getTipoCompromisso().getNomeTipo();
        }

        String nomeProf = null;
        if (item.getProfissional() != null && item.getProfissional().getPessoaProf() != null) {
            nomeProf = item.getProfissional().getPessoaProf().getNomePessoa();
        }

        return GradeTerapeuticaItemDTO.builder()
                .idGradeTerapeuticaItem(item.getIdGradeTerapeuticaItem())
                .diaSemana(item.getDiaSemana())
                .diaSemanaDescricao(item.getDiaSemana() != null ? item.getDiaSemana().getDescricao() : null)
                .horaInicio(item.getHoraInicio())
                .horaFim(item.getHoraFim())
                .idTipoCompromisso(item.getTipoCompromisso() != null ? item.getTipoCompromisso().getIdTipoCompromisso() : null)
                .nomeTipoCompromisso(nomeTipo)
                .idProfissional(item.getProfissional() != null ? item.getProfissional().getIdProf() : null)
                .nomeProfissional(nomeProf)
                .sala(item.getSala())
                .build();
    }
}
