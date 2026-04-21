package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Compromisso;
import lombok.Data;

@Data
public class AgendaItemDTO {

    private final String idCompromisso;
    private final String titulo;
    private final String descricao;
    private final String dataHora;
    private final String local;
    private final String status;
    private final String idPaciente;
    private final String nomePaciente;

    public static AgendaItemDTO create(Compromisso c) {
        return new AgendaItemDTO(
                c.getIdCompromisso(),
                c.getTituloCompromisso(),
                c.getDescricaoCompromisso(),
                c.getDataHoraCompromisso().toString(),
                c.getLocalCompromisso(),
                c.getStatus().name(),
                c.getPaciente().getIdPaciente(),
                c.getPaciente().getPessoaPaciente().getNomePessoa()
        );
    }
}