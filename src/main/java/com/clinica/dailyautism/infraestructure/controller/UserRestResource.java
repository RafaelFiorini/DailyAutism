package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.UserService;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.infraestructure.dto.SaveUserDTO;
import com.clinica.dailyautism.infraestructure.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserRestResource {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody SaveUserDTO saveUserDTO) {
        User user = userService.createUser(saveUserDTO);
        return ResponseEntity.created(URI.create("/users/" + user.getIdUser()))
                .body(UserDTO.create(user));
    }

    @PutMapping("/{userId}/pessoa/{pessoaId}")
    public ResponseEntity<UserDTO> vincularPessoa(@PathVariable String userId,
                                                  @PathVariable String pessoaId) {
        User user = userService.vincularPessoa(userId, pessoaId);
        return ResponseEntity.ok(UserDTO.create(user));
    }

    @PutMapping("/{userId}/perfis")
    @PreAuthorize("hasAuthority('EDITAR_USUARIO')")
    public ResponseEntity<Void> atualizarPerfis(@PathVariable String userId,
                                                @RequestBody Set<String> idPerfis) {
        userService.atualizarPerfis(userId, idPerfis);
        return ResponseEntity.noContent().build();
    }
}