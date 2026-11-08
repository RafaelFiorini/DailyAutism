package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Responsavel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsavelDTO {

    private String idResponsavel;
    private String idPessoa;
    private String nomeResponsavel;
    private String cpf;
    private String email;
    private String telefone;
    private Boolean isPaciente;
    private List<PacienteResumoDTO> pacientes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteResumoDTO {
        private String idPaciente;
        private String nomePaciente;
        private String apelidoPaciente;
    }

    public static ResponsavelDTO create(Responsavel responsavel) {
        String idPessoa = null;
        String nome = null;
        String cpf = null;
        String email = null;
        String telefone = null;
        boolean isPac = false;

        if (responsavel.getPessoaResponsavel() != null) {
            idPessoa = responsavel.getPessoaResponsavel().getIdPessoa();
            nome = responsavel.getPessoaResponsavel().getNomePessoa();
            cpf = responsavel.getPessoaResponsavel().getCPFPessoa();
            email = responsavel.getPessoaResponsavel().getEmailPessoa();
            telefone = responsavel.getPessoaResponsavel().getCelularPessoa() != null
                    ? responsavel.getPessoaResponsavel().getCelularPessoa()
                    : responsavel.getPessoaResponsavel().getTelefonePessoa();
        }

        List<PacienteResumoDTO> listaPacientes = new ArrayList<>();
        if (responsavel.getPacientes() != null) {
            for (var p : responsavel.getPacientes()) {
                if (p.getAtivo() != null && !p.getAtivo()) {
                    continue;
                }
                String nomePac = (p.getPessoaPaciente() != null) ? p.getPessoaPaciente().getNomePessoa() : "Sem nome";
                listaPacientes.add(new PacienteResumoDTO(p.getIdPaciente(), nomePac, p.getApelidoPaciente()));

                if (p.getPessoaPaciente() != null && idPessoa != null && idPessoa.equals(p.getPessoaPaciente().getIdPessoa())) {
                    isPac = true;
                }
            }
        }

        return new ResponsavelDTO(
                responsavel.getIdResponsavel(),
                idPessoa,
                nome,
                cpf,
                email,
                telefone,
                isPac,
                listaPacientes
        );
    }
}
