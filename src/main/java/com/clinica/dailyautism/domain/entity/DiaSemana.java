package com.clinica.dailyautism.domain.entity;

import java.time.DayOfWeek;

public enum DiaSemana {
    SEGUNDA(DayOfWeek.MONDAY, "Segunda-feira"),
    TERCA(DayOfWeek.TUESDAY, "Terça-feira"),
    QUARTA(DayOfWeek.WEDNESDAY, "Quarta-feira"),
    QUINTA(DayOfWeek.THURSDAY, "Quinta-feira"),
    SEXTA(DayOfWeek.FRIDAY, "Sexta-feira"),
    SABADO(DayOfWeek.SATURDAY, "Sábado"),
    DOMINGO(DayOfWeek.SUNDAY, "Domingo");

    private final DayOfWeek dayOfWeek;
    private final String descricao;

    DiaSemana(DayOfWeek dayOfWeek, String descricao) {
        this.dayOfWeek = dayOfWeek;
        this.descricao = descricao;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDescricao() {
        return descricao;
    }

    public static DiaSemana fromDayOfWeek(DayOfWeek dow) {
        for (DiaSemana ds : values()) {
            if (ds.dayOfWeek == dow) {
                return ds;
            }
        }
        throw new IllegalArgumentException("Dia da semana inválido: " + dow);
    }
}
