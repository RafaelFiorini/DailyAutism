package com.clinica.dailyautism.infrastructure.dto;

import lombok.Data;

@Data
public class SaveUserDTO {

    private final String nomeUser;
    private final String emailUser;
    private final String passwordUser;
}
