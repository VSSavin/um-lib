package com.github.vssavin.umlib.base.repository;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a function for repository type 'T' and returns the 'R' result as the
 * Optional type.
 *
 * @param <T> the type of the input repository
 * @param <R> the type of the result of the function
 * @author vssavin on 27.07.2023
 */
@FunctionalInterface
public interface RepositoryOptionalFunction<T, R> extends Function<T, Optional<R>> {

}
