package com.civiclens.service;

import com.civiclens.entity.Constituency;
import com.civiclens.entity.County;
import com.civiclens.entity.Ward;
import com.civiclens.repository.ConstituencyRepository;
import com.civiclens.repository.CountyRepository;
import com.civiclens.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Cached lookups for static reference data (counties, constituencies, wards).
 * These never change at runtime, so caching eliminates repeated DB queries.
 */
@Service
public class LocationCacheService {

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private ConstituencyRepository constituencyRepository;

    @Autowired
    private WardRepository wardRepository;

    @Cacheable(value = "county-by-id", key = "#id")
    public County getCounty(Integer id) {
        return countyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("County not found: " + id));
    }

    @Cacheable(value = "constituency-by-id", key = "#id")
    public Optional<Constituency> getConstituency(Integer id) {
        return constituencyRepository.findById(id);
    }

    @Cacheable(value = "ward-by-id", key = "#id")
    public Optional<Ward> getWard(Integer id) {
        return wardRepository.findById(id);
    }
}
