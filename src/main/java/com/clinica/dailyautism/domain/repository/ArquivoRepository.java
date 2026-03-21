package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArquivoRepository extends JpaRepository<Arquivo, String> {
    List<Arquivo> findByPacienteIdPacienteOrderByDataArquivoDesc(String pacienteId);
}