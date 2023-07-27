package com.github.vssavin.umlib.base.repository;

import java.util.function.Consumer;

/**
 * Represents an operation for repository type 'T' and returns no result.
 *
 * @param <T> the type of the input repository
 *
 * @author vssavin on 27.07.2023
 */
@FunctionalInterface
public interface RepositoryConsumer<T> extends Consumer<T> {
}
