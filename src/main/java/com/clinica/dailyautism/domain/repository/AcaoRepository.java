package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Arquivo;
import com.clinica.dailyautism.domain.entity.security.Acao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AcaoRepository extends JpaRepository<Acao, String> {

    Optional<Acao> findByNome(String nome);
    List<Acao> findAllByNomeIn(Set<String> nomes);

}
