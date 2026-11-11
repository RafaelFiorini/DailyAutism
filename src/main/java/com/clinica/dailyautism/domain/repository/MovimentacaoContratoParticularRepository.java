package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.MovimentacaoContratoParticular;
import com.clinica.dailyautism.domain.entity.TipoMovimentacaoContrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoContratoParticularRepository extends JpaRepository<MovimentacaoContratoParticular, String> {

    List<MovimentacaoContratoParticular> findByContratoItemIdContratoItemOrderByDataMovimentacaoDesc(String idContratoItem);

    List<MovimentacaoContratoParticular> findByCompromissoIdCompromissoOrderByDataMovimentacaoDesc(String idCompromisso);

    Optional<MovimentacaoContratoParticular> findFirstByCompromissoIdCompromissoOrderByDataMovimentacaoDesc(String idCompromisso);

    boolean existsByCompromissoIdCompromissoAndTipoMovimentacao(String idCompromisso, TipoMovimentacaoContrato tipo);
}
