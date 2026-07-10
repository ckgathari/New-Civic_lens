package com.civiclens.controller;

import com.civiclens.entity.Comment;
import com.civiclens.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Get all comments (including hidden ones) for moderation
     */
    @GetMapping("/comments")
    public ResponseEntity<List<Map<String, Object>>> getAllCommentsForModeration(
            @RequestParam(required = false) Boolean hidden) {
        try {
            List<Comment> comments;
            if (hidden != null) {
                comments = commentRepository.findAll().stream()
                    .filter(c -> Objects.equals(c.getHidden(), hidden))
                    .collect(Collectors.toList());
            } else {
                comments = commentRepository.findAll();
            }

            List<Map<String, Object>> result = comments.stream()
                .map(this::mapCommentToModeration)
                .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request when getting comments for moderation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (DataAccessException e) {
            logger.error("Database error when getting comments for moderation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Failed to get comments for moderation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get flagged/hidden comments for review
     */
    @GetMapping("/comments/flagged")
    public ResponseEntity<List<Map<String, Object>>> getFlaggedComments() {
        try {
            List<Comment> flaggedComments = commentRepository.findAll().stream()
                .filter(c -> c.getHidden() != null && c.getHidden())
                .collect(Collectors.toList());

            List<Map<String, Object>> result = flaggedComments.stream()
                .map(this::mapCommentToModeration)
                .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request when getting flagged comments", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (DataAccessException e) {
            logger.error("Database error when getting flagged comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Failed to get flagged comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Hide/moderate a comment
     */
    @PutMapping("/comments/{id}/hide")
    public ResponseEntity<?> hideComment(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment ID is required"));
            }
            Optional<Comment> commentOpt = commentRepository.findById(id);
            if (commentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Comment comment = commentOpt.get();
            comment.setHidden(true);
            commentRepository.save(comment);

            return ResponseEntity.ok(Map.of("message", "Comment hidden successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when hiding comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error when hiding comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to hide comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Unhide a comment (restore it)
     */
    @PutMapping("/comments/{id}/unhide")
    public ResponseEntity<?> unhideComment(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment ID is required"));
            }
            Optional<Comment> commentOpt = commentRepository.findById(id);
            if (commentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Comment comment = commentOpt.get();
            comment.setHidden(false);
            commentRepository.save(comment);

            return ResponseEntity.ok(Map.of("message", "Comment restored successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when unhiding comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error when unhiding comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to unhide comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Delete a comment permanently
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment ID is required"));
            }
            Optional<Comment> commentOpt = commentRepository.findById(id);
            if (commentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            commentRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when deleting comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error when deleting comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to delete comment id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            long totalComments = commentRepository.count();
            long hiddenComments = commentRepository.findAll().stream()
                .filter(c -> c.getHidden() != null && c.getHidden())
                .count();
            long visibleComments = totalComments - hiddenComments;

            return ResponseEntity.ok(Map.of(
                "totalComments", totalComments,
                "visibleComments", visibleComments,
                "hiddenComments", hiddenComments
            ));
        } catch (DataAccessException e) {
            logger.error("Database error when getting admin stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to get admin stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    // Helper method to map comment to moderation view
    private Map<String, Object> mapCommentToModeration(Comment comment) {
        return Map.of(
            "id", comment.getId(),
            "authorName", comment.getUser() != null ? comment.getUser().getFullName() : null,
            "authorEmail", comment.getUser() != null ? comment.getUser().getEmail() : null,
            "leaderName", comment.getLeader() != null ? comment.getLeader().getName() : null,
            "leaderId", comment.getLeader() != null ? comment.getLeader().getId() : null,
            "comment", comment.getComment(),
            "rating", comment.getRating() != null ? comment.getRating() : 0,
            "createdAt", comment.getCreatedAt(),
            "hidden", comment.getHidden() != null ? comment.getHidden() : false,
            "parentId", comment.getParent() != null ? comment.getParent().getId() : null
        );
    }
}