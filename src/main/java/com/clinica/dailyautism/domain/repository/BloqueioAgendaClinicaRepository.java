package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.BloqueioAgendaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloqueioAgendaClinicaRepository extends JpaRepository<BloqueioAgendaClinica, String> {

    List<BloqueioAgendaClinica> findByClinicaIdClinicaOrderByDataInicioAsc(String idClinica);

    List<BloqueioAgendaClinica> findByClinicaIdClinicaInOrderByDataInicioAsc(List<String> clinicaIds);

    List<BloqueioAgendaClinica> findAllByOrderByDataInicioAsc();

    @Query("""
            SELECT b FROM BloqueioAgendaClinica b
            WHERE b.clinica.idClinica = :idClinica
              AND b.dataInicio <= :dataFim
              AND b.dataFim >= :dataInicio
            ORDER BY b.dataInicio ASC
            """)
    List<BloqueioAgendaClinica> findConflitantesPorPeriodo(
            @Param("idClinica") String idClinica,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
