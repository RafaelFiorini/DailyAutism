package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.AcaoAuditoria;
import com.clinica.dailyautism.domain.entity.AuditoriaLog;
import com.clinica.dailyautism.domain.entity.CategoriaAuditoria;
import com.clinica.dailyautism.domain.repository.AuditoriaLogRepository;
import com.clinica.dailyautism.infrastructure.dto.AuditoriaLogDTO;
import com.clinica.dailyautism.infrastructure.dto.FiltroAuditoriaDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaLogRepository auditoriaLogRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    @DisplayName("Deve registrar auditoria de login com sucesso")
    void deveRegistrarLoginComSucesso() {
        auditoriaService.registrarAutenticacaoAsync(
                "admin@dailyautism.com",
                "ADMIN",
                true,
                null,
                "192.168.0.1",
                "Chrome/Windows",
                "clinica-1"
        );

        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(auditoriaLogRepository, times(1)).save(captor.capture());

        AuditoriaLog salvo = captor.getValue();
        assertEquals("admin@dailyautism.com", salvo.getLoginUsuario());
        assertEquals("ADMIN", salvo.getPerfilUsuario());
        assertEquals(CategoriaAuditoria.AUTENTICACAO, salvo.getCategoria());
        assertEquals(AcaoAuditoria.LOGIN_SUCESSO, salvo.getAcao());
        assertEquals("192.168.0.1", salvo.getIpOrigem());
        assertEquals("Chrome/Windows", salvo.getUserAgent());
        assertEquals("clinica-1", salvo.getIdClinica());
    }

    @Test
    @DisplayName("Deve registrar auditoria de falha de login com categoria de segurança")
    void deveRegistrarFalhaDeLogin() {
        auditoriaService.registrarAutenticacaoAsync(
                "invasor@teste.com",
                null,
                false,
                "Senha incorreta",
                "201.55.44.33",
                "UnknownBot/1.0",
                null
        );

        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(auditoriaLogRepository, times(1)).save(captor.capture());

        AuditoriaLog salvo = captor.getValue();
        assertEquals("invasor@teste.com", salvo.getLoginUsuario());
        assertEquals(CategoriaAuditoria.SEGURANCA, salvo.getCategoria());
        assertEquals(AcaoAuditoria.LOGIN_FALHA, salvo.getAcao());
        assertTrue(salvo.getDescricao().contains("Senha incorreta"));
    }

    @Test
    @DisplayName("Deve registrar ação de mutação de negócio com snapshots de antes e depois")
    void deveRegistrarAcaoDeNegocio() {
        auditoriaService.registrarAcaoAsync(
                "usr-1",
                "Secretária Ana",
                "ana@clinica.com",
                "SECRETARIA",
                "cli-1",
                CategoriaAuditoria.GRADE_TERAPEUTICA,
                AcaoAuditoria.CRIACAO,
                "GradeTerapeutica",
                "grade-100",
                "Criou proposta de grade para paciente Pedro",
                null,
                "{\"versao\": 1, \"itens\": 4}",
                "10.0.0.5",
                "Firefox/Linux"
        );

        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(auditoriaLogRepository, times(1)).save(captor.capture());

        AuditoriaLog salvo = captor.getValue();
        assertEquals("Secretária Ana", salvo.getNomeUsuario());
        assertEquals(CategoriaAuditoria.GRADE_TERAPEUTICA, salvo.getCategoria());
        assertEquals(AcaoAuditoria.CRIACAO, salvo.getAcao());
        assertEquals("GradeTerapeutica", salvo.getEntidade());
        assertEquals("grade-100", salvo.getIdEntidade());
        assertEquals("{\"versao\": 1, \"itens\": 4}", salvo.getDadosNovos());
    }

    @Test
    @DisplayName("Deve buscar logs paginados com filtros")
    void deveBuscarComFiltrosPaginados() {
        AuditoriaLog log = AuditoriaLog.builder()
                .idAuditoriaLog("log-1")
                .dataHora(LocalDateTime.now())
                .categoria(CategoriaAuditoria.CLINICO)
                .acao(AcaoAuditoria.EDICAO)
                .descricao("Atualizou prontuário")
                .build();

        Page<AuditoriaLog> pageMock = new PageImpl<>(List.of(log));
        when(auditoriaLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageMock);

        FiltroAuditoriaDTO filtro = FiltroAuditoriaDTO.builder().categoria(CategoriaAuditoria.CLINICO).build();
        Page<AuditoriaLogDTO> resultado = auditoriaService.buscarComFiltros(filtro, PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("log-1", resultado.getContent().get(0).getIdAuditoriaLog());
    }

    @Test
    @DisplayName("Deve buscar log por ID com sucesso ou lançar exceção se não encontrado")
    void deveBuscarPorIdOuLancarExcecao() {
        AuditoriaLog log = AuditoriaLog.builder()
                .idAuditoriaLog("log-123")
                .descricao("Teste")
                .categoria(CategoriaAuditoria.CADASTRO)
                .acao(AcaoAuditoria.CRIACAO)
                .build();

        when(auditoriaLogRepository.findById("log-123")).thenReturn(Optional.of(log));
        when(auditoriaLogRepository.findById("log-inexistente")).thenReturn(Optional.empty());

        AuditoriaLogDTO dto = auditoriaService.buscarPorId("log-123");
        assertNotNull(dto);
        assertEquals("log-123", dto.getIdAuditoriaLog());

        assertThrows(EntityNotFoundException.class, () -> auditoriaService.buscarPorId("log-inexistente"));
    }

    @Test
    @DisplayName("Deve exportar relatório em CSV com cabeçalho e codificação UTF-8 com BOM")
    void deveExportarCsvCorretamente() {
        AuditoriaLog log1 = AuditoriaLog.builder()
                .idAuditoriaLog("log-1")
                .dataHora(LocalDateTime.of(2026, 11, 18, 14, 30, 0))
                .nomeUsuario("Rafael Fiorini")
                .loginUsuario("rafael@dailyautism.com")
                .perfilUsuario("ADMIN")
                .categoria(CategoriaAuditoria.GRADE_TERAPEUTICA)
                .acao(AcaoAuditoria.APROVACAO)
                .entidade("GradeTerapeutica")
                .idEntidade("g-1")
                .descricao("Aprovou a grade de horários")
                .ipOrigem("192.168.1.1")
                .userAgent("Chrome 120")
                .build();

        when(auditoriaLogRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(log1));

        byte[] csvBytes = auditoriaService.exportarCsv(new FiltroAuditoriaDTO());
        assertNotNull(csvBytes);

        String conteudo = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(conteudo.startsWith("\ufeffData/Hora;Usuário;Login;Perfil;Categoria;Ação"));
        assertTrue(conteudo.contains("Rafael Fiorini"));
        assertTrue(conteudo.contains("Aprovou a grade de horários"));
    }
}
