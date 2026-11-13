package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.StatusCompromisso;
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

    List<Compromisso> findByProfissionalIdProfOrderByDataHoraCompromissoDesc(String idProf);

    List<Compromisso> findByProfissionalIdProfAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
            String idProf,
            LocalDateTime de,
            LocalDateTime ate
    );

    @Query("SELECT DISTINCT c.paciente FROM Compromisso c WHERE c.criadoPor.idUser = :idUser AND c.paciente IS NOT NULL")
    List<Paciente> findPacientesByCriadoPorIdUser(@Param("idUser") String idUser);

    List<Compromisso> findByPacienteIdPacienteAndStatusAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
            String pacienteId,
            StatusCompromisso status,
            LocalDateTime de,
            LocalDateTime ate
    );

    @Query("SELECT COUNT(c) FROM Compromisso c " +
           "WHERE c.profissional.idProf = :idProf " +
           "AND c.dataHoraCompromisso = :dataHora " +
           "AND c.status NOT IN (com.clinica.dailyautism.domain.entity.StatusCompromisso.CANCELADO, com.clinica.dailyautism.domain.entity.StatusCompromisso.REJEITADO) " +
           "AND (:idCompromissoIgnorado IS NULL OR c.idCompromisso <> :idCompromissoIgnorado)")
    long countConflitosProfissional(
            @Param("idProf") String idProf,
            @Param("dataHora") LocalDateTime dataHora,
            @Param("idCompromissoIgnorado") String idCompromissoIgnorado
    );
}
