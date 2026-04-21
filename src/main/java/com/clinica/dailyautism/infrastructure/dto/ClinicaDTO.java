package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Clinica;
import lombok.Data;

@Data
public class ClinicaDTO {

    private final String idClinica;
    private final String nomeClinica;
    private final String cnpjClinica;
    private final String enderecoClinica;
    private final String telefoneClinica;
    private final String whatsappClinica;
    private final String emailClinica;

    public static ClinicaDTO create(Clinica clinica) {
        return new ClinicaDTO(
                clinica.getIdClinica(),
                clinica.getNomeClinica(),
                clinica.getCnpjClinica(),
                clinica.getEnderecoClinica(),
                clinica.getTelefoneClinica(),
                clinica.getWhatsappClinica(),
                clinica.getEmailClinica()
        );
    }
}