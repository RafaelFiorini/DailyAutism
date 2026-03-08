package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.UserService;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.infraestructure.dto.SaveUserDTO;
import com.clinica.dailyautism.infraestructure.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;


@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserRestResource {

        private final UserService userService;

        @PostMapping
        public ResponseEntity<UserDTO> createUser(@RequestBody SaveUserDTO saveUserDTO){
            User user = userService.createUser(saveUserDTO);
            return ResponseEntity.created(URI.create("/users/" + user.getIdUser()))
                    .body(UserDTO.create(user));
        }

}
