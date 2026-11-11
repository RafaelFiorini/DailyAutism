package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.domain.entity.StatusCompromisso;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompromissoDTO {

    private final String idCompromisso;
    private final String tituloCompromisso;
    private final String descricaoCompromisso;
    private final LocalDateTime dataHoraCompromisso;
    private final String localCompromisso;
    private final StatusCompromisso status;
    private final String idPaciente;
    private final String nomePaciente;
    private final String idTipoCompromisso;
    private final String nomeTipoCompromisso;
    private final String idPeriodicidade;
    private final String nomePeriodicidade;
    private final String idProfissional;
    private final String nomeProfissional;
    private final String conselhoProfissional;
    private final String idClinica;
    private final String nomeClinica;
    private final String idUser;

    public static CompromissoDTO create(Compromisso compromisso) {
        String idProf = compromisso.getProfissional() != null ? compromisso.getProfissional().getIdProf() : null;
        String nomeProf = null;
        String conselhoProf = null;
        if (compromisso.getProfissional() != null) {
            conselhoProf = compromisso.getProfissional().getConselhoProf();
            if (compromisso.getProfissional().getPessoaProf() != null) {
                nomeProf = compromisso.getProfissional().getPessoaProf().getNomePessoa();
            }
        }

        String nomePac = (compromisso.getPaciente() != null && compromisso.getPaciente().getPessoaPaciente() != null)
                ? compromisso.getPaciente().getPessoaPaciente().getNomePessoa()
                : (compromisso.getPaciente() != null ? compromisso.getPaciente().getApelidoPaciente() : null);

        String idTipo = compromisso.getTipoCompromisso() != null ? compromisso.getTipoCompromisso().getIdTipoCompromisso() : null;
        String nomeTipo = compromisso.getTipoCompromisso() != null ? compromisso.getTipoCompromisso().getNomeTipo() : null;

        String idPeriod = compromisso.getPeriodicidade() != null ? compromisso.getPeriodicidade().getIdPeriodicidade() : null;
        String nomePeriod = compromisso.getPeriodicidade() != null ? compromisso.getPeriodicidade().getNomePeriodicidade() : null;

        String idUser = compromisso.getCriadoPor() != null ? compromisso.getCriadoPor().getIdUser() : null;
        String idClinica = compromisso.getClinica() != null ? compromisso.getClinica().getIdClinica() : null;
        String nomeClinica = compromisso.getClinica() != null ? compromisso.getClinica().getNomeClinica() : null;

        return new CompromissoDTO(
                compromisso.getIdCompromisso(),
                compromisso.getTituloCompromisso(),
                compromisso.getDescricaoCompromisso(),
                compromisso.getDataHoraCompromisso(),
                compromisso.getLocalCompromisso(),
                compromisso.getStatus(),
                compromisso.getPaciente() != null ? compromisso.getPaciente().getIdPaciente() : null,
                nomePac,
                idTipo,
                nomeTipo,
                idPeriod,
                nomePeriod,
                idProf,
                nomeProf,
                conselhoProf,
                idClinica,
                nomeClinica,
                idUser
        );
    }
}
