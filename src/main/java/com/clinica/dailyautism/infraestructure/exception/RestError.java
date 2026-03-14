package com.clinica.dailyautism.infraestructure.exception;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestError {

    private final String errorCode;
    private final String errorMessage;
    private final List<String> details;
    private final int status;
    private final String path;

}
