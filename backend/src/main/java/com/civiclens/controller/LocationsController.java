package com.civiclens.controller;

import com.civiclens.entity.County;
import com.civiclens.entity.Constituency;
import com.civiclens.entity.Ward;
import com.civiclens.repository.CountyRepository;
import com.civiclens.repository.ConstituencyRepository;
import com.civiclens.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationsController {
    @Autowired
    private CountyRepository countyRepository;
    @Autowired
    private ConstituencyRepository constituencyRepository;
    @Autowired
    private WardRepository wardRepository;

    @GetMapping("/counties")
    public ResponseEntity<List<County>> getCounties() {
        return ResponseEntity.ok(countyRepository.findAll());
    }

    @GetMapping("/constituencies/{countyId}")
    public ResponseEntity<List<Constituency>> getConstituencies(@PathVariable Integer countyId) {
        java.util.Objects.requireNonNull(countyId, "countyId must not be null");
        return ResponseEntity.ok(constituencyRepository.findByCounty_Id(countyId));
    }

    @GetMapping("/wards/{constituencyId}")
    public ResponseEntity<List<Ward>> getWards(@PathVariable Integer constituencyId) {
        java.util.Objects.requireNonNull(constituencyId, "constituencyId must not be null");
        return ResponseEntity.ok(wardRepository.findByConstituency_Id(constituencyId));
    }
}
