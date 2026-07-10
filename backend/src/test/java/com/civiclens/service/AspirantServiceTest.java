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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AspirantService Tests")
@SuppressWarnings("null")
class AspirantServiceTest {

    @Mock
    private AspirantRepository aspirantRepository;

    @Mock
    private CountyRepository countyRepository;

    @Mock
    private ConstituencyRepository constituencyRepository;

    @Mock
    private WardRepository wardRepository;

    @InjectMocks
    private AspirantService aspirantService;

    private AspirantCreateDto testCreateDto;
    private Aspirant testAspirant;
    private County testCounty;
    private Constituency testConstituency;
    private Ward testWard;

    @BeforeEach
    void setUp() {
        testCounty = new County();
        testCounty.setId(1);
        testCounty.setName("Test County");

        testConstituency = new Constituency();
        testConstituency.setId(1);
        testConstituency.setName("Test Constituency");
        testConstituency.setCounty(testCounty);

        testWard = new Ward();
        testWard.setId(1);
        testWard.setName("Test Ward");
        testWard.setConstituency(testConstituency);

        testCreateDto = new AspirantCreateDto(
            "John Doe",
            "0712345678",
            "john@example.com",
            "Governor",
            "Bio text",
            "Manifesto text",
            "photo_url",
            1, null, null
        );

        testAspirant = Aspirant.builder()
            .id("asp1")
            .name("John Doe")
            .phoneNumber("0712345678")
            .email("john@example.com")
            .position("Governor")
            .bio("Bio text")
            .manifesto("Manifesto text")
            .county(testCounty)
            .build();
    }

    @Test
    @DisplayName("Should create aspirant successfully with county only")
    void testCreateAspirantSuccess() {
        when(aspirantRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(aspirantRepository.save(any(Aspirant.class))).thenReturn(testAspirant);

        AspirantDto result = aspirantService.createAspirant(testCreateDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(aspirantRepository, times(1)).save(any(Aspirant.class));
    }

    @Test
    @DisplayName("Should create aspirant with all location fields")
    void testCreateAspirantWithAllLocations() {
        testCreateDto.setConstituencyId(1);
        testCreateDto.setWardId(1);

        when(aspirantRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(constituencyRepository.findById(1)).thenReturn(Optional.of(testConstituency));
        when(wardRepository.findById(1)).thenReturn(Optional.of(testWard));
        when(aspirantRepository.save(any(Aspirant.class))).thenReturn(testAspirant);

        AspirantDto result = aspirantService.createAspirant(testCreateDto);

        assertNotNull(result);
        verify(aspirantRepository, times(1)).save(any(Aspirant.class));
    }

    @Test
    @DisplayName("Should reject aspirant with duplicate email")
    void testCreateAspirantDuplicateEmail() {
        when(aspirantRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> aspirantService.createAspirant(testCreateDto));
        verify(aspirantRepository, never()).save(any(Aspirant.class));
    }

    @Test
    @DisplayName("Should throw exception when county not found")
    void testCreateAspirantCountyNotFound() {
        when(aspirantRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(countyRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> aspirantService.createAspirant(testCreateDto));
    }

    @Test
    @DisplayName("Should throw exception when county is null")
    void testCreateAspirantNullCounty() {
        testCreateDto.setCountyId(null);

        assertThrows(IllegalArgumentException.class, () -> aspirantService.createAspirant(testCreateDto));
    }

    @Test
    @DisplayName("Should throw exception when constituency not found")
    void testCreateAspirantConstituencyNotFound() {
        testCreateDto.setConstituencyId(1);

        when(aspirantRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(constituencyRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> aspirantService.createAspirant(testCreateDto));
    }

    @Test
    @DisplayName("Should get all aspirants")
    void testGetAllAspirantsSuccess() {
        when(aspirantRepository.findAll()).thenReturn(List.of(testAspirant));

        List<AspirantDto> results = aspirantService.getAllAspirants();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
    }

    @Test
    @DisplayName("Should return empty list when no aspirants")
    void testGetAllAspirantsEmpty() {
        when(aspirantRepository.findAll()).thenReturn(new ArrayList<>());

        List<AspirantDto> results = aspirantService.getAllAspirants();

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    @DisplayName("Should get aspirant by id")
    void testGetAspirantByIdSuccess() {
        when(aspirantRepository.findById("asp1")).thenReturn(Optional.of(testAspirant));

        AspirantDto result = aspirantService.getAspirantById("asp1");

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when aspirant not found by id")
    void testGetAspirantByIdNotFound() {
        when(aspirantRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> aspirantService.getAspirantById("invalid"));
    }

    @Test
    @DisplayName("Should update aspirant successfully")
    void testUpdateAspirantSuccess() {
        AspirantCreateDto updateDto = new AspirantCreateDto(
            "Jane Doe",
            "0787654321",
            "jane@example.com",
            "Senator",
            "New bio",
            "New manifesto",
            "new_photo",
            1, null, null
        );

        Aspirant updatedAspirant = Aspirant.builder()
            .id("asp1")
            .name("Jane Doe")
            .phoneNumber("0787654321")
            .email("jane@example.com")
            .position("Senator")
            .bio("New bio")
            .county(testCounty)
            .build();

        when(aspirantRepository.findById("asp1")).thenReturn(Optional.of(testAspirant));
        when(aspirantRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(countyRepository.findById(1)).thenReturn(Optional.of(testCounty));
        when(aspirantRepository.save(any(Aspirant.class))).thenReturn(updatedAspirant);

        AspirantDto result = aspirantService.updateAspirant("asp1", updateDto);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(aspirantRepository, times(1)).save(any(Aspirant.class));
    }

    @Test
    @DisplayName("Should reject update with duplicate email")
    void testUpdateAspirantDuplicateEmail() {
        AspirantCreateDto updateDto = new AspirantCreateDto(
            "Jane Doe",
            "0787654321",
            "existing@example.com",
            "Senator",
            "Bio",
            "Manifesto",
            "photo",
            1, null, null
        );

        when(aspirantRepository.findById("asp1")).thenReturn(Optional.of(testAspirant));
        when(aspirantRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> aspirantService.updateAspirant("asp1", updateDto));
    }

    @Test
    @DisplayName("Should delete aspirant successfully")
    void testDeleteAspirantSuccess() {
        when(aspirantRepository.existsById("asp1")).thenReturn(true);

        assertDoesNotThrow(() -> aspirantService.deleteAspirant("asp1"));
        verify(aspirantRepository, times(1)).deleteById("asp1");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent aspirant")
    void testDeleteAspirantNotFound() {
        when(aspirantRepository.existsById("invalid")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> aspirantService.deleteAspirant("invalid"));
    }

    @Test
    @DisplayName("Should get aspirants by position and location")
    void testGetAspirantsByPositionAndLocation() {
        when(aspirantRepository.findByPositionAndCountyIdAndConstituencyIdAndWardId(
            "Governor", 1, null, null))
            .thenReturn(List.of(testAspirant));

        List<Aspirant> results = aspirantService.getAspirantsByPositionAndLocation("Governor", 1, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}
