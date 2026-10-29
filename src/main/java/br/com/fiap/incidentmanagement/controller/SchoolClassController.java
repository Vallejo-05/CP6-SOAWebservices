package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassCreateRequest;
import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassResponse;
import br.com.fiap.incidentmanagement.dto.schoolclass.SchoolClassUpdateRequest;
import br.com.fiap.incidentmanagement.dto.schoolclass.StudentsLinkRequest;
import br.com.fiap.incidentmanagement.dto.student.StudentResponse;
import br.com.fiap.incidentmanagement.service.SchoolClassService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/schoolclasses")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Turmas", description = "Turmas semestrais e vínculo de alunos (escrita: perfil ADMINISTRATIVO)")
@RequiredArgsConstructor
public class SchoolClassController {
    private static final String ADMIN = "hasRole('ADMINISTRATIVO')";

    private final SchoolClassService service;

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Cadastrar turma")
    public ResponseEntity<SchoolClassResponse> create(@RequestBody @Valid SchoolClassCreateRequest request, UriComponentsBuilder uriBuilder) {
        SchoolClassResponse response = service.create(request);
        return ResponseEntity.created(uriBuilder.path("/schoolclasses/{id}").buildAndExpand(response.id()).toUri()).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar turmas ativas (paginado)")
    public ResponseEntity<Page<SchoolClassResponse>> list(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.list(false, pageable));
    }

    @GetMapping("/canceled")
    @Operation(summary = "Listar turmas canceladas (paginado)")
    public ResponseEntity<Page<SchoolClassResponse>> listCanceled(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.list(true, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar turma")
    public ResponseEntity<SchoolClassResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Atualizar turma")
    public ResponseEntity<SchoolClassResponse> update(@RequestBody @Valid SchoolClassUpdateRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Cancelar turma", description = "Exclusão lógica: as ocorrências da turma são preservadas.")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shifts")
    @Operation(summary = "Listar turnos")
    public ResponseEntity<List<String>> shifts() {
        return ResponseEntity.ok(service.shifts());
    }

    @GetMapping("/semesters")
    @Operation(summary = "Listar semestres")
    public ResponseEntity<List<String>> semesters() {
        return ResponseEntity.ok(service.semesters());
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Listar alunos da turma", description = "Usado para escolher o aluno ao registrar uma ocorrência.")
    public ResponseEntity<List<StudentResponse>> students(@PathVariable Long id) {
        return ResponseEntity.ok(service.listStudents(id));
    }

    @PostMapping("/{id}/students")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Vincular alunos à turma")
    public ResponseEntity<List<StudentResponse>> linkStudents(@PathVariable Long id, @RequestBody @Valid StudentsLinkRequest request) {
        return ResponseEntity.ok(service.linkStudents(id, request.studentIds()));
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Desvincular aluno da turma")
    public ResponseEntity<Void> unlinkStudent(@PathVariable Long id, @PathVariable Long studentId) {
        service.unlinkStudent(id, studentId);
        return ResponseEntity.noContent().build();
    }
}
