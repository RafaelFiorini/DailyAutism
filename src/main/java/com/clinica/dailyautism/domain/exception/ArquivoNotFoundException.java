// ArquivoNotFoundException.java
package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;

public class ArquivoNotFoundException extends RequestException {
    public ArquivoNotFoundException(String arquivoId) {
        super("Arquivo não encontrado: " + arquivoId, "ArquivoNotFound");
    }
}