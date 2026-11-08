package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ResponsavelNotFoundException;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.ResponsavelRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveResponsavelDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final PessoaRepository pessoaRepository;
    private final PacienteRepository pacienteRepository;

    @Transactional
    public Responsavel createResponsavel(SaveResponsavelDTO saveResponsavelDTO) {
        Pessoa pessoa;
        String idPessoa = saveResponsavelDTO.getIdPessoa();
        String rawCpf = saveResponsavelDTO.getCPFPessoa();
        String cpf = (rawCpf != null) ? rawCpf.replaceAll("\\D", "").trim() : "";

        if (idPessoa != null && !idPessoa.isBlank()) {
            pessoa = pessoaRepository.findById(idPessoa.trim())
                    .orElseThrow(() -> new PessoaNotFoundException(idPessoa));
        } else if (!cpf.isBlank()) {
            final String cpfLimpo = cpf;
            pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo)
                    .orElseGet(() -> {
                        Pessoa nova = Pessoa.builder()
                                .nomePessoa(saveResponsavelDTO.getNomePessoa() != null ? saveResponsavelDTO.getNomePessoa().trim() : "")
                                .CPFPessoa(cpfLimpo)
                                .datanascPessoa(saveResponsavelDTO.getDatanascPessoa())
                                .RGPessoa(saveResponsavelDTO.getRGPessoa())
                                .enderecoPessoa(saveResponsavelDTO.getEnderecoPessoa())
                                .telefonePessoa(saveResponsavelDTO.getTelefonePessoa())
                                .celularPessoa(saveResponsavelDTO.getCelularPessoa())
                                .emailPessoa(saveResponsavelDTO.getEmailPessoa())
                                .build();
                        return pessoaRepository.save(nova);
                    });
        } else {
            throw new IllegalArgumentException("Informe a pessoa existente ou os dados pessoais com CPF");
        }

        if (responsavelRepository.existsByPessoaResponsavel(pessoa)) {
            throw new IllegalArgumentException("Esta pessoa já está cadastrada como responsável.");
        }

        Responsavel responsavel = Responsavel.builder()
                .pessoaResponsavel(pessoa)
                .pacientes(new ArrayList<>())
                .build();

        // Se o responsável também é paciente
        if (Boolean.TRUE.equals(saveResponsavelDTO.getIsPacienteTambem())) {
            Paciente pacProprio = pacienteRepository.findByPessoaPaciente(pessoa)
                    .orElseGet(() -> {
                        Paciente novoPac = Paciente.builder()
                                .pessoaPaciente(pessoa)
                                .isResponsavel(true)
                                .build();
                        return pacienteRepository.save(novoPac);
                    });
            responsavel.getPacientes().add(pacProprio);
        }

        // Vínculo com pacientes existentes selecionados
        if (saveResponsavelDTO.getPacienteIds() != null && !saveResponsavelDTO.getPacienteIds().isEmpty()) {
            for (String idPac : saveResponsavelDTO.getPacienteIds()) {
                if (idPac != null && !idPac.isBlank()) {
                    Paciente pac = pacienteRepository.findById(idPac.trim()).orElse(null);
                    if (pac != null && !responsavel.getPacientes().contains(pac)) {
                        responsavel.getPacientes().add(pac);
                    }
                }
            }
        }

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
