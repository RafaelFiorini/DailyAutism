package com.clinica.dailyautism.domain.entity;

import lombok.Getter;

@Getter
public enum AcaoAuditoria {
    LOGIN_SUCESSO("Login com Sucesso"),
    LOGIN_FALHA("Falha de Login"),
    LOGOUT("Logout"),
    CRIACAO("Criação de Registro"),
    EDICAO("Edição / Atualização"),
    EXCLUSAO("Exclusão / Desativação"),
    APROVACAO("Aprovação"),
    REJEICAO("Rejeição"),
    ASSINATURA_DIGITAL("Assinatura Digital"),
    CANCELAMENTO("Cancelamento"),
    EXPORTACAO("Exportação de Dados");

    private final String descricao;

    AcaoAuditoria(String descricao) {
        this.descricao = descricao;
    }
}
