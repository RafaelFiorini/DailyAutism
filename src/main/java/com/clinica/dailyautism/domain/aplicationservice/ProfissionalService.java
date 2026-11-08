package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ProfissionalNotFoundException;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveProfissionalDTO;
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
        Pessoa pessoa;
        String idPessoa = saveProfissionalDTO.getIdPessoa();
        String rawCpf = saveProfissionalDTO.getCPFPessoa();
        String cpf = (rawCpf != null) ? rawCpf.replaceAll("\\D", "").trim() : "";

        if (idPessoa != null && !idPessoa.isBlank()) {
            pessoa = pessoaRepository.findById(idPessoa.trim())
                    .orElseThrow(() -> new PessoaNotFoundException(idPessoa));
        } else if (!cpf.isBlank()) {
            final String cpfLimpo = cpf;
            pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo)
                    .orElseGet(() -> {
                        Pessoa nova = Pessoa.builder()
                                .nomePessoa(saveProfissionalDTO.getNomePessoa() != null ? saveProfissionalDTO.getNomePessoa().trim() : "")
                                .CPFPessoa(cpfLimpo)
                                .datanascPessoa(saveProfissionalDTO.getDatanascPessoa())
                                .RGPessoa(saveProfissionalDTO.getRGPessoa())
                                .enderecoPessoa(saveProfissionalDTO.getEnderecoPessoa())
                                .telefonePessoa(saveProfissionalDTO.getTelefonePessoa())
                                .celularPessoa(saveProfissionalDTO.getCelularPessoa())
                                .emailPessoa(saveProfissionalDTO.getEmailPessoa())
                                .build();
                        return pessoaRepository.save(nova);
                    });
        } else {
            throw new IllegalArgumentException("Informe a pessoa existente ou os dados pessoais com CPF");
        }

        if (profissionalRepository.existsByPessoaProf(pessoa)) {
            throw new IllegalArgumentException("Esta pessoa já está cadastrada como profissional.");
        }

        Profissional profissional = Profissional.builder()
                .pessoaProf(pessoa)
                .conselhoProf(saveProfissionalDTO.getConselhoProf())
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
        if (saveProfissionalDTO.getConselhoProf() != null && !saveProfissionalDTO.getConselhoProf().isBlank()) {
            profissional.setConselhoProf(saveProfissionalDTO.getConselhoProf().trim());
        }

        Pessoa pessoa = profissional.getPessoaProf();
        if (pessoa != null) {
            if (saveProfissionalDTO.getNomePessoa() != null && !saveProfissionalDTO.getNomePessoa().isBlank()) {
                pessoa.setNomePessoa(saveProfissionalDTO.getNomePessoa().trim());
            }
            if (saveProfissionalDTO.getEmailPessoa() != null) {
                pessoa.setEmailPessoa(saveProfissionalDTO.getEmailPessoa().trim());
            }
            if (saveProfissionalDTO.getCelularPessoa() != null) {
                pessoa.setCelularPessoa(saveProfissionalDTO.getCelularPessoa().trim());
            }
            if (saveProfissionalDTO.getTelefonePessoa() != null) {
                pessoa.setTelefonePessoa(saveProfissionalDTO.getTelefonePessoa().trim());
            }
            if (saveProfissionalDTO.getEnderecoPessoa() != null) {
                pessoa.setEnderecoPessoa(saveProfissionalDTO.getEnderecoPessoa().trim());
            }
            if (saveProfissionalDTO.getRGPessoa() != null) {
                pessoa.setRGPessoa(saveProfissionalDTO.getRGPessoa().trim());
            }
            if (saveProfissionalDTO.getDatanascPessoa() != null) {
                pessoa.setDatanascPessoa(saveProfissionalDTO.getDatanascPessoa());
            }
            pessoaRepository.save(pessoa);
        }

        return profissionalRepository.save(profissional);
    }

    @Transactional
    public void deleteProfissional(String profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ProfissionalNotFoundException(profissionalId));
        profissional.desativar();
        profissionalRepository.save(profissional);
    }
}
