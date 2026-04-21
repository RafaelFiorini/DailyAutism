// UserNotFoundException.java
package com.clinica.dailyautism.domain.exception;

import com.clinica.dailyautism.infrastructure.exception.RequestException;

public class UserNotFoundException extends RequestException {
    public UserNotFoundException(String userId) {
        super("Usuário não encontrado: " + userId, "UserNotFound");
    }
}