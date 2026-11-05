package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveProfissionalDTO {

    private String idPessoa;

    private String nomePessoa;
    private String CPFPessoa;
    private LocalDate datanascPessoa;
    private String RGPessoa;
    private String enderecoPessoa;
    private String telefonePessoa;
    private String celularPessoa;
    private String emailPessoa;

    @NotBlank(message = "O registro no conselho profissional é obrigatório")
    private String conselhoProf;
}
