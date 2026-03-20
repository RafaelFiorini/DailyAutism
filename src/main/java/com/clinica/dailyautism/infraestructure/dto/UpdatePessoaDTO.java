package com.clinica.dailyautism.infraestructure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePessoaDTO {

    @NotNull(message = "O nome da pessoa é obrigatório")
    @Size(min = 2, max = 100, message = "Nome inválido")
    private final String nomePessoa;

    //@NotNull(message = "A data de nascimento da pessoa é obrigatória")
    private final LocalDate datanascPessoa;

//    @Size(max = 20, message = "RG inválido")
//    private final String RGPessoa;

    private final String enderecoPessoa;

    private final String telefonePessoa;

    private final String celularPessoa;

    @Email(message = "Email inválido" )
    private final String emailPessoa;

}