package com.civiclens.controller;

import com.civiclens.dto.LeaderDto;
import com.civiclens.entity.Leader;
import com.civiclens.repository.LeaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@SuppressWarnings("null")
public class LeaderController {

    @Autowired
    private LeaderRepository leaderRepository;

    @GetMapping
    public ResponseEntity<List<LeaderDto>> getAllLeaders() {
        List<Leader> leaders = leaderRepository.findAll();
        List<LeaderDto> dtos = leaders.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaderDto> getLeader(@PathVariable String id) {
        Optional<Leader> leaderOpt = leaderRepository.findById(id);
        if (leaderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Leader leader = leaderOpt.get();

        // All leader profiles now require authentication
        // President is accessible to all logged-in users, regardless of location
        return ResponseEntity.ok(convertToDto(leader));
    }

    @GetMapping("/position/{position}")
    public ResponseEntity<List<LeaderDto>> getLeadersByPosition(@PathVariable String position) {
        List<Leader> leaders = leaderRepository.findByPosition(position);
        List<LeaderDto> dtos = leaders.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/county/{countyId}")
    public ResponseEntity<List<LeaderDto>> getLeadersByCounty(@PathVariable Integer countyId) {
        List<Leader> leaders = leaderRepository.findByCounty_IdAndPositionIn(countyId, List.of("Governor", "Senator", "Women Rep"));
        List<LeaderDto> dtos = leaders.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private LeaderDto convertToDto(Leader leader) {
        return LeaderDto.builder()
                .id(leader.getId())
                .name(leader.getName())
                .position(leader.getPosition())
                .photoUrl(leader.getPhotoUrl())
                .bio(leader.getBio())
                .manifesto(leader.getManifesto())
                .countyId(leader.getCounty() != null ? leader.getCounty().getId().toString() : null)
                .countyName(leader.getCounty() != null ? leader.getCounty().getName() : null)
                .constituencyId(leader.getConstituency() != null ? leader.getConstituency().getId().toString() : null)
                .constituencyName(leader.getConstituency() != null ? leader.getConstituency().getName() : null)
                .wardId(leader.getWard() != null ? leader.getWard().getId().toString() : null)
                .wardName(leader.getWard() != null ? leader.getWard().getName() : null)
                .build();
    }
}