package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, String> {
    List<Compromisso> findByPacienteIdPacienteOrderByDataHoraCompromissoAsc(String pacienteId);

    List<Compromisso> findByPacienteIdPacienteInAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
            List<String> pacienteIds,
            LocalDateTime de,
            LocalDateTime ate
    );
}