package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class OpcoesCompartilhamentoDTO {

    private List<ClinicaOpcao> clinicas;
    private List<ProfissionalOpcao> profissionais;

    @Data
    @AllArgsConstructor
    public static class ClinicaOpcao {
        private String idClinica;
        private String nomeClinica;
    }

    @Data
    @AllArgsConstructor
    public static class ProfissionalOpcao {
        private String idProf;
        private String nomeProfissional;
        private String conselhoProf;
    }
}
