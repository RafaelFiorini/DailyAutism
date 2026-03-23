package com.clinica.dailyautism.domain.entity.security;

import com.clinica.dailyautism.domain.entity.BaseEntity;
import com.clinica.dailyautism.domain.entity.Pessoa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)  // ← adicionar isso
@SQLRestriction("ativo = true")        // ← adicionar isso
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idUser;

    @Column(nullable = false, length = 200)
    private String nomeUser;

    @Column(nullable = false, length = 100, unique = true)
    private String emailUser;

    @Column(nullable = false)
    private String passwordUser;

    @OneToOne
    @JoinColumn(name = "idPessoa", referencedColumnName = "idPessoa")
    private Pessoa pessoa;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordUser;
    }

    @Override
    public String getUsername() {
        return emailUser;
    }
}

