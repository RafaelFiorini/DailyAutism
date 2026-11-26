package com.clinica.dailyautism.domain.entity;

import com.clinica.dailyautism.domain.entity.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idNotificacao", callSuper = false)
@ToString(exclude = {"usuarioDestino"})
@SQLRestriction("ativo = true")
public class Notificacao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String idNotificacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user_destino", referencedColumnName = "idUser", nullable = false)
    private User usuarioDestino;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private TipoNotificacao tipo = TipoNotificacao.SISTEMA;

    @Column(length = 36)
    private String idReferencia;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lida = false;

    private LocalDateTime lidaEm;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
