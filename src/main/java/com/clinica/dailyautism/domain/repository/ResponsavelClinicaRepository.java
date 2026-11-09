package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.ResponsavelClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResponsavelClinicaRepository extends JpaRepository<ResponsavelClinica, String> {

    List<ResponsavelClinica> findByResponsavelIdResponsavel(String idResponsavel);

    List<ResponsavelClinica> findByClinicaIdClinica(String idClinica);

    Optional<ResponsavelClinica> findByResponsavelAndClinica(Responsavel responsavel, Clinica clinica);

    boolean existsByResponsavelAndClinica(Responsavel responsavel, Clinica clinica);

    void deleteByResponsavel(Responsavel responsavel);

    @Query("SELECT DISTINCT rc.responsavel FROM ResponsavelClinica rc WHERE rc.clinica.idClinica IN :clinicaIds")
    List<Responsavel> findResponsaveisByClinicaIds(@Param("clinicaIds") List<String> clinicaIds);
}
