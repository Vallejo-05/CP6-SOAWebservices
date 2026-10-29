package br.com.fiap.incidentmanagement.dto.auth;

// O front-end guarda o token e os dados do usuário (id e login) no localStorage
public record LoginResponse(
        String tokenJWT,
        Long id,
        String login,
        String name,
        String role) {
}
