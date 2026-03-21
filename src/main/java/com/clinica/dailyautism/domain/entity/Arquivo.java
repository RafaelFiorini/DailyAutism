package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.domain.entity.security.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idArquivo;

    @Column(nullable = false, length = 200)
    private String nomeArquivo;

    @Column(nullable = false)
    private LocalDate dataArquivo;

    @Column(nullable = false)
    private LocalDate dataUpload;

    @Column(columnDefinition = "LONGTEXT")
    private String arquivoBase64;

    private String urlArquivo;

    @Column(nullable = false, length = 10)
    private String extensaoArquivo; // "pdf", "docx", "xlsx", "jpg", "png", "tiff"

    @ManyToOne
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idTipoArquivo", nullable = false)
    private TipoArquivo tipoArquivo;

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User uploadadoPor;
}