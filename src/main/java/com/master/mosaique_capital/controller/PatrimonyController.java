package com.master.mosaique_capital.controller;

import com.master.mosaique_capital.security.FirebaseUserDetails;
import com.master.mosaique_capital.service.PatrimonyCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/patrimony")
@RequiredArgsConstructor
@Slf4j
public class PatrimonyController {

    private final PatrimonyCalculationService patrimonyCalculationService;

    @GetMapping("/net-worth")
    public ResponseEntity<Map<String, Object>> getNetWorth(@AuthenticationPrincipal FirebaseUserDetails userDetails) {
        try {
            Map<String, Object> netWorth = patrimonyCalculationService.calculateNetWorth(userDetails.getUid()).get();
            return ResponseEntity.ok(netWorth);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors du calcul du patrimoine net: {}", e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul du patrimoine net");
        }
    }

    @GetMapping("/distribution")
    public ResponseEntity<Map<String, Object>> getAssetDistribution(@AuthenticationPrincipal FirebaseUserDetails userDetails) {
        try {
            Map<String, Object> distribution = patrimonyCalculationService.calculateAssetDistribution(userDetails.getUid()).get();
            return ResponseEntity.ok(distribution);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors du calcul de la répartition des actifs: {}", e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul de la répartition des actifs");
        }
    }

    @GetMapping("/evolution")
    public ResponseEntity<Map<String, Object>> getPatrimonyEvolution(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Map<String, Object> evolution = patrimonyCalculationService
                    .calculatePatrimonyEvolution(userDetails.getUid(), startDate, endDate).get();
            return ResponseEntity.ok(evolution);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors du calcul de l'évolution du patrimoine: {}", e.getMessage());
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul de l'évolution du patrimoine");
        }
    }
}