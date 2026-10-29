package br.com.fiap.incidentmanagement.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

// Converte campos de ordenação enviados pelo front-end (ex.: "category") para o atributo
// da entidade relacionada (ex.: "category.name")
public final class PageableTranslator {
    private PageableTranslator() {
    }

    public static Pageable translate(Pageable pageable, Map<String, String> aliases) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(order -> order.withProperty(aliases.getOrDefault(order.getProperty(), order.getProperty())))
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
