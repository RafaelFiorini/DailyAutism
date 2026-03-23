package com.clinica.dailyautism.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(nullable = false)
    private Boolean ativo = true;

    public void desativar() {
        this.ativo = false;
    }
}