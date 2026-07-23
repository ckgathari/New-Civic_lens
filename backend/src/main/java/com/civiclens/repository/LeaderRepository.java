package com.civiclens.repository;

import com.civiclens.entity.Leader;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderRepository extends JpaRepository<Leader, String> {

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @NonNull
    @Override
    List<Leader> findAll();

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @NonNull
    @Override
    Optional<Leader> findById(@NonNull String id);

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    List<Leader> findByPosition(String position);

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    List<Leader> findByCounty_IdAndPositionIn(Integer countyId, List<String> positions);

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    Optional<Leader> findByPositionAndConstituency_Id(String position, Integer constituencyId);

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    Optional<Leader> findByPositionAndWard_Id(String position, Integer wardId);

    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @Query("SELECT l FROM Leader l WHERE l.position = :position")
    Optional<Leader> findLeaderByPosition(@Param("position") String position);

    @Query("SELECT DISTINCT l FROM Leader l " +
           "LEFT JOIN FETCH l.county " +
           "LEFT JOIN FETCH l.constituency lc LEFT JOIN FETCH lc.county " +
           "LEFT JOIN FETCH l.ward lw LEFT JOIN FETCH lw.constituency lwc LEFT JOIN FETCH lwc.county " +
           "WHERE l.position = :position AND l.county.id = :countyId " +
           "AND (:constituencyId IS NULL OR l.constituency.id = :constituencyId) " +
           "AND (:wardId IS NULL OR l.ward.id = :wardId)")
    List<Leader> findByPositionAndCountyIdAndConstituencyIdAndWardId(
        @Param("position") String position,
        @Param("countyId") Integer countyId,
        @Param("constituencyId") Integer constituencyId,
        @Param("wardId") Integer wardId);
}
