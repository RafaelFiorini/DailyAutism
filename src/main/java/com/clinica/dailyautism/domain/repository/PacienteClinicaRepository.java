// PacienteClinicaRepository
package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.PacienteClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacienteClinicaRepository extends JpaRepository<PacienteClinica, String> {
    List<PacienteClinica> findByPacienteIdPaciente(String idPaciente);
    List<PacienteClinica> findByClinicaIdClinica(String idClinica);
}