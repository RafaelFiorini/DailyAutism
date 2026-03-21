package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;

public class EmailAlreadyExistsException extends RequestException {
    public EmailAlreadyExistsException(String email) {
        super("Email já cadastrado: " + email, "EmailAlreadyExists");
    }
}