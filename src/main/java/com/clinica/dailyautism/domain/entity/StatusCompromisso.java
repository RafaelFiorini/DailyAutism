package com.clinica.dailyautism.domain.entity;

public enum StatusCompromisso {

    AGUARDANDO_APROVACAO,  // clínica criou, responsável ainda não aprovou
    AGENDADO,              // responsável aprovou
    REJEITADO,             // responsável rejeitou
    REALIZADO,             // compromisso aconteceu
    CANCELADO,             // cancelado por qualquer motivo
    FALTOU                 // paciente não compareceu
}