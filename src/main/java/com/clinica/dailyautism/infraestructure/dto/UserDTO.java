package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.security.User;
import lombok.Data;

@Data
public class UserDTO {

    private final String idUser;
    private final String nomeUser;
    private final String emailUser;
    private final String idPessoa;

    public static UserDTO create(User user) {
        return new UserDTO(
                user.getIdUser(),
                user.getNomeUser(),
                user.getEmailUser(),
                user.getPessoa() != null ? user.getPessoa().getIdPessoa() : null
        );
    }
}
