package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infrastructure.exception.RequestException;

public class PessoaAccessDeniedException extends RequestException {
    public PessoaAccessDeniedException(){
        super("CPF já cadastrado para outro usuário", "PessoaAccessDenied");
    }
}