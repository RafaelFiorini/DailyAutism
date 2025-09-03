package com.clinica.dailyautism.domain.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Pessoa {

    private String IdPessoa;
    private String CPFPessoa;
    private LocalDate DataNascPessoa;
    private String RGPessoa;
    private String Endereco;

    public Pessoa(String endereco, String RGPessoa, LocalDate dataNascPessoa, String CPFPessoa, String idPessoa) {
        Endereco = endereco;
        this.RGPessoa = RGPessoa;
        DataNascPessoa = dataNascPessoa;
        this.CPFPessoa = CPFPessoa;
        IdPessoa = idPessoa;
    }

    public Pessoa() {

    }

    public String getIdPessoa() {
        return IdPessoa;
    }

    public void setIdPessoa(String idPessoa) {
        IdPessoa = idPessoa;
    }

    public String getCPFPessoa() {
        return CPFPessoa;
    }

    public void setCPFPessoa(String CPFPessoa) {
        this.CPFPessoa = CPFPessoa;
    }

    public LocalDate getDataNascPessoa() {
        return DataNascPessoa;
    }

    public void setDataNascPessoa(LocalDate dataNascPessoa) {
        DataNascPessoa = dataNascPessoa;
    }

    public String getRGPessoa() {
        return RGPessoa;
    }

    public void setRGPessoa(String RGPessoa) {
        this.RGPessoa = RGPessoa;
    }

    public String getEndereco() {
        return Endereco;
    }

    public void setEndereco(String endereco) {
        Endereco = endereco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(IdPessoa, pessoa.IdPessoa) && Objects.equals(CPFPessoa, pessoa.CPFPessoa) && Objects.equals(DataNascPessoa, pessoa.DataNascPessoa) && Objects.equals(RGPessoa, pessoa.RGPessoa) && Objects.equals(Endereco, pessoa.Endereco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IdPessoa, CPFPessoa, DataNascPessoa, RGPessoa, Endereco);
    }
}

