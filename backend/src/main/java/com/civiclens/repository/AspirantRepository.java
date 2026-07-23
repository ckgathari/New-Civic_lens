package com.civiclens.repository;

import com.civiclens.entity.Aspirant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AspirantRepository extends JpaRepository<Aspirant, String> {

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @NonNull
    @Override
    List<Aspirant> findAll();

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @NonNull
    @Override
    Optional<Aspirant> findById(@NonNull String id);

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    List<Aspirant> findByPosition(String position);

    @Query("SELECT DISTINCT a FROM Aspirant a " +
           "LEFT JOIN FETCH a.county " +
           "LEFT JOIN FETCH a.constituency ac LEFT JOIN FETCH ac.county " +
           "LEFT JOIN FETCH a.ward aw LEFT JOIN FETCH aw.constituency awc LEFT JOIN FETCH awc.county " +
           "WHERE a.position = :position AND a.county.id = :countyId " +
           "AND (:constituencyId IS NULL OR a.constituency.id = :constituencyId) " +
           "AND (:wardId IS NULL OR a.ward.id = :wardId)")
    List<Aspirant> findByPositionAndCountyIdAndConstituencyIdAndWardId(
        @Param("position") String position,
        @Param("countyId") Integer countyId,
        @Param("constituencyId") Integer constituencyId,
        @Param("wardId") Integer wardId);

    boolean existsByEmail(String email);
}