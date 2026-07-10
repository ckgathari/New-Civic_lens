package com.civiclens.controller;

import com.civiclens.dto.CommentDto;
import com.civiclens.dto.PaginatedCommentsDto;
import com.civiclens.entity.Comment;
import com.civiclens.entity.Leader;
import com.civiclens.entity.User;
import com.civiclens.repository.CommentRepository;
import com.civiclens.repository.LeaderRepository;
import com.civiclens.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@SuppressWarnings("null")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LeaderRepository leaderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/leader/{leaderId}")
    public ResponseEntity<PaginatedCommentsDto> getComments(
            @PathVariable String leaderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Ensure the requesting user is in the same location as the leader
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        Optional<Leader> leaderOpt = leaderRepository.findById(leaderId);

        if (userOpt.isEmpty() || leaderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Leader leader = leaderOpt.get();

        // If the leader is not tied to a specific location (e.g., national office), allow all users to view.
        if (!(leader.getCounty() == null && leader.getConstituency() == null && leader.getWard() == null)) {
            boolean sameLocation = false;
            if (leader.getCounty() != null && user.getCounty() != null && leader.getCounty().getId().equals(user.getCounty().getId())) {
                sameLocation = true;
            }
            if (leader.getConstituency() != null && user.getConstituency() != null && leader.getConstituency().getId().equals(user.getConstituency().getId())) {
                sameLocation = true;
            }
            if (leader.getWard() != null && user.getWard() != null && leader.getWard().getId().equals(user.getWard().getId())) {
                sameLocation = true;
            }

            if (!sameLocation) {
                return ResponseEntity.status(403).build();
            }
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> topLevelPage = commentRepository.findByLeader_IdAndParentIsNullAndHiddenFalse(leaderId, pageRequest);
        List<Comment> topLevelComments = topLevelPage.getContent();

        List<String> parentIds = topLevelComments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> replies = parentIds.isEmpty() ? Collections.emptyList() : commentRepository.findByParent_IdInAndHiddenFalse(parentIds);

        Map<String, CommentDto> dtoMap = new HashMap<>();
        topLevelComments.forEach(c -> dtoMap.put(c.getId(), convertToDto(c)));

        replies.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .forEach(reply -> {
                    CommentDto dto = convertToDto(reply);
                    CommentDto parent = dtoMap.get(reply.getParent().getId());
                    if (parent != null) {
                        if (parent.getReplies() == null) {
                            parent.setReplies(new ArrayList<>());
                        }
                        parent.getReplies().add(dto);
                    }
                });

        List<CommentDto> content = new ArrayList<>(dtoMap.values());

        PaginatedCommentsDto response = PaginatedCommentsDto.builder()
                .comments(content)
                .page(topLevelPage.getNumber())
                .size(topLevelPage.getSize())
                .totalElements(topLevelPage.getTotalElements())
                .totalPages(topLevelPage.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody CreateCommentRequest request) {
        Optional<Leader> leader = leaderRepository.findById(request.getLeaderId());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(userEmail);

        if (leader.isEmpty() || user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Leader leaderEntity = leader.get();
        User userEntity = user.get();

        // Enforce that comments can only be posted by users in the same location as the leader
        if (!(leaderEntity.getCounty() == null && leaderEntity.getConstituency() == null && leaderEntity.getWard() == null)) {
            boolean sameLocation = false;
            if (leaderEntity.getCounty() != null && userEntity.getCounty() != null && leaderEntity.getCounty().getId().equals(userEntity.getCounty().getId())) {
                sameLocation = true;
            }
            if (leaderEntity.getConstituency() != null && userEntity.getConstituency() != null && leaderEntity.getConstituency().getId().equals(userEntity.getConstituency().getId())) {
                sameLocation = true;
            }
            if (leaderEntity.getWard() != null && userEntity.getWard() != null && leaderEntity.getWard().getId().equals(userEntity.getWard().getId())) {
                sameLocation = true;
            }
            if (!sameLocation) {
                return ResponseEntity.status(403).build();
            }
        }

        Comment.CommentBuilder builder = Comment.builder()
                .leader(leaderEntity)
                .user(userEntity)
                .comment(request.getComment())
                .rating(request.getRating())
                .type(request.getType() != null ? request.getType() : "review")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hidden(false);

        if (request.getParentId() != null) {
            Optional<Comment> parent = commentRepository.findById(request.getParentId());
            parent.ifPresent(builder::parent);
        }

        Comment saved = commentRepository.save(builder.build());
        return ResponseEntity.ok(convertToDto(saved));
    }

    private CommentDto convertToDto(Comment comment) {
        User user = comment.getUser();
        String username = user.getEmail() != null ? user.getEmail().split("@")[0] : "anonymous";
        return CommentDto.builder()
                .id(comment.getId())
                .leaderId(comment.getLeader().getId())
                .userId(user.getId())
                .comment(comment.getComment())
                .rating(comment.getRating())
                .type(comment.getType())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(username)
                .photoUrl(user.getPhotoUrl())
                .build();
    }

    public static class CreateCommentRequest {
        private String leaderId;
        private String comment;
        private Integer rating;
        private String parentId;
        private String type;

        // getters and setters
        public String getLeaderId() { return leaderId; }
        public void setLeaderId(String leaderId) { this.leaderId = leaderId; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}