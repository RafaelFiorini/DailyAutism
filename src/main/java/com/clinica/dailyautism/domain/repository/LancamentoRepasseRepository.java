package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.LancamentoRepasseProfissional;
import com.clinica.dailyautism.domain.entity.StatusLancamentoRepasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LancamentoRepasseRepository extends JpaRepository<LancamentoRepasseProfissional, String> {

    Optional<LancamentoRepasseProfissional> findByCompromisso_IdCompromisso(String idCompromisso);

    List<LancamentoRepasseProfissional> findByProfissional_IdProfAndDataCompetencia(
            String idProf, LocalDate dataCompetencia);

    List<LancamentoRepasseProfissional> findByProfissional_IdProfAndDataCompetenciaBetween(
            String idProf, LocalDate inicio, LocalDate fim);

    List<LancamentoRepasseProfissional> findByProfissional_IdProfAndClinica_IdClinicaAndDataCompetencia(
            String idProf, String idClinica, LocalDate dataCompetencia);

    List<LancamentoRepasseProfissional> findByProfissional_IdProfAndClinica_IdClinicaAndDataCompetenciaAndStatusPagamento(
            String idProf, String idClinica, LocalDate dataCompetencia, StatusLancamentoRepasse status);

    List<LancamentoRepasseProfissional> findByProfissional_IdProfAndClinicaIsNullAndDataCompetencia(
            String idProf, LocalDate dataCompetencia);

    @Query("SELECT l FROM LancamentoRepasseProfissional l " +
           "WHERE l.profissional.idProf = :idProf " +
           "AND l.dataCompetencia = :dataCompetencia " +
           "AND (:idClinica IS NULL OR l.clinica.idClinica = :idClinica) " +
           "AND (:status IS NULL OR l.statusPagamento = :status) " +
           "ORDER BY l.dataAtendimento ASC")
    List<LancamentoRepasseProfissional> filtrarLancamentos(
            @Param("idProf") String idProf,
            @Param("dataCompetencia") LocalDate dataCompetencia,
            @Param("idClinica") String idClinica,
            @Param("status") StatusLancamentoRepasse status);
}
