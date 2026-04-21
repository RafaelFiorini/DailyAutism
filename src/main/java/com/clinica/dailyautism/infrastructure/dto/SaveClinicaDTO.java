package com.clinica.dailyautism.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveClinicaDTO {

    @NotNull(message = "O nome da clínica é obrigatório")
    private String nomeClinica;

    @NotNull(message = "O CNPJ é obrigatório")
    private String cnpjClinica;

    private String enderecoClinica;
    private String telefoneClinica;
    private String whatsappClinica;
    private String emailClinica;
}