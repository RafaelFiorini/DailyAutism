package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infraestructure.exception.RequestException;


public class PessoaNotFoundException extends RequestException {

    public PessoaNotFoundException(String pessoaId){
        super("Não existe pessoa" + pessoaId, "PessoaNotFound");
    }

}
