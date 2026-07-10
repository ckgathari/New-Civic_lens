package com.civiclens.repository;

import com.civiclens.entity.Constituency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstituencyRepository extends JpaRepository<Constituency, Integer> {
    @org.springframework.lang.NonNull
    List<Constituency> findByCounty_Id(@org.springframework.lang.NonNull Integer countyId);
}
