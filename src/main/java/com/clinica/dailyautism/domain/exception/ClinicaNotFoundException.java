package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;

public class ClinicaNotFoundException extends RequestException {
    public ClinicaNotFoundException(String clinicaId) {
        super("Clínica não encontrada: " + clinicaId, "ClinicaNotFound");
    }
}