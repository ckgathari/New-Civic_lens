package com.civiclens.repository;

import com.civiclens.entity.Aspirant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AspirantRepository extends JpaRepository<Aspirant, String> {

    List<Aspirant> findByPosition(String position);

    @Query("SELECT a FROM Aspirant a WHERE a.position = :position AND a.county.id = :countyId " +
           "AND (:constituencyId IS NULL OR a.constituency.id = :constituencyId) " +
           "AND (:wardId IS NULL OR a.ward.id = :wardId)")
    List<Aspirant> findByPositionAndCountyIdAndConstituencyIdAndWardId(
        @Param("position") String position,
        @Param("countyId") Integer countyId,
        @Param("constituencyId") Integer constituencyId,
        @Param("wardId") Integer wardId);

    boolean existsByEmail(String email);
}