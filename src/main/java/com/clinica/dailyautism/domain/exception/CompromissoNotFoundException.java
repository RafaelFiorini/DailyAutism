// CompromissoNotFoundException.java
package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infrastructure.exception.RequestException;

public class CompromissoNotFoundException extends RequestException {
    public CompromissoNotFoundException(String compromissoId) {
        super("Compromisso não encontrado: " + compromissoId, "CompromissoNotFound");
    }
}