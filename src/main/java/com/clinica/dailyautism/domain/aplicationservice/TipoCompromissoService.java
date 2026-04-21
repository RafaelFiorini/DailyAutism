package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.TipoCompromisso;
import com.clinica.dailyautism.domain.exception.TipoArquivoNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.TipoCompromissoRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveTipoCompromissoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TipoCompromissoService {

    private final TipoCompromissoRepository tipoCompromissoRepository;

    @Transactional
    public TipoCompromisso createTipoCompromisso(SaveTipoCompromissoDTO saveTipoCompromissoDTO) {
        TipoCompromisso tipoCompromisso = TipoCompromisso.builder()
                .nomeTipo(saveTipoCompromissoDTO.getNomeTipo())
                .descricaoTipo(saveTipoCompromissoDTO.getDescricaoTipo())
                .build();
        return tipoCompromissoRepository.save(tipoCompromisso);
    }

    public TipoCompromisso loadTipoCompromisso(String tipoCompromissoId) {
        return tipoCompromissoRepository.findById(tipoCompromissoId)
                .orElseThrow(() -> new TipoArquivoNotFoundException("Tipo de compromisso não encontrado: " + tipoCompromissoId));
    }

    public List<TipoCompromisso> listTiposCompromisso() {
        return tipoCompromissoRepository.findAll();
    }

    @Transactional
    public TipoCompromisso updateTipoCompromisso(String tipoCompromissoId, SaveTipoCompromissoDTO saveTipoCompromissoDTO) {
        TipoCompromisso tipoCompromisso = loadTipoCompromisso(tipoCompromissoId);
        tipoCompromisso.setNomeTipo(saveTipoCompromissoDTO.getNomeTipo());
        tipoCompromisso.setDescricaoTipo(saveTipoCompromissoDTO.getDescricaoTipo());
        return tipoCompromissoRepository.save(tipoCompromisso);
    }

    @Transactional
    public void deleteTipoCompromisso(String tipoCompromissoId) {
        TipoCompromisso tipoCompromisso = tipoCompromissoRepository.findById(tipoCompromissoId)
                .orElseThrow(() -> new TipoCompromissoNotFoundException("Tipo de compromisso não encontrado: " + tipoCompromissoId));
        tipoCompromisso.desativar();
        tipoCompromissoRepository.save(tipoCompromisso);
    }
}