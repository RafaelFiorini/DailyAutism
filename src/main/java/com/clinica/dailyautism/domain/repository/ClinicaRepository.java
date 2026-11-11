package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClinicaRepository extends JpaRepository<Clinica, String> {

    @Query(value = "SELECT * FROM clinica WHERE ativo = false ORDER BY nome_clinica ASC", nativeQuery = true)
    List<Clinica> findAllInactive();

    @Query(value = "SELECT * FROM clinica WHERE id_clinica = :id LIMIT 1", nativeQuery = true)
    Optional<Clinica> findByIdAnyAtivo(@Param("id") String id);

    @Query(value = "SELECT * FROM clinica WHERE REPLACE(REPLACE(REPLACE(REPLACE(cnpj_clinica, '.', ''), '/', ''), '-', ''), ' ', '') = :cnpjLimpo LIMIT 1", nativeQuery = true)
    Optional<Clinica> findByCnpjAnyAtivo(@Param("cnpjLimpo") String cnpjLimpo);
}