package com.clinica.dailyautism.infraestructure.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;



import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.List;;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(value = RequestException.class)
        public ResponseEntity<Object> handleRequestException(RequestException ex, WebRequest request) {

                return handleException(
                                ex,
                                ex.getErrorCode(),
                                ex.getMessage(),
                                null,
                                BAD_REQUEST,
                                request);
        }

        @ExceptionHandler(value = Exception.class)
        public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
                return handleException(
                                ex,
                                null,
                                ex.getMessage(),
                                null,
                                INTERNAL_SERVER_ERROR,
                                request);
        }

        // Sobrescreve o método handleMethodArgumentNotValid para personalizar a
        // resposta quando ocorrer um erro de validação

        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {

                List<String> details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .toList();
                return handleException(
                                ex,
                                "VALIDATION_ERROR",
                                null,
                                details,
                                BAD_REQUEST,
                                request);
        }

        // Método privado para aceitar todas as exceções, usado para centralizar a
        // construção do objeto RestError e evitar repetição de código
        @ExceptionHandler(value = Exception.class)
        private ResponseEntity<Object> handleException(
                        Exception ex,
                        String errorCode,
                        String message,
                        List<String> details,
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
