package com.master.mosaique_capital.service;

import com.master.mosaique_capital.model.Asset;
import com.master.mosaique_capital.model.AssetValuation;
import com.master.mosaique_capital.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;

    public CompletableFuture<Asset> createAsset(Asset asset) {
        // Validation et initialisation
        if (asset.getAcquisitionDate() == null) {
            asset.setAcquisitionDate(LocalDateTime.now());
        }

        if (asset.getLastUpdateDate() == null) {
            asset.setLastUpdateDate(LocalDateTime.now());
        }

        // Si la valeur courante n'est pas fournie, utiliser la valeur d'acquisition
        if (asset.getCurrentValue() == null && asset.getAcquisitionValue() != null) {
            asset.setCurrentValue(asset.getAcquisitionValue());
        }

        // Créer la valorisation initiale si nécessaire
        if (asset.getValuationHistory() == null) {
            asset.setValuationHistory(new ArrayList<>());
        }

        if (asset.getValuationHistory().isEmpty() && asset.getCurrentValue() != null) {
            AssetValuation initialValuation = AssetValuation.builder()
                    .id(UUID.randomUUID().toString())
                    .assetId(asset.getId()) // Sera mis à jour après la sauvegarde
                    .value(asset.getCurrentValue())
                    .valuationDate(asset.getLastUpdateDate())
                    .currency(asset.getCurrency())
                    .source("Initial")
                    .build();

            asset.getValuationHistory().add(initialValuation);
        }

        return assetRepository.save(asset)
                .thenApply(savedAsset -> {
                    // Mettre à jour l'ID de l'actif dans l'historique de valorisation
                    if (savedAsset.getValuationHistory() != null) {
                        savedAsset.getValuationHistory().forEach(v -> {
                            if (v.getAssetId() == null) {
                                v.setAssetId(savedAsset.getId());
                            }
                        });
                    }
                    return savedAsset;
                });
    }

    public CompletableFuture<Optional<Asset>> getAssetById(String id) {
        return assetRepository.findById(id);
    }

    public CompletableFuture<List<Asset>> getAssetsByUserId(String userId) {
        return assetRepository.findByUserId(userId);
    }

    public CompletableFuture<Asset> updateAsset(Asset asset) {
        return assetRepository.findById(asset.getId())
                .thenCompose(optionalAsset -> {
                    if (optionalAsset.isPresent()) {
                        Asset existingAsset = optionalAsset.get();

                        // Conserver l'historique de valorisation existant
                        asset.setValuationHistory(existingAsset.getValuationHistory());

                        // Si la valeur courante a changé, ajouter une nouvelle valorisation
                        if (asset.getCurrentValue() != null &&
                                (existingAsset.getCurrentValue() == null ||
                                        !asset.getCurrentValue().equals(existingAsset.getCurrentValue()))) {

                            AssetValuation newValuation = AssetValuation.builder()
                                    .id(UUID.randomUUID().toString())
                                    .assetId(asset.getId())
                                    .value(asset.getCurrentValue())
                                    .valuationDate(LocalDateTime.now())
                                    .currency(asset.getCurrency())
                                    .source("Manual Update")
                                    .build();

                            asset.getValuationHistory().add(newValuation);
                        }

                        asset.setLastUpdateDate(LocalDateTime.now());
                        return assetRepository.save(asset);
                    } else {
                        throw new NoSuchElementException("Asset not found with id: " + asset.getId());
                    }
                });
    }

    public CompletableFuture<Void> deleteAsset(String id) {
        return assetRepository.deleteById(id);
    }

    public CompletableFuture<Asset> addValuation(String assetId, AssetValuation valuation) {
        return assetRepository.findById(assetId)
                .thenCompose(optionalAsset -> {
                    if (optionalAsset.isPresent()) {
                        Asset asset = optionalAsset.get();

                        // Configurer la nouvelle valorisation
                        valuation.setId(UUID.randomUUID().toString());
                        valuation.setAssetId(assetId);
                        if (valuation.getValuationDate() == null) {
                            valuation.setValuationDate(LocalDateTime.now());
                        }

                        // Ajouter à l'historique
                        asset.getValuationHistory().add(valuation);

                        // Mettre à jour la valeur courante
                        asset.setCurrentValue(valuation.getValue());
                        asset.setLastUpdateDate(valuation.getValuationDate());

                        return assetRepository.save(asset);
                    } else {
                        throw new NoSuchElementException("Asset not found with id: " + assetId);
                    }
                });
    }

    public CompletableFuture<Map<Asset.AssetCategory, List<Asset>>> getAssetsByCategories(String userId) {
        return assetRepository.findByUserId(userId)
                .thenApply(assets -> assets.stream()
                        .collect(Collectors.groupingBy(Asset::getCategory)));
    }

    public CompletableFuture<Map<Asset.AssetType, List<Asset>>> getAssetsByTypes(String userId) {
        return assetRepository.findByUserId(userId)
                .thenApply(assets -> assets.stream()
                        .collect(Collectors.groupingBy(Asset::getType)));
    }

    public CompletableFuture<Map<String, Object>> getAssetsSummary(String userId) {
        return assetRepository.findByUserId(userId)
                .thenApply(assets -> {
                    Map<String, Object> summary = new HashMap<>();

                    // Valeur totale des actifs
                    BigDecimal totalAssetsValue = assets.stream()
                            .filter(a -> a.getCategory() != Asset.AssetCategory.LIABILITY)
                            .map(Asset::getCurrentValue)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Valeur totale des passifs
                    BigDecimal totalLiabilitiesValue = assets.stream()
                            .filter(a -> a.getCategory() == Asset.AssetCategory.LIABILITY)
                            .map(Asset::getCurrentValue)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Valeur nette
                    BigDecimal netWorth = totalAssetsValue.subtract(totalLiabilitiesValue);

                    // Répartition par catégorie
                    Map<Asset.AssetCategory, BigDecimal> distributionByCategory = assets.stream()
                            .filter(a -> a.getCurrentValue() != null)
                            .collect(Collectors.groupingBy(
                                    Asset::getCategory,
                                    Collectors.reducing(
                                            BigDecimal.ZERO,
                                            Asset::getCurrentValue,
                                            BigDecimal::add
                                    )
                            ));

                    // Répartition par type
                    Map<Asset.AssetType, BigDecimal> distributionByType = assets.stream()
                            .filter(a -> a.getCurrentValue() != null)
                            .collect(Collectors.groupingBy(
                                    Asset::getType,
                                    Collectors.reducing(
                                            BigDecimal.ZERO,
                                            Asset::getCurrentValue,
                                            BigDecimal::add
                                    )
                            ));

                    // Nombre d'actifs par catégorie
                    Map<Asset.AssetCategory, Long> countByCategory = assets.stream()
                            .collect(Collectors.groupingBy(
                                    Asset::getCategory,
                                    Collectors.counting()
                            ));

                    summary.put("totalAssetsValue", totalAssetsValue);
                    summary.put("totalLiabilitiesValue", totalLiabilitiesValue);
                    summary.put("netWorth", netWorth);
                    summary.put("distributionByCategory", distributionByCategory);
                    summary.put("distributionByType", distributionByType);
                    summary.put("countByCategory", countByCategory);
                    summary.put("totalAssetsCount", assets.size());

                    return summary;
                });
    }
}