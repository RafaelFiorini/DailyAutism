package com.clinica.dailyautism.domain.aplicationservice;


import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.infraestructure.dto.SavePessoaDTO;
import com.clinica.dailyautism.infraestructure.dto.UpdatePessoaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    @Transactional
    public Pessoa createPessoa(SavePessoaDTO savePessoaData) {
        // busca pelo CPF primeiro, só cria se não existir
        return pessoaRepository.findByCPFPessoa(savePessoaData.getCPFPessoa())
                .orElseGet(() -> {
                    Pessoa pessoa = Pessoa.builder()
                            .nomePessoa(savePessoaData.getNomePessoa())
                            .CPFPessoa(savePessoaData.getCPFPessoa())
                            .datanascPessoa(savePessoaData.getDatanascPessoa())
                            .RGPessoa(savePessoaData.getRGPessoa())
                            .enderecoPessoa(savePessoaData.getEnderecoPessoa())
                            .telefonePessoa(savePessoaData.getTelefonePessoa())
                            .celularPessoa(savePessoaData.getCelularPessoa())
                            .emailPessoa(savePessoaData.getEmailPessoa())
                            .build();
                    return pessoaRepository.save(pessoa);
                });
    }

    public Pessoa loadPessoa(String pessoaId) {
        return pessoaRepository
                .findById(pessoaId)
                .orElseThrow(() -> new PessoaNotFoundException(pessoaId));
    }

    @Transactional
    public Pessoa updatePessoa(String pessoaId, UpdatePessoaDTO updatePessoaDTO) {
        Pessoa pessoa = loadPessoa(pessoaId);
        pessoa.setNomePessoa(updatePessoaDTO.getNomePessoa());
        //pessoa.setRGPessoa(updatePessoaDTO.getRGPessoa());
        pessoa.setEnderecoPessoa(updatePessoaDTO.getEnderecoPessoa());
        pessoa.setTelefonePessoa(updatePessoaDTO.getTelefonePessoa());
        pessoa.setCelularPessoa(updatePessoaDTO.getCelularPessoa());
        pessoa.setEmailPessoa(updatePessoaDTO.getEmailPessoa());
        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public void deletePessoa(String pessoaId) {
        Pessoa pessoa = loadPessoa(pessoaId);
        pessoaRepository.delete(pessoa);
    }

    public List<Pessoa> listPessoas() {
        return pessoaRepository.findAll();
    }
}