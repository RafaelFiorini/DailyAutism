package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.AuditoriaService;
import com.clinica.dailyautism.domain.entity.AcaoAuditoria;
import com.clinica.dailyautism.domain.entity.CategoriaAuditoria;
import com.clinica.dailyautism.infrastructure.dto.AuditoriaLogDTO;
import com.clinica.dailyautism.infrastructure.dto.FiltroAuditoriaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaRestResourceTest {

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AuditoriaRestResource auditoriaRestResource;

    @Test
    @DisplayName("Deve listar logs com filtros paginados")
    void deveListarLogsPaginados() {
        AuditoriaLogDTO dto = AuditoriaLogDTO.builder()
                .idAuditoriaLog("log-1")
                .dataHora(LocalDateTime.now())
                .categoria(CategoriaAuditoria.AUTENTICACAO)
                .acao(AcaoAuditoria.LOGIN_SUCESSO)
                .descricao("Login efetuado com sucesso")
                .build();

        Page<AuditoriaLogDTO> pageMock = new PageImpl<>(List.of(dto));
        when(auditoriaService.buscarComFiltros(any(FiltroAuditoriaDTO.class), any(Pageable.class))).thenReturn(pageMock);

        ResponseEntity<Page<AuditoriaLogDTO>> response = auditoriaRestResource.listarLogs(new FiltroAuditoriaDTO(), PageRequest.of(0, 20));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("log-1", response.getBody().getContent().get(0).getIdAuditoriaLog());
    }

    @Test
    @DisplayName("Deve buscar detalhes do log por ID")
    void deveBuscarLogPorId() {
        AuditoriaLogDTO dto = AuditoriaLogDTO.builder()
                .idAuditoriaLog("log-123")
                .descricao("Criou paciente")
                .build();

        when(auditoriaService.buscarPorId("log-123")).thenReturn(dto);

        ResponseEntity<AuditoriaLogDTO> response = auditoriaRestResource.buscarPorId("log-123");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("log-123", response.getBody().getIdAuditoriaLog());
    }

    @Test
    @DisplayName("Deve exportar relatório CSV com cabeçalhos apropriados")
    void deveExportarCsv() {
        byte[] csvMock = "CSV TEST".getBytes(StandardCharsets.UTF_8);
        when(auditoriaService.exportarCsv(any(FiltroAuditoriaDTO.class))).thenReturn(csvMock);

        ResponseEntity<byte[]> response = auditoriaRestResource.exportarCsv(new FiltroAuditoriaDTO());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("relatorio_auditoria_dailyautism.csv"));
        assertArrayEquals(csvMock, response.getBody());
    }
}
