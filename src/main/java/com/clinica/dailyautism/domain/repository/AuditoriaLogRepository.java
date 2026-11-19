package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, String>, JpaSpecificationExecutor<AuditoriaLog> {

    List<AuditoriaLog> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

    Page<AuditoriaLog> findByOrderByDataHoraDesc(Pageable pageable);
}
