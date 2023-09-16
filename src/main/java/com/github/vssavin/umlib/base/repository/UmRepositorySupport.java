package com.github.vssavin.umlib.base.repository;

import org.springframework.data.domain.Page;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

/**
 * Executes the specified function and wraps any exception using the specified exception
 * class.
 *
 * @param <T> the type of the input repository
 * @param <R> the type of the result of the function
 * @author vssavin on 21.07.2023
 */

public class UmRepositorySupport<T, R> {

	private final T repository;

	private final Class<? extends RuntimeException> exceptionClass;

	public UmRepositorySupport(T repository, Class<? extends RuntimeException> exceptionClass) {
		this.repository = repository;
		this.exceptionClass = exceptionClass;
	}

	public Page<R> execute(PagedRepositoryFunction<T, R> function, String message) {
		try {
			return function.apply(repository);
		}
		catch (Exception e) {
			throw wrapException(e, exceptionClass, message);
		}
	}

	public Optional<R> execute(RepositoryOptionalFunction<T, R> function, String message) {
		try {
			return function.apply(repository);
		}
		catch (Exception e) {
			throw wrapException(e, exceptionClass, message);
		}
	}

	public R execute(RepositoryFunction<T, R> function, String message) {
		try {
			return function.apply(repository);
		}
		catch (Exception e) {
			throw wrapException(e, exceptionClass, message);
		}
	}

	public List<R> execute(RepositoryListFunction<T, R> function, String message) {
		try {
			return function.apply(repository);
		}
		catch (Exception e) {
			throw wrapException(e, exceptionClass, message);
		}
	}

	public void execute(RepositoryConsumer<T> consumer, String message) {
		try {
			consumer.accept(repository);
		}
		catch (Exception e) {
			throw wrapException(e, exceptionClass, message);
		}
	}

	private RuntimeException wrapException(Exception e, Class<? extends RuntimeException> exceptionClass,
			String message) {
		Constructor<?>[] ctors = exceptionClass.getDeclaredConstructors();
		Constructor<?> ctor = null;
		RuntimeException wrappedException = new UmRepositorySupportException(
				"An error occurred while wrapping the exception!");
		for (Constructor<?> constructor : ctors) {
			ctor = constructor;
			if (ctor.getGenericParameterTypes().length == 2) {
				break;
			}
		}

		if (ctor != null) {
			try {
				ctor.setAccessible(true);
				wrappedException = (RuntimeException) ctor.newInstance(message, e);
			}
			catch (InstantiationException | InvocationTargetException | IllegalAccessException x) {
				throw new UmRepositorySupportException("An error occurred while wrapping the exception!", e);
			}
		}
		return wrappedException;
	}

}
