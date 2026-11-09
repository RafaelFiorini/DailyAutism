package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.ProfissionalClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfissionalClinicaRepository extends JpaRepository<ProfissionalClinica, String> {

    List<ProfissionalClinica> findByProfissionalIdProf(String idProf);

    List<ProfissionalClinica> findByClinicaIdClinica(String idClinica);

    Optional<ProfissionalClinica> findByProfissionalAndClinica(Profissional profissional, Clinica clinica);

    boolean existsByProfissionalAndClinica(Profissional profissional, Clinica clinica);

    @Query("SELECT DISTINCT pc.profissional FROM ProfissionalClinica pc WHERE pc.clinica.idClinica IN :clinicaIds")
    List<Profissional> findProfissionaisByClinicaIds(@Param("clinicaIds") List<String> clinicaIds);
}
