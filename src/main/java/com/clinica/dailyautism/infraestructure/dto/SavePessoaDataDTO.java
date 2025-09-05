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

    public SavePessoaDataDTO(String nomePessoa, String CPFPessoa, LocalDate datanascPessoa, String RGPessoa, String enderecoPessoa, String telefonePessoa, String celularPessoa, String emailPessoa) {
        this.nomePessoa = nomePessoa;
        this.CPFPessoa = CPFPessoa;
        this.datanascPessoa = datanascPessoa;
        this.RGPessoa = RGPessoa;
        this.enderecoPessoa = enderecoPessoa;
        this.telefonePessoa = telefonePessoa;
        this.celularPessoa = celularPessoa;
        this.emailPessoa = emailPessoa;
    }

     public String getNomePessoa() {
        return nomePessoa;
    }

    public String getCPFPessoa() {
        return CPFPessoa;
    }

    public LocalDate getDatanascPessoa() {
        return datanascPessoa;
    }

    public String getRGPessoa() {
        return RGPessoa;
    }

    public String getEnderecoPessoa() {
        return enderecoPessoa;
    }

    public String getTelefonePessoa() {
        return telefonePessoa;
    }

    public String getCelularPessoa() {
        return celularPessoa;
    }

    public String getEmailPessoa() {
        return emailPessoa;
    }
}
