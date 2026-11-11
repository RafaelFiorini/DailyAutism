package com.clinica.dailyautism.domain.exception;

import lombok.Getter;

/**
 * Exceção lançada ao tentar criar um registro cujo identificador UNIQUE pertence a um registro inativo (soft-deleted).
 */
@Getter
public class InactiveResourceConflictException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;
    private final String resourceName;

    public InactiveResourceConflictException(
            String resourceType,
            String resourceId,
            String resourceName,
            String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
    }
}
