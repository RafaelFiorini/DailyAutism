// ProfissionalClinicaRepository
package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.ProfissionalClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfissionalClinicaRepository extends JpaRepository<ProfissionalClinica, String> {
    List<ProfissionalClinica> findByProfissionalIdProf(String idProf);
    List<ProfissionalClinica> findByClinicaIdClinica(String idClinica);
}