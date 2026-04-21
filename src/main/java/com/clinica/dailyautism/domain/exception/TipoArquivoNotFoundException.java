package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infrastructure.exception.RequestException;

public class TipoArquivoNotFoundException extends RequestException {
    public TipoArquivoNotFoundException(String tipoArquivoId) {
        super("Tipo de arquivo não encontrado: " + tipoArquivoId, "TipoArquivoNotFound");
    }
}