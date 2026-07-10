package com.civiclens.integration;

import com.civiclens.dto.AspirantCreateDto;
import com.civiclens.entity.*;
import com.civiclens.repository.*;
import com.civiclens.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Aspirant Integration Tests")
@SuppressWarnings("null")
class AspirantIntegrationTest {

    @Autowired
    private AspirantService aspirantService;

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private ConstituencyRepository constituencyRepository;

    private County testCounty;
    private Constituency testConstituency;

    @BeforeEach
    void setUp() {
        testCounty = new County();
        testCounty.setId(1);
        testCounty.setName("Integration Test County");
        countyRepository.save(testCounty);

        testConstituency = new Constituency();
        testConstituency.setId(1);
        testConstituency.setName("Integration Test Constituency");
        testConstituency.setCounty(testCounty);
        constituencyRepository.save(testConstituency);
    }

    @Test
    @DisplayName("Should create aspirant and retrieve it")
    void testCreateAndRetrieveAspirant() {
        AspirantCreateDto createDto = new AspirantCreateDto(
            "John Aspirant",
            "0712345678",
            "aspirant@integration.com",
            "Governor",
            "Integration bio",
            "Integration manifesto",
            "photo_url",
            testCounty.getId(), null, null
        );

        var result = aspirantService.createAspirant(createDto);

        assertNotNull(result);
        assertEquals("John Aspirant", result.getName());
        assertEquals("Governor", result.getPosition());

        var retrieved = aspirantService.getAspirantById(result.getId());
        assertEquals("John Aspirant", retrieved.getName());
    }

    @Test
    @DisplayName("Should update aspirant successfully")
    void testUpdateAspirant() {
        AspirantCreateDto createDto = new AspirantCreateDto(
            "John Aspirant",
            "0712345678",
            "aspirant@integration.com",
            "Governor",
            "Original bio",
            "Original manifesto",
            "photo_url",
            testCounty.getId(), null, null
        );

        var created = aspirantService.createAspirant(createDto);

        AspirantCreateDto updateDto = new AspirantCreateDto(
            "Jane Aspirant",
            "0787654321",
            "aspirant@integration.com",
            "Senator",
            "Updated bio",
            "Updated manifesto",
            "new_photo",
            testCounty.getId(), testConstituency.getId(), null
        );

        var updated = aspirantService.updateAspirant(created.getId(), updateDto);

        assertEquals("Jane Aspirant", updated.getName());
        assertEquals("Senator", updated.getPosition());
    }

    @Test
    @DisplayName("Should delete aspirant successfully")
    void testDeleteAspirant() {
        AspirantCreateDto createDto = new AspirantCreateDto(
            "John Aspirant",
            "0712345678",
            "aspirant@integration.com",
            "Governor",
            "Bio",
            "Manifesto",
            "photo",
            testCounty.getId(), null, null
        );

        var created = aspirantService.createAspirant(createDto);
        String id = created.getId();

        aspirantService.deleteAspirant(id);

        assertThrows(RuntimeException.class, () -> aspirantService.getAspirantById(id));
    }

    @Test
    @DisplayName("Should enforce email uniqueness across creations")
    void testEmailUniquenessConstraint() {
        AspirantCreateDto createDto = new AspirantCreateDto(
            "John Aspirant",
            "0712345678",
            "unique@integration.com",
            "Governor",
            "Bio",
            "Manifesto",
            "photo",
            testCounty.getId(), null, null
        );

        aspirantService.createAspirant(createDto);

        AspirantCreateDto duplicate = new AspirantCreateDto(
            "Jane Aspirant",
            "0787654321",
            "unique@integration.com",
            "Senator",
            "Bio",
            "Manifesto",
            "photo",
            testCounty.getId(), null, null
        );

        assertThrows(RuntimeException.class, () -> aspirantService.createAspirant(duplicate));
    }

    @Test
    @DisplayName("Should maintain location hierarchy")
    void testLocationHierarchy() {
        AspirantCreateDto createDto = new AspirantCreateDto(
            "John Aspirant",
            "0712345678",
            "aspirant@integration.com",
            "MP",
            "Bio",
            "Manifesto",
            "photo",
            testCounty.getId(),
            testConstituency.getId(),
            null
        );

        var result = aspirantService.createAspirant(createDto);

        assertNotNull(result);
        assertEquals(testCounty.getName(), result.getCountyName());
        assertEquals(testConstituency.getName(), result.getConstituencyName());
    }
}
