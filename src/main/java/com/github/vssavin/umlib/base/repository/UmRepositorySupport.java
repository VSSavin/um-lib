package com.github.vssavin.umlib.base.repository;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.user.UserServiceException;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ensures that the datasource is switched before and after execution of the repository method.
 *
 * @param <T> the type of the input repository
 * @param <R> the type of the result of the function
 *
 * @author vssavin on 21.07.2023
 */

public class UmRepositorySupport<T, R> {
    private final T repository;
    private final DataSourceSwitcher dataSourceSwitcher;

    public UmRepositorySupport(T repository, DataSourceSwitcher dataSourceSwitcher) {
        this.repository = repository;
        this.dataSourceSwitcher = dataSourceSwitcher;
    }

    public Page<R> execute(PagedRepositoryFunction<T, R> function, String message, Object... params) {
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

    public Optional<R> execute(RepositoryOptionalFunction<T, R> function, String message, Object... params) {
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

    public R execute(RepositoryFunction<T, R> function, String message, Object... params) {
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

    public List<R> execute(RepositoryListFunction<T, R> function, String message, Object... params) {
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

    public void execute(RepositoryConsumer<T> consumer, String message, Object... params) {
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
}
