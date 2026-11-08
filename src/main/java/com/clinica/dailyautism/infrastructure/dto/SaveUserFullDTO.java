package com.clinica.dailyautism.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class SaveUserFullDTO {

    @NotBlank(message = "O nome completo é obrigatório")
    @JsonProperty("nome")
    @JsonAlias({"nomeUser", "nomePessoa"})
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @JsonProperty("email")
    @JsonAlias({"emailUser", "emailPessoa"})
    private String email;

    @JsonProperty("senha")
    @JsonAlias({"password", "passwordUser"})
    private String senha;

    @NotBlank(message = "O CPF é obrigatório")
    @JsonProperty("cpf")
    @JsonAlias({"CPFPessoa", "cpfPessoa"})
    private String cpf;

    @JsonProperty("telefone")
    @JsonAlias({"telefonePessoa"})
    private String telefone;

    @JsonProperty("celular")
    @JsonAlias({"celularPessoa"})
    private String celular;

    @JsonProperty("rg")
    @JsonAlias({"RGPessoa", "rgPessoa"})
    private String rg;

    @JsonProperty("datanasc")
    @JsonAlias({"datanascPessoa"})
    private LocalDate datanasc;

    @JsonProperty("endereco")
    @JsonAlias({"enderecoPessoa"})
    private String endereco;

    @JsonProperty("isResponsavel")
    private Boolean isResponsavel;

    @JsonProperty("isProfissional")
    private Boolean isProfissional;

    @JsonProperty("conselhoProf")
    @JsonAlias({"conselho", "registroConselho"})
    private String conselhoProf;

    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    @JsonProperty("isFuncionarioClinica")
    @JsonAlias({"isClinica", "funcionarioClinica"})
    private Boolean isFuncionarioClinica;

    @JsonProperty("idClinica")
    private String idClinica;

    @JsonProperty("cargoClinica")
    @JsonAlias({"cargo"})
    private String cargoClinica;

    @JsonProperty("perfis")
    private List<String> perfis;
}
