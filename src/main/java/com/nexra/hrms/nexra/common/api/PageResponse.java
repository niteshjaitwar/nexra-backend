package com.nexra.hrms.nexra.common.api;

import org.springframework.data.domain.Page;

import java.util.function.Function;
import java.util.List;

/**
 * Paginated payload block serialized under ApiResponse.data for any list
 * endpoint. Wraps the Spring Data Page contract in a stable, framework
 * agnostic shape so REST consumers do not depend on Spring internals.
 *
 * @param items       page content.
 * @param page        current zero based page index.
 * @param size        requested page size.
 * @param totalItems  total number of items across all pages.
 * @param totalPages  total number of pages given the size.
 * @param hasNext     true when a next page exists.
 * @param hasPrevious true when a previous page exists.
 * @param <T>         content type.
 * @author niteshjaitwar
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {

    /**
     * Builds a PageResponse from a Spring Data Page.
     *
     * @param page Spring Data page instance.
     * @param <T>  content type.
     * @return immutable page response.
     */
    public static <T> PageResponse<T> from(final Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }

    /**
     * Maps a PageResponse to a different item type.
     *
     * @param source  existing page response.
     * @param mapper  mapping function applied to every item.
     * @param <S>     source type.
     * @param <T>     target type.
     * @return immutable page response with mapped items.
     */
    public static <S, T> PageResponse<T> map(final PageResponse<S> source, final Function<S, T> mapper) {
        final List<T> mapped = source.items().stream().map(mapper).toList();
        return new PageResponse<>(
                mapped,
                source.page(),
                source.size(),
                source.totalItems(),
                source.totalPages(),
                source.hasNext(),
                source.hasPrevious());
    }
}
