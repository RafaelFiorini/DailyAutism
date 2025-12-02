package com.clinica.dailyautism.infraestructure.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class SaveUserDTO {

    private final String nomeUser;
    private final String emailUser;
    private final String celularUser;
}
