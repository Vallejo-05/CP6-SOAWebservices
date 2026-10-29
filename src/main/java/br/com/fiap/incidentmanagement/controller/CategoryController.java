package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.category.CategoryRequest;
import br.com.fiap.incidentmanagement.dto.category.CategoryResponse;
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
@RequestMapping("/categories")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Categorias", description = "Categorias de ocorrência (escrita: perfil ADMINISTRATIVO)")
@RequiredArgsConstructor
public class CategoryController {
    private static final String ADMIN = "hasRole('ADMINISTRATIVO')";

    private final CategoryService service;

    @GetMapping
    @Operation(summary = "Listar categorias")
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(service.listCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria")
    public ResponseEntity<CategoryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCategory(id));
    }

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Cadastrar categoria", description = "O nome é padronizado (ex.: \"Pedagógica\" vira PEDAGOGICA).")
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CategoryRequest request, UriComponentsBuilder uriBuilder) {
        CategoryResponse response = service.createCategory(request);
        return ResponseEntity.created(uriBuilder.path("/categories/{id}").buildAndExpand(response.id()).toUri()).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Renomear categoria")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.ok(service.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Excluir categoria", description = "Somente categorias sem tipos e sem ocorrências.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
