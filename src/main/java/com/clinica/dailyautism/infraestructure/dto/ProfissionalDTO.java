package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import lombok.Data;

@Data
public class ProfissionalDTO {

    private final String idProf;
    private final Pessoa pessoaProf;
    private final String conselhoProf;
    private final boolean ativoProf;

    public static ProfissionalDTO create(Profissional prof) {
        return new ProfissionalDTO(
                prof.getIdProf(),
                prof.getPessoaProf(),
                prof.getConselhoProf(),
                prof.isAtivoProf()
        );
    }


}
