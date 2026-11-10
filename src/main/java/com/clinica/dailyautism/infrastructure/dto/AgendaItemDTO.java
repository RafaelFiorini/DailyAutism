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
    private final boolean temPlano;
    private final String idProfissional;
    private final String nomeProfissional;
    private final String especialidade;

    public static AgendaItemDTO create(Compromisso c) {
        boolean temPlano = c.getPaciente() != null &&
                c.getPaciente().getPlanos() != null &&
                !c.getPaciente().getPlanos().isEmpty();

        String idProf = c.getProfissional() != null ? c.getProfissional().getIdProf() : null;
        String nomeProf = (c.getProfissional() != null && c.getProfissional().getPessoaProf() != null)
                ? c.getProfissional().getPessoaProf().getNomePessoa()
                : null;
        String esp = c.getTipoCompromisso() != null ? c.getTipoCompromisso().getNomeTipo() : null;

        String idPac = c.getPaciente() != null ? c.getPaciente().getIdPaciente() : null;
        String nomePac = (c.getPaciente() != null && c.getPaciente().getPessoaPaciente() != null)
                ? c.getPaciente().getPessoaPaciente().getNomePessoa()
                : (c.getPaciente() != null ? c.getPaciente().getApelidoPaciente() : null);

        return new AgendaItemDTO(
                c.getIdCompromisso(),
                c.getTituloCompromisso(),
                c.getDescricaoCompromisso(),
                c.getDataHoraCompromisso() != null ? c.getDataHoraCompromisso().toString() : null,
                c.getLocalCompromisso(),
                c.getStatus() != null ? c.getStatus().name() : null,
                idPac,
                nomePac,
                temPlano,
                idProf,
                nomeProf,
                esp
        );
    }
}
