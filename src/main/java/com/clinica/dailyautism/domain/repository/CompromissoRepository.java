package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.domain.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, String> {
    List<Compromisso> findByPacienteIdPacienteOrderByDataHoraCompromissoAsc(String pacienteId);

    List<Compromisso> findAllByOrderByDataHoraCompromissoDesc();

    List<Compromisso> findByPacienteIdPacienteInOrderByDataHoraCompromissoDesc(List<String> pacienteIds);

    List<Compromisso> findByPacienteIdPacienteInAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
            List<String> pacienteIds,
            LocalDateTime de,
            LocalDateTime ate
    );

    List<Compromisso> findByCriadoPorIdUser(String idUser);

    @Query("SELECT DISTINCT c.paciente FROM Compromisso c WHERE c.criadoPor.idUser = :idUser AND c.paciente IS NOT NULL")
    List<Paciente> findPacientesByCriadoPorIdUser(@Param("idUser") String idUser);
}
