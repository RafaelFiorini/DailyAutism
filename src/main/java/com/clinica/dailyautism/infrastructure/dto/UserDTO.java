package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String idUser;
    private String nomeUser;
    private String emailUser;
    private String idPessoa;
    private String nomePessoa;
    private String cpf;
    private LocalDate datanasc;
    private String rg;
    private String endereco;
    private String telefone;
    private String celular;
    private List<String> perfis;
    private boolean isResponsavel;
    private String idResponsavel;
    private boolean isProfissional;
    private String idProfissional;
    private String conselhoProf;
    private boolean isAdmin;
    private Boolean ativo;

    public static UserDTO create(User user) {
        if (user == null) return null;

        Pessoa pessoa = user.getPessoa();

        String idResponsavel = null;
        boolean isResp = false;
        String idProfissional = null;
        boolean isProf = false;
        String conselhoProf = null;

        if (pessoa != null) {
            if (pessoa.getResponsavel() != null) {
                idResponsavel = pessoa.getResponsavel().getIdResponsavel();
                isResp = true;
            }
            if (pessoa.getProfissional() != null) {
                idProfissional = pessoa.getProfissional().getIdProf();
                isProf = true;
                conselhoProf = pessoa.getProfissional().getConselhoProf();
            }
        }

        List<String> perfilNames = user.getPerfis() != null
                ? user.getPerfis().stream().map(Perfil::getNome).toList()
                : List.of();

        boolean isAdmin = perfilNames.contains("ADMIN") || perfilNames.contains("ROLE_ADMIN");
        if (perfilNames.contains("RESPONSAVEL") || perfilNames.contains("ROLE_RESPONSAVEL")) {
            isResp = true;
        }
        if (perfilNames.contains("PROFISSIONAL") || perfilNames.contains("ROLE_PROFISSIONAL")) {
            isProf = true;
        }

        return UserDTO.builder()
                .idUser(user.getIdUser())
                .nomeUser(user.getNomeUser())
                .emailUser(user.getEmailUser())
                .idPessoa(pessoa != null ? pessoa.getIdPessoa() : null)
                .nomePessoa(pessoa != null ? pessoa.getNomePessoa() : user.getNomeUser())
                .cpf(pessoa != null ? pessoa.getCPFPessoa() : null)
                .datanasc(pessoa != null ? pessoa.getDatanascPessoa() : null)
                .rg(pessoa != null ? pessoa.getRGPessoa() : null)
                .endereco(pessoa != null ? pessoa.getEnderecoPessoa() : null)
                .telefone(pessoa != null ? pessoa.getTelefonePessoa() : null)
                .celular(pessoa != null ? pessoa.getCelularPessoa() : null)
                .perfis(perfilNames)
                .isResponsavel(isResp)
                .idResponsavel(idResponsavel)
                .isProfissional(isProf)
                .idProfissional(idProfissional)
                .conselhoProf(conselhoProf)
                .isAdmin(isAdmin)
                .ativo(user.getAtivo())
                .build();
    }
}
