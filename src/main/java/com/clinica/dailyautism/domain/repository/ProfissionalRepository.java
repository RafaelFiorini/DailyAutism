package com.clinica.dailyautism.domain.repository;


import com.clinica.dailyautism.domain.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProfissionalRepository extends JpaRepository<Profissional, String> {
}