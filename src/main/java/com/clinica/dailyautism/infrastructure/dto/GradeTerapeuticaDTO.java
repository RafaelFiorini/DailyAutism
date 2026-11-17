package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.GradeTerapeutica;
import com.clinica.dailyautism.domain.entity.StatusGradeTerapeutica;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeTerapeuticaDTO {

    private String idGradeTerapeutica;
    private String idPaciente;
    private String nomePaciente;
    private String idClinica;
    private String idGradeAnterior;
    private Integer numeroVersao;
    private String tipoOrigemVigencia;
    private String idOrigemVigencia;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private StatusGradeTerapeutica status;
    private String justificativaSugestaoResponsavel;
    private String motivoRejeicao;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataResposta;
    @Builder.Default
    private List<GradeTerapeuticaItemDTO> itens = new ArrayList<>();

    public static GradeTerapeuticaDTO fromEntity(GradeTerapeutica grade) {
        if (grade == null) return null;

        String nomePaciente = null;
        if (grade.getPaciente() != null && grade.getPaciente().getPessoaPaciente() != null) {
            nomePaciente = grade.getPaciente().getPessoaPaciente().getNomePessoa();
        } else if (grade.getPaciente() != null && grade.getPaciente().getApelidoPaciente() != null) {
            nomePaciente = grade.getPaciente().getApelidoPaciente();
        }

        List<GradeTerapeuticaItemDTO> itensDTO = new ArrayList<>();
        if (grade.getItens() != null) {
            for (var item : grade.getItens()) {
                itensDTO.add(GradeTerapeuticaItemDTO.fromEntity(item));
            }
        }

        return GradeTerapeuticaDTO.builder()
                .idGradeTerapeutica(grade.getIdGradeTerapeutica())
                .idPaciente(grade.getPaciente() != null ? grade.getPaciente().getIdPaciente() : null)
                .nomePaciente(nomePaciente)
                .idClinica(grade.getClinica() != null ? grade.getClinica().getIdClinica() : null)
                .idGradeAnterior(grade.getIdGradeAnterior())
                .numeroVersao(grade.getNumeroVersao())
                .tipoOrigemVigencia(grade.getTipoOrigemVigencia())
                .idOrigemVigencia(grade.getIdOrigemVigencia())
                .dataInicioVigencia(grade.getDataInicioVigencia())
                .dataFimVigencia(grade.getDataFimVigencia())
                .status(grade.getStatus())
                .justificativaSugestaoResponsavel(grade.getJustificativaSugestaoResponsavel())
                .motivoRejeicao(grade.getMotivoRejeicao())
                .dataEnvio(grade.getDataEnvio())
                .dataResposta(grade.getDataResposta())
                .itens(itensDTO)
                .build();
    }
}
