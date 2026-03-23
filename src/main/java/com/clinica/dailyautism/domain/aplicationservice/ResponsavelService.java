package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ResponsavelNotFoundException;
import com.clinica.dailyautism.domain.repository.ResponsavelRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.infraestructure.dto.SaveResponsavelDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final PessoaRepository pessoaRepository;

    @Transactional
    public Responsavel createResponsavel(SaveResponsavelDTO saveResponsavelDTO) {
        Pessoa pessoa = pessoaRepository.findById(saveResponsavelDTO.getIdPessoa())
                .orElseThrow(() -> new PessoaNotFoundException(saveResponsavelDTO.getIdPessoa()));

        Responsavel responsavel = Responsavel.builder()
                .pessoaResponsavel(pessoa)
                .build();

        return responsavelRepository.save(responsavel);
    }

    public Responsavel loadResponsavel(String responsavelId) {
        return responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNotFoundException("Responsável não encontrado: " + responsavelId));
    }

    public List<Responsavel> listResponsaveis() {
        return responsavelRepository.findAll();
    }

    @Transactional
    public void deleteResponsavel(String responsavelId) {
        Responsavel responsavel = responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNotFoundException(responsavelId));
        responsavel.desativar();
        responsavelRepository.save(responsavel);
    }
}