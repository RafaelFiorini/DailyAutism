package com.clinica.dailyautism.domain.entity;

import java.time.LocalDate;

public class Paciente {

    private String idPaciente;
    private Pessoa idPessoa;
    private String apelidoPaciente;
    private char planoPaciente;
    private LocalDate vencimentoLiberacaoPlanoPaciente;
    private boolean isResponsavel;
    private Responsavel idResponsavel;

}
