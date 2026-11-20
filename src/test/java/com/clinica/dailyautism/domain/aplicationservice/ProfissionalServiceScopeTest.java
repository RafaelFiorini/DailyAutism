package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceScopeTest {

    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private UserRepository userRepository;
    @Mock private PerfilRepository perfilRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProfissionalClinicaRepository profissionalClinicaRepository;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private FuncionarioClinicaRepository funcionarioClinicaRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    private Clinica clinicaAttenzione;
    private Pessoa pessoaAlexandra;
    private User userAlexandra;
    private Profissional profAlexandra;
    private Pessoa pessoaColega;
    private Profissional profColega;

    @BeforeEach
    void setUp() {
        clinicaAttenzione = Clinica.builder()
                .idClinica("clinica-1")
                .nomeClinica("Attenzione Saúde Mental")
                .build();

        pessoaAlexandra = Pessoa.builder()
                .idPessoa("pessoa-alexandra")
                .nomePessoa("Alexandra Borges Stertz")
                .CPFPessoa("11122233344")
                .build();

        profAlexandra = Profissional.builder()
                .idProf("prof-alexandra")
                .pessoaProf(pessoaAlexandra)
                .conselhoProf("CRP 12345")
                .build();
        profAlexandra.setAtivo(true);
        pessoaAlexandra.setProfissional(profAlexandra);

        userAlexandra = User.builder()
                .idUser("user-alexandra")
                .emailUser("alexandra@attenzione.com.br")
                .pessoa(pessoaAlexandra)
                .perfis(Set.of(
                        Perfil.builder().nome("ADMIN_CLINICA").build(),
                        Perfil.builder().nome("PROFISSIONAL").build()
                ))
                .build();

        pessoaColega = Pessoa.builder()
                .idPessoa("pessoa-colega")
                .nomePessoa("Dr. Carlos Fono")
                .CPFPessoa("55566677788")
                .build();

        profColega = Profissional.builder()
                .idProf("prof-colega")
                .pessoaProf(pessoaColega)
                .conselhoProf("CRFa 9999")
                .build();
        profColega.setAtivo(true);
        pessoaColega.setProfissional(profColega);
    }

    @Test
    @DisplayName("Usuário ADMIN_CLINICA que também é PROFISSIONAL deve listar todos os profissionais da clínica incluindo funcionários com perfil de saúde")
    void testListProfissionaisForUser_AdminClinicaEProfissional_RetornaProfissionais() {
        when(userRepository.findByEmailUser("alexandra@attenzione.com.br")).thenReturn(Optional.of(userAlexandra));

        FuncionarioClinica funcAlexandra = FuncionarioClinica.builder()
                .pessoa(pessoaAlexandra)
                .clinica(clinicaAttenzione)
                .build();
        FuncionarioClinica funcColega = FuncionarioClinica.builder()
                .pessoa(pessoaColega)
                .clinica(clinicaAttenzione)
                .build();

        when(funcionarioClinicaRepository.findByPessoa(pessoaAlexandra)).thenReturn(List.of(funcAlexandra));
        when(funcionarioClinicaRepository.findByClinicaIdClinica("clinica-1")).thenReturn(List.of(funcAlexandra, funcColega));
        when(profissionalClinicaRepository.findProfissionaisByClinicaIds(List.of("clinica-1"))).thenReturn(List.of());
        when(profissionalRepository.findByPessoaProf(pessoaColega)).thenReturn(Optional.of(profColega));
        when(profissionalRepository.findByPessoaProf(pessoaAlexandra)).thenReturn(Optional.of(profAlexandra));

        List<Profissional> result = profissionalService.listProfissionaisForUser("alexandra@attenzione.com.br");

        assertEquals(2, result.size());
        assertTrue(result.contains(profAlexandra));
        assertTrue(result.contains(profColega));
        verify(profissionalClinicaRepository, atLeastOnce()).save(any(ProfissionalClinica.class));
    }

    @Test
    @DisplayName("Usuário ADMIN_CLINICA pode carregar profissional que é funcionário da clínica")
    void testLoadProfissionalForUser_FuncionarioDaClinica_Sucesso() {
        when(profissionalRepository.findById("prof-colega")).thenReturn(Optional.of(profColega));
        when(userRepository.findByEmailUser("alexandra@attenzione.com.br")).thenReturn(Optional.of(userAlexandra));

        FuncionarioClinica funcAlexandra = FuncionarioClinica.builder()
                .pessoa(pessoaAlexandra)
                .clinica(clinicaAttenzione)
                .build();
        FuncionarioClinica funcColega = FuncionarioClinica.builder()
                .pessoa(pessoaColega)
                .clinica(clinicaAttenzione)
                .build();

        when(funcionarioClinicaRepository.findByPessoa(pessoaAlexandra)).thenReturn(List.of(funcAlexandra));
        when(funcionarioClinicaRepository.findByPessoa(pessoaColega)).thenReturn(List.of(funcColega));
        when(profissionalClinicaRepository.findByProfissionalIdProf("prof-colega")).thenReturn(List.of());

        Profissional loaded = profissionalService.loadProfissionalForUser("prof-colega", "alexandra@attenzione.com.br");

        assertNotNull(loaded);
        assertEquals("prof-colega", loaded.getIdProf());
    }

    @Test
    @DisplayName("Usuário ADMIN_CLINICA tentando acessar profissional de outra clínica sem vínculo lança AccessDeniedException")
    void testLoadProfissionalForUser_OutraClinica_LancaAccessDenied() {
        Pessoa pessoaOutra = Pessoa.builder().idPessoa("pessoa-outra").build();
        Profissional profOutra = Profissional.builder().idProf("prof-outra").pessoaProf(pessoaOutra).build();

        when(profissionalRepository.findById("prof-outra")).thenReturn(Optional.of(profOutra));
        when(userRepository.findByEmailUser("alexandra@attenzione.com.br")).thenReturn(Optional.of(userAlexandra));

        FuncionarioClinica funcAlexandra = FuncionarioClinica.builder()
                .pessoa(pessoaAlexandra)
                .clinica(clinicaAttenzione)
                .build();

        when(funcionarioClinicaRepository.findByPessoa(pessoaAlexandra)).thenReturn(List.of(funcAlexandra));
        when(funcionarioClinicaRepository.findByPessoa(pessoaOutra)).thenReturn(List.of());
        when(profissionalClinicaRepository.findByProfissionalIdProf("prof-outra")).thenReturn(List.of());

        assertThrows(AccessDeniedException.class, () ->
                profissionalService.loadProfissionalForUser("prof-outra", "alexandra@attenzione.com.br")
        );
    }
}
