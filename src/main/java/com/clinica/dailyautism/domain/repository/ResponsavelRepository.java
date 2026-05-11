package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResponsavelRepository extends JpaRepository<Responsavel, String> {

    // Todos os pacientes vinculados ao responsável
    @Query("SELECT p FROM Paciente p JOIN p.responsaveis r WHERE r.idResponsavel = :idResponsavel")
    List<Paciente> findPacientesByResponsavelId(@Param("idResponsavel") String idResponsavel);

    // Agenda da semana de todos os pacientes do responsável
    @Query("""
        SELECT c FROM Compromisso c
        WHERE c.paciente.idPaciente IN (
            SELECT p.idPaciente FROM Paciente p
            JOIN p.responsaveis r
            WHERE r.idResponsavel = :idResponsavel
        )
        AND c.dataHoraCompromisso BETWEEN :inicio AND :fim
        ORDER BY c.dataHoraCompromisso ASC
    """)
    List<Compromisso> findAgendaSemanaByResponsavel(
            @Param("idResponsavel") String idResponsavel,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    Optional<Responsavel> findByPessoaResponsavel(Pessoa pessoa);
}