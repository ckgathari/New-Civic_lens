package com.civiclens.controller;

import com.civiclens.dto.PollResultDto;
import com.civiclens.dto.PollVoteDto;
import com.civiclens.entity.Leader;
import com.civiclens.entity.Aspirant;
import com.civiclens.entity.User;
import com.civiclens.repository.UserRepository;
import com.civiclens.service.PollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    @Autowired
    private PollService pollService;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PollController.class);

    @PostMapping("/vote")
    public ResponseEntity<?> castVote(
            @RequestBody Map<String, Object> voteRequest,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed"));
            }
            User voter = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String candidateId = (String) voteRequest.get("candidateId");
            String candidateType = (String) voteRequest.get("candidateType");
            String position = (String) voteRequest.get("position");
            
            if (candidateId == null || candidateType == null || position == null || voteRequest.get("countyId") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }
            
            Integer countyId = Integer.valueOf(voteRequest.get("countyId").toString());
            Integer constituencyId = voteRequest.get("constituencyId") != null ?
                Integer.valueOf(voteRequest.get("constituencyId").toString()) : null;
            Integer wardId = voteRequest.get("wardId") != null ?
                Integer.valueOf(voteRequest.get("wardId").toString()) : null;

            if (countyId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "County ID is required"));
            }

            PollVoteDto vote = pollService.castVote(voter, candidateId, candidateType, position,
                                                   countyId, constituencyId, wardId);

            return ResponseEntity.ok(vote);
        } catch (DataAccessException e) {
            logger.error("Database error when casting vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to cast vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/results")
    public ResponseEntity<List<PollResultDto>> getPollResults(
            @RequestParam String position,
            @RequestParam Integer countyId,
            @RequestParam(required = false) Integer constituencyId,
            @RequestParam(required = false) Integer wardId) {
        if (position == null || countyId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<PollResultDto> results = pollService.getPollResults(position, countyId,
                                                               constituencyId, wardId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/check-vote")
    public ResponseEntity<Map<String, Boolean>> checkUserVote(
            @RequestParam String position,
            @RequestParam Integer countyId,
            @RequestParam(required = false) Integer constituencyId,
            @RequestParam(required = false) Integer wardId,
            Authentication authentication) {
        try {
            if (position == null || countyId == null) {
                return ResponseEntity.badRequest().body(Map.of("hasVoted", false));
            }
            String email = authentication.getName();
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("hasVoted", false));
            }
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            boolean hasVoted = pollService.hasUserVoted(user, position, countyId,
                                                       constituencyId, wardId);

            return ResponseEntity.ok(Map.of("hasVoted", hasVoted));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when checking user vote", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("hasVoted", false));
        } catch (DataAccessException e) {
            logger.error("Database error when checking user vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("hasVoted", false));
        } catch (Exception e) {
            logger.error("Failed to check user vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("hasVoted", false));
        }
    }

    @GetMapping("/user-vote")
    public ResponseEntity<Map<String, Object>> getUserVote(
            @RequestParam String position,
            @RequestParam Integer countyId,
            @RequestParam(required = false) Integer constituencyId,
            @RequestParam(required = false) Integer wardId,
            Authentication authentication) {
        try {
            if (position == null || countyId == null) {
                return ResponseEntity.badRequest().body(Map.of("hasVoted", false));
            }
            String email = authentication.getName();
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("hasVoted", false));
            }
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            java.util.Optional<com.civiclens.entity.PollVote> vote = pollService.getUserVote(user, position, countyId,
                                                          constituencyId, wardId);
            if (vote.isEmpty()) {
                return ResponseEntity.ok(Map.of("hasVoted", false));
            }

            com.civiclens.entity.PollVote pollVote = vote.get();
            String candidateId = pollVote.getCandidateType() == com.civiclens.entity.PollVote.CandidateType.LEADER
                    ? pollVote.getLeaderCandidate().getId()
                    : pollVote.getAspirantCandidate().getId();
            String candidateName = pollVote.getCandidateType() == com.civiclens.entity.PollVote.CandidateType.LEADER
                    ? pollVote.getLeaderCandidate().getName()
                    : pollVote.getAspirantCandidate().getName();
            String candidateType = pollVote.getCandidateType().name();

            return ResponseEntity.ok(Map.of(
                "hasVoted", true,
                "candidateId", candidateId,
                "candidateName", candidateName,
                "candidateType", candidateType
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when fetching user vote", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("hasVoted", false));
        } catch (DataAccessException e) {
            logger.error("Database error when fetching user vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("hasVoted", false));
        } catch (Exception e) {
            logger.error("Failed to fetch user vote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("hasVoted", false));
        }
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<Map<String, Object>>> getCandidates(
            @RequestParam String position,
            @RequestParam Integer countyId,
            @RequestParam(required = false) Integer constituencyId,
            @RequestParam(required = false) Integer wardId) {
        List<Object> candidates = pollService.getCandidatesForPosition(position, countyId,
                                                                     constituencyId, wardId);

        List<Map<String, Object>> candidateDtos = candidates.stream()
            .map(candidate -> {
                Map<String, Object> dto = new java.util.HashMap<>();
                if (candidate instanceof Leader) {
                    Leader leader = (Leader) candidate;
                    dto.put("id", leader.getId());
                    dto.put("name", leader.getName());
                    dto.put("party", leader.getParty());
                    dto.put("type", "LEADER");
                    dto.put("isAspirant", false);
                } else if (candidate instanceof Aspirant) {
                    Aspirant aspirant = (Aspirant) candidate;
                    dto.put("id", aspirant.getId());
                    dto.put("name", aspirant.getName());
                    dto.put("party", null);
                    dto.put("type", "ASPIRANT");
                    dto.put("isAspirant", true);
                }
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(candidateDtos);
    }
}