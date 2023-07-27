package com.github.vssavin.umlib.base.repository;

import org.springframework.data.domain.Page;

import java.util.function.Function;

/**
 * Represents a function for repository type 'T' and returns the 'R' result as the Page type.
 *
 * @param <T> the type of the input repository
 * @param <R> the type of the result of the function
 *
 * @author vssavin on 27.07.2023
 */
@FunctionalInterface
public interface PagedRepositoryFunction<T, R> extends Function<T, Page<R>> {
}
