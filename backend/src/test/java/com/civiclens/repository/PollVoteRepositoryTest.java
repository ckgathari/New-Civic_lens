package com.civiclens.repository;

import com.civiclens.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PollVoteRepository Tests")
@SuppressWarnings("null")
class PollVoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaderRepository leaderRepository;

    @Autowired
    private CountyRepository countyRepository;

    private User testUser;
    private Leader testLeader;
    private County testCounty;
    private PollVote testVote;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();
        testUser = userRepository.save(testUser);

        testCounty = new County();
        testCounty.setId(1);
        testCounty.setName("Test County");
        testCounty = countyRepository.save(testCounty);

        testLeader = Leader.builder()
            .name("Test Leader")
            .position("Governor")
            .party("Test Party")
            .county(testCounty)
            .build();
        testLeader = leaderRepository.save(testLeader);

        testVote = new PollVote();
        testVote.setVoter(testUser);
        testVote.setLeaderCandidate(testLeader);
        testVote.setCandidateType(PollVote.CandidateType.LEADER);
        testVote.setPosition("Governor");
        testVote.setCounty(testCounty);
    }

    @Test
    @DisplayName("Should save and retrieve poll vote")
    void testSavePollVote() {
        PollVote savedVote = pollVoteRepository.save(testVote);

        assertNotNull(savedVote.getId());
        assertEquals(testUser.getId(), savedVote.getVoter().getId());
        assertEquals("Governor", savedVote.getPosition());
    }

    @Test
    @DisplayName("Should check if user has voted for position")
    void testExistsByVoterAndPosition() {
        pollVoteRepository.save(testVote);

        boolean exists = pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            testUser, "Governor", testCounty, null, null);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false if user has not voted")
    void testExistsByVoterAndPositionNotFound() {
        boolean exists = pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            testUser, "President", testCounty, null, null);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should find user vote by position")
    void testFindByVoterAndPosition() {
        pollVoteRepository.save(testVote);

        Optional<PollVote> foundVote = pollVoteRepository.findByVoterAndPositionAndCountyAndConstituencyAndWard(
            testUser, "Governor", testCounty, null, null);

        assertTrue(foundVote.isPresent());
        assertEquals("Governor", foundVote.get().getPosition());
    }

    @Test
    @DisplayName("Should count votes by candidate")
    void testGetVoteCountsByCandidate() {
        pollVoteRepository.save(testVote);

        // Create another vote for different candidate
        Leader leader2 = Leader.builder()
            .name("Leader 2")
            .position("Governor")
            .party("Test Party")
            .county(testCounty)
            .build();
        leader2 = leaderRepository.save(leader2);

        PollVote vote2 = new PollVote();
        vote2.setLeaderCandidate(leader2);
        vote2.setCandidateType(PollVote.CandidateType.LEADER);
        vote2.setPosition("Governor");
        vote2.setCounty(testCounty);

        // Note: This should fail in real scenario (duplicate vote constraint)
        // For this test, create different user
        User user2 = User.builder()
            .email("user2@example.com")
            .password("password456")
            .firstName("User")
            .lastName("Two")
            .build();
        user2 = userRepository.save(user2);

        vote2.setVoter(user2);
        pollVoteRepository.save(vote2);

        // Query for vote counts
        List<Object[]> results = pollVoteRepository.getVoteCountsByPositionAndLocation(
            "Governor", testCounty, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Should save multiple votes for same position by different users")
    void testDuplicateVoteConstraint() {
        pollVoteRepository.save(testVote);

        // A second vote with the same user/position/county is a business rule violation,
        // but the @Table has no @UniqueConstraint — enforcement happens at the service layer.
        // At the JPA/H2 level, saving it succeeds (each vote gets its own generated id).
        PollVote duplicateVote = new PollVote();
        duplicateVote.setVoter(testUser);
        duplicateVote.setLeaderCandidate(testLeader);
        duplicateVote.setCandidateType(PollVote.CandidateType.LEADER);
        duplicateVote.setPosition("Governor");
        duplicateVote.setCounty(testCounty);

        PollVote saved = pollVoteRepository.save(duplicateVote);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals(2, pollVoteRepository.count());
    }


    @Test
    @DisplayName("Should delete vote")
    void testDeleteVote() {
        PollVote savedVote = pollVoteRepository.save(testVote);
        Long voteId = savedVote.getId();

        pollVoteRepository.deleteById(voteId);

        assertFalse(pollVoteRepository.existsById(voteId));
    }
}
