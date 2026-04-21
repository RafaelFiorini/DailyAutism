package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PessoaDTO {

    private final String idPessoa;
    private final String nomePessoa;
    private final String CPFPessoa;
    private final LocalDate datanascPessoa;
    private final String RGPessoa;
    private final String enderecoPessoa;
    private final String telefonePessoa;
    private final String celularPessoa;
    private final String emailPessoa;
    private final String idProfissional;

    public static PessoaDTO create(Pessoa pessoa) {
        String idProfissional = pessoa.getProfissional() != null
                ? pessoa.getProfissional().getIdProf()
                : null;

        return new PessoaDTO(
                pessoa.getIdPessoa(),
                pessoa.getNomePessoa(),
                pessoa.getCPFPessoa(),
                pessoa.getDatanascPessoa(),
                pessoa.getRGPessoa(),
                pessoa.getEnderecoPessoa(),
                pessoa.getTelefonePessoa(),
                pessoa.getCelularPessoa(),
                pessoa.getEmailPessoa(),
                idProfissional
        );
    }
}