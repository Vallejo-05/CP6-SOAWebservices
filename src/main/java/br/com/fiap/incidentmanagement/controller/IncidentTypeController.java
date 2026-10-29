package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.category.IncidentTypeRequest;
import br.com.fiap.incidentmanagement.dto.category.IncidentTypeResponse;
import br.com.fiap.incidentmanagement.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/incident-types")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Tipos de ocorrência", description = "Tipos de ocorrência vinculados a categorias (escrita: perfil ADMINISTRATIVO)")
@RequiredArgsConstructor
public class IncidentTypeController {
    private static final String ADMIN = "hasRole('ADMINISTRATIVO')";

    private final CategoryService service;

    @GetMapping
    @Operation(summary = "Listar tipos de ocorrência", description = "Filtro opcional pelo nome da categoria.")
    public ResponseEntity<List<IncidentTypeResponse>> list(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(service.listTypes(category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar tipo de ocorrência")
    public ResponseEntity<IncidentTypeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getType(id));
    }

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Cadastrar tipo de ocorrência")
    public ResponseEntity<IncidentTypeResponse> create(@RequestBody @Valid IncidentTypeRequest request, UriComponentsBuilder uriBuilder) {
        IncidentTypeResponse response = service.createType(request);
        return ResponseEntity.created(uriBuilder.path("/incident-types/{id}").buildAndExpand(response.id()).toUri()).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Atualizar tipo de ocorrência")
    public ResponseEntity<IncidentTypeResponse> update(@PathVariable Long id, @RequestBody @Valid IncidentTypeRequest request) {
        return ResponseEntity.ok(service.updateType(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Excluir tipo de ocorrência", description = "Somente tipos sem ocorrências registradas.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteType(id);
        return ResponseEntity.noContent().build();
    }
}
