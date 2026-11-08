package com.clinica.dailyautism.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("idPessoa")
    @JsonAlias({"id_pessoa", "id"})
    private String idPessoa;

    @JsonProperty("nomePessoa")
    @JsonAlias({"nome", "nomeProfissional"})
    private String nomePessoa;

    @JsonProperty("CPFPessoa")
    @JsonAlias({"cpf", "cpfPessoa", "cpfpessoa"})
    private String CPFPessoa;

    @JsonProperty("datanascPessoa")
    @JsonAlias({"datanasc", "dataNascimento", "dataNasc"})
    private LocalDate datanascPessoa;

    @JsonProperty("RGPessoa")
    @JsonAlias({"rg", "rgPessoa", "rgpessoa"})
    private String RGPessoa;

    @JsonProperty("enderecoPessoa")
    @JsonAlias({"endereco"})
    private String enderecoPessoa;

    @JsonProperty("telefonePessoa")
    @JsonAlias({"telefone"})
    private String telefonePessoa;

    @JsonProperty("celularPessoa")
    @JsonAlias({"celular", "whatsapp"})
    private String celularPessoa;

    @JsonProperty("emailPessoa")
    @JsonAlias({"email"})
    private String emailPessoa;

    @NotBlank(message = "O registro no conselho profissional é obrigatório")
    @JsonProperty("conselhoProf")
    @JsonAlias({"conselho", "registroConselho"})
    private String conselhoProf;
}
