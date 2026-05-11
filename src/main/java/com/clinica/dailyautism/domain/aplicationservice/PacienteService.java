package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.ResponsavelRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.dto.SavePacienteCompletoDTO;
import com.clinica.dailyautism.infrastructure.dto.SavePacienteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PessoaRepository pessoaRepository;
    private final UserRepository userRepository;
    private final ResponsavelRepository responsavelRepository;

    @Transactional
    public Paciente createPaciente(SavePacienteDTO savePacienteDTO) {
        Pessoa pessoa = pessoaRepository.findById(savePacienteDTO.getIdPessoa())
                .orElseThrow(() -> new PessoaNotFoundException(savePacienteDTO.getIdPessoa()));

        Paciente paciente = Paciente.builder()
                .pessoaPaciente(pessoa)
                .apelidoPaciente(savePacienteDTO.getApelidoPaciente())
                .isResponsavel(savePacienteDTO.isResponsavel())
                // planoPaciente e vencimentoLiberacaoPlanoPaciente removidos
                .build();

        return pacienteRepository.save(paciente);
    }

    public Paciente loadPaciente(String pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        // corrigido: estava usando PessoaNotFoundException por engano!
    }

    public List<Paciente> listPacientes() {
        return pacienteRepository.findAll();
    }

    @Transactional
    public Paciente updatePaciente(String pacienteId, SavePacienteDTO savePacienteDTO) {
        Paciente paciente = loadPaciente(pacienteId);
        paciente.setApelidoPaciente(savePacienteDTO.getApelidoPaciente());
        paciente.setResponsavel(savePacienteDTO.isResponsavel());
        // planoPaciente e vencimentoLiberacaoPlanoPaciente removidos
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void deletePaciente(String pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        paciente.desativar();
        pacienteRepository.save(paciente);
    }
    @Transactional
    public Paciente createPacienteCompleto(
            SavePacienteCompletoDTO dto,
            String emailDoToken) {

        Pessoa pessoa = pessoaRepository.findByCPFPessoa(dto.getCPFPessoa())
                .orElseGet(() -> {
                    Pessoa nova = Pessoa.builder()
                            .nomePessoa(dto.getNomePessoa())
                            .CPFPessoa(dto.getCPFPessoa())
                            .datanascPessoa(dto.getDatanascPessoa())
                            .RGPessoa(dto.getRGPessoa())
                            .enderecoPessoa(dto.getEnderecoPessoa())
                            .telefonePessoa(dto.getTelefonePessoa())
                            .celularPessoa(dto.getCelularPessoa())
                            .emailPessoa(dto.getEmailPessoa())
                            .build();
                    return pessoaRepository.save(nova);
                });

        Paciente paciente = Paciente.builder()
                .pessoaPaciente(pessoa)
                .apelidoPaciente(dto.getApelidoPaciente())
                .isResponsavel(dto.isResponsavel())
                .build();

        paciente = pacienteRepository.save(paciente);

        // SEMPRE vincula ao responsável do usuário logado
        User user = userRepository.findByEmailUser(emailDoToken)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Responsavel responsavel = responsavelRepository
                .findByPessoaResponsavel(user.getPessoa())
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));

        responsavel.getPacientes().add(paciente);
        responsavelRepository.save(responsavel);

        return paciente;
    }
}