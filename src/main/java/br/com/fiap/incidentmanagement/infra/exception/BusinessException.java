package br.com.fiap.incidentmanagement.infra.exception;

// Violação de regra de negócio (HTTP 400)
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
