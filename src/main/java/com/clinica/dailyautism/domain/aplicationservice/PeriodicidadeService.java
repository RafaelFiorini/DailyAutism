package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Periodicidade;
import com.clinica.dailyautism.domain.repository.PeriodicidadeRepository;
import com.clinica.dailyautism.infraestructure.dto.SavePeriodicidadeDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PeriodicidadeService {

    private final PeriodicidadeRepository periodicidadeRepository;

    @Transactional
    public Periodicidade createPeriodicidade(SavePeriodicidadeDTO savePeriodicidadeDTO) {
        Periodicidade periodicidade = Periodicidade.builder()
                .nomePeriodicidade(savePeriodicidadeDTO.getNomePeriodicidade())
                .diasIntervalo(savePeriodicidadeDTO.getDiasIntervalo())
                .descricaoPeriodicidade(savePeriodicidadeDTO.getDescricaoPeriodicidade())
                .build();
        return periodicidadeRepository.save(periodicidade);
    }

    public Periodicidade loadPeriodicidade(String periodicidadeId) {
        return periodicidadeRepository.findById(periodicidadeId)
                .orElseThrow(() -> new RuntimeException("Periodicidade não encontrada: " + periodicidadeId));
    }

    public List<Periodicidade> listPeriodicidades() {
        return periodicidadeRepository.findAll();
    }

    @Transactional
    public Periodicidade updatePeriodicidade(String periodicidadeId, SavePeriodicidadeDTO savePeriodicidadeDTO) {
        Periodicidade periodicidade = loadPeriodicidade(periodicidadeId);
        periodicidade.setNomePeriodicidade(savePeriodicidadeDTO.getNomePeriodicidade());
        periodicidade.setDiasIntervalo(savePeriodicidadeDTO.getDiasIntervalo());
        periodicidade.setDescricaoPeriodicidade(savePeriodicidadeDTO.getDescricaoPeriodicidade());
        return periodicidadeRepository.save(periodicidade);
    }

    @Transactional
    public void deletePeriodicidade(String periodicidadeId) {
        Periodicidade periodicidade = loadPeriodicidade(periodicidadeId);
        periodicidadeRepository.delete(periodicidade);
    }
}