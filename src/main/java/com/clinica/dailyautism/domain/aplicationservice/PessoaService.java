package com.clinica.dailyautism.domain.aplicationservice;


import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.infraestructure.dto.SavePessoaDataDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    @Transactional
    public Pessoa createPessoa(SavePessoaDataDTO savePessoaData){
     Pessoa pessoa = Pessoa
             .builder()
             .nomePessoa(savePessoaData.getNomePessoa())
             .CPFPessoa(savePessoaData.getCPFPessoa())
             .datanascPessoa(savePessoaData.getDatanascPessoa())
             .RGPessoa(savePessoaData.getRGPessoa())
             .enderecoPessoa(savePessoaData.getEnderecoPessoa())
             .telefonePessoa(savePessoaData.getTelefonePessoa())
             .celularPessoa(savePessoaData.getCelularPessoa())
             .emailPessoa(savePessoaData.getEmailPessoa())
             .build();

     pessoaRepository.save(pessoa);

     return pessoa;
    }
}
