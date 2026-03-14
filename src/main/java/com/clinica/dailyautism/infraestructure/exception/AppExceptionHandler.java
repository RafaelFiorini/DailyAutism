package com.clinica.dailyautism.infraestructure.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RequestException.class)
    public ResponseEntity<Object> handleRequestException(RequestException ex, WebRequest request) {

        return handleException(
                ex,
                ex.getErrorCode(),
                ex.getMessage(),
                BAD_REQUEST,
                request);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        return handleException(
                ex,
                null,
                ex.getMessage(),
                INTERNAL_SERVER_ERROR,
                request);
    }

    // Método privado para aceitar todas as exceções, usado para centralizar a
    // construção do objeto RestError e evitar repetição de código
    @ExceptionHandler(value = Exception.class)
    private ResponseEntity<Object> handleException(
            Exception ex,
            String errorCode,
            String message,
            HttpStatus status,
            WebRequest request) {

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;

        return handleExceptionInternal(
                ex,
                RestError
                        .builder()
                        .errorCode(errorCode)
                        .errorMessage(message)
                        .status(status.value())
                        .path(servletWebRequest.getRequest().getRequestURI())
                        .build(),
                new HttpHeaders(),
                status,
                request);
    }

}
