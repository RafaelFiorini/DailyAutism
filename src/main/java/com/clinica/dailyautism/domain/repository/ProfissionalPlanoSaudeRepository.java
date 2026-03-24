// ProfissionalPlanoSaudeRepository
package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.ProfissionalPlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfissionalPlanoSaudeRepository extends JpaRepository<ProfissionalPlanoSaude, String> {
    List<ProfissionalPlanoSaude> findByProfissionalIdProf(String idProf);
    List<ProfissionalPlanoSaude> findByPlanoSaudeIdPlanoSaude(String idPlanoSaude);
}