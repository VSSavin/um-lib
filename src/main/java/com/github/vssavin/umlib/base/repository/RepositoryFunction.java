package com.github.vssavin.umlib.base.repository;

import java.util.function.Function;

/**
 * Represents a function for repository type 'T' and returns a 'R' result.
 *
 * @param <T> the type of the input repository
 * @param <R> the type of the result of the function
 *
 * @author vssavin on 27.07.2023
 */
@FunctionalInterface
public interface RepositoryFunction<T, R> extends Function<T, R> {
}
