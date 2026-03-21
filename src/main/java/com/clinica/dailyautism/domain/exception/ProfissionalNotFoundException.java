package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;

public class ProfissionalNotFoundException extends RequestException {
    public ProfissionalNotFoundException(String profissionalId) {
        super("Profissional não encontrado: " + profissionalId, "ProfissionalNotFound");
    }
}