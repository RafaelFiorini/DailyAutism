package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, String> {
    List<Compromisso> findByPacienteIdPacienteOrderByDataHoraCompromissoAsc(String pacienteId);
}