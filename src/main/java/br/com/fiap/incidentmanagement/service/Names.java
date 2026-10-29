package br.com.fiap.incidentmanagement.service;

import java.text.Normalizer;
import java.util.Locale;

// Padroniza nomes de categorias, tipos e perfis (ex.: "Uso de celular" -> "USO_DE_CELULAR"),
// pois eles são usados como valores de seleção e na URL /incidents/types/{category}
public final class Names {
    private Names() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }
}
