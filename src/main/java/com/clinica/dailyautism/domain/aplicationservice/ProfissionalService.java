package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ProfissionalNotFoundException;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalRepository;
import com.clinica.dailyautism.infraestructure.dto.SaveProfissionalDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final PessoaRepository pessoaRepository;

    @Transactional
    public Profissional createProfissional(SaveProfissionalDTO saveProfissionalDTO) {
        Pessoa pessoa = pessoaRepository.findById(saveProfissionalDTO.getIdPessoa())
                .orElseThrow(() -> new PessoaNotFoundException(saveProfissionalDTO.getIdPessoa()));

        Profissional profissional = Profissional.builder()
                .pessoaProf(pessoa)
                .conselhoProf(saveProfissionalDTO.getConselhoProf())
                .ativoProf(saveProfissionalDTO.isAtivoProf())
                .build();

        return profissionalRepository.save(profissional);
    }

    public Profissional loadProfissional(String profissionalId) {
        return profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ProfissionalNotFoundException("Profissional não encontrado: " + profissionalId));
    }

    public List<Profissional> listProfissionais() {
        return profissionalRepository.findAll();
    }

    @Transactional
    public Profissional updateProfissional(String profissionalId, SaveProfissionalDTO saveProfissionalDTO) {
        Profissional profissional = loadProfissional(profissionalId);
        profissional.setConselhoProf(saveProfissionalDTO.getConselhoProf());
        profissional.setAtivoProf(saveProfissionalDTO.isAtivoProf());
        return profissionalRepository.save(profissional);
    }

    @Transactional
    public void deleteProfissional(String profissionalId) {
        Profissional profissional = loadProfissional(profissionalId);
        profissionalRepository.delete(profissional);
    }
}