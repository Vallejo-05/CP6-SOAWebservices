package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.incident.IncidentResponse;
import br.com.fiap.incidentmanagement.dto.student.StudentCreateRequest;
import br.com.fiap.incidentmanagement.dto.student.StudentResponse;
import br.com.fiap.incidentmanagement.dto.student.StudentUpdateRequest;
import br.com.fiap.incidentmanagement.service.IncidentService;
import br.com.fiap.incidentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/students")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Alunos", description = "Cadastro e gestão de alunos (escrita: perfil ADMINISTRATIVO)")
@RequiredArgsConstructor
public class StudentController {
    private static final String ADMIN = "hasRole('ADMINISTRATIVO')";

    private final StudentService service;
    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Cadastrar aluno", description = "Data de nascimento no formato dd/MM/yyyy.")
    public ResponseEntity<StudentResponse> create(@RequestBody @Valid StudentCreateRequest request, UriComponentsBuilder uriBuilder) {
        StudentResponse response = service.create(request);
        return ResponseEntity.created(uriBuilder.path("/students/{id}").buildAndExpand(response.id()).toUri()).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar alunos ativos (paginado)")
    public ResponseEntity<Page<StudentResponse>> list(@ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar aluno")
    public ResponseEntity<StudentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping
    @PreAuthorize(ADMIN)
    @Operation(summary = "Atualizar nome e data de nascimento do aluno")
    public ResponseEntity<StudentResponse> update(@RequestBody @Valid StudentUpdateRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Inativar aluno", description = "Exclusão lógica: o histórico de ocorrências é preservado.")
    public ResponseEntity<Void> inactivate(@PathVariable Long id) {
        service.inactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(ADMIN)
    @Operation(summary = "Enviar foto do aluno", description = "Multipart com o arquivo no campo 'image' (máximo 2MB).")
    public ResponseEntity<StudentResponse> uploadPhoto(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(service.uploadPhoto(id, image));
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Obter foto do aluno", description = "Endpoint público, usado em <img src>.")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        StudentService.Photo photo = service.getPhoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.noCache())
                .body(photo.content());
    }

    @GetMapping("/{id}/incidents")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ANALISTA', 'COORDENADOR')")
    @Operation(summary = "Histórico de ocorrências do aluno", description = "Inclui as ocorrências de todas as turmas do aluno.")
    public ResponseEntity<Page<IncidentResponse>> history(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 10, sort = "registerDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(incidentService.studentHistory(id, pageable));
    }
}
