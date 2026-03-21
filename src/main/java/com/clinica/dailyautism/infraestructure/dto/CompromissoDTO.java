package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Compromisso;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompromissoDTO {

    private final String idCompromisso;
    private final String tituloCompromisso;
    private final String descricaoCompromisso;
    private final LocalDateTime dataHoraCompromisso;
    private final String localCompromisso;
    private final boolean aprovado;
    private final String idPaciente;
    private final String nomePaciente;
    private final String idTipoCompromisso;
    private final String nomeTipoCompromisso;
    private final String idPeriodicidade;
    private final String nomePeriodicidade;
    private final String idUser;

    public static CompromissoDTO create(Compromisso compromisso) {
        return new CompromissoDTO(
                compromisso.getIdCompromisso(),
                compromisso.getTituloCompromisso(),
                compromisso.getDescricaoCompromisso(),
                compromisso.getDataHoraCompromisso(),
                compromisso.getLocalCompromisso(),
                compromisso.isAprovado(),
                compromisso.getPaciente().getIdPaciente(),
                compromisso.getPaciente().getPessoaPaciente().getNomePessoa(),
                compromisso.getTipoCompromisso().getIdTipoCompromisso(),
                compromisso.getTipoCompromisso().getNomeTipo(),
                compromisso.getPeriodicidade().getIdPeriodicidade(),
                compromisso.getPeriodicidade().getNomePeriodicidade(),
                compromisso.getCriadoPor().getIdUser()
        );
    }
}