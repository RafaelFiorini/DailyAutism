package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.infrastructure.dto.AuditoriaLogDTO;
import com.clinica.dailyautism.infrastructure.dto.FiltroAuditoriaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditoriaLogTest {

    @Test
    @DisplayName("Deve construir entidade AuditoriaLog com todos os campos")
    void deveConstruirAuditoriaLogComSucesso() {
        LocalDateTime agora = LocalDateTime.now();

        AuditoriaLog log = AuditoriaLog.builder()
                .idAuditoriaLog("log-uuid-123")
                .dataHora(agora)
                .idUsuario("usr-1")
                .nomeUsuario("Dra. Paula Silva")
                .loginUsuario("paula.silva@clinica.com")
                .perfilUsuario("PROFISSIONAL")
                .idClinica("cli-1")
                .categoria(CategoriaAuditoria.GRADE_TERAPEUTICA)
                .acao(AcaoAuditoria.APROVACAO)
                .entidade("GradeTerapeutica")
                .idEntidade("grade-456")
                .descricao("Aprovou a grade terapêutica v1 do paciente")
                .dadosAnteriores("{\"status\": \"PENDENTE_APROVACAO\"}")
                .dadosNovos("{\"status\": \"APROVADA\"}")
                .ipOrigem("192.168.1.100")
                .userAgent("Mozilla/5.0 DailyAutismApp/1.0")
                .build();

        assertEquals("log-uuid-123", log.getIdAuditoriaLog());
        assertEquals(agora, log.getDataHora());
        assertEquals("usr-1", log.getIdUsuario());
        assertEquals("Dra. Paula Silva", log.getNomeUsuario());
        assertEquals(CategoriaAuditoria.GRADE_TERAPEUTICA, log.getCategoria());
        assertEquals("Grade Terapêutica", log.getCategoria().getDescricao());
        assertEquals(AcaoAuditoria.APROVACAO, log.getAcao());
        assertEquals("Aprovação", log.getAcao().getDescricao());
        assertEquals("GradeTerapeutica", log.getEntidade());
        assertEquals("grade-456", log.getIdEntidade());
        assertEquals("192.168.1.100", log.getIpOrigem());
    }

    @Test
    @DisplayName("Deve converter entidade para DTO com descrições amigáveis")
    void deveConverterParaDTOComSucesso() {
        AuditoriaLog log = AuditoriaLog.builder()
                .idAuditoriaLog("log-999")
                .dataHora(LocalDateTime.now())
                .idUsuario("usr-admin")
                .nomeUsuario("Administrador")
                .loginUsuario("admin@dailyautism.com")
                .perfilUsuario("ADMIN")
                .categoria(CategoriaAuditoria.AUTENTICACAO)
                .acao(AcaoAuditoria.LOGIN_SUCESSO)
                .descricao("Login efetuado com sucesso")
                .ipOrigem("200.100.50.25")
                .userAgent("Chrome 120 / Windows")
                .build();

        AuditoriaLogDTO dto = AuditoriaLogDTO.fromEntity(log);

        assertNotNull(dto);
        assertEquals("log-999", dto.getIdAuditoriaLog());
        assertEquals("Administrador", dto.getNomeUsuario());
        assertEquals(CategoriaAuditoria.AUTENTICACAO, dto.getCategoria());
        assertEquals("Autenticação e Acesso", dto.getCategoriaDescricao());
        assertEquals(AcaoAuditoria.LOGIN_SUCESSO, dto.getAcao());
        assertEquals("Login com Sucesso", dto.getAcaoDescricao());
    }

    @Test
    @DisplayName("Deve construir FiltroAuditoriaDTO corretamente")
    void deveConstruirFiltroAuditoriaDTO() {
        LocalDate hoje = LocalDate.now();

        FiltroAuditoriaDTO filtro = FiltroAuditoriaDTO.builder()
                .dataInicio(hoje.minusDays(7))
                .dataFim(hoje)
                .categoria(CategoriaAuditoria.CLINICO)
                .acao(AcaoAuditoria.ASSINATURA_DIGITAL)
                .buscaTexto("Pedro Lucas")
                .build();

        assertEquals(hoje.minusDays(7), filtro.getDataInicio());
        assertEquals(hoje, filtro.getDataFim());
        assertEquals(CategoriaAuditoria.CLINICO, filtro.getCategoria());
        assertEquals(AcaoAuditoria.ASSINATURA_DIGITAL, filtro.getAcao());
        assertEquals("Pedro Lucas", filtro.getBuscaTexto());
    }
}
