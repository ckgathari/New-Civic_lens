package com.civiclens.repository;

import com.civiclens.entity.County;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountyRepository extends JpaRepository<County, Integer> {
    @org.springframework.lang.NonNull
    Optional<County> findByName(@org.springframework.lang.NonNull String name);
}
