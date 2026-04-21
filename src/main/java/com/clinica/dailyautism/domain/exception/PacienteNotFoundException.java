package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infrastructure.exception.RequestException;

public class PacienteNotFoundException extends RequestException {
    public PacienteNotFoundException(String pacienteId) {
        super("Paciente não encontrado: " + pacienteId, "PacienteNotFound");
    }
}