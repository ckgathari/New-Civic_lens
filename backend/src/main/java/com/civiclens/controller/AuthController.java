package com.civiclens.controller;

import com.civiclens.dto.AuthResponse;
import com.civiclens.dto.LoginRequest;
import com.civiclens.dto.SignupRequest;
import com.civiclens.dto.UserDto;
import com.civiclens.entity.User;
import com.civiclens.repository.UserRepository;
import com.civiclens.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.civiclens.repository.CountyRepository countyRepository;
    @Autowired
    private com.civiclens.repository.ConstituencyRepository constituencyRepository;
    @Autowired
    private com.civiclens.repository.WardRepository wardRepository;

    @Value("${google.client-id}")
    private String googleClientId;


    @PostMapping("/signup")
    @SuppressWarnings("null")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            // Check if user already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "User already exists with this email"));
            }

            // Ensure location selection was provided
            if (request.getCountyId() == null || request.getConstituencyId() == null || request.getWardId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "County, constituency, and ward must be selected"));
            }

            // Create new user
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .county(countyRepository.findById(request.getCountyId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid countyId: " + request.getCountyId())))
                    .constituency(constituencyRepository.findById(request.getConstituencyId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid constituencyId: " + request.getConstituencyId())))
                    .ward(wardRepository.findById(request.getWardId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid wardId: " + request.getWardId())))
                    .isLeader(false)
                    .isAspirant(false)
                    .isAdmin(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

                User savedUser = userRepository.save(java.util.Objects.requireNonNull(user));
            if (savedUser == null || savedUser.getId() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to create user"));
            }

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

            UserDto userDto = UserDto.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .countyId(savedUser.getCounty() != null ? savedUser.getCounty().getId() : null)
                    .constituencyId(savedUser.getConstituency() != null ? savedUser.getConstituency().getId() : null)
                    .wardId(savedUser.getWard() != null ? savedUser.getWard().getId() : null)
                    .countyName(savedUser.getCounty() != null ? savedUser.getCounty().getName() : null)
                    .constituencyName(savedUser.getConstituency() != null ? savedUser.getConstituency().getName() : null)
                    .wardName(savedUser.getWard() != null ? savedUser.getWard().getName() : null)
                    .build();

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .user(userDto)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid signup request for email={}", request != null ? request.getEmail() : "<null>", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error during signup for email={}", request != null ? request.getEmail() : "<null>", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Signup failed"));
        } catch (Exception e) {
            logger.error("Signup failed for email={}", request != null ? request.getEmail() : "<null>", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Signup failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(java.util.Objects.requireNonNull(request.getEmail()));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

            UserDto userDto = UserDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .countyId(user.getCounty() != null ? user.getCounty().getId() : null)
                    .constituencyId(user.getConstituency() != null ? user.getConstituency().getId() : null)
                    .wardId(user.getWard() != null ? user.getWard().getId() : null)
                    .countyName(user.getCounty() != null ? user.getCounty().getName() : null)
                    .constituencyName(user.getConstituency() != null ? user.getConstituency().getName() : null)
                    .wardName(user.getWard() != null ? user.getWard().getName() : null)
                    .build();

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .user(userDto)
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid login request for email={}", request != null ? request.getEmail() : "<null>", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error during login for email={}", request != null ? request.getEmail() : "<null>", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Login failed"));
        } catch (Exception e) {
            logger.error("Login failed for email={}", request != null ? request.getEmail() : "<null>", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid authorization header"));
            }

            String token = authHeader.substring(7);
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            String userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            UserDto userDto = UserDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .countyId(user.getCounty() != null ? user.getCounty().getId() : null)
                    .constituencyId(user.getConstituency() != null ? user.getConstituency().getId() : null)
                    .wardId(user.getWard() != null ? user.getWard().getId() : null)
                    .countyName(user.getCounty() != null ? user.getCounty().getName() : null)
                    .constituencyName(user.getConstituency() != null ? user.getConstituency().getName() : null)
                    .wardName(user.getWard() != null ? user.getWard().getName() : null)
                    .build();

            return ResponseEntity.ok(userDto);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token when getting current user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error getting current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get current user"));
        } catch (Exception e) {
            logger.error("Failed to get current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get current user"));
        }
    }

    @PostMapping("/forgot-password")
    @SuppressWarnings("all")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "If an account exists, a password reset email has been sent"));
        }

        // NOTE: Future enhancement - Implement email service to send password reset token
        // Currently returns success to prevent email enumeration attacks
        return ResponseEntity.ok(Map.of("message", "If an account exists, a password reset email has been sent"));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "Missing or invalid authorization header"));
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtTokenProvider.validateToken(token);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "Invalid or expired token"));
            }

            return ResponseEntity.ok(Map.of("valid", true));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token check request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("valid", false, "message", "Invalid request"));
        } catch (DataAccessException e) {
            logger.error("Database error during token check", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("valid", false, "message", "Token validation failed"));
        } catch (Exception e) {
            logger.error("Token check failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("valid", false, "message", "Token validation failed"));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("idToken");
        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing idToken"));
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");

            // Find or create user
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update picture if changed
                if (pictureUrl != null && !pictureUrl.equals(user.getPhotoUrl())) {
                    user.setPhotoUrl(pictureUrl);
                    user = userRepository.save(user);
                }
            } else {
                // Create new user — location will be set after first login via CompleteProfile
                user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .firstName(firstName)
                        .lastName(lastName)
                        .photoUrl(pictureUrl)
                        .isLeader(false)
                        .isAspirant(false)
                        .isAdmin(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                user = userRepository.save(user);
            }

            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

            UserDto userDto = UserDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .countyId(user.getCounty() != null ? user.getCounty().getId() : null)
                    .constituencyId(user.getConstituency() != null ? user.getConstituency().getId() : null)
                    .wardId(user.getWard() != null ? user.getWard().getId() : null)
                    .countyName(user.getCounty() != null ? user.getCounty().getName() : null)
                    .constituencyName(user.getConstituency() != null ? user.getConstituency().getName() : null)
                    .wardName(user.getWard() != null ? user.getWard().getName() : null)
                    .build();

            return ResponseEntity.ok(AuthResponse.builder().token(token).user(userDto).build());
        } catch (DataAccessException e) {
            logger.error("Database error during Google auth for email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        } catch (Exception e) {
            logger.error("Google auth failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        }
    }
}
