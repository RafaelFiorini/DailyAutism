package com.clinica.dailyautism.domain.repository.specification;

import com.clinica.dailyautism.domain.entity.AuditoriaLog;
import com.clinica.dailyautism.infrastructure.dto.FiltroAuditoriaDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaLogSpecification {

    public static Specification<AuditoriaLog> comFiltro(FiltroAuditoriaDTO filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getDataInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), filtro.getDataInicio().atStartOfDay()));
            }

            if (filtro.getDataFim() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), filtro.getDataFim().atTime(23, 59, 59, 999999999)));
            }

            if (filtro.getCategoria() != null) {
                predicates.add(cb.equal(root.get("categoria"), filtro.getCategoria()));
            }

            if (filtro.getAcao() != null) {
                predicates.add(cb.equal(root.get("acao"), filtro.getAcao()));
            }

            if (filtro.getIdUsuario() != null && !filtro.getIdUsuario().isBlank()) {
                predicates.add(cb.equal(root.get("idUsuario"), filtro.getIdUsuario().trim()));
            }

            if (filtro.getLoginUsuario() != null && !filtro.getLoginUsuario().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("loginUsuario")), "%" + filtro.getLoginUsuario().toLowerCase().trim() + "%"));
            }

            if (filtro.getEntidade() != null && !filtro.getEntidade().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("entidade")), filtro.getEntidade().toLowerCase().trim()));
            }

            if (filtro.getIdEntidade() != null && !filtro.getIdEntidade().isBlank()) {
                predicates.add(cb.equal(root.get("idEntidade"), filtro.getIdEntidade().trim()));
            }

            if (filtro.getIdClinica() != null && !filtro.getIdClinica().isBlank()) {
                predicates.add(cb.equal(root.get("idClinica"), filtro.getIdClinica().trim()));
            }

            if (filtro.getBuscaTexto() != null && !filtro.getBuscaTexto().isBlank()) {
                String term = "%" + filtro.getBuscaTexto().toLowerCase().trim() + "%";
                Predicate porDescricao = cb.like(cb.lower(root.get("descricao")), term);
                Predicate porNome = cb.like(cb.lower(root.get("nomeUsuario")), term);
                Predicate porLogin = cb.like(cb.lower(root.get("loginUsuario")), term);
                Predicate porEntidade = cb.like(cb.lower(root.get("entidade")), term);
                predicates.add(cb.or(porDescricao, porNome, porLogin, porEntidade));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
