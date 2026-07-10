package com.civiclens.service;

import com.civiclens.entity.*;
import com.civiclens.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
@Profile("dev")  // Only run in dev profile
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CountyRepository countyRepository;
    private final ConstituencyRepository constituencyRepository;
    private final WardRepository wardRepository;
    private final LeaderRepository leaderRepository;

    @Override
    public void run(String... args) throws Exception {
        if (countyRepository.count() == 0) {
            loadCounties();
        }
        if (constituencyRepository.count() == 0) {
            loadConstituencies();
        }
        if (wardRepository.count() == 0) {
            loadWards();
        }
        if (userRepository.count() == 0) {
            loadUsers();
        }
        if (leaderRepository.count() == 0) {
            loadLeaders();
        }
        log.info("Data loading completed");
    }

    private void loadCounties() {
        // Hardcoded for dev
        if (countyRepository.findById(1).isEmpty()) {
            County county1 = County.builder().id(1).name("Nairobi").build();
            countyRepository.save(county1);
        }
        if (countyRepository.findById(2).isEmpty()) {
            County county2 = County.builder().id(2).name("Mombasa").build();
            countyRepository.save(county2);
        }
        log.info("Loaded hardcoded counties");
    }

    private void loadConstituencies() {
        // Hardcoded for dev
        County county1 = countyRepository.findById(1).orElse(null);
        if (county1 != null && constituencyRepository.findById(1).isEmpty()) {
            Constituency const1 = Constituency.builder().id(1).name("Westlands").county(county1).build();
            constituencyRepository.save(const1);
        }
        County county2 = countyRepository.findById(2).orElse(null);
        if (county2 != null && constituencyRepository.findById(2).isEmpty()) {
            Constituency const2 = Constituency.builder().id(2).name("Mvita").county(county2).build();
            constituencyRepository.save(const2);
        }
        log.info("Loaded hardcoded constituencies");
    }

    private void loadWards() {
        // Hardcoded for dev
        Constituency const1 = constituencyRepository.findById(1).orElse(null);
        if (const1 != null && wardRepository.findById(1).isEmpty()) {
            Ward ward1 = Ward.builder().id(1).name("Westlands Ward").constituency(const1).build();
            wardRepository.save(ward1);
        }
        Constituency const2 = constituencyRepository.findById(2).orElse(null);
        if (const2 != null && wardRepository.findById(2).isEmpty()) {
            Ward ward2 = Ward.builder().id(2).name("Mvita Ward").constituency(const2).build();
            wardRepository.save(ward2);
        }
        log.info("Loaded hardcoded wards");
    }

    private void loadUsers() {
        // Skip for dev
        log.info("Skipped loading users");
    }

    private void loadLeaders() {
        // Leaders will be loaded from the database
        log.info("Leader loading skipped - load data from your database");
    }
}