package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Arquivo;
import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.TipoArquivo;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.ArquivoNotFoundException;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoArquivoNotFoundException;
import com.clinica.dailyautism.domain.repository.ArquivoRepository;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.TipoArquivoRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infraestructure.dto.SaveArquivoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;
    private final PacienteRepository pacienteRepository;
    private final TipoArquivoRepository tipoArquivoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Arquivo createArquivo(SaveArquivoDTO saveArquivoDTO) {
        Paciente paciente = pacienteRepository.findById(saveArquivoDTO.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(saveArquivoDTO.getIdPaciente()));

        TipoArquivo tipoArquivo = tipoArquivoRepository.findById(saveArquivoDTO.getIdTipoArquivo())
                .orElseThrow(() -> new TipoArquivoNotFoundException("Tipo de arquivo não encontrado: " + saveArquivoDTO.getIdTipoArquivo()));

        User user = userRepository.findById(saveArquivoDTO.getIdUser())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + saveArquivoDTO.getIdUser()));

        Arquivo arquivo = Arquivo.builder()
                .nomeArquivo(saveArquivoDTO.getNomeArquivo())
                .dataArquivo(saveArquivoDTO.getDataArquivo())
                .dataUpload(LocalDate.now()) // gerado automaticamente
                .extensaoArquivo(saveArquivoDTO.getExtensaoArquivo())
                .arquivoBase64(saveArquivoDTO.getArquivoBase64())
                .urlArquivo(saveArquivoDTO.getUrlArquivo())
                .paciente(paciente)
                .tipoArquivo(tipoArquivo)
                .uploadadoPor(user)
                .build();

        return arquivoRepository.save(arquivo);
    }

    public Arquivo loadArquivo(String arquivoId) {
        return arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNotFoundException("Arquivo não encontrado: " + arquivoId));
    }

    public List<Arquivo> listArquivosByPaciente(String pacienteId) {
        return arquivoRepository.findByPacienteIdPacienteOrderByDataArquivoDesc(pacienteId);
    }

    public Arquivo loadArquivoComBase64(String arquivoId) {
        return arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNotFoundException("Arquivo não encontrado: " + arquivoId));
    }

    @Transactional
    public void deleteArquivo(String arquivoId) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNotFoundException("Arquivo não encontrado: " + arquivoId));
        arquivo.desativar();
        arquivoRepository.save(arquivo);
    }

}
