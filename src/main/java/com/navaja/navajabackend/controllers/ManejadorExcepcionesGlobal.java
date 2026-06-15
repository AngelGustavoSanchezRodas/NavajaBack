package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.exceptions.AccesoDenegadoException;
import com.navaja.navajabackend.exceptions.AliasEnUsoException;
import com.navaja.navajabackend.exceptions.LimiteExcedidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ProblemDetail;
import java.net.URI;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(LimiteExcedidoException.class)
    public ProblemDetail handleLimiteExcedido(LimiteExcedidoException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle("Límite Excedido");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("error_code", "LIMITE_EXCEDIDO");
        return problemDetail;
    }

    @ExceptionHandler(AliasEnUsoException.class)
    public ProblemDetail handleAliasEnUso(AliasEnUsoException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Alias En Uso");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("error_code", "ALIAS_EN_USO");
        return problemDetail;
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle("Acceso Denegado");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("error_code", "REQUIERE_PREMIUM");
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, 
            exception.getMessage() == null ? "Solicitud inválida" : exception.getMessage());
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("error_code", "BAD_REQUEST");
        return problemDetail;
    }
}
