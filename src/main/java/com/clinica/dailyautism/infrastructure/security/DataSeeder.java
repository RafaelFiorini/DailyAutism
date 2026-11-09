package com.clinica.dailyautism.infrastructure.security;

import com.clinica.dailyautism.domain.entity.Periodicidade;
import com.clinica.dailyautism.domain.entity.security.Acao;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.AcaoRepository;
import com.clinica.dailyautism.domain.repository.PerfilRepository;
import com.clinica.dailyautism.domain.repository.PeriodicidadeRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AcaoRepository acaoRepository;
    private final PerfilRepository perfilRepository;
    private final UserRepository userRepository;
    private final PeriodicidadeRepository periodicidadeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAcoes();
        seedPerfis();
        seedAdminUser();
        seedPeriodicidades();
    }

    private void seedAdminUser() {
        if (userRepository.findByEmailUser("admin@dailyautism.com").isEmpty()) {
            Perfil adminPerfil = perfilRepository.findByNome("ADMIN").orElseThrow();
            User admin = User.builder()
                    .nomeUser("Administrador DailyAutism")
                    .emailUser("admin@dailyautism.com")
                    .passwordUser(passwordEncoder.encode("admin123"))
                    .perfis(Set.of(adminPerfil))
                    .build();
            userRepository.save(admin);
        }
    }

    private void seedAcoes() {
        List<String> acoes = List.of(
                // Paciente
                "CRIAR_PACIENTE", "VER_PACIENTE", "LISTAR_PACIENTE",
                "EDITAR_PACIENTE", "DELETAR_PACIENTE",
                // Responsavel
                "CRIAR_RESPONSAVEL", "VER_RESPONSAVEL", "LISTAR_RESPONSAVEL",
                "EDITAR_RESPONSAVEL", "DELETAR_RESPONSAVEL",
                // Profissional
                "CRIAR_PROFISSIONAL", "VER_PROFISSIONAL", "LISTAR_PROFISSIONAL",
                "EDITAR_PROFISSIONAL", "DELETAR_PROFISSIONAL",
                // Arquivo
                "CRIAR_ARQUIVO", "VER_ARQUIVO", "LISTAR_ARQUIVO",
                "DELETAR_ARQUIVO", "BAIXAR_ARQUIVO",
                // Compromisso
                "CRIAR_COMPROMISSO", "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "EDITAR_COMPROMISSO", "DELETAR_COMPROMISSO", "APROVAR_COMPROMISSO",
                // Clinica
                "CRIAR_CLINICA", "VER_CLINICA", "LISTAR_CLINICA",
                "EDITAR_CLINICA", "DELETAR_CLINICA",
                // Plano Saude
                "CRIAR_PLANO_SAUDE", "VER_PLANO_SAUDE", "LISTAR_PLANO_SAUDE",
                "EDITAR_PLANO_SAUDE", "DELETAR_PLANO_SAUDE",
                // Periodicidade
                "CRIAR_PERIODICIDADE", "VER_PERIODICIDADE", "LISTAR_PERIODICIDADE",
                "EDITAR_PERIODICIDADE", "DELETAR_PERIODICIDADE",
                // Vínculos
                "VINCULAR_PLANO_SAUDE", "VER_CLINICA_PLANO_SAUDE", "DESVINCULAR_PLANO_SAUDE",
                "VINCULAR_PROFISSIONAL_CLINICA", "VER_PROFISSIONAL_CLINICA", "DESVINCULAR_PROFISSIONAL_CLINICA",
                "VINCULAR_PACIENTE_CLINICA", "VER_PACIENTE_CLINICA", "DESVINCULAR_PACIENTE_CLINICA",
                "VINCULAR_PACIENTE_PLANO_SAUDE", "VER_PACIENTE_PLANO_SAUDE", "DESVINCULAR_PACIENTE_PLANO_SAUDE",
                "EDITAR_PACIENTE_PLANO_SAUDE", "VINCULAR_PROFISSIONAL_PACIENTE", "VER_PROFISSIONAL_PACIENTE",
                "EDITAR_ANAMNESE",
                // Usuario
                "CRIAR_USUARIO", "VER_USUARIO", "LISTAR_USUARIO",
                "EDITAR_USUARIO", "DELETAR_USUARIO",
                //agenda
                "VER_AGENDA",
                // Pessoa
                "CRIAR_PESSOA", "VER_PESSOA", "LISTAR_PESSOA",
                "EDITAR_PESSOA", "DELETAR_PESSOA"
        );

        acoes.forEach(nome -> {
            if (acaoRepository.findByNome(nome).isEmpty()) {
                Acao acao = Acao.builder()
                        .nome(nome)
                        .build();
                acaoRepository.save(acao);
            }
        });
    }

    private void seedPerfis() {
        // ADMIN — acesso total
        criarPerfilSeNaoExistir("ADMIN", acaoRepository.findAll());

        // RESPONSAVEL — vê agenda, arquivos e dados do paciente, e cadastra/edita seus pacientes
        criarPerfilSeNaoExistir("RESPONSAVEL", acaoRepository.findAllByNomeIn(Set.of(
                "VER_PACIENTE", "LISTAR_PACIENTE", "CRIAR_PACIENTE", "EDITAR_PACIENTE",
                "VER_ARQUIVO", "LISTAR_ARQUIVO", "BAIXAR_ARQUIVO",
                "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "CRIAR_COMPROMISSO", "VER_AGENDA", "APROVAR_COMPROMISSO",
                "VER_PLANO_SAUDE", "LISTAR_PLANO_SAUDE",
                "VER_PERIODICIDADE", "LISTAR_PERIODICIDADE"
        )));

        // PROFISSIONAL — gerencia agenda e documentação dos seus pacientes
        criarPerfilSeNaoExistir("PROFISSIONAL", acaoRepository.findAllByNomeIn(Set.of(
                "VER_PACIENTE", "LISTAR_PACIENTE",
                "CRIAR_ARQUIVO", "VER_ARQUIVO", "LISTAR_ARQUIVO", "BAIXAR_ARQUIVO",
                "CRIAR_COMPROMISSO", "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "EDITAR_COMPROMISSO", "APROVAR_COMPROMISSO",
                "VER_PROFISSIONAL_PACIENTE", "EDITAR_ANAMNESE", "VER_AGENDA",
                "VER_PLANO_SAUDE", "LISTAR_PLANO_SAUDE",
                "VER_PERIODICIDADE", "LISTAR_PERIODICIDADE"
        )));

        // ADMIN_CLINICA — administrador/gestor da clínica (gestão completa da unidade, planos e periodicidades)
        Set<String> acoesAdminClinica = Set.of(
                "VER_CLINICA", "EDITAR_CLINICA",
                "CRIAR_PROFISSIONAL", "VER_PROFISSIONAL", "LISTAR_PROFISSIONAL", "EDITAR_PROFISSIONAL",
                "VINCULAR_PROFISSIONAL_CLINICA", "VER_PROFISSIONAL_CLINICA",
                "VINCULAR_PACIENTE_CLINICA", "VER_PACIENTE_CLINICA",
                "VER_CLINICA_PLANO_SAUDE", "VINCULAR_PLANO_SAUDE", "VER_AGENDA",
                "CRIAR_COMPROMISSO", "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "EDITAR_COMPROMISSO", "APROVAR_COMPROMISSO",
                "LISTAR_PACIENTE", "VER_PACIENTE", "CRIAR_PACIENTE", "EDITAR_PACIENTE",
                "VER_RESPONSAVEL", "LISTAR_RESPONSAVEL", "CRIAR_RESPONSAVEL", "EDITAR_RESPONSAVEL",
                "CRIAR_PLANO_SAUDE", "VER_PLANO_SAUDE", "LISTAR_PLANO_SAUDE", "EDITAR_PLANO_SAUDE", "DELETAR_PLANO_SAUDE",
                "CRIAR_PERIODICIDADE", "VER_PERIODICIDADE", "LISTAR_PERIODICIDADE", "EDITAR_PERIODICIDADE", "DELETAR_PERIODICIDADE"
        );
        criarPerfilSeNaoExistir("ADMIN_CLINICA", acaoRepository.findAllByNomeIn(acoesAdminClinica));

        // FUNCIONARIO_CLINICA / CLINICA — operacional / recepção (consulta planos e periodicidades, faz agendamentos)
        Set<String> acoesFuncionarioClinica = Set.of(
                "VER_CLINICA",
                "VER_PROFISSIONAL", "LISTAR_PROFISSIONAL",
                "VINCULAR_PROFISSIONAL_CLINICA", "VER_PROFISSIONAL_CLINICA",
                "VINCULAR_PACIENTE_CLINICA", "VER_PACIENTE_CLINICA",
                "VER_CLINICA_PLANO_SAUDE", "VINCULAR_PLANO_SAUDE", "VER_AGENDA",
                "CRIAR_COMPROMISSO", "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "EDITAR_COMPROMISSO", "APROVAR_COMPROMISSO",
                "LISTAR_PACIENTE", "VER_PACIENTE", "CRIAR_PACIENTE", "EDITAR_PACIENTE",
                "VER_RESPONSAVEL", "LISTAR_RESPONSAVEL", "CRIAR_RESPONSAVEL", "EDITAR_RESPONSAVEL",
                "VER_PLANO_SAUDE", "LISTAR_PLANO_SAUDE",
                "VER_PERIODICIDADE", "LISTAR_PERIODICIDADE"
        );
        criarPerfilSeNaoExistir("CLINICA", acaoRepository.findAllByNomeIn(acoesFuncionarioClinica));
        criarPerfilSeNaoExistir("FUNCIONARIO_CLINICA", acaoRepository.findAllByNomeIn(acoesFuncionarioClinica));
    }

    private void seedPeriodicidades() {
        if (periodicidadeRepository.count() == 0) {
            List<Periodicidade> padroes = List.of(
                    Periodicidade.builder()
                            .nomePeriodicidade("Semanal")
                            .diasIntervalo(7)
                            .descricaoPeriodicidade("Atendimento 1 vez por semana")
                            .build(),
                    Periodicidade.builder()
                            .nomePeriodicidade("Quinzenal")
                            .diasIntervalo(14)
                            .descricaoPeriodicidade("Atendimento a cada 15 dias (duas semanas)")
                            .build(),
                    Periodicidade.builder()
                            .nomePeriodicidade("Mensal")
                            .diasIntervalo(30)
                            .descricaoPeriodicidade("Atendimento 1 vez ao mês")
                            .build(),
                    Periodicidade.builder()
                            .nomePeriodicidade("Diária")
                            .diasIntervalo(1)
                            .descricaoPeriodicidade("Atendimento diário")
                            .build(),
                    Periodicidade.builder()
                            .nomePeriodicidade("Avulsa / Única")
                            .diasIntervalo(0)
                            .descricaoPeriodicidade("Sessão ou avaliação avulsa sem recorrência fixa")
                            .build()
            );
            padroes.forEach(p -> {
                if (p.getAtivo() == null) p.setAtivo(true);
            });
            periodicidadeRepository.saveAll(padroes);
        }
    }

    private void criarPerfilSeNaoExistir(String nome, Iterable<Acao> acoes) {
        Perfil perfil = perfilRepository.findByNome(nome).orElseGet(() -> {
            Perfil novo = Perfil.builder().nome(nome).build();
            return perfilRepository.save(novo);
        });

        boolean atualizado = false;
        for (Acao acao : acoes) {
            if (perfil.getAcoes().stream().noneMatch(a -> a.getNome().equals(acao.getNome()))) {
                perfil.getAcoes().add(acao);
                atualizado = true;
            }
        }

        if (atualizado) {
            perfilRepository.save(perfil);
        }
    }
}
