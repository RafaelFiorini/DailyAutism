package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.GradeTerapeuticaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeTerapeuticaItemRepository extends JpaRepository<GradeTerapeuticaItem, String> {

    List<GradeTerapeuticaItem> findByGradeTerapeuticaIdGradeTerapeutica(String idGradeTerapeutica);

    List<GradeTerapeuticaItem> findByProfissionalIdProf(String idProf);
}
