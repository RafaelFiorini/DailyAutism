package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import lombok.Data;

@Data
public class SaveProfissionalDTO {

    private final Pessoa pessoaProf;
    private final String ConselhoProf;
    private final boolean ativoProf;
    }
