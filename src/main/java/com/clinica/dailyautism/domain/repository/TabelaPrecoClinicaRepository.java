package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.TabelaPrecoClinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TabelaPrecoClinicaRepository extends JpaRepository<TabelaPrecoClinica, String> {

    List<TabelaPrecoClinica> findByClinicaIdClinica(String idClinica);

    Optional<TabelaPrecoClinica> findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIsNull(
            String idClinica, String idTipoCompromisso
    );

    Optional<TabelaPrecoClinica> findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIdPlanoSaude(
            String idClinica, String idTipoCompromisso, String idPlanoSaude
    );
}
