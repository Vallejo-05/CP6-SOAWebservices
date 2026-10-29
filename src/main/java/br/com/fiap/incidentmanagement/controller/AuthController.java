package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.domain.user.User;
import br.com.fiap.incidentmanagement.dto.auth.LoginRequest;
import br.com.fiap.incidentmanagement.dto.auth.LoginResponse;
import br.com.fiap.incidentmanagement.infra.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager manager;
    private final TokenService tokenService;

    @PostMapping
    @Operation(summary = "Efetuar login", description = "Retorna o token JWT a ser enviado no cabeçalho Authorization: Bearer <token>.")
    @ApiResponse(responseCode = "200", description = "Login efetuado")
    @ApiResponse(responseCode = "401", description = "Login ou senha inválidos, ou usuário inativo")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var authentication = manager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new LoginResponse(
                tokenService.generateToken(user),
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getRole().getName()));
    }
}
