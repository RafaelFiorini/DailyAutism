package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.User;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserDTO {

    private final String idUser;
    private final String nomeUser;
    private final String passwordUser;
    private final String emailUser;

    public static UserDTO create(User user) {
        return new UserDTO(
                user.getIdUser(),
                user.getNomeUser(),
                user.getPasswordUser(),
                user.getEmailUser()
        );
    }
}
