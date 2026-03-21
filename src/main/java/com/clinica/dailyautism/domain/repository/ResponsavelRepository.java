package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.infraestructure.exception.RequestException;
import org.springframework.data.jpa.repository.JpaRepository;

public class ResponsavelNotFoundException extends RequestException {
    public ResponsavelNotFoundException(String responsavelId) {
        super("Responsável não encontrado: " + responsavelId, "ResponsavelNotFound");
    }
}