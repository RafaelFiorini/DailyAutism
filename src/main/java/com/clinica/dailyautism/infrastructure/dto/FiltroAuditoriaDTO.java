package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.AcaoAuditoria;
import com.clinica.dailyautism.domain.entity.CategoriaAuditoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroAuditoriaDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;

    private CategoriaAuditoria categoria;
    private AcaoAuditoria acao;
    private String idUsuario;
    private String loginUsuario;
    private String entidade;
    private String idEntidade;
    private String buscaTexto;
    private String idClinica;
}
