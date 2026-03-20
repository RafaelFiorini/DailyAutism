package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, String> {
    Optional<Pessoa> findByCPFPessoa(String CPFPessoa);
}
