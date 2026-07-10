package com.civiclens.service;

import com.civiclens.dto.AspirantCreateDto;
import com.civiclens.dto.AspirantDto;
import com.civiclens.entity.Aspirant;
import com.civiclens.entity.County;
import com.civiclens.entity.Constituency;
import com.civiclens.entity.Ward;
import com.civiclens.repository.AspirantRepository;
import com.civiclens.repository.CountyRepository;
import com.civiclens.repository.ConstituencyRepository;
import com.civiclens.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AspirantService {

    @Autowired
    private AspirantRepository aspirantRepository;

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private ConstituencyRepository constituencyRepository;

    @Autowired
    private WardRepository wardRepository;

    public AspirantDto createAspirant(@NonNull AspirantCreateDto createDto) {
        if (createDto.getCountyId() == null) {
            throw new IllegalArgumentException("County is required");
        }

        // Check if email already exists
        if (aspirantRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("Aspirant with this email already exists");
        }

        // Validate location entities
        Integer countyId = Objects.requireNonNull(createDto.getCountyId(), "County is required");
        County county = countyRepository.findById(countyId)
            .orElseThrow(() -> new RuntimeException("County not found"));

        Constituency constituency = null;
        if (createDto.getConstituencyId() != null) {
            Integer constituencyId = Objects.requireNonNull(createDto.getConstituencyId(), "Constituency is required");
            constituency = constituencyRepository.findById(constituencyId)
                .orElseThrow(() -> new RuntimeException("Constituency not found"));
        }

        Ward ward = null;
        if (createDto.getWardId() != null) {
            Integer wardId = Objects.requireNonNull(createDto.getWardId(), "Ward is required");
            ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new RuntimeException("Ward not found"));
        }

        // Create aspirant
        Aspirant aspirant = Aspirant.builder()
            .name(createDto.getName())
            .phoneNumber(createDto.getPhoneNumber())
            .email(createDto.getEmail())
            .position(createDto.getPosition())
            .bio(createDto.getBio())
            .manifesto(createDto.getManifesto())
            .profilePicture(createDto.getProfilePicture())
            .county(county)
            .constituency(constituency)
            .ward(ward)
            .build();

        @SuppressWarnings("null")
        Aspirant saved = aspirantRepository.save(aspirant);
        return convertToDto(saved);
    }

    public List<AspirantDto> getAllAspirants() {
        return aspirantRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public AspirantDto getAspirantById(@NonNull String id) {
        Aspirant aspirant = aspirantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Aspirant not found"));
        return convertToDto(Objects.requireNonNull(aspirant, "Aspirant must not be null"));
    }

    public AspirantDto updateAspirant(@NonNull String id, @NonNull AspirantCreateDto updateDto) {
        if (updateDto.getCountyId() == null) {
            throw new IllegalArgumentException("County is required");
        }

        Aspirant aspirant = aspirantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Aspirant not found"));

        // Check email uniqueness if changed
        if (!aspirant.getEmail().equals(updateDto.getEmail()) &&
            aspirantRepository.existsByEmail(updateDto.getEmail())) {
            throw new RuntimeException("Aspirant with this email already exists");
        }

        // Validate location entities
        Integer countyId = Objects.requireNonNull(updateDto.getCountyId(), "County is required");
        County county = countyRepository.findById(countyId)
            .orElseThrow(() -> new RuntimeException("County not found"));

        Constituency constituency = null;
        if (updateDto.getConstituencyId() != null) {
            Integer constituencyId = Objects.requireNonNull(updateDto.getConstituencyId(), "Constituency is required");
            constituency = constituencyRepository.findById(constituencyId)
                .orElseThrow(() -> new RuntimeException("Constituency not found"));
        }

        Ward ward = null;
        if (updateDto.getWardId() != null) {
            Integer wardId = Objects.requireNonNull(updateDto.getWardId(), "Ward is required");
            ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new RuntimeException("Ward not found"));
        }

        // Update aspirant
        aspirant.setName(updateDto.getName());
        aspirant.setPhoneNumber(updateDto.getPhoneNumber());
        aspirant.setEmail(updateDto.getEmail());
        aspirant.setPosition(updateDto.getPosition());
        aspirant.setBio(updateDto.getBio());
        aspirant.setManifesto(updateDto.getManifesto());
        aspirant.setProfilePicture(updateDto.getProfilePicture());
        aspirant.setCounty(county);
        aspirant.setConstituency(constituency);
        aspirant.setWard(ward);

        @SuppressWarnings("null")
        Aspirant updated = aspirantRepository.save(aspirant);
        return convertToDto(updated);
    }

    public void deleteAspirant(@NonNull String id) {
        if (!aspirantRepository.existsById(id)) {
            throw new RuntimeException("Aspirant not found");
        }
        aspirantRepository.deleteById(id);
    }

    public List<Aspirant> getAspirantsByPositionAndLocation(@NonNull String position, @NonNull Integer countyId,
                                                          Integer constituencyId, Integer wardId) {
        return aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            position, countyId, constituencyId, wardId);
    }

    private AspirantDto convertToDto(@NonNull Aspirant aspirant) {
        return new AspirantDto(
            aspirant.getId(),
            aspirant.getName(),
            aspirant.getPhoneNumber(),
            aspirant.getEmail(),
            aspirant.getPosition(),
            aspirant.getBio(),
            aspirant.getManifesto(),
            aspirant.getProfilePicture(),
            aspirant.getCounty() != null ? aspirant.getCounty().getId() : null,
            aspirant.getCounty() != null ? aspirant.getCounty().getName() : null,
            aspirant.getConstituency() != null ? aspirant.getConstituency().getId() : null,
            aspirant.getConstituency() != null ? aspirant.getConstituency().getName() : null,
            aspirant.getWard() != null ? aspirant.getWard().getId() : null,
            aspirant.getWard() != null ? aspirant.getWard().getName() : null,
            aspirant.getCreatedAt(),
            aspirant.getUpdatedAt()
        );
    }
}