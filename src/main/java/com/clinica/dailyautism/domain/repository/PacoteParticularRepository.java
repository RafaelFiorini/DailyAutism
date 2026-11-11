package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.PacoteParticular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacoteParticularRepository extends JpaRepository<PacoteParticular, String> {

    List<PacoteParticular> findByClinicaIdClinicaOrderByNomePacoteAsc(String idClinica);
}
