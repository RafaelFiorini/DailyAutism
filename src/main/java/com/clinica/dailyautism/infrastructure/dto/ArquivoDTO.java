package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.Arquivo;
import com.clinica.dailyautism.domain.entity.TipoArmazenamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ArquivoDTO {

    private final String idArquivo;
    private final String nomeArquivo;
    private final String descricao;
    private final LocalDate dataArquivo;
    private final LocalDate dataUpload;
    private final String extensaoArquivo;
    private final String urlArquivo;
    private final TipoArmazenamento tipoArmazenamento;
    private final String objectKey;
    private final Long tamanhoBytes;
    private final String idPaciente;
    private final String nomePaciente;
    private final String idTipoArquivo;
    private final String nomeTipoArquivo;
    private final String idUser;
    private final String nomeUploadadoPor;
    private final boolean compartilhadoComResponsavel;
    private final boolean compartilhadoComClinica;
    private final String idClinicaCompartilhada;
    private final String nomeClinicaCompartilhada;
    private final List<ProfissionalResumo> profissionaisCompartilhados;

    @Data
    @AllArgsConstructor
    public static class ProfissionalResumo {
        private String idProf;
        private String nomeProfissional;
        private String conselhoProf;
    }

    public static ArquivoDTO create(Arquivo arquivo) {
        String idPac = arquivo.getPaciente() != null ? arquivo.getPaciente().getIdPaciente() : null;
        String nomePac = (arquivo.getPaciente() != null && arquivo.getPaciente().getPessoaPaciente() != null)
                ? arquivo.getPaciente().getPessoaPaciente().getNomePessoa()
                : (arquivo.getPaciente() != null ? arquivo.getPaciente().getApelidoPaciente() : null);

        String idTipo = arquivo.getTipoArquivo() != null ? arquivo.getTipoArquivo().getIdTipoArquivo() : null;
        String nomeTipo = arquivo.getTipoArquivo() != null ? arquivo.getTipoArquivo().getNomeTipo() : null;

        String idUser = arquivo.getUploadadoPor() != null ? arquivo.getUploadadoPor().getIdUser() : null;
        String nomeUser = arquivo.getUploadadoPor() != null ? arquivo.getUploadadoPor().getNomeUser() : null;

        String idClinica = arquivo.getClinicaCompartilhada() != null
                ? arquivo.getClinicaCompartilhada().getIdClinica()
                : null;
        String nomeClinica = arquivo.getClinicaCompartilhada() != null
                ? arquivo.getClinicaCompartilhada().getNomeClinica()
                : null;

        List<ProfissionalResumo> profs = arquivo.getProfissionaisCompartilhados() != null
                ? arquivo.getProfissionaisCompartilhados().stream()
                    .filter(p -> p.getAtivo() == null || p.getAtivo())
                    .map(p -> new ProfissionalResumo(
                            p.getIdProf(),
                            p.getPessoaProf() != null ? p.getPessoaProf().getNomePessoa() : "Profissional",
                            p.getConselhoProf()
                    ))
                    .toList()
                : List.of();

        return new ArquivoDTO(
                arquivo.getIdArquivo(),
                arquivo.getNomeArquivo(),
                arquivo.getDescricao(),
                arquivo.getDataArquivo(),
                arquivo.getDataUpload(),
                arquivo.getExtensaoArquivo(),
                arquivo.getUrlArquivo(),
                arquivo.getTipoArmazenamento(),
                arquivo.getObjectKey(),
                arquivo.getTamanhoBytes(),
                idPac,
                nomePac,
                idTipo,
                nomeTipo,
                idUser,
                nomeUser,
                arquivo.isCompartilhadoComResponsavel(),
                arquivo.isCompartilhadoComClinica(),
                idClinica,
                nomeClinica,
                profs
        );
    }
}
