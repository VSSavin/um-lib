package com.github.vssavin.umlib.domain.event;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Main repository of event management.
 *
 * @author vssavin on 24.08.2023
 */
@Repository
public interface EventRepository extends PagingAndSortingRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @Transactional
    @NonNull
    List<Event> findAll();

}
