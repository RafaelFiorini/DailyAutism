package com.clinica.dailyautism.infraestructure.security;


import com.clinica.dailyautism.domain.entity.security.Acao;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.repository.AcaoRepository;
import com.clinica.dailyautism.domain.repository.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AcaoRepository acaoRepository;
    private final PerfilRepository perfilRepository;

    @Override
    public void run(String... args) {
        seedAcoes();
        seedPerfis();
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
                // Vínculos
                "VINCULAR_PLANO_SAUDE", "VER_CLINICA_PLANO_SAUDE", "DESVINCULAR_PLANO_SAUDE",
                "VINCULAR_PROFISSIONAL_CLINICA", "VER_PROFISSIONAL_CLINICA", "DESVINCULAR_PROFISSIONAL_CLINICA",
                "VINCULAR_PACIENTE_CLINICA", "VER_PACIENTE_CLINICA", "DESVINCULAR_PACIENTE_CLINICA",
                "VINCULAR_PACIENTE_PLANO_SAUDE", "VER_PACIENTE_PLANO_SAUDE", "DESVINCULAR_PACIENTE_PLANO_SAUDE",
                "EDITAR_PACIENTE_PLANO_SAUDE", "VINCULAR_PROFISSIONAL_PACIENTE", "VER_PROFISSIONAL_PACIENTE",
                "EDITAR_ANAMNESE",
                // Usuario
                "CRIAR_USUARIO", "VER_USUARIO", "LISTAR_USUARIO",
                "EDITAR_USUARIO", "DELETAR_USUARIO"
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

        // RESPONSAVEL — vê agenda, arquivos e dados do paciente
        criarPerfilSeNaoExistir("RESPONSAVEL", acaoRepository.findAllByNomeIn(Set.of(
                "VER_PACIENTE", "LISTAR_PACIENTE",
                "VER_ARQUIVO", "LISTAR_ARQUIVO", "BAIXAR_ARQUIVO",
                "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "CRIAR_COMPROMISSO"
        )));

        // PROFISSIONAL — gerencia agenda e documentação dos seus pacientes
        criarPerfilSeNaoExistir("PROFISSIONAL", acaoRepository.findAllByNomeIn(Set.of(
                "VER_PACIENTE", "LISTAR_PACIENTE",
                "CRIAR_ARQUIVO", "VER_ARQUIVO", "LISTAR_ARQUIVO", "BAIXAR_ARQUIVO",
                "CRIAR_COMPROMISSO", "VER_COMPROMISSO", "LISTAR_COMPROMISSO",
                "EDITAR_COMPROMISSO", "APROVAR_COMPROMISSO",
                "VER_PROFISSIONAL_PACIENTE", "EDITAR_ANAMNESE"
        )));

        // CLINICA — gerencia estrutura da clínica
        criarPerfilSeNaoExistir("CLINICA", acaoRepository.findAllByNomeIn(Set.of(
                "VER_CLINICA", "EDITAR_CLINICA",
                "CRIAR_PROFISSIONAL", "VER_PROFISSIONAL", "LISTAR_PROFISSIONAL",
                "VINCULAR_PROFISSIONAL_CLINICA", "VER_PROFISSIONAL_CLINICA",
                "VINCULAR_PACIENTE_CLINICA", "VER_PACIENTE_CLINICA",
                "VER_CLINICA_PLANO_SAUDE", "VINCULAR_PLANO_SAUDE"
        )));
    }

    private void criarPerfilSeNaoExistir(String nome, Iterable<Acao> acoes) {
        if (perfilRepository.findByNome(nome).isEmpty()) {
            Perfil perfil = Perfil.builder()
                    .nome(nome)
                    .build();
            perfil.getAcoes().addAll((java.util.Collection<? extends Acao>) acoes);
            perfilRepository.save(perfil);
        }
    }
}