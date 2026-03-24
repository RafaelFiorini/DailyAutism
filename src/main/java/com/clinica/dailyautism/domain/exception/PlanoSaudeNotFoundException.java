package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;

public class PlanoSaudeNotFoundException extends RequestException {
    public PlanoSaudeNotFoundException(String planoId) {
        super("Plano de saúde não encontrado: " + planoId, "PlanoSaudeNotFound");
    }
}