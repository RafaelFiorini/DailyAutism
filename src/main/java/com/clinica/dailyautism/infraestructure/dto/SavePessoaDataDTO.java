package com.clinica.dailyautism.infraestructure.dto;
import lombok.Data;
import java.time.LocalDate;


@Data
public class SavePessoaDataDTO {

    private final String nomePessoa;
    private final String CPFPessoa;
    private final LocalDate datanascPessoa;
    private final String RGPessoa;
    private final String enderecoPessoa;
    private final String telefonePessoa;
    private final String celularPessoa;
    private final String emailPessoa;

}
