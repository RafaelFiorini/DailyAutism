package com.clinica.dailyautism.infraestructure.dto;

import com.clinica.dailyautism.domain.entity.Arquivo;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ArquivoDTO {

    private final String idArquivo;
    private final String nomeArquivo;
    private final LocalDate dataArquivo;
    private final LocalDate dataUpload;
    private final String extensaoArquivo;
    private final String urlArquivo;
    private final String idPaciente;
    private final String nomePaciente;
    private final String idTipoArquivo;
    private final String nomeTipoArquivo;
    private final String idUser;

    public static ArquivoDTO create(Arquivo arquivo) {
        return new ArquivoDTO(
                arquivo.getIdArquivo(),
                arquivo.getNomeArquivo(),
                arquivo.getDataArquivo(),
                arquivo.getDataUpload(),
                arquivo.getExtensaoArquivo(),
                arquivo.getUrlArquivo(),
                arquivo.getPaciente().getIdPaciente(),
                arquivo.getPaciente().getPessoaPaciente().getNomePessoa(),
                arquivo.getTipoArquivo().getIdTipoArquivo(),
                arquivo.getTipoArquivo().getNomeTipo(),
                arquivo.getUploadadoPor().getIdUser()
        );
    }
}