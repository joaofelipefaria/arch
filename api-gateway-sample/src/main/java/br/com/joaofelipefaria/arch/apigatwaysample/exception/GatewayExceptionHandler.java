package br.com.joaofelipefaria.arch.apigatwaysample.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotFound(
            RouteNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Route Not Found",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ErrorResponse> handleGatewayException(
            GatewayException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_GATEWAY.value(),
                        "Gateway Error",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

}