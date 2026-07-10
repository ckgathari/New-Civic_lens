package com.civiclens.repository;

import com.civiclens.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Integer> {
    @org.springframework.lang.NonNull
    List<Ward> findByConstituency_Id(@org.springframework.lang.NonNull Integer constituencyId);
}
