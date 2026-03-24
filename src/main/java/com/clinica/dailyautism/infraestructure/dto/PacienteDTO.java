package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.PacientePlanoSaude;
import com.clinica.dailyautism.domain.entity.PacienteClinica;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PacienteDTO {

    private final String idPaciente;
    private final String idPessoa;
    private final String nomePessoa;
    private final String apelidoPaciente;
    private final boolean isResponsavel;
    private final List<String> planos;
    private final List<String> clinicas;

    public static PacienteDTO create(Paciente paciente) {
        List<String> planos = paciente.getPlanos() != null
                ? paciente.getPlanos().stream()
                .map(p -> p.getPlanoSaude().getNomePlano())
                .collect(Collectors.toList())
                : List.of();

        List<String> clinicas = paciente.getClinicas() != null
                ? paciente.getClinicas().stream()
                .map(c -> c.getClinica().getNomeClinica())
                .collect(Collectors.toList())
                : List.of();

        return new PacienteDTO(
                paciente.getIdPaciente(),
                paciente.getPessoaPaciente().getIdPessoa(),
                paciente.getPessoaPaciente().getNomePessoa(),
                paciente.getApelidoPaciente(),
                paciente.isResponsavel(),
                planos,
                clinicas
        );
    }
}