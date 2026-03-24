package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicaRepository extends JpaRepository<Clinica, String> {
}