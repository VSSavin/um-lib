package com.github.vssavin.umlib.user;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author vssavin on 21.07.2023
 */

public class UmRepositorySupport<T, R> {
    private final T repository;
    private final DataSourceSwitcher dataSourceSwitcher;

    public UmRepositorySupport(T repository, DataSourceSwitcher dataSourceSwitcher) {
        this.repository = repository;
        this.dataSourceSwitcher = dataSourceSwitcher;
    }

    Page<R> execute(PagedRepositoryFunction<T, R> function, String message, Object... params) {
        Throwable throwable = null;
        Page<R> result = null;
        dataSourceSwitcher.switchToUmDataSource();

        try {
            result = function.apply(repository);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format(message, params), throwable);
        }

        return result;
    }

    Optional<R> execute(RepositoryOptionalFunction<T, R> function, String message, Object... params) {
        Throwable throwable = null;
        Optional<R> optionalResult = Optional.empty();
        dataSourceSwitcher.switchToUmDataSource();

        try {
            optionalResult = function.apply(repository);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format(message, params), throwable);
        }

        return optionalResult;
    }

    R execute(RepositoryFunction<T, R> function, String message, Object... params) {
        Throwable throwable = null;
        R result = null;
        dataSourceSwitcher.switchToUmDataSource();

        try {
            result = function.apply(repository);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format(message, params), throwable);
        }

        return result;
    }

    List<R> execute(RepositoryListFunction<T, R> function, String message, Object... params) {
        Throwable throwable = null;
        List<R> resultList = Collections.emptyList();
        dataSourceSwitcher.switchToUmDataSource();

        try {
            resultList = function.apply(repository);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format(message, params), throwable);
        }

        return resultList;
    }

    void execute(RepositoryConsumer<T> consumer, String message, Object... params) {
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            consumer.accept(repository);
        } catch (Exception e) {
            throwable = e;
        }
        dataSourceSwitcher.switchToPreviousDataSource();
        if (throwable != null) {
            throw new UserServiceException(String.format(message, params), throwable);
        }
    }

    interface RepositoryOptionalFunction<T, R> extends Function<T, Optional<R>> {
    }

    interface RepositoryListFunction<T, R> extends Function<T, List<R>> {
    }

    interface PagedRepositoryFunction<T, R> extends Function<T, Page<R>> {
    }

    interface RepositoryConsumer<T> extends Consumer<T> {
    }

    interface RepositoryFunction<T, R> extends Function<T, R> {
    }
}
