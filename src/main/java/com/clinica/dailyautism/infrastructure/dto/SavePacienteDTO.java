package com.clinica.dailyautism.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavePacienteDTO {

    @JsonProperty("idPessoa")
    @JsonAlias({"id_pessoa", "id"})
    private String idPessoa;

    @JsonProperty("nomePessoa")
    @JsonAlias({"nome", "nomePaciente"})
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

    @JsonProperty("apelidoPaciente")
    @JsonAlias({"apelido"})
    private String apelidoPaciente;

    @JsonProperty("isResponsavel")
    @JsonAlias({"responsavel", "is_responsavel"})
    private Boolean isResponsavel;

    // Espera para Plano de Saúde e Clínica:
    @JsonProperty("idPlanoSaude")
    @JsonAlias({"planoSaudeId", "idPlano"})
    private String idPlanoSaude;

    @JsonProperty("numeroCarteirinha")
    private String numeroCarteirinha;

    @JsonProperty("idClinica")
    @JsonAlias({"clinicaId"})
    private String idClinica;
}
