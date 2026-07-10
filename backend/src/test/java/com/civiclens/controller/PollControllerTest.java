package com.civiclens.controller;

import com.civiclens.dto.PollResultDto;
import com.civiclens.dto.PollVoteDto;
import com.civiclens.entity.User;
import com.civiclens.service.PollService;
import com.civiclens.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PollController Tests")
@SuppressWarnings("null")
class PollControllerTest {

    @Mock
    private PollService pollService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PollController pollController;

    private User testUser;
    private Map<String, Object> voteRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("user1")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        voteRequest = new HashMap<>();
        voteRequest.put("candidateId", "leader1");
        voteRequest.put("candidateType", "LEADER");
        voteRequest.put("position", "Governor");
        voteRequest.put("countyId", 1);
        voteRequest.put("constituencyId", null);
        voteRequest.put("wardId", null);
    }

    @Test
    @DisplayName("Should cast vote successfully")
    void testCastVoteSuccess() {
        PollVoteDto voteDto = new PollVoteDto();
        voteDto.setId(1L);

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(pollService.castVote(
            eq(testUser),
            eq("leader1"),
            eq("LEADER"),
            eq("Governor"),
            eq(1),
            isNull(),
            isNull()
        )).thenReturn(voteDto);

        ResponseEntity<?> response = pollController.castVote(voteRequest, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return error when authentication is null")
    void testCastVoteAuthenticationNull() {
        when(authentication.getName()).thenReturn(null);

        ResponseEntity<?> response = pollController.castVote(voteRequest, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Authentication failed"));
    }

    @Test
    @DisplayName("Should return error when user not found")
    void testCastVoteUserNotFound() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = pollController.castVote(voteRequest, authentication);

        // Controller throws exception which is caught by global handler, returns 500
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return error when missing required fields")
    void testCastVoteMissingFields() {
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("candidateId", "leader1");
        // Missing candidateType, position, countyId

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = pollController.castVote(invalidRequest, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Missing required fields"));
    }

    @Test
    @DisplayName("Should get poll results for position")
    void testGetPollResultsSuccess() {
        PollResultDto result1 = new PollResultDto();
        result1.setCandidateName("Candidate 1");
        result1.setVoteCount(50);

        when(pollService.getPollResults(
            eq("Governor"),
            eq(1),
            isNull(),
            isNull()
        )).thenReturn(Arrays.asList(result1));

        ResponseEntity<List<PollResultDto>> response = pollController.getPollResults(
            "Governor",
            1,
            null,
            null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should check if user has voted")
    void testCheckUserVoteSuccess() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(pollService.hasUserVoted(
            eq(testUser),
            eq("Governor"),
            eq(1),
            isNull(),
            isNull()
        )).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = pollController.checkUserVote("Governor", 1, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("hasVoted"));
    }

    @Test
    @DisplayName("Should get user vote for position")
    void testGetUserVoteSuccess() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(pollService.getUserVote(
            eq(testUser),
            eq("Governor"),
            eq(1),
            isNull(),
            isNull()
        )).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = pollController.getUserVote("Governor", 1, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get candidates for position and location")
    void testGetCandidatesSuccess() {
        when(pollService.getCandidatesForPosition(
            eq("Governor"),
            eq(1),
            isNull(),
            isNull()
        )).thenReturn(new ArrayList<>());

        ResponseEntity<List<Map<String, Object>>> response = pollController.getCandidates(
            "Governor",
            1,
            null,
            null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should handle missing position parameter")
    void testGetPollResultsMissingPosition() {
        // Controller accepts null position and handles it gracefully
        ResponseEntity<List<Map<String, Object>>> response = pollController.getCandidates(
            null, 1, null, null
        );

        // Should return error or empty result, not throw exception
        assertNotNull(response);
    }
}

