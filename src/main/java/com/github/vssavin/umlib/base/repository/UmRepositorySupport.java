package com.github.vssavin.umlib.base.repository;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.user.UserServiceException;
import org.springframework.data.domain.Page;

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
        try {
            dataSourceSwitcher.switchToUmDataSource();
            return function.apply(repository);
        } catch (Exception e) {
            throw new UserServiceException(String.format(message, params), e);
        } finally {
            dataSourceSwitcher.switchToPreviousDataSource();
        }
    }

    public Optional<R> execute(RepositoryOptionalFunction<T, R> function, String message, Object... params) {
        try {
            dataSourceSwitcher.switchToUmDataSource();
            return function.apply(repository);
        } catch (Exception e) {
            throw new UserServiceException(String.format(message, params), e);
        } finally {
            dataSourceSwitcher.switchToPreviousDataSource();
        }
    }

    public R execute(RepositoryFunction<T, R> function, String message, Object... params) {
        try {
            dataSourceSwitcher.switchToUmDataSource();
            return function.apply(repository);
        } catch (Exception e) {
            throw new UserServiceException(String.format(message, params), e);
        } finally {
            dataSourceSwitcher.switchToPreviousDataSource();
        }
    }

    public List<R> execute(RepositoryListFunction<T, R> function, String message, Object... params) {
        try {
            dataSourceSwitcher.switchToUmDataSource();
            return function.apply(repository);
        } catch (Exception e) {
            throw new UserServiceException(String.format(message, params), e);
        } finally {
            dataSourceSwitcher.switchToPreviousDataSource();
        }
    }

    public void execute(RepositoryConsumer<T> consumer, String message, Object... params) {
        try {
            dataSourceSwitcher.switchToUmDataSource();
            consumer.accept(repository);
        } catch (Exception e) {
            throw new UserServiceException(String.format(message, params), e);
        } finally {
            dataSourceSwitcher.switchToPreviousDataSource();
        }
    }
}
