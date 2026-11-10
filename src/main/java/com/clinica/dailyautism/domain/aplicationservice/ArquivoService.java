package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.ArquivoNotFoundException;
import com.clinica.dailyautism.domain.exception.ClinicaNotFoundException;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoArquivoNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.CompartilharArquivoDTO;
import com.clinica.dailyautism.infrastructure.dto.OpcoesCompartilhamentoDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveArquivoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ArquivoService {

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("pdf", "doc", "docx", "txt", "odt", "rtf");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ArquivoRepository arquivoRepository;
    private final PacienteRepository pacienteRepository;
    private final TipoArquivoRepository tipoArquivoRepository;
    private final UserRepository userRepository;
    private final ClinicaRepository clinicaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ResponsavelRepository responsavelRepository;
    private final FuncionarioClinicaRepository funcionarioClinicaRepository;
    private final ProfissionalClinicaRepository profissionalClinicaRepository;
    private final PacienteService pacienteService;

    @Transactional
    public Arquivo createArquivo(SaveArquivoDTO dto, String emailDoToken) {
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));

        TipoArquivo tipoArquivo = tipoArquivoRepository.findById(dto.getIdTipoArquivo())
                .orElseThrow(() -> new TipoArquivoNotFoundException("Tipo de arquivo não encontrado: " + dto.getIdTipoArquivo()));

        User user;
        if (emailDoToken != null && !emailDoToken.isBlank()) {
            user = userRepository.findByEmailUser(emailDoToken)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + emailDoToken));
        } else if (dto.getIdUser() != null && !dto.getIdUser().isBlank()) {
            user = userRepository.findById(dto.getIdUser())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + dto.getIdUser()));
        } else {
            throw new IllegalArgumentException("Usuário de upload não identificado");
        }

        String ext = normalizarExtensao(dto.getExtensaoArquivo());
        String nomePadronizado = gerarNomePadronizado(tipoArquivo, paciente, ext);

        Clinica clinicaComp = null;
        boolean compClinica = Boolean.TRUE.equals(dto.getCompartilhadoComClinica());
        if (compClinica && dto.getIdClinicaCompartilhada() != null && !dto.getIdClinicaCompartilhada().isBlank()) {
            clinicaComp = clinicaRepository.findById(dto.getIdClinicaCompartilhada().trim())
                    .orElseThrow(() -> new ClinicaNotFoundException(dto.getIdClinicaCompartilhada()));
        }

        List<Profissional> profsComp = new ArrayList<>();
        if (dto.getIdsProfissionaisCompartilhados() != null && !dto.getIdsProfissionaisCompartilhados().isEmpty()) {
            profsComp.addAll(profissionalRepository.findAllById(dto.getIdsProfissionaisCompartilhados()));
        }

        Arquivo arquivo = Arquivo.builder()
                .nomeArquivo(nomePadronizado)
                .descricao(dto.getDescricao() != null ? dto.getDescricao().trim() : null)
                .dataArquivo(dto.getDataArquivo())
                .dataUpload(LocalDate.now())
                .extensaoArquivo(ext)
                .arquivoBase64(dto.getArquivoBase64())
                .urlArquivo(dto.getUrlArquivo())
                .paciente(paciente)
                .tipoArquivo(tipoArquivo)
                .uploadadoPor(user)
                .compartilhadoComResponsavel(Boolean.TRUE.equals(dto.getCompartilhadoComResponsavel()))
                .compartilhadoComClinica(compClinica)
                .clinicaCompartilhada(clinicaComp)
                .profissionaisCompartilhados(profsComp)
                .build();

        return arquivoRepository.save(arquivo);
    }

    public Arquivo loadArquivo(String arquivoId, String email) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNotFoundException("Arquivo não encontrado: " + arquivoId));
        validarVisualizacao(arquivo, email);
        return arquivo;
    }

    public List<Arquivo> listArquivosForUser(String email, String pacienteId, String idTipoArquivo) {
        User user = (email != null && !email.isBlank())
                ? userRepository.findByEmailUser(email).orElse(null)
                : null;

        List<Arquivo> base;
        if (pacienteId != null && !pacienteId.isBlank()) {
            base = arquivoRepository.findByPacienteIdPacienteOrderByDataArquivoDesc(pacienteId.trim());
        } else {
            base = arquivoRepository.findAllByOrderByDataArquivoDescDataUploadDesc();
        }

        return base.stream()
                .filter(a -> idTipoArquivo == null || idTipoArquivo.isBlank()
                        || (a.getTipoArquivo() != null && idTipoArquivo.equals(a.getTipoArquivo().getIdTipoArquivo())))
                .filter(a -> user == null || podeVisualizar(a, user))
                .toList();
    }

    public List<Arquivo> listArquivosByPacienteForUser(String pacienteId, String email) {
        return listArquivosForUser(email, pacienteId, null);
    }

    public Arquivo loadArquivoComBase64(String arquivoId, String email) {
        return loadArquivo(arquivoId, email);
    }

    @Transactional
    public Arquivo compartilharArquivo(String arquivoId, CompartilharArquivoDTO dto, String email) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNotFoundException("Arquivo não encontrado: " + arquivoId));

        validarProprietarioOuAdmin(arquivo, email, "Apenas quem enviou o arquivo ou um administrador pode alterar o compartilhamento.");

        arquivo.setCompartilhadoComResponsavel(Boolean.TRUE.equals(dto.getCompartilhadoComResponsavel()));

        boolean compClinica = Boolean.TRUE.equals(dto.getCompartilhadoComClinica());
        arquivo.setCompartilhadoComClinica(compClinica);
        if (compClinica && dto.getIdClinicaCompartilhada() != null && !dto.getIdClinicaCompartilhada().isBlank()) {
            Clinica clinica = clinicaRepository.findById(dto.getIdClinicaCompartilhada().trim())
                    .orElseThrow(() -> new ClinicaNotFoundException(dto.getIdClinicaCompartilhada()));
            arquivo.setClinicaCompartilhada(clinica);
        } else {
            arquivo.setClinicaCompartilhada(null);
        }

        if (arquivo.getProfissionaisCompartilhados() == null) {
            arquivo.setProfissionaisCompartilhados(new ArrayList<>());
        } else {
            arquivo.getProfissionaisCompartilhados().clear();
        }
        if (dto.getIdsProfissionaisCompartilhados() != null && !dto.getIdsProfissionaisCompartilhados().isEmpty()) {
            arquivo.getProfissionaisCompartilhados().addAll(
                    profissionalRepository.findAllById(dto.getIdsProfissionaisCompartilhados())
            );
        }

        return arquivoRepository.save(arquivo);
    }

    @Transactional
    public void deleteArquivo(String arquivoId, String email) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNotFoundException("Arquivo não encontrado: " + arquivoId));

        validarProprietarioOuAdmin(arquivo, email, "Apenas quem enviou o arquivo ou um administrador pode excluí-lo.");

        arquivo.desativar();
        arquivoRepository.save(arquivo);
    }

    public OpcoesCompartilhamentoDTO opcoesCompartilhamento(String pacienteId, String email) {
        Paciente paciente = pacienteService.loadPacienteForUser(pacienteId, email);

        Map<String, OpcoesCompartilhamentoDTO.ClinicaOpcao> clinicasMap = new LinkedHashMap<>();
        List<String> clinicaIds = new ArrayList<>();

        if (paciente.getClinicas() != null) {
            for (PacienteClinica pc : paciente.getClinicas()) {
                if (pc.getAtivo() != null && !pc.getAtivo()) continue;
                if (pc.getClinica() != null) {
                    Clinica c = pc.getClinica();
                    clinicasMap.put(c.getIdClinica(), new OpcoesCompartilhamentoDTO.ClinicaOpcao(c.getIdClinica(), c.getNomeClinica()));
                    clinicaIds.add(c.getIdClinica());
                }
            }
        }

        // Se o usuário logado é da clínica, inclui a clínica dele se não estiver listada
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmailUser(email).orElse(null);
            if (user != null && user.getPessoa() != null) {
                for (FuncionarioClinica fc : funcionarioClinicaRepository.findByPessoa(user.getPessoa())) {
                    if (fc.getClinica() != null) {
                        Clinica c = fc.getClinica();
                        clinicasMap.putIfAbsent(c.getIdClinica(), new OpcoesCompartilhamentoDTO.ClinicaOpcao(c.getIdClinica(), c.getNomeClinica()));
                        if (!clinicaIds.contains(c.getIdClinica())) {
                            clinicaIds.add(c.getIdClinica());
                        }
                    }
                }
            }
        }

        Map<String, OpcoesCompartilhamentoDTO.ProfissionalOpcao> profsMap = new LinkedHashMap<>();

        if (paciente.getProfissionais() != null) {
            for (ProfissionalPaciente pp : paciente.getProfissionais()) {
                if (pp.getAtivo() != null && !pp.getAtivo()) continue;
                if (pp.getProfissional() != null) {
                    Profissional p = pp.getProfissional();
                    profsMap.put(p.getIdProf(), new OpcoesCompartilhamentoDTO.ProfissionalOpcao(
                            p.getIdProf(),
                            p.getPessoaProf() != null ? p.getPessoaProf().getNomePessoa() : "Profissional",
                            p.getConselhoProf()
                    ));
                }
            }
        }

        if (!clinicaIds.isEmpty()) {
            for (Profissional p : profissionalClinicaRepository.findProfissionaisByClinicaIds(clinicaIds)) {
                profsMap.putIfAbsent(p.getIdProf(), new OpcoesCompartilhamentoDTO.ProfissionalOpcao(
                        p.getIdProf(),
                        p.getPessoaProf() != null ? p.getPessoaProf().getNomePessoa() : "Profissional",
                        p.getConselhoProf()
                ));
            }
        }

        if (profsMap.isEmpty()) {
            for (Profissional p : profissionalRepository.findAll()) {
                profsMap.putIfAbsent(p.getIdProf(), new OpcoesCompartilhamentoDTO.ProfissionalOpcao(
                        p.getIdProf(),
                        p.getPessoaProf() != null ? p.getPessoaProf().getNomePessoa() : "Profissional",
                        p.getConselhoProf()
                ));
            }
        }

        return new OpcoesCompartilhamentoDTO(
                new ArrayList<>(clinicasMap.values()),
                new ArrayList<>(profsMap.values())
        );
    }

    private void validarVisualizacao(Arquivo arquivo, String email) {
        if (email == null || email.isBlank()) return;
        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user != null && !podeVisualizar(arquivo, user)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar este arquivo.");
        }
    }

    private void validarProprietarioOuAdmin(Arquivo arquivo, String email, String mensagemErro) {
        if (email == null || email.isBlank()) return;
        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) return;

        boolean isAdmin = hasPerfil(user, "ADMIN");
        boolean isOwner = arquivo.getUploadadoPor() != null
                && arquivo.getUploadadoPor().getIdUser().equals(user.getIdUser());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(mensagemErro);
        }
    }

    private boolean podeVisualizar(Arquivo arquivo, User user) {
        // 1. Admin geral vê tudo
        if (hasPerfil(user, "ADMIN")) {
            return true;
        }

        // 2. Autor do upload sempre vê (para revisar antes de liberar)
        if (arquivo.getUploadadoPor() != null
                && arquivo.getUploadadoPor().getIdUser().equals(user.getIdUser())) {
            return true;
        }

        Paciente paciente = arquivo.getPaciente();
        if (paciente == null) {
            return false;
        }

        // 3. Responsável vê se estiver compartilhado com o responsável E o paciente for dependente dele
        if (hasPerfil(user, "RESPONSAVEL") && arquivo.isCompartilhadoComResponsavel()) {
            if (user.getPessoa() != null) {
                Responsavel resp = responsavelRepository.findByPessoaResponsavel(user.getPessoa()).orElse(null);
                if (resp != null && resp.getPacientes() != null) {
                    boolean ehDependente = resp.getPacientes().stream()
                            .anyMatch(p -> p.getIdPaciente().equals(paciente.getIdPaciente()));
                    if (ehDependente) return true;
                }
            }
        }

        // 4. Profissional vê se estiver nos profissionais compartilhados OU se liberado para uma clínica em que atua
        if (hasPerfil(user, "PROFISSIONAL") && user.getPessoa() != null && user.getPessoa().getProfissional() != null) {
            String idProf = user.getPessoa().getProfissional().getIdProf();
            if (arquivo.getProfissionaisCompartilhados() != null) {
                boolean compDireto = arquivo.getProfissionaisCompartilhados().stream()
                        .anyMatch(p -> p.getIdProf().equals(idProf));
                if (compDireto) return true;
            }
            if (arquivo.isCompartilhadoComClinica()) {
                List<String> clinicasDoProf = profissionalClinicaRepository.findByProfissionalIdProf(idProf).stream()
                        .filter(pc -> pc.getClinica() != null)
                        .map(pc -> pc.getClinica().getIdClinica())
                        .toList();
                if (bateClinicaCompartilhada(arquivo, paciente, clinicasDoProf)) {
                    return true;
                }
            }
        }

        // 5. Funcionário / Admin de Clínica vê se compartilhado com a clínica onde trabalha
        if ((hasPerfil(user, "CLINICA") || hasPerfil(user, "FUNCIONARIO_CLINICA") || hasPerfil(user, "ADMIN_CLINICA"))
                && arquivo.isCompartilhadoComClinica() && user.getPessoa() != null) {
            List<String> clinicasDoFunc = funcionarioClinicaRepository.findByPessoa(user.getPessoa()).stream()
                    .filter(fc -> fc.getClinica() != null)
                    .map(fc -> fc.getClinica().getIdClinica())
                    .toList();
            if (bateClinicaCompartilhada(arquivo, paciente, clinicasDoFunc)) {
                return true;
            }
        }

        return false;
    }

    private boolean bateClinicaCompartilhada(Arquivo arquivo, Paciente paciente, List<String> clinicasDoUsuario) {
        if (clinicasDoUsuario.isEmpty()) return false;
        if (arquivo.getClinicaCompartilhada() != null) {
            return clinicasDoUsuario.contains(arquivo.getClinicaCompartilhada().getIdClinica());
        }
        // Se foi liberado para "clínica" sem especificar uma única, verifica as clínicas em que o paciente é atendido
        if (paciente.getClinicas() != null) {
            return paciente.getClinicas().stream()
                    .filter(pc -> pc.getAtivo() == null || pc.getAtivo())
                    .anyMatch(pc -> pc.getClinica() != null && clinicasDoUsuario.contains(pc.getClinica().getIdClinica()));
        }
        return false;
    }

    private boolean hasPerfil(User user, String nomePerfil) {
        return user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> nomePerfil.equalsIgnoreCase(p.getNome()));
    }

    private String normalizarExtensao(String rawExt) {
        if (rawExt == null || rawExt.isBlank()) {
            throw new IllegalArgumentException("A extensão do arquivo é obrigatória.");
        }
        String ext = rawExt.trim().toLowerCase().replaceAll("^\\.+", "");
        if (!EXTENSOES_PERMITIDAS.contains(ext)) {
            throw new IllegalArgumentException("Formato de arquivo não permitido (" + ext + "). Permitidos: PDF, DOC, DOCX, TXT, ODT e RTF.");
        }
        return ext;
    }

    private String gerarNomePadronizado(TipoArquivo tipoArquivo, Paciente paciente, String ext) {
        String tipoSlug = toPascalSlug(tipoArquivo.getNomeTipo(), "Documento");
        String nomePac = paciente.getPessoaPaciente() != null
                ? paciente.getPessoaPaciente().getNomePessoa()
                : paciente.getApelidoPaciente();
        String pacienteSlug = toPascalSlug(nomePac, "Paciente");
        String dataHora = LocalDateTime.now().format(DATA_HORA_FMT);

        String baseName = tipoSlug + "_" + pacienteSlug + "_" + dataHora;
        if (baseName.length() > 180) {
            baseName = baseName.substring(0, 180);
        }
        return baseName + "." + ext;
    }

    private String toPascalSlug(String input, String fallback) {
        if (input == null || input.isBlank()) return fallback;
        String semAcento = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String[] palavras = semAcento.replaceAll("[^a-zA-Z0-9\\s]", " ").trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String palavra : palavras) {
            if (palavra.isEmpty()) continue;
            sb.append(Character.toUpperCase(palavra.charAt(0)));
            if (palavra.length() > 1) {
                sb.append(palavra.substring(1).toLowerCase());
            }
        }
        return sb.length() > 0 ? sb.toString() : fallback;
    }
}
