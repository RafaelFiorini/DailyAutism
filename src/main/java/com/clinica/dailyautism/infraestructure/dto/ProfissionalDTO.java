package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Profissional;
import lombok.Data;

@Data
public class ProfissionalDTO {

    private final String idProf;
    private final String idPessoa;
    private final String nomeProfissional;
    private final String conselhoProf;
   // private final boolean ativoProf;

    public static ProfissionalDTO create(Profissional profissional) {
        return new ProfissionalDTO(
                profissional.getIdProf(),
                profissional.getPessoaProf().getIdPessoa(),
                profissional.getPessoaProf().getNomePessoa(),
                profissional.getConselhoProf()
               // profissional.isAtivoProf()
        );
    }
}