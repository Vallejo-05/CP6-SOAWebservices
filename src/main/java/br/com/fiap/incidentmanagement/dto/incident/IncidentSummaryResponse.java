package br.com.fiap.incidentmanagement.dto.incident;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Dados do dashboard. Os nomes dos campos seguem o que o front-end (dashboard-ocorrencias.js) espera
public record IncidentSummaryResponse(
        long total,
        long active,
        long waiting,
        long progressing,
        long solved,
        long unsolved,
        long closed,
        List<KeyValue> byCategory,
        List<KeyValue> byType,

        // Mantido com a grafia usada pelo front-end
        @JsonProperty("bySchooclass")
        List<KeyValue> bySchoolClass,

        // Ranking de alunos reincidentes
        List<KeyValue> byStudent,

        // Volume de ocorrências por mês (yyyy-MM)
        List<KeyValue> byPeriod) {
}
