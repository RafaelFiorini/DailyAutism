package com.clinica.dailyautism.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveResponsavelDTO {

    @JsonProperty("idPessoa")
    @JsonAlias({"id_pessoa", "id"})
    private String idPessoa;

    @JsonProperty("nomePessoa")
    @JsonAlias({"nome", "nomeResponsavel"})
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

    @JsonProperty("isPacienteTambem")
    @JsonAlias({"isPaciente", "incluirComoPaciente"})
    private Boolean isPacienteTambem;

    @JsonProperty("pacienteIds")
    @JsonAlias({"pacientes", "idsPacientes"})
    private List<String> pacienteIds;
}
