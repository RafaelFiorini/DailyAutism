package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infrastructure.exception.RequestException;

public class PeriodicidadeNotFoundException extends RequestException {
    public PeriodicidadeNotFoundException(String periodicidadeId) {
        super("Periodicidade não encontrada: " + periodicidadeId, "PeriodicidadeNotFound");
    }
}