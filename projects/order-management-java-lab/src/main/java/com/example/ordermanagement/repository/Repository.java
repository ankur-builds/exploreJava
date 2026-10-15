package com.example.ordermanagement.repository;

import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    void save(ID id, T entity);
    boolean deleteById(ID id);
}
