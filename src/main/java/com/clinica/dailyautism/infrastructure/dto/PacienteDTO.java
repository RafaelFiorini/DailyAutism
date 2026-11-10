package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.TipoLiberacaoGuia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteDTO {

    private String idPaciente;
    private String idPessoa;
    private String nomePessoa;
    private String cpf;
    private String apelidoPaciente;
    private Boolean isResponsavel;
    private Boolean hasOutroResponsavel;
    private String rg;
    private String endereco;
    private String telefone;
    private String celular;
    private String email;
    private LocalDate datanasc;
    private List<String> planos;
    private List<String> clinicas;
    private List<String> responsaveis;
    private String idPlanoSaude;
    private String idPacientePlanoSaude;
    private String numeroCarteirinha;
    private LocalDate validadeCarteirinha;
    private String numeroGuia;
    private TipoLiberacaoGuia tipoLiberacao;
    private LocalDate dataInicioLiberacao;
    private LocalDate vencimentoLiberacao;
    private Integer quantidadeSessoesLiberadas;
    private Long diasParaVencimento;
    private String statusGuia;
    private List<PacientePlanoSaudeDTO> guias;
    private String idClinica;
    private String idResponsavel;

    public static PacienteDTO create(Paciente paciente) {
        String idPessoa = null;
        String nome = null;
        String cpf = null;
        String rg = null;
        String endereco = null;
        String telefone = null;
        String celular = null;
        String email = null;
        LocalDate datanasc = null;

        if (paciente.getPessoaPaciente() != null) {
            idPessoa = paciente.getPessoaPaciente().getIdPessoa();
            nome = paciente.getPessoaPaciente().getNomePessoa();
            cpf = paciente.getPessoaPaciente().getCPFPessoa();
            rg = paciente.getPessoaPaciente().getRGPessoa();
            endereco = paciente.getPessoaPaciente().getEnderecoPessoa();
            telefone = paciente.getPessoaPaciente().getTelefonePessoa();
            celular = paciente.getPessoaPaciente().getCelularPessoa();
            email = paciente.getPessoaPaciente().getEmailPessoa();
            datanasc = paciente.getPessoaPaciente().getDatanascPessoa();
        }

        List<String> planos = new ArrayList<>();
        List<PacientePlanoSaudeDTO> guias = new ArrayList<>();
        String idPlano = null;
        String idPacientePlano = null;
        String numCart = null;
        LocalDate validadeCart = null;
        String numeroGuia = null;
        TipoLiberacaoGuia tipoLib = null;
        LocalDate dataInicioLib = null;
        LocalDate vencLib = null;
        Integer qtdSessoes = null;
        Long diasVenc = null;
        String statusGuia = null;

        if (paciente.getPlanos() != null) {
            for (var p : paciente.getPlanos()) {
                if (p.getAtivo() != null && !p.getAtivo()) continue;
                PacientePlanoSaudeDTO guiaDto = PacientePlanoSaudeDTO.create(p);
                if (guiaDto != null) {
                    guias.add(guiaDto);
                }
                if (p.getPlanoSaude() != null) {
                    planos.add(p.getPlanoSaude().getNomePlano());
                    if (idPlano == null) {
                        idPlano = p.getPlanoSaude().getIdPlanoSaude();
                        idPacientePlano = p.getIdPacientePlanoSaude();
                        numCart = p.getNumeroCarteirinha();
                        validadeCart = p.getValidadeCarteirinha();
                        numeroGuia = p.getNumeroGuia();
                        tipoLib = p.getTipoLiberacao() != null ? p.getTipoLiberacao() : TipoLiberacaoGuia.PERIODO;
                        dataInicioLib = p.getDataInicioLiberacao();
                        vencLib = p.getVencimentoLiberacao();
                        qtdSessoes = p.getQuantidadeSessoesLiberadas();
                        if (guiaDto != null) {
                            diasVenc = guiaDto.getDiasParaVencimento();
                            statusGuia = guiaDto.getStatusGuia();
                        }
                    }
                }
            }
        }

        // Quando o paciente for Particular (sem plano ativo), usa o limite de terapias direto do Paciente
        if (idPlano == null) {
            tipoLib = paciente.getTipoLiberacao();
            dataInicioLib = paciente.getDataInicioLiberacao();
            vencLib = paciente.getVencimentoLiberacao();
            qtdSessoes = paciente.getQuantidadeSessoesLiberadas();
            diasVenc = PacientePlanoSaudeDTO.calcularDiasParaVencimento(vencLib);
            statusGuia = PacientePlanoSaudeDTO.calcularStatusGuia(vencLib, qtdSessoes);
        }

        List<String> clinicas = new ArrayList<>();
        String idClinica = null;
        if (paciente.getClinicas() != null) {
            for (var c : paciente.getClinicas()) {
                if (c.getAtivo() != null && !c.getAtivo()) continue;
                if (c.getClinica() != null) {
                    clinicas.add(c.getClinica().getNomeClinica());
                    if (idClinica == null) {
                        idClinica = c.getClinica().getIdClinica();
                    }
                }
            }
        }

        List<String> responsaveis = new ArrayList<>();
        boolean temOutroResponsavel = false;
        String idResp = null;
        if (paciente.getResponsaveis() != null) {
            for (var r : paciente.getResponsaveis()) {
                if (r.getAtivo() != null && !r.getAtivo()) continue;
                if (r.getPessoaResponsavel() != null) {
                    responsaveis.add(r.getPessoaResponsavel().getNomePessoa());
                    if (idPessoa == null || !idPessoa.equals(r.getPessoaResponsavel().getIdPessoa())) {
                        temOutroResponsavel = true;
                        if (idResp == null) {
                            idResp = r.getIdResponsavel();
                        }
                    } else if (idResp == null) {
                        idResp = r.getIdResponsavel();
                    }
                }
            }
        }

        return PacienteDTO.builder()
                .idPaciente(paciente.getIdPaciente())
                .idPessoa(idPessoa)
                .nomePessoa(nome)
                .cpf(cpf)
                .apelidoPaciente(paciente.getApelidoPaciente())
                .isResponsavel(paciente.isResponsavel())
                .hasOutroResponsavel(temOutroResponsavel)
                .rg(rg)
                .endereco(endereco)
                .telefone(telefone)
                .celular(celular)
                .email(email)
                .datanasc(datanasc)
                .planos(planos)
                .clinicas(clinicas)
                .responsaveis(responsaveis)
                .idPlanoSaude(idPlano)
                .idPacientePlanoSaude(idPacientePlano)
                .numeroCarteirinha(numCart)
                .validadeCarteirinha(validadeCart)
                .numeroGuia(numeroGuia)
                .tipoLiberacao(tipoLib)
                .dataInicioLiberacao(dataInicioLib)
                .vencimentoLiberacao(vencLib)
                .quantidadeSessoesLiberadas(qtdSessoes)
                .diasParaVencimento(diasVenc)
                .statusGuia(statusGuia)
                .guias(guias)
                .idClinica(idClinica)
                .idResponsavel(idResp)
                .build();
    }
}
