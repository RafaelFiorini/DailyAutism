package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.domain.entity.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idArquivo", callSuper = false)
@ToString(exclude = {"arquivoBase64", "paciente", "tipoArquivo", "uploadadoPor", "clinicaCompartilhada", "profissionaisCompartilhados"})
@SQLRestriction("ativo = true")
public class Arquivo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idArquivo;

    @Column(nullable = false, length = 200)
    private String nomeArquivo;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataArquivo;

    @Column(nullable = false)
    private LocalDate dataUpload;

    @Column(columnDefinition = "LONGTEXT")
    private String arquivoBase64;

    private String urlArquivo;

    @Column(nullable = false, length = 10)
    private String extensaoArquivo;

    @ManyToOne
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idTipoArquivo", nullable = false)
    private TipoArquivo tipoArquivo;

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User uploadadoPor;

    @Builder.Default
    @Column(nullable = false)
    private boolean compartilhadoComResponsavel = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean compartilhadoComClinica = false;

    @ManyToOne
    @JoinColumn(name = "idClinicaCompartilhada")
    private Clinica clinicaCompartilhada;

    @ManyToMany
    @JoinTable(
            name = "arquivo_profissional",
            joinColumns = @JoinColumn(name = "idArquivo"),
            inverseJoinColumns = @JoinColumn(name = "idProfissional")
    )
    @Builder.Default
    private List<Profissional> profissionaisCompartilhados = new ArrayList<>();
}
