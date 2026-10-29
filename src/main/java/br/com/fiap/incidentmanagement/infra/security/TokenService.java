package br.com.fiap.incidentmanagement.infra.security;

import br.com.fiap.incidentmanagement.domain.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    private static final String ISSUER = "IncidentManagement";

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getLogin())
                    .withClaim("id", user.getId())
                    .withClaim("role", user.getRole().getName())
                    .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Erro ao gerar o token JWT.", e);
        }
    }

    public String getSubject(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token JWT inválido ou expirado.", e);
        }
    }
}
