package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Profissional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalDTO {

    private String idProf;
    private String idPessoa;
    private String nomeProfissional;
    private String cpf;
    private String email;
    private String telefone;
    private String conselhoProf;
    private String rg;
    private String endereco;
    private LocalDate datanasc;

    public static ProfissionalDTO create(Profissional profissional) {
        String idPessoa = null;
        String nome = null;
        String cpf = null;
        String email = null;
        String telefone = null;
        String rg = null;
        String endereco = null;
        LocalDate datanasc = null;

        if (profissional.getPessoaProf() != null) {
            idPessoa = profissional.getPessoaProf().getIdPessoa();
            nome = profissional.getPessoaProf().getNomePessoa();
            cpf = profissional.getPessoaProf().getCPFPessoa();
            email = profissional.getPessoaProf().getEmailPessoa();
            telefone = profissional.getPessoaProf().getCelularPessoa() != null
                    ? profissional.getPessoaProf().getCelularPessoa()
                    : profissional.getPessoaProf().getTelefonePessoa();
            rg = profissional.getPessoaProf().getRGPessoa();
            endereco = profissional.getPessoaProf().getEnderecoPessoa();
            datanasc = profissional.getPessoaProf().getDatanascPessoa();
        }

        return new ProfissionalDTO(
                profissional.getIdProf(),
                idPessoa,
                nome,
                cpf,
                email,
                telefone,
                profissional.getConselhoProf(),
                rg,
                endereco,
                datanasc
        );
    }
}
