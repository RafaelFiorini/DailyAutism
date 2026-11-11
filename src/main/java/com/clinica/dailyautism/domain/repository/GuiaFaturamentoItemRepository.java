package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.GuiaFaturamentoItem;
import com.clinica.dailyautism.domain.entity.StatusGuiaFaturamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuiaFaturamentoItemRepository extends JpaRepository<GuiaFaturamentoItem, String> {

    boolean existsByCompromissoIdCompromissoAndGuiaStatusNot(String idCompromisso, StatusGuiaFaturamento status);

    @Query("SELECT i.compromisso.idCompromisso FROM GuiaFaturamentoItem i " +
           "WHERE i.compromisso.idCompromisso IN :ids AND i.guia.status <> :statusExcluido")
    List<String> findCompromissoIdsJaFaturados(
            @Param("ids") List<String> compromissoIds,
            @Param("statusExcluido") StatusGuiaFaturamento statusExcluido
    );
}
