package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.domain.incident.IncidentStatus;

import java.time.LocalDate;

// Filtros da consulta de ocorrências (todos opcionais)
public record IncidentFilter(
        Long studentId,
        Long schoolClassId,
        String category,
        String type,
        IncidentStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Boolean deleted) {
}
