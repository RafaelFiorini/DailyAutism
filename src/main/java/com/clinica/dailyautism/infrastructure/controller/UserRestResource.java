package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.UserService;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.infrastructure.dto.SaveUserFullDTO;
import com.clinica.dailyautism.infrastructure.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserRestResource {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('LISTAR_USUARIO')")
    public ResponseEntity<List<UserDTO>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('VER_USUARIO')")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('CRIAR_USUARIO')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody SaveUserFullDTO dto) {
        UserDTO user = userService.createFullUser(dto);
        return ResponseEntity.created(URI.create("/users/" + user.getIdUser()))
                .body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('EDITAR_USUARIO')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id,
                                              @Valid @RequestBody SaveUserFullDTO dto) {
        return ResponseEntity.ok(userService.updateFullUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('DELETAR_USUARIO')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/pessoa/{pessoaId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('EDITAR_USUARIO')")
    public ResponseEntity<UserDTO> vincularPessoa(@PathVariable String userId,
                                                  @PathVariable String pessoaId) {
        User user = userService.vincularPessoa(userId, pessoaId);
        return ResponseEntity.ok(UserDTO.create(user));
    }

    @PutMapping("/{userId}/perfis")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('EDITAR_USUARIO')")
    public ResponseEntity<Void> atualizarPerfis(@PathVariable String userId,
                                                @RequestBody Set<String> idPerfis) {
        userService.atualizarPerfis(userId, idPerfis);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getMe(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(userService.mapUserToDTO(user));
    }
}
