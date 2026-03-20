package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Data
@Builder
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idPessoa;

    @Column(nullable = false, length = 255)
    private String nomePessoa;

    @Column(nullable = false, length = 11)
    private String CPFPessoa;

    @Column(nullable = false)
    private LocalDate datanascPessoa;

    @Column(nullable = false)
    private String RGPessoa;

    private String enderecoPessoa;

    private String telefonePessoa;

    //@Column(nullable = false, length = 11)
    private String celularPessoa;

    @Column(nullable = false, length = 255)
    private String emailPessoa;

    public Pessoa() {
    }

    public Pessoa(String idPessoa, String nomePessoa, String CPFPessoa, LocalDate datanascPessoa, String RGPessoa, String enderecoPessoa, String telefonePessoa, String celularPessoa, String emailPessoa) {
        this.idPessoa = idPessoa;
        this.nomePessoa = nomePessoa;
        this.CPFPessoa = CPFPessoa;
        this.datanascPessoa = datanascPessoa;
        this.RGPessoa = RGPessoa;
        this.enderecoPessoa = enderecoPessoa;
        this.telefonePessoa = telefonePessoa;
        this.celularPessoa = celularPessoa;
        this.emailPessoa = emailPessoa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(idPessoa, pessoa.idPessoa) && Objects.equals(nomePessoa, pessoa.nomePessoa) && Objects.equals(CPFPessoa, pessoa.CPFPessoa) && Objects.equals(datanascPessoa, pessoa.datanascPessoa) && Objects.equals(RGPessoa, pessoa.RGPessoa) && Objects.equals(enderecoPessoa, pessoa.enderecoPessoa) && Objects.equals(telefonePessoa, pessoa.telefonePessoa) && Objects.equals(celularPessoa, pessoa.celularPessoa) && Objects.equals(emailPessoa, pessoa.emailPessoa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPessoa, nomePessoa, CPFPessoa, datanascPessoa, RGPessoa, enderecoPessoa, telefonePessoa, celularPessoa, emailPessoa);
    }

    public String getIdPessoa() {
        return idPessoa;
    }

    public void setIdPessoa(String idPessoa) {
        this.idPessoa = idPessoa;
    }

    public String getNomePessoa() {
        return nomePessoa;
    }

    public void setNomePessoa(String nomePessoa) {
        this.nomePessoa = nomePessoa;
    }

    public String getCPFPessoa() {
        return CPFPessoa;
    }

    public void setCPFPessoa(String CPFPessoa) {
        this.CPFPessoa = CPFPessoa;
    }

    public LocalDate getDatanascPessoa() {
        return datanascPessoa;
    }

    public void setDatanascPessoa(LocalDate datanascPessoa) {
        this.datanascPessoa = datanascPessoa;
    }

    public String getRGPessoa() {
        return RGPessoa;
    }

    public void setRGPessoa(String RGPessoa) {
        this.RGPessoa = RGPessoa;
    }

    public String getEnderecoPessoa() {
        return enderecoPessoa;
    }

    public void setEnderecoPessoa(String enderecoPessoa) {
        this.enderecoPessoa = enderecoPessoa;
    }

    public String getTelefonePessoa() {
        return telefonePessoa;
    }

    public void setTelefonePessoa(String telefonePessoa) {
        this.telefonePessoa = telefonePessoa;
    }

    public String getCelularPessoa() {
        return celularPessoa;
    }

    public void setCelularPessoa(String celularPessoa) {
        this.celularPessoa = celularPessoa;
    }

    public String getEmailPessoa() {
        return emailPessoa;
    }

    public void setEmailPessoa(String emailPessoa) {
        this.emailPessoa = emailPessoa;
    }
 }

