package com.civiclens.repository;

import com.civiclens.entity.PollVote;
import com.civiclens.entity.User;
import com.civiclens.entity.County;
import com.civiclens.entity.Constituency;
import com.civiclens.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    // Check if user has already voted for a specific position in a location
    boolean existsByVoterAndPositionAndCountyAndConstituencyAndWard(
        User voter,
        String position,
        County county,
        Constituency constituency,
        Ward ward
    );

    // Get all votes for a specific position in a location
    List<PollVote> findByPositionAndCountyAndConstituencyAndWard(
        String position,
        County county,
        Constituency constituency,
        Ward ward
    );

    // Get vote counts for candidates in a specific position/location
    @Query("SELECT " +
           "CASE WHEN pv.candidateType = 'LEADER' THEN CONCAT('L_', pv.leaderCandidate.id) " +
           "ELSE CONCAT('A_', pv.aspirantCandidate.id) END, " +
           "COUNT(pv) FROM PollVote pv " +
           "WHERE pv.position = :position " +
           "AND pv.county = :county " +
           "AND (:constituency IS NULL OR pv.constituency = :constituency) " +
           "AND (:ward IS NULL OR pv.ward = :ward) " +
           "GROUP BY pv.candidateType, pv.leaderCandidate.id, pv.aspirantCandidate.id")
    List<Object[]> getVoteCountsByPositionAndLocation(
        @Param("position") String position,
        @Param("county") County county,
        @Param("constituency") Constituency constituency,
        @Param("ward") Ward ward
    );

    // Get user's vote for a specific position/location
    Optional<PollVote> findByVoterAndPositionAndCountyAndConstituencyAndWard(
        User voter,
        String position,
        County county,
        Constituency constituency,
        Ward ward
    );
}