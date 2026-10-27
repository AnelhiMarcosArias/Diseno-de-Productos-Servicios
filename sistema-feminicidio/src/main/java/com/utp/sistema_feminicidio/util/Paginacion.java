package com.utp.sistema_feminicidio.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class Paginacion {
    private Paginacion() {}
    public static Pageable limitar(Pageable pageable) {
        int size = Math.max(1, Math.min(pageable.getPageSize(), 100));
        return PageRequest.of(Math.max(0, pageable.getPageNumber()), size, pageable.getSort());
    }
}
