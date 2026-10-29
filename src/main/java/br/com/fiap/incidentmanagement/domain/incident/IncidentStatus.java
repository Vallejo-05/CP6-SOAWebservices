package br.com.fiap.incidentmanagement.domain.incident;

import java.util.EnumSet;
import java.util.Set;

// Situação do atendimento da ocorrência
public enum IncidentStatus {
    WAITING("Aguardando atendimento"),
    PROGRESSING("Em atendimento"),
    SOLVED("Resolvida"),
    UNSOLVED("Encerrada sem solução"),
    CLOSED("Fechada");

    // Ocorrências "ativas" são as que ainda estão em aberto
    public static final Set<IncidentStatus> ACTIVE = EnumSet.of(WAITING, PROGRESSING);

    private final String description;

    IncidentStatus(String description) {
        this.description = description;
    }

    public String getName() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
