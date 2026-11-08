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
public class RegisterDTO {

    @NotBlank(message = "O tipo de perfil é obrigatório (RESPONSAVEL ou PROFISSIONAL)")
    @JsonProperty("tipo")
    @JsonAlias({"perfil", "role"})
    private String tipo;

    @NotBlank(message = "O nome completo é obrigatório")
    @JsonProperty("nome")
    @JsonAlias({"nomePessoa", "nomeUser"})
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @JsonProperty("email")
    @JsonAlias({"emailUser", "emailPessoa"})
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @JsonProperty("senha")
    @JsonAlias({"password", "passwordUser"})
    private String senha;

    @NotBlank(message = "O CPF é obrigatório")
    @JsonProperty("cpf")
    @JsonAlias({"CPFPessoa", "cpfPessoa"})
    private String cpf;

    @JsonProperty("telefone")
    private String telefone;

    @JsonProperty("celular")
    private String celular;

    @JsonProperty("rg")
    private String rg;

    @JsonProperty("datanasc")
    private LocalDate datanasc;

    @JsonProperty("endereco")
    private String endereco;

    // Se PROFISSIONAL:
    @JsonProperty("conselhoProf")
    @JsonAlias({"conselho", "registroConselho"})
    private String conselhoProf;

    // Se RESPONSAVEL:
    @JsonProperty("isPacienteTambem")
    private Boolean isPacienteTambem;

    @JsonProperty("pacientes")
    private List<PacienteInicialDTO> pacientes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteInicialDTO {
        private String nome;
        private String cpf;
        private String apelido;
        private LocalDate datanasc;
    }
}
