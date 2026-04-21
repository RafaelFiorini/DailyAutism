package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {

    private final String idUser;
    private final String nomeUser;
    private final String emailUser;
    private final String idPessoa;
    private final List<String> perfis;
    private final String idResponsavel;
    private final String idProfissional;

    public static UserDTO create(User user) {
        Pessoa pessoa = user.getPessoa();

        String idResponsavel = null;
        String idProfissional = null;

        if (pessoa != null) {
            if (pessoa.getResponsavel() != null) {
                idResponsavel = pessoa.getResponsavel().getIdResponsavel();
            }
            if (pessoa.getProfissional() != null) {
                idProfissional = pessoa.getProfissional().getIdProf();
            }
        }

        return new UserDTO(
                user.getIdUser(),
                user.getNomeUser(),
                user.getEmailUser(),
                pessoa != null ? pessoa.getIdPessoa() : null,
                user.getPerfis().stream().map(Perfil::getNome).toList(),
                idResponsavel,
                idProfissional
        );
    }
}