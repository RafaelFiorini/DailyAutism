package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.PlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoSaudeRepository extends JpaRepository<PlanoSaude, String> {
}