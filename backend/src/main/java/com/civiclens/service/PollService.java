package com.civiclens.service;

import com.civiclens.dto.PollResultDto;
import com.civiclens.dto.PollVoteDto;
import com.civiclens.entity.Aspirant;
import com.civiclens.entity.Constituency;
import com.civiclens.entity.County;
import com.civiclens.entity.Leader;
import com.civiclens.entity.PollVote;
import com.civiclens.entity.User;
import com.civiclens.entity.Ward;
import com.civiclens.repository.AspirantRepository;
import com.civiclens.repository.ConstituencyRepository;
import com.civiclens.repository.CountyRepository;
import com.civiclens.repository.LeaderRepository;
import com.civiclens.repository.PollVoteRepository;
import com.civiclens.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PollService {

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private LeaderRepository leaderRepository;

    @Autowired
    private AspirantRepository aspirantRepository;

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private ConstituencyRepository constituencyRepository;

    @Autowired
    private WardRepository wardRepository;

    @Transactional
    public PollVoteDto castVote(User voter, @NonNull String candidateId, @NonNull String candidateType, String position,
                                @NonNull Integer countyId, Integer constituencyId, Integer wardId) {
        Integer scopedConstituencyId = scopedConstituencyId(position, constituencyId);
        Integer scopedWardId = scopedWardId(position, wardId);

        Leader leaderCandidate = null;
        Aspirant aspirantCandidate = null;
        PollVote.CandidateType voteCandidateType;

        if ("LEADER".equals(candidateType)) {
            leaderCandidate = leaderRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Leader candidate not found"));
            voteCandidateType = PollVote.CandidateType.LEADER;
        } else if ("ASPIRANT".equals(candidateType)) {
            aspirantCandidate = aspirantRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Aspirant candidate not found"));
            voteCandidateType = PollVote.CandidateType.ASPIRANT;
        } else {
            throw new RuntimeException("Invalid candidate type");
        }

        County county = countyRepository.findById(countyId)
            .orElseThrow(() -> new RuntimeException("County not found"));

        Constituency constituency = scopedConstituencyId != null
            ? constituencyRepository.findById(scopedConstituencyId).orElse(null)
            : null;

        Ward ward = scopedWardId != null
            ? wardRepository.findById(scopedWardId).orElse(null)
            : null;

        boolean hasVoted = pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            voter, position, county, constituency, ward);

        if (hasVoted) {
            throw new RuntimeException("You have already voted for this position in this location");
        }

        PollVote vote = new PollVote();
        vote.setVoter(voter);
        vote.setLeaderCandidate(leaderCandidate);
        vote.setAspirantCandidate(aspirantCandidate);
        vote.setCandidateType(voteCandidateType);
        vote.setPosition(position);
        vote.setCounty(county);
        vote.setConstituency(constituency);
        vote.setWard(ward);

        PollVote savedVote = pollVoteRepository.save(vote);
        return convertToDto(savedVote);
    }

    public List<PollResultDto> getPollResults(String position, @NonNull Integer countyId,
                                              Integer constituencyId, Integer wardId) {
        Integer scopedConstituencyId = scopedConstituencyId(position, constituencyId);
        Integer scopedWardId = scopedWardId(position, wardId);

        County county = countyRepository.findById(countyId)
            .orElseThrow(() -> new RuntimeException("County not found"));

        Constituency constituency = scopedConstituencyId != null
            ? constituencyRepository.findById(scopedConstituencyId).orElse(null)
            : null;

        Ward ward = scopedWardId != null
            ? wardRepository.findById(scopedWardId).orElse(null)
            : null;

        List<Object> candidates = getCandidatesForPosition(position, countyId, scopedConstituencyId, scopedWardId);
        List<Object[]> voteCounts = pollVoteRepository.getVoteCountsByPositionAndLocation(position, county, constituency, ward);

        int totalVotes = voteCounts.stream()
            .mapToInt(row -> ((Long) row[1]).intValue())
            .sum();

        Map<String, Integer> voteCountMap = new HashMap<>();
        for (Object[] row : voteCounts) {
            String candidateKey = (String) row[0];
            int voteCount = ((Long) row[1]).intValue();
            voteCountMap.put(candidateKey, voteCount);
        }

        return candidates.stream()
            .map(candidateObj -> {
                String candidateKey;
                String candidateId;
                String candidateName;
                String party = null;
                boolean isAspirant;

                if (candidateObj instanceof Leader) {
                    Leader leader = (Leader) candidateObj;
                    candidateKey = "L_" + leader.getId();
                    candidateId = leader.getId();
                    candidateName = leader.getName();
                    party = leader.getParty();
                    isAspirant = false;
                } else {
                    Aspirant aspirant = (Aspirant) candidateObj;
                    candidateKey = "A_" + aspirant.getId();
                    candidateId = aspirant.getId();
                    candidateName = aspirant.getName();
                    isAspirant = true;
                }

                int voteCount = voteCountMap.getOrDefault(candidateKey, 0);
                double percentage = totalVotes > 0 ? (voteCount * 100.0) / totalVotes : 0.0;

                return new PollResultDto(
                    candidateId,
                    candidateName,
                    party,
                    isAspirant,
                    voteCount,
                    percentage
                );
            })
            .sorted((a, b) -> {
                int byCount = Integer.compare(b.getVoteCount(), a.getVoteCount());
                if (byCount != 0) {
                    return byCount;
                }
                return a.getCandidateName().compareToIgnoreCase(b.getCandidateName());
            })
            .collect(Collectors.toList());
    }

    public Optional<PollVote> getUserVote(User user, String position, @NonNull Integer countyId,
                                          Integer constituencyId, Integer wardId) {
        Integer scopedConstituencyId = scopedConstituencyId(position, constituencyId);
        Integer scopedWardId = scopedWardId(position, wardId);

        County county = countyRepository.findById(countyId)
            .orElseThrow(() -> new RuntimeException("County not found"));

        Constituency constituency = scopedConstituencyId != null
            ? constituencyRepository.findById(scopedConstituencyId).orElse(null)
            : null;

        Ward ward = scopedWardId != null
            ? wardRepository.findById(scopedWardId).orElse(null)
            : null;

        return pollVoteRepository.findByVoterAndPositionAndCountyAndConstituencyAndWard(
            user, position, county, constituency, ward);
    }

    public boolean hasUserVoted(User user, String position, @NonNull Integer countyId,
                                Integer constituencyId, Integer wardId) {
        Integer scopedConstituencyId = scopedConstituencyId(position, constituencyId);
        Integer scopedWardId = scopedWardId(position, wardId);

        County county = countyRepository.findById(countyId)
            .orElseThrow(() -> new RuntimeException("County not found"));

        Constituency constituency = scopedConstituencyId != null
            ? constituencyRepository.findById(scopedConstituencyId).orElse(null)
            : null;

        Ward ward = scopedWardId != null
            ? wardRepository.findById(scopedWardId).orElse(null)
            : null;

        return pollVoteRepository.existsByVoterAndPositionAndCountyAndConstituencyAndWard(
            user, position, county, constituency, ward);
    }

    public List<Object> getCandidatesForPosition(String position, Integer countyId,
                                                 Integer constituencyId, Integer wardId) {
        Integer scopedConstituencyId = scopedConstituencyId(position, constituencyId);
        Integer scopedWardId = scopedWardId(position, wardId);

        List<Leader> leaders;
        List<Aspirant> aspirants;

        if ("President".equalsIgnoreCase(position)) {
            leaders = leaderRepository.findByPosition(position);
            aspirants = aspirantRepository.findByPosition(position);
        } else {
            leaders = leaderRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
                position, countyId, scopedConstituencyId, scopedWardId);
            aspirants = aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
                position, countyId, scopedConstituencyId, scopedWardId);
        }

        List<Object> candidates = new ArrayList<>();
        candidates.addAll(leaders);
        candidates.addAll(aspirants);
        return candidates;
    }

    private Integer scopedConstituencyId(String position, Integer constituencyId) {
        if (position == null) {
            return constituencyId;
        }
        if ("MP".equalsIgnoreCase(position) || "MCA".equalsIgnoreCase(position)) {
            return constituencyId;
        }
        return null;
    }

    private Integer scopedWardId(String position, Integer wardId) {
        if (position == null) {
            return wardId;
        }
        if ("MCA".equalsIgnoreCase(position)) {
            return wardId;
        }
        return null;
    }

    private PollVoteDto convertToDto(PollVote vote) {
        String candidateName;
        if (vote.getCandidateType() == PollVote.CandidateType.LEADER) {
            candidateName = vote.getLeaderCandidate().getName();
        } else {
            candidateName = vote.getAspirantCandidate().getName();
        }

        return new PollVoteDto(
            vote.getId(),
            null,
            vote.getVoter().getFullName(),
            null,
            candidateName,
            vote.getPosition(),
            vote.getCounty() != null ? vote.getCounty().getId().longValue() : null,
            vote.getCounty() != null ? vote.getCounty().getName() : null,
            vote.getConstituency() != null ? vote.getConstituency().getId().longValue() : null,
            vote.getConstituency() != null ? vote.getConstituency().getName() : null,
            vote.getWard() != null ? vote.getWard().getId().longValue() : null,
            vote.getWard() != null ? vote.getWard().getName() : null,
            vote.getCreatedAt()
        );
    }
}
