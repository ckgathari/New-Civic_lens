package com.civiclens.service;

import com.civiclens.dto.PollResultDto;
import com.civiclens.dto.PollVoteDto;
import com.civiclens.entity.*;
import com.civiclens.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PollService Tests")
@SuppressWarnings("null")
class PollServiceTest {

    @Mock
    private PollVoteRepository pollVoteRepository;

    @Mock
    private LeaderRepository leaderRepository;

    @Mock
    private AspirantRepository aspirantRepository;

    @Mock
    private CountyRepository countyRepository;

    @Mock
    private ConstituencyRepository constituencyRepository;

    @Mock
    private WardRepository wardRepository;

    @InjectMocks
    private PollService pollService;

    private User testUser;
    private Leader testLeader;
    private Aspirant testAspirant;
    private County testCounty;
    private PollVote testVote;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("user1")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        testCounty = new County();
        testCounty.setId(1);
        testCounty.setName("Test County");

        testLeader = new Leader();
        testLeader.setId("leader1");
        testLeader.setName("Test Leader");
        testLeader.setPosition("Governor");
        testLeader.setParty("Test Party");
        testLeader.setCounty(testCounty);

        testAspirant = new Aspirant();
        testAspirant.setId("aspirant1");
        testAspirant.setName("Test Aspirant");
        testAspirant.setPosition("Governor");
        testAspirant.setCounty(testCounty);

        testVote = new PollVote();
        testVote.setId(1L);
        testVote.setVoter(testUser);
        testVote.setLeaderCandidate(testLeader);
        testVote.setCandidateType(PollVote.CandidateType.LEADER);
        testVote.setPosition("Governor");
        testVote.setCounty(testCounty);
    }

    @Test
    @DisplayName("Should cast vote successfully for leader candidate")
    void testCastVoteForLeaderSuccess() {
        when(leaderRepository.findById("leader1")).thenReturn(Optional.of(testLeader));
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            eq(testUser), eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(false);
        when(pollVoteRepository.save(any(PollVote.class))).thenReturn(testVote);

        PollVoteDto result = pollService.castVote(testUser, "leader1", "LEADER", "Governor", 1, null, null);

        assertNotNull(result);
        verify(pollVoteRepository, times(1)).save(any(PollVote.class));
    }

    @Test
    @DisplayName("Should cast vote successfully for aspirant candidate")
    void testCastVoteForAspirantSuccess() {
        testVote.setLeaderCandidate(null);
        testVote.setAspirantCandidate(testAspirant);
        testVote.setCandidateType(PollVote.CandidateType.ASPIRANT);

        when(aspirantRepository.findById("aspirant1")).thenReturn(Optional.of(testAspirant));
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            eq(testUser), eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(false);
        when(pollVoteRepository.save(any(PollVote.class))).thenReturn(testVote);

        PollVoteDto result = pollService.castVote(testUser, "aspirant1", "ASPIRANT", "Governor", 1, null, null);

        assertNotNull(result);
        verify(pollVoteRepository, times(1)).save(any(PollVote.class));
    }

    @Test
    @DisplayName("Should reject duplicate vote for same position")
    void testCastVoteDuplicateVote() {
        when(pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            eq(testUser), eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(true);
        when(leaderRepository.findById("leader1")).thenReturn(Optional.of(testLeader));
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));

        assertThrows(RuntimeException.class, () -> 
            pollService.castVote(testUser, "leader1", "LEADER", "Governor", 1, null, null)
        );

        verify(pollVoteRepository, never()).save(any(PollVote.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid candidate type")
    void testCastVoteInvalidCandidateType() {
        assertThrows(RuntimeException.class, () -> 
            pollService.castVote(testUser, "candidate1", "INVALID", "Governor", 1, null, null)
        );
    }

    @Test
    @DisplayName("Should throw exception for non-existent county")
    void testCastVoteNonExistentCounty() {
        when(leaderRepository.findById("leader1")).thenReturn(Optional.of(testLeader));
        when(countyRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            pollService.castVote(testUser, "leader1", "LEADER", "Governor", 999, null, null)
        );
    }

    @Test
    @DisplayName("Should get poll results with vote counts and percentages")
    void testGetPollResultsSuccess() {
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(leaderRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of(testLeader));
        when(aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of(testAspirant));
        when(pollVoteRepository.getVoteCountsByPositionAndLocation(
            eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(Arrays.asList(
                new Object[]{"L_leader1", 10L},
                new Object[]{"A_aspirant1", 5L}
            ));

        List<PollResultDto> results = pollService.getPollResults("Governor", 1, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Test Leader", results.get(0).getCandidateName());
        assertEquals(66.67, results.get(0).getPercentage(), 0.1);
    }

    @Test
    @DisplayName("Should return zero percentage when no votes")
    void testGetPollResultsNoVotes() {
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(leaderRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of(testLeader));
        when(aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of());
        when(pollVoteRepository.getVoteCountsByPositionAndLocation(
            eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(new ArrayList<>());

        List<PollResultDto> results = pollService.getPollResults("Governor", 1, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(0.0, results.get(0).getPercentage());
    }

    @Test
    @DisplayName("Should get user vote for position")
    void testGetUserVoteSuccess() {
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(pollVoteRepository.findByVoterAndPositionAndCountyAndConstituencyAndWard(
            eq(testUser), eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(Optional.of(testVote));

        Optional<PollVote> result = pollService.getUserVote(testUser, "Governor", 1, null, null);

        assertTrue(result.isPresent());
        assertEquals("leader1", result.get().getLeaderCandidate().getId());
    }

    @Test
    @DisplayName("Should check user has voted")
    void testHasUserVotedTrue() {
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            eq(testUser), eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(true);

        boolean result = pollService.hasUserVoted(testUser, "Governor", 1, null, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should check user has not voted")
    void testHasUserVotedFalse() {
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            eq(testUser), eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(false);

        boolean result = pollService.hasUserVoted(testUser, "Governor", 1, null, null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should get candidates for President (no location filter)")
    void testGetCandidatesForPresident() {
        Leader president = new Leader();
        president.setId("pres1");
        president.setName("President Candidate");
        president.setPosition("President");

        when(leaderRepository.findByPosition("President")).thenReturn(List.of(president));
        when(aspirantRepository.findByPosition("President")).thenReturn(new ArrayList<>());

        List<Object> candidates = pollService.getCandidatesForPosition("President", 1, null, null);

        assertNotNull(candidates);
        assertEquals(1, candidates.size());
    }

    @Test
    @DisplayName("Should get candidates for Governor (county only)")
    void testGetCandidatesForGovernor() {
        when(leaderRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of(testLeader));
        when(aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of(testAspirant));

        List<Object> candidates = pollService.getCandidatesForPosition("Governor", 1, null, null);

        assertNotNull(candidates);
        assertEquals(2, candidates.size());
    }

    @Test
    @DisplayName("Should sort poll results by vote count descending")
    void testGetPollResultsSortedByVoteCount() {
        Leader leader2 = new Leader();
        leader2.setId("leader2");
        leader2.setName("Leader 2");
        leader2.setPosition("Governor");

        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(leaderRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(Arrays.asList(testLeader, leader2));
        when(aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(new ArrayList<>());
        when(pollVoteRepository.getVoteCountsByPositionAndLocation(
            eq("Governor"), eq(testCounty), isNull(), isNull()))
            .thenReturn(Arrays.asList(
                new Object[]{"L_leader1", 5L},
                new Object[]{"L_leader2", 10L}
            ));

        List<PollResultDto> results = pollService.getPollResults("Governor", 1, null, null);

        assertEquals("Leader 2", results.get(0).getCandidateName());
        assertEquals("Test Leader", results.get(1).getCandidateName());
    }
}
