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
    @JsonAlias({"responsavel"})
    private Boolean isResponsavel;

    @JsonProperty("isProfissional")
    @JsonAlias({"profissional"})
    private Boolean isProfissional;

    @JsonProperty("conselhoProf")
    @JsonAlias({"conselho", "registroConselho"})
    private String conselhoProf;

    @JsonProperty("isAdmin")
    @JsonAlias({"admin"})
    private Boolean isAdmin;

    @JsonProperty("isFuncionarioClinica")
    @JsonAlias({"funcionarioClinica", "isClinica", "clinica"})
    private Boolean isFuncionarioClinica;

    @JsonProperty("isAdminClinica")
    @JsonAlias({"adminClinica", "gestorClinica"})
    private Boolean isAdminClinica;

    @JsonProperty("idClinica")
    private String idClinica;

    @JsonProperty("cargoClinica")
    @JsonAlias({"cargo"})
    private String cargoClinica;

    @JsonProperty("perfis")
    private List<String> perfis;

    @JsonProperty("pacientesIds")
    @JsonAlias({"idsPacientes"})
    private List<String> pacientesIds;

    @JsonProperty("isResponsavel")
    public Boolean getIsResponsavel() {
        return isResponsavel;
    }

    @JsonProperty("isResponsavel")
    public void setIsResponsavel(Boolean isResponsavel) {
        this.isResponsavel = isResponsavel;
    }

    @JsonProperty("isProfissional")
    public Boolean getIsProfissional() {
        return isProfissional;
    }

    @JsonProperty("isProfissional")
    public void setIsProfissional(Boolean isProfissional) {
        this.isProfissional = isProfissional;
    }

    @JsonProperty("isAdmin")
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    @JsonProperty("isAdmin")
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @JsonProperty("isFuncionarioClinica")
    public Boolean getIsFuncionarioClinica() {
        return isFuncionarioClinica;
    }

    @JsonProperty("isFuncionarioClinica")
    public void setIsFuncionarioClinica(Boolean isFuncionarioClinica) {
        this.isFuncionarioClinica = isFuncionarioClinica;
    }

    @JsonProperty("isAdminClinica")
    public Boolean getIsAdminClinica() {
        return isAdminClinica;
    }

    @JsonProperty("isAdminClinica")
    public void setIsAdminClinica(Boolean isAdminClinica) {
        this.isAdminClinica = isAdminClinica;
    }
}
