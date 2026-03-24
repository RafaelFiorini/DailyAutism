package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, String> {

}