package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.TipoArquivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoArquivoRepository extends JpaRepository<TipoArquivo, String> {
}
