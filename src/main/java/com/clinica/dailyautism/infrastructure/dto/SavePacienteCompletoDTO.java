package com.clinica.dailyautism.infrastructure.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

    @Data
    public class SavePacienteCompletoDTO {

        @NotNull(message = "O nome é obrigatório")
        private final String nomePessoa;

        @NotNull(message = "O CPF é obrigatório")
        private final String CPFPessoa;

        @NotNull(message = "A data de nascimento é obrigatória")
        private final LocalDate datanascPessoa;

        // opcionais
        private final String apelidoPaciente;
        private final String RGPessoa;
        private final String enderecoPessoa;
        private final String telefonePessoa;
        private final String celularPessoa;
        private final String emailPessoa;
        private final boolean isResponsavel;
    }

