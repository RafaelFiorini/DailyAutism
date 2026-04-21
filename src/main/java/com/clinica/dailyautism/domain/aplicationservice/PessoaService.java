package com.clinica.dailyautism.domain.aplicationservice;


import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.PessoaAccessDeniedException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.dto.SavePessoaDTO;
import com.clinica.dailyautism.infrastructure.dto.UpdatePessoaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final UserRepository userRepository;

    @Transactional
    public Pessoa createPessoa(SavePessoaDTO savePessoaData, String emailDoToken) {

        User user = userRepository.findByEmailUser(emailDoToken)
                .orElseThrow(() -> new PessoaNotFoundException("Usuário não encontrado"));

        // CPF já existe? vincula e retorna (clínica já cadastrou, não valida email)
        Optional<Pessoa> pessoaExistente = pessoaRepository.findByCPFPessoa(savePessoaData.getCPFPessoa());
        if (pessoaExistente.isPresent()) {
            Pessoa pessoa = pessoaExistente.get();

            // valida que o email do token bate com o email da pessoa cadastrada
            if (!pessoa.getEmailPessoa().equalsIgnoreCase(emailDoToken)) {
                throw new PessoaAccessDeniedException();
            }

            user.setPessoa(pessoa);
            userRepository.save(user);
            return pessoa;
        }

        // CPF novo — valida email (usuário cadastrando a si mesmo)
        if (!savePessoaData.getEmailPessoa().equalsIgnoreCase(emailDoToken)) {
            throw new RuntimeException("Email da pessoa deve ser igual ao email do usuário logado");
        }

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

        pessoa = pessoaRepository.save(pessoa);
        user.setPessoa(pessoa);
        userRepository.save(user);
        return pessoa;
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
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new PessoaNotFoundException(pessoaId));
        pessoa.desativar();
        pessoaRepository.save(pessoa);
    }

    public List<Pessoa> listPessoas() {
        return pessoaRepository.findAll();
    }
}