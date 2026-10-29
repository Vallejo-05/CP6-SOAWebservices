package br.com.fiap.incidentmanagement.infra.exception;

// Operação não permitida para o usuário autenticado (HTTP 403)
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
