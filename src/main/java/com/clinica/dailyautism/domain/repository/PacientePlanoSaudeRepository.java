// PacientePlanoSaudeRepository
package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.PacientePlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacientePlanoSaudeRepository extends JpaRepository<PacientePlanoSaude, String> {
    List<PacientePlanoSaude> findByPacienteIdPaciente(String idPaciente);
    List<PacientePlanoSaude> findByPlanoSaudeIdPlanoSaude(String idPlanoSaude);
}