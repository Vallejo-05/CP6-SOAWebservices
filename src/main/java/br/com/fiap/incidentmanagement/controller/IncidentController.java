package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;
import br.com.fiap.incidentmanagement.dto.incident.*;
import br.com.fiap.incidentmanagement.service.CategoryService;
import br.com.fiap.incidentmanagement.service.IncidentFilter;
import br.com.fiap.incidentmanagement.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/incidents")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Ocorrências", description = "Registro, consulta, acompanhamento e dashboard de ocorrências")
@RequiredArgsConstructor
public class IncidentController {
    private static final String CONSULTATION = "hasAnyRole('PROFESSOR', 'ANALISTA', 'COORDENADOR')";

    private final IncidentService service;
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Registrar ocorrência",
            description = "Perfil PROFESSOR. O aluno deve pertencer à turma e o tipo à categoria. " +
                    "registerDate no formato dd/MM/yyyy - HH:mm (opcional: assume o momento atual).")
    public ResponseEntity<IncidentResponse> create(
            @RequestBody @Valid IncidentCreateRequest request,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        IncidentResponse response = service.create(authentication.getName(), request);
        return ResponseEntity.created(uriBuilder.path("/incidents/{id}").buildAndExpand(response.id()).toUri()).body(response);
    }

    @GetMapping
    @PreAuthorize(CONSULTATION)
    @Operation(summary = "Consultar ocorrências (paginado, com filtros)",
            description = "Filtros opcionais por aluno, turma, categoria, tipo, status e período (yyyy-MM-dd). " +
                    "Também serve de base para relatórios.")
    public ResponseEntity<Page<IncidentResponse>> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long schoolClassId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean deleted,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        IncidentFilter filter = new IncidentFilter(studentId, schoolClassId, category, type, status, startDate, endDate, deleted);
        return ResponseEntity.ok(service.list(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CONSULTATION)
    @Operation(summary = "Detalhar ocorrência")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Alterar ocorrência", description = "O professor só pode alterar as próprias ocorrências.")
    public ResponseEntity<IncidentResponse> update(@RequestBody @Valid IncidentUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(service.update(authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Excluir ocorrência", description = "Exclusão lógica. O professor só pode excluir as próprias ocorrências.")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/status")
    @PreAuthorize("hasRole('COORDENADOR')")
    @Operation(summary = "Alterar status da ocorrência", description = "Perfil COORDENADOR. Ocorrências fechadas não podem mais ser alteradas.")
    public ResponseEntity<IncidentResponse> changeStatus(@RequestBody @Valid IncidentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(request));
    }

    @GetMapping("/status")
    @Operation(summary = "Listar status possíveis")
    public ResponseEntity<List<StatusResponse>> statuses() {
        return ResponseEntity.ok(service.statuses());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(CONSULTATION)
    @Operation(summary = "Listar ocorrências por status",
            description = "waiting, progressing, solved, unsolved, closed ou active (aguardando + em atendimento).")
    public ResponseEntity<Page<IncidentResponse>> listByStatus(
            @PathVariable String status,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listByStatus(status, pageable));
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar nomes das categorias")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(categoryService.categoryNames());
    }

    @GetMapping("/types/{category}")
    @Operation(summary = "Listar nomes dos tipos de uma categoria")
    public ResponseEntity<List<String>> types(@PathVariable String category) {
        return ResponseEntity.ok(categoryService.typeNames(category));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('COORDENADOR', 'ANALISTA')")
    @Operation(summary = "Dashboard",
            description = "Totais por status, categoria, tipo e turma, ranking de alunos reincidentes e volume por mês.")
    public ResponseEntity<IncidentSummaryResponse> summary() {
        return ResponseEntity.ok(service.summary());
    }
}
