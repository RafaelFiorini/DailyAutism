package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Notificacao;
import com.clinica.dailyautism.domain.entity.TipoNotificacao;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.NotificacaoRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.dto.NotificacaoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UserRepository userRepository;

    public List<NotificacaoDTO> listarPorUsuario(String emailUsuario) {
        User user = userRepository.findByEmailUserIgnoreCase(emailUsuario).orElse(null);
        if (user == null) {
            return List.of();
        }
        return notificacaoRepository.findTop50ByUsuarioDestinoIdUserOrderByCriadoEmDesc(user.getIdUser())
                .stream()
                .map(NotificacaoDTO::create)
                .toList();
    }

    public long contarNaoLidas(String emailUsuario) {
        User user = userRepository.findByEmailUserIgnoreCase(emailUsuario).orElse(null);
        if (user == null) {
            return 0L;
        }
        return notificacaoRepository.countByUsuarioDestinoIdUserAndLidaFalse(user.getIdUser());
    }

    @Transactional
    public void notificarUsuarios(
            Collection<User> destinatarios,
            String idUsuarioIgnorar,
            String titulo,
            String mensagem,
            TipoNotificacao tipo,
            String idReferencia
    ) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }
        Set<String> jaNotificados = new HashSet<>();
        LocalDateTime agora = LocalDateTime.now();

        for (User destino : destinatarios) {
            if (destino == null || destino.getIdUser() == null) continue;
            if (idUsuarioIgnorar != null && idUsuarioIgnorar.equals(destino.getIdUser())) continue;
            if (!jaNotificados.add(destino.getIdUser())) continue;

            Notificacao n = Notificacao.builder()
                    .usuarioDestino(destino)
                    .titulo(titulo)
                    .mensagem(mensagem)
                    .tipo(tipo != null ? tipo : TipoNotificacao.SISTEMA)
                    .idReferencia(idReferencia)
                    .lida(false)
                    .criadoEm(agora)
                    .build();
            if (n.getAtivo() == null) n.setAtivo(true);
            notificacaoRepository.save(n);
        }
    }

    @Transactional
    public NotificacaoDTO marcarComoLida(String idNotificacao, String emailUsuario) {
        User user = userRepository.findByEmailUserIgnoreCase(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Notificacao notificacao = notificacaoRepository.findById(idNotificacao)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada."));

        if (notificacao.getUsuarioDestino() == null || !notificacao.getUsuarioDestino().getIdUser().equals(user.getIdUser())) {
            throw new AccessDeniedException("Você não tem permissão para alterar esta notificação.");
        }

        if (!Boolean.TRUE.equals(notificacao.getLida())) {
            notificacao.setLida(true);
            notificacao.setLidaEm(LocalDateTime.now());
            notificacaoRepository.save(notificacao);
        }
        return NotificacaoDTO.create(notificacao);
    }

    @Transactional
    public int marcarTodasComoLidas(String emailUsuario) {
        User user = userRepository.findByEmailUserIgnoreCase(emailUsuario).orElse(null);
        if (user == null) {
            return 0;
        }
        List<Notificacao> naoLidas = notificacaoRepository.findByUsuarioDestinoIdUserAndLidaFalse(user.getIdUser());
        LocalDateTime agora = LocalDateTime.now();
        for (Notificacao n : naoLidas) {
            n.setLida(true);
            n.setLidaEm(agora);
        }
        notificacaoRepository.saveAll(naoLidas);
        return naoLidas.size();
    }
}
