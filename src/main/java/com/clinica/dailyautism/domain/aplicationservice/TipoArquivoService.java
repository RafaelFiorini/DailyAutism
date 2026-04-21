package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.TipoArquivo;
import com.clinica.dailyautism.domain.exception.TipoArquivoNotFoundException;
import com.clinica.dailyautism.domain.repository.TipoArquivoRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveTipoArquivoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TipoArquivoService {

    private final TipoArquivoRepository tipoArquivoRepository;

    @Transactional
    public TipoArquivo createTipoArquivo(SaveTipoArquivoDTO saveTipoArquivoDTO) {
        TipoArquivo tipoArquivo = TipoArquivo.builder()
                .nomeTipo(saveTipoArquivoDTO.getNomeTipo())
                .descricaoTipo(saveTipoArquivoDTO.getDescricaoTipo())
                .build();
        return tipoArquivoRepository.save(tipoArquivo);
    }

    public TipoArquivo loadTipoArquivo(String tipoArquivoId) {
        return tipoArquivoRepository.findById(tipoArquivoId)
                .orElseThrow(() -> new TipoArquivoNotFoundException("Tipo de arquivo não encontrado: " + tipoArquivoId));
    }

    public List<TipoArquivo> listTiposArquivo() {
        return tipoArquivoRepository.findAll();
    }

    @Transactional
    public TipoArquivo updateTipoArquivo(String tipoArquivoId, SaveTipoArquivoDTO saveTipoArquivoDTO) {
        TipoArquivo tipoArquivo = loadTipoArquivo(tipoArquivoId);
        tipoArquivo.setNomeTipo(saveTipoArquivoDTO.getNomeTipo());
        tipoArquivo.setDescricaoTipo(saveTipoArquivoDTO.getDescricaoTipo());
        return tipoArquivoRepository.save(tipoArquivo);
    }

    @Transactional
    public void deleteTipoArquivo(String tipoArquivoId) {
        TipoArquivo tipoArquivo = tipoArquivoRepository.findById(tipoArquivoId)
                .orElseThrow(() -> new TipoArquivoNotFoundException("Tipo de arquivo não encontrado: " + tipoArquivoId));
        tipoArquivo.desativar();
        tipoArquivoRepository.save(tipoArquivo);
    }
}