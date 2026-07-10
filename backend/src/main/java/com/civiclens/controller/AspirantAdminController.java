package com.civiclens.controller;

import com.civiclens.dto.AspirantCreateDto;
import com.civiclens.dto.AspirantDto;
import com.civiclens.service.AspirantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/aspirants")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AspirantAdminController {

    private static final Logger logger = LoggerFactory.getLogger(AspirantAdminController.class);

    @Autowired
    private AspirantService aspirantService;

    @GetMapping
    public ResponseEntity<List<AspirantDto>> getAllAspirants() {
        List<AspirantDto> aspirants = aspirantService.getAllAspirants();
        return ResponseEntity.ok(aspirants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AspirantDto> getAspirantById(@PathVariable String id) {
        if (id == null || id.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AspirantDto aspirant = aspirantService.getAspirantById(id);
        return ResponseEntity.ok(aspirant);
    }

    @PostMapping
    public ResponseEntity<?> createAspirant(@RequestBody AspirantCreateDto createDto) {
        try {
            if (createDto == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
            }
            AspirantDto aspirant = aspirantService.createAspirant(createDto);
            return ResponseEntity.ok(aspirant);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when creating aspirant", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error when creating aspirant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to create aspirant", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create aspirant"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAspirant(@PathVariable String id, @RequestBody AspirantCreateDto updateDto) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aspirant ID is required"));
            }
            if (updateDto == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
            }
            AspirantDto aspirant = aspirantService.updateAspirant(id, updateDto);
            return ResponseEntity.ok(aspirant);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when updating aspirant id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error when updating aspirant id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to update aspirant id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to update aspirant"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAspirant(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aspirant ID is required"));
            }
            aspirantService.deleteAspirant(id);
            return ResponseEntity.ok(Map.of("message", "Aspirant deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input when deleting aspirant id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error when deleting aspirant id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (Exception e) {
            logger.error("Failed to delete aspirant id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}