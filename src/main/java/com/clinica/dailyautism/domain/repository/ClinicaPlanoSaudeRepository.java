// ClinicaPlanoSaudeRepository
package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.ClinicaPlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClinicaPlanoSaudeRepository extends JpaRepository<ClinicaPlanoSaude, String> {
    List<ClinicaPlanoSaude> findByClinicaIdClinica(String idClinica);
    List<ClinicaPlanoSaude> findByPlanoSaudeIdPlanoSaude(String idPlanoSaude);
    Optional<ClinicaPlanoSaude> findByClinicaIdClinicaAndPlanoSaudeIdPlanoSaude(String idClinica, String idPlanoSaude);
}