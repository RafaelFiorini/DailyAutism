package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;

public class TipoCompromissoNotFoundException extends RequestException {
    public TipoCompromissoNotFoundException(String tipoCompromissoId) {
        super("Tipo de compromisso não encontrado: " + tipoCompromissoId, "TipoCompromissoNotFound");
    }
}