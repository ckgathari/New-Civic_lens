package com.civiclens.controller;

import com.civiclens.dto.AspirantCreateDto;
import com.civiclens.dto.AspirantDto;
import com.civiclens.service.AspirantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AspirantAdminController Tests")
@SuppressWarnings("null")
class AspirantAdminControllerTest {

    @Mock
    private AspirantService aspirantService;

    @InjectMocks
    private AspirantAdminController aspirantAdminController;

    private AspirantCreateDto createDto;
    private AspirantDto aspirantDto;

    @BeforeEach
    void setUp() {
        createDto = new AspirantCreateDto(
            "John Doe",
            "0712345678",
            "john@example.com",
            "Governor",
            "Bio text",
            "Manifesto text",
            "photo_url",
            1, null, null
        );

        aspirantDto = new AspirantDto(
            "asp1",
            "John Doe",
            "0712345678",
            "john@example.com",
            "Governor",
            "Bio text",
            "Manifesto text",
            "photo_url",
            1,           // countyId
            "Test County",  // countyName
            null,        // constituencyId
            null,        // constituencyName
            null,        // wardId
            null,        // wardName
            null,        // createdAt
            null         // updatedAt
        );
    }

    @Test
    @DisplayName("Should create aspirant successfully")
    void testCreateAspirantSuccess() {
        when(aspirantService.createAspirant(any(AspirantCreateDto.class)))
            .thenReturn(aspirantDto);

        ResponseEntity<?> response = aspirantAdminController.createAspirant(createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(aspirantDto, response.getBody());
        verify(aspirantService, times(1)).createAspirant(any(AspirantCreateDto.class));
    }

    @Test
    @DisplayName("Should return 400 on duplicate email")
    void testCreateAspirantDuplicateEmail() {
        when(aspirantService.createAspirant(any(AspirantCreateDto.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        ResponseEntity<?> response = aspirantAdminController.createAspirant(createDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    @DisplayName("Should return 400 on missing county")
    void testCreateAspirantMissingCounty() {
        createDto.setCountyId(null);

        when(aspirantService.createAspirant(any(AspirantCreateDto.class)))
            .thenThrow(new IllegalArgumentException("County is required"));

        ResponseEntity<?> response = aspirantAdminController.createAspirant(createDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    @DisplayName("Should get all aspirants")
    void testGetAllAspirantsSuccess() {
        List<AspirantDto> aspirants = Arrays.asList(aspirantDto);

        when(aspirantService.getAllAspirants()).thenReturn(aspirants);

        ResponseEntity<?> response = aspirantAdminController.getAllAspirants();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(aspirants, response.getBody());
    }

    @Test
    @DisplayName("Should return empty list when no aspirants")
    void testGetAllAspirantsEmpty() {
        when(aspirantService.getAllAspirants()).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = aspirantAdminController.getAllAspirants();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new ArrayList<>(), response.getBody());
    }

    @Test
    @DisplayName("Should get aspirant by id")
    void testGetAspirantByIdSuccess() {
        when(aspirantService.getAspirantById("asp1")).thenReturn(aspirantDto);

        ResponseEntity<?> response = aspirantAdminController.getAspirantById("asp1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(aspirantDto, response.getBody());
    }

    @Test
    @DisplayName("Should return 404 when aspirant not found")
    void testGetAspirantByIdNotFound() {
        when(aspirantService.getAspirantById("invalid"))
            .thenThrow(new RuntimeException("Aspirant not found"));

        assertThrows(RuntimeException.class, () ->
            aspirantAdminController.getAspirantById("invalid")
        );
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

        when(aspirantService.updateAspirant("asp1", updateDto))
            .thenReturn(aspirantDto);

        ResponseEntity<?> response = aspirantAdminController.updateAspirant("asp1", updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(aspirantDto, response.getBody());
        verify(aspirantService, times(1)).updateAspirant("asp1", updateDto);
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent aspirant")
    void testUpdateAspirantNotFound() {
        when(aspirantService.updateAspirant("invalid", createDto))
            .thenThrow(new RuntimeException("Aspirant not found"));

        ResponseEntity<?> response = aspirantAdminController.updateAspirant("invalid", createDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    @DisplayName("Should delete aspirant successfully")
    void testDeleteAspirantSuccess() {
        doNothing().when(aspirantService).deleteAspirant("asp1");

        ResponseEntity<?> response = aspirantAdminController.deleteAspirant("asp1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("deleted"));
        verify(aspirantService, times(1)).deleteAspirant("asp1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent aspirant")
    void testDeleteAspirantNotFound() {
        doThrow(new RuntimeException("Aspirant not found"))
            .when(aspirantService).deleteAspirant("invalid");

        ResponseEntity<?> response = aspirantAdminController.deleteAspirant("invalid");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    @DisplayName("Should validate required fields on creation")
    void testCreateAspirantValidation() {
        AspirantCreateDto invalidDto = new AspirantCreateDto(
            "", // Empty name
            "0712345678",
            "john@example.com",
            "Governor",
            "Bio",
            "Manifesto",
            "photo",
            1, null, null
        );

        when(aspirantService.createAspirant(any(AspirantCreateDto.class)))
            .thenThrow(new IllegalArgumentException("Name is required"));

        ResponseEntity<?> response = aspirantAdminController.createAspirant(invalidDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("error"));
    }

    @Test
    @DisplayName("Should handle constituency filtering")
    void testGetAspirantWithConstituency() {
        createDto.setConstituencyId(1);

        when(aspirantService.createAspirant(any(AspirantCreateDto.class)))
            .thenReturn(aspirantDto);

        ResponseEntity<?> response = aspirantAdminController.createAspirant(createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle ward filtering")
    void testGetAspirantWithWard() {
        createDto.setConstituencyId(1);
        createDto.setWardId(1);

        when(aspirantService.createAspirant(any(AspirantCreateDto.class)))
            .thenReturn(aspirantDto);

        ResponseEntity<?> response = aspirantAdminController.createAspirant(createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
