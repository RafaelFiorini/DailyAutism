package com.clinica.dailyautism.domain.entity;

import lombok.Getter;

@Getter
public enum CategoriaAuditoria {
    AUTENTICACAO("Autenticação e Acesso"),
    CLINICO("Atendimento Clínico"),
    GRADE_TERAPEUTICA("Grade Terapêutica"),
    FINANCEIRO("Financeiro e Repasse"),
    CADASTRO("Cadastro e Pacientes"),
    SEGURANCA("Segurança e Permissões");

    private final String descricao;

    CategoriaAuditoria(String descricao) {
        this.descricao = descricao;
    }
}
