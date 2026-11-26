package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, String> {

    List<Notificacao> findTop50ByUsuarioDestinoIdUserOrderByCriadoEmDesc(String idUser);

    List<Notificacao> findByUsuarioDestinoIdUserAndLidaFalse(String idUser);

    long countByUsuarioDestinoIdUserAndLidaFalse(String idUser);
}
