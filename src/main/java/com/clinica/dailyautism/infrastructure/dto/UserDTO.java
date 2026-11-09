package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isResponsavel")
    private boolean isResponsavel;

    private String idResponsavel;

    @JsonProperty("isProfissional")
    private boolean isProfissional;

    private String idProfissional;
    private String conselhoProf;

    @JsonProperty("isFuncionarioClinica")
    private boolean isFuncionarioClinica;

    @JsonProperty("isAdminClinica")
    private boolean isAdminClinica;

    private String idClinica;
    private String nomeClinica;
    private String cargoClinica;

    @JsonProperty("isAdmin")
    private boolean isAdmin;

    private Boolean ativo;

    public static UserDTO create(User user) {
        return create(user, null);
    }

    public static UserDTO create(User user, com.clinica.dailyautism.domain.entity.FuncionarioClinica funcionario) {
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
        boolean isAdminClinica = perfilNames.contains("ADMIN_CLINICA") || perfilNames.contains("ROLE_ADMIN_CLINICA");
        boolean isFuncClinica = funcionario != null
                || isAdminClinica
                || perfilNames.contains("CLINICA")
                || perfilNames.contains("FUNCIONARIO_CLINICA")
                || perfilNames.contains("ROLE_CLINICA")
                || perfilNames.contains("ROLE_FUNCIONARIO_CLINICA");

        String idClinica = null;
        String nomeClinica = null;
        String cargoClinica = null;

        if (funcionario != null && funcionario.getClinica() != null) {
            idClinica = funcionario.getClinica().getIdClinica();
            nomeClinica = funcionario.getClinica().getNomeClinica();
            cargoClinica = funcionario.getCargo();
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
                .isFuncionarioClinica(isFuncClinica)
                .isAdminClinica(isAdminClinica)
                .idClinica(idClinica)
                .nomeClinica(nomeClinica)
                .cargoClinica(cargoClinica)
                .isAdmin(isAdmin)
                .ativo(user.getAtivo())
                .build();
    }

    @JsonProperty("responsavel")
    public boolean getResponsavel() {
        return isResponsavel;
    }

    @JsonProperty("profissional")
    public boolean getProfissional() {
        return isProfissional;
    }

    @JsonProperty("funcionarioClinica")
    public boolean getFuncionarioClinica() {
        return isFuncionarioClinica;
    }

    @JsonProperty("adminClinica")
    public boolean getAdminClinica() {
        return isAdminClinica;
    }

    @JsonProperty("admin")
    public boolean getAdmin() {
        return isAdmin;
    }
}
