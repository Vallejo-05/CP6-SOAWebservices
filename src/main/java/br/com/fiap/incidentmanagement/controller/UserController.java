package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.user.*;
import br.com.fiap.incidentmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Usuários", description = "Gestão de usuários (perfil ADMINISTRATIVO) e troca da própria senha")
@RequiredArgsConstructor
public class UserController {
    private static final String ADMIN = "hasRole('ADMINISTRATIVO')";

    private final UserService service;

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Cadastrar usuário", description = "A senha é armazenada criptografada (BCrypt).")
    public ResponseEntity<UserResponse> create(@RequestBody @Valid UserCreateRequest request, UriComponentsBuilder uriBuilder) {
        UserResponse response = service.create(request);
        return ResponseEntity.created(uriBuilder.path("/users/{id}").buildAndExpand(response.id()).toUri()).body(response);
    }

    @GetMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Listar usuários (paginado)")
    public ResponseEntity<Page<UserResponse>> list(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Consultar usuário")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Atualizar nome e login do usuário")
    public ResponseEntity<UserResponse> update(@RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @PatchMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Atualizar o perfil do usuário")
    public ResponseEntity<UserResponse> changeRole(@RequestBody @Valid UserRoleRequest request) {
        return ResponseEntity.ok(service.changeRole(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Inativar usuário", description = "Exclusão lógica. Não é permitido inativar o próprio usuário.")
    public ResponseEntity<Void> inactivate(@PathVariable Long id, Authentication authentication) {
        service.inactivate(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Listar perfis disponíveis")
    public ResponseEntity<List<String>> roles() {
        return ResponseEntity.ok(service.roles());
    }

    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alterar a própria senha", description = "Disponível para qualquer usuário autenticado.")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid PasswordChangeRequest request,
            Authentication authentication) {
        service.changeOwnPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
