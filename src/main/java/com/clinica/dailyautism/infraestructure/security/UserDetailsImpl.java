package com.clinica.dailyautism.infraestructure.security;

import com.clinica.dailyautism.domain.entity.security.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private final String id;
    private final String email;
    private final String senha;
    private final Collection<GrantedAuthority> authorities;

    public UserDetailsImpl(User user) {
        this.id = user.getIdUser();
        this.email = user.getEmailUser();
        this.senha = user.getPasswordUser();

        // Achata: User → Perfis → Ações → GrantedAuthority
        this.authorities = user.getPerfis().stream()
                .flatMap(perfil -> perfil.getAcoes().stream())
                .map(acao -> new SimpleGrantedAuthority(acao.getNome()))
                .collect(Collectors.toSet());
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
}