package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, String> {

    // Agenda da semana do profissional
    @Query("""
        SELECT c FROM Compromisso c
        WHERE c.paciente.idPaciente IN (
            SELECT pp.paciente.idPaciente FROM ProfissionalPaciente pp
            WHERE pp.profissional.idProf = :idProf
        )
        AND c.dataHoraCompromisso BETWEEN :inicio AND :fim
        ORDER BY c.dataHoraCompromisso ASC
    """)
    List<Compromisso> findAgendaSemanaByProfissional(
            @Param("idProf") String idProf,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}