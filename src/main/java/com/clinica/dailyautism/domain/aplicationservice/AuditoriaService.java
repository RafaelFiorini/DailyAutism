package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.AcaoAuditoria;
import com.clinica.dailyautism.domain.entity.AuditoriaLog;
import com.clinica.dailyautism.domain.entity.CategoriaAuditoria;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.AuditoriaLogRepository;
import com.clinica.dailyautism.domain.repository.specification.AuditoriaLogSpecification;
import com.clinica.dailyautism.infrastructure.dto.AuditoriaLogDTO;
import com.clinica.dailyautism.infrastructure.dto.FiltroAuditoriaDTO;
import com.clinica.dailyautism.infrastructure.util.HttpRequestUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;
    private static final DateTimeFormatter FORMATTER_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Registra eventos de autenticação de forma assíncrona.
     */
    @Async
    @Transactional
    public void registrarAutenticacaoAsync(String login, String perfil, boolean sucesso, String motivoFalha, String ip, String userAgent, String idClinica) {
        try {
            CategoriaAuditoria categoria = sucesso ? CategoriaAuditoria.AUTENTICACAO : CategoriaAuditoria.SEGURANCA;
            AcaoAuditoria acao = sucesso ? AcaoAuditoria.LOGIN_SUCESSO : AcaoAuditoria.LOGIN_FALHA;
            String descricao = sucesso
                    ? "Login efetuado com sucesso no sistema"
                    : "Falha de autenticação: " + (motivoFalha != null ? motivoFalha : "Credenciais inválidas");

            AuditoriaLog logRegistro = AuditoriaLog.builder()
                    .dataHora(LocalDateTime.now())
                    .loginUsuario(login)
                    .perfilUsuario(perfil != null ? perfil : "DESCONHECIDO")
                    .idClinica(idClinica)
                    .categoria(categoria)
                    .acao(acao)
                    .entidade("Autenticacao")
                    .idEntidade(login)
                    .descricao(descricao)
                    .ipOrigem(ip != null ? ip : "UNKNOWN")
                    .userAgent(userAgent != null ? userAgent : "UNKNOWN")
                    .build();

            auditoriaLogRepository.save(logRegistro);
            log.info("Auditoria registrada [{}]: {} - {}", acao, login, ip);
        } catch (Exception e) {
            log.error("Erro ao registrar log de auditoria de autenticação assíncrono: {}", e.getMessage(), e);
        }
    }

    /**
     * Registra mutações e operações de negócio de forma assíncrona.
     */
    @Async
    @Transactional
    public void registrarAcaoAsync(String idUsuario,
                                   String nomeUsuario,
                                   String loginUsuario,
                                   String perfilUsuario,
                                   String idClinica,
                                   CategoriaAuditoria categoria,
                                   AcaoAuditoria acao,
                                   String entidade,
                                   String idEntidade,
                                   String descricao,
                                   String dadosAnteriores,
                                   String dadosNovos,
                                   String ip,
                                   String userAgent) {
        try {
            AuditoriaLog logRegistro = AuditoriaLog.builder()
                    .dataHora(LocalDateTime.now())
                    .idUsuario(idUsuario)
                    .nomeUsuario(nomeUsuario)
                    .loginUsuario(loginUsuario)
                    .perfilUsuario(perfilUsuario)
                    .idClinica(idClinica)
                    .categoria(categoria)
                    .acao(acao)
                    .entidade(entidade)
                    .idEntidade(idEntidade)
                    .descricao(descricao)
                    .dadosAnteriores(dadosAnteriores)
                    .dadosNovos(dadosNovos)
                    .ipOrigem(ip != null ? ip : "UNKNOWN")
                    .userAgent(userAgent != null ? userAgent : "UNKNOWN")
                    .build();

            auditoriaLogRepository.save(logRegistro);
            log.info("Auditoria de ação registrada: {} - {} por {}", acao, entidade, loginUsuario);
        } catch (Exception e) {
            log.error("Erro ao gravar log de auditoria assíncrono: {}", e.getMessage(), e);
        }
    }

    /**
     * Sobrecarga facilitadora recebendo objeto User e HttpServletRequest.
     */
    @Async
    @Transactional
    public void registrarAcaoAsync(User usuario,
                                   CategoriaAuditoria categoria,
                                   AcaoAuditoria acao,
                                   String entidade,
                                   String idEntidade,
                                   String descricao,
                                   String dadosAnteriores,
                                   String dadosNovos,
                                   HttpServletRequest request) {
        String ip = HttpRequestUtil.extrairIpOrigem(request);
        String ua = HttpRequestUtil.extrairUserAgent(request);

        String idUsr = usuario != null ? usuario.getIdUser() : null;
        String nomeUsr = usuario != null ? usuario.getNomeUser() : "Sistema";
        String loginUsr = usuario != null ? usuario.getEmailUser() : "sistema@dailyautism.com";

        registrarAcaoAsync(idUsr, nomeUsr, loginUsr, null, null, categoria, acao, entidade, idEntidade, descricao, dadosAnteriores, dadosNovos, ip, ua);
    }

    /**
     * Consulta paginada com múltiplos filtros.
     */
    @Transactional(readOnly = true)
    public Page<AuditoriaLogDTO> buscarComFiltros(FiltroAuditoriaDTO filtro, Pageable pageable) {
        return auditoriaLogRepository.findAll(AuditoriaLogSpecification.comFiltro(filtro), pageable)
                .map(AuditoriaLogDTO::fromEntity);
    }

    /**
     * Busca um log por ID para exibir os detalhes completos (incluindo JSON antes/depois).
     */
    @Transactional(readOnly = true)
    public AuditoriaLogDTO buscarPorId(String idAuditoriaLog) {
        return auditoriaLogRepository.findById(idAuditoriaLog)
                .map(AuditoriaLogDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Registro de auditoria não encontrado: " + idAuditoriaLog));
    }

    /**
     * Gera arquivo CSV oficial para auditorias externas e relatórios LGPD.
     */
    @Transactional(readOnly = true)
    public byte[] exportarCsv(FiltroAuditoriaDTO filtro) {
        List<AuditoriaLog> logs = auditoriaLogRepository.findAll(
                AuditoriaLogSpecification.comFiltro(filtro),
                Sort.by(Sort.Direction.DESC, "dataHora")
        );

        StringBuilder sb = new StringBuilder();
        // BOM UTF-8 para reconhecimento automático de acentos no Excel
        sb.append('\ufeff');

        // Cabeçalho CSV
        sb.append("Data/Hora;Usuário;Login;Perfil;Categoria;Ação;Entidade;ID Registro;Descrição;IP Origem;Navegador/Dispositivo\n");

        for (AuditoriaLog item : logs) {
            sb.append(escapeCsv(item.getDataHora() != null ? item.getDataHora().format(FORMATTER_CSV) : "")).append(";");
            sb.append(escapeCsv(item.getNomeUsuario())).append(";");
            sb.append(escapeCsv(item.getLoginUsuario())).append(";");
            sb.append(escapeCsv(item.getPerfilUsuario())).append(";");
            sb.append(escapeCsv(item.getCategoria() != null ? item.getCategoria().getDescricao() : "")).append(";");
            sb.append(escapeCsv(item.getAcao() != null ? item.getAcao().getDescricao() : "")).append(";");
            sb.append(escapeCsv(item.getEntidade())).append(";");
            sb.append(escapeCsv(item.getIdEntidade())).append(";");
            sb.append(escapeCsv(item.getDescricao())).append(";");
            sb.append(escapeCsv(item.getIpOrigem())).append(";");
            sb.append(escapeCsv(item.getUserAgent())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String valor) {
        if (valor == null) {
            return "";
        }
        String clean = valor.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }
}
