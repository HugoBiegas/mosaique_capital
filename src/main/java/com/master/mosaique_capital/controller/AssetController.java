package com.master.mosaique_capital.controller;

import com.master.mosaique_capital.dto.AssetDto;
import com.master.mosaique_capital.dto.AssetValuationDto;
import com.master.mosaique_capital.model.Asset;
import com.master.mosaique_capital.security.FirebaseUserDetails;
import com.master.mosaique_capital.service.AssetService;
import com.master.mosaique_capital.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<Asset> createAsset(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @Valid @RequestBody AssetDto assetDto) {
        try {
            Asset asset = assetDto.toAsset();
            asset.setUserId(userDetails.getUid());

            Asset createdAsset = assetService.createAsset(asset).get();
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAsset);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la création de l'actif: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création de l'actif");
        }
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets(@AuthenticationPrincipal FirebaseUserDetails userDetails) {
        try {
            List<Asset> assets = assetService.getAssetsByUserId(userDetails.getUid()).get();
            return ResponseEntity.ok(assets);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la récupération des actifs: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération des actifs");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) {
        try {
            Asset asset = assetService.getAssetById(id).get()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actif non trouvé"));

            // Vérifier que l'utilisateur est propriétaire de l'actif
            if (!asset.getUserId().equals(userDetails.getUid())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à cet actif");
            }

            return ResponseEntity.ok(asset);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la récupération de l'actif: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération de l'actif");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody AssetDto assetDto) {
        try {
            // Vérifier que l'actif existe et appartient à l'utilisateur
            Asset existingAsset = assetService.getAssetById(id).get()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actif non trouvé"));

            if (!existingAsset.getUserId().equals(userDetails.getUid())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à cet actif");
            }

            // Mettre à jour l'actif
            Asset assetToUpdate = assetDto.toAsset();
            assetToUpdate.setId(id);
            assetToUpdate.setUserId(userDetails.getUid());

            Asset updatedAsset = assetService.updateAsset(assetToUpdate).get();
            return ResponseEntity.ok(updatedAsset);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la mise à jour de l'actif: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la mise à jour de l'actif");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) {
        try {
            // Vérifier que l'actif existe et appartient à l'utilisateur
            Asset existingAsset = assetService.getAssetById(id).get()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actif non trouvé"));

            if (!existingAsset.getUserId().equals(userDetails.getUid())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à cet actif");
            }

            assetService.deleteAsset(id).get();
            return ResponseEntity.noContent().build();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la suppression de l'actif: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la suppression de l'actif");
        }
    }

    @PostMapping("/{id}/valuations")
    public ResponseEntity<Asset> addValuation(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody AssetValuationDto valuationDto) {
        try {
            // Vérifier que l'actif existe et appartient à l'utilisateur
            Asset existingAsset = assetService.getAssetById(id).get()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actif non trouvé"));

            if (!existingAsset.getUserId().equals(userDetails.getUid())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à cet actif");
            }

            Asset updatedAsset = assetService.addValuation(id, valuationDto.toAssetValuation()).get();
            return ResponseEntity.ok(updatedAsset);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de l'ajout de la valorisation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'ajout de la valorisation");
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<Asset.AssetCategory, List<Asset>>> getAssetsByCategories(
            @AuthenticationPrincipal FirebaseUserDetails userDetails) {
        try {
            Map<Asset.AssetCategory, List<Asset>> assetsByCategory =
                    assetService.getAssetsByCategories(userDetails.getUid()).get();
            return ResponseEntity.ok(assetsByCategory);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la récupération des actifs par catégorie: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la récupération des actifs par catégorie");
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAssetsSummary(
            @AuthenticationPrincipal FirebaseUserDetails userDetails) {
        try {
            Map<String, Object> summary = assetService.getAssetsSummary(userDetails.getUid()).get();
            return ResponseEntity.ok(summary);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la récupération du résumé des actifs: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la récupération du résumé des actifs");
        }
    }
}