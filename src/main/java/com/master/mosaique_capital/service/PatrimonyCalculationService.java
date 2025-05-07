package com.master.mosaique_capital.service;

import com.master.mosaique_capital.model.Asset;
import com.master.mosaique_capital.model.AssetValuation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatrimonyCalculationService {

    private final AssetService assetService;

    /**
     * Calcule le patrimoine net total d'un utilisateur
     */
    public CompletableFuture<Map<String, Object>> calculateNetWorth(String userId) {
        return assetService.getAssetsByUserId(userId)
                .thenApply(assets -> {
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

                    Map<String, Object> result = new HashMap<>();
                    result.put("totalAssetsValue", totalAssetsValue);
                    result.put("totalLiabilitiesValue", totalLiabilitiesValue);
                    result.put("netWorth", netWorth);
                    result.put("calculationDate", LocalDateTime.now());

                    return result;
                });
    }

    /**
     * Calcule la répartition des actifs par catégorie
     */
    public CompletableFuture<Map<String, Object>> calculateAssetDistribution(String userId) {
        return assetService.getAssetsByUserId(userId)
                .thenApply(assets -> {
                    Map<String, Object> result = new HashMap<>();

                    // Valeur totale des actifs (hors passifs)
                    BigDecimal totalAssetsValue = assets.stream()
                            .filter(a -> a.getCategory() != Asset.AssetCategory.LIABILITY)
                            .map(Asset::getCurrentValue)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Répartition par catégorie
                    Map<Asset.AssetCategory, BigDecimal> amountByCategory = assets.stream()
                            .filter(a -> a.getCurrentValue() != null)
                            .collect(Collectors.groupingBy(
                                    Asset::getCategory,
                                    Collectors.reducing(
                                            BigDecimal.ZERO,
                                            Asset::getCurrentValue,
                                            BigDecimal::add
                                    )
                            ));

                    // Calcul des pourcentages par catégorie
                    Map<String, Object> percentageByCategory = new HashMap<>();
                    if (totalAssetsValue.compareTo(BigDecimal.ZERO) > 0) {
                        amountByCategory.forEach((category, amount) -> {
                            if (category != Asset.AssetCategory.LIABILITY) {
                                BigDecimal percentage = amount
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(totalAssetsValue, 2, RoundingMode.HALF_UP);
                                percentageByCategory.put(category.name(), percentage);
                            }
                        });
                    }

                    // Répartition par type
                    Map<Asset.AssetType, BigDecimal> amountByType = assets.stream()
                            .filter(a -> a.getCurrentValue() != null && a.getCategory() != Asset.AssetCategory.LIABILITY)
                            .collect(Collectors.groupingBy(
                                    Asset::getType,
                                    Collectors.reducing(
                                            BigDecimal.ZERO,
                                            Asset::getCurrentValue,
                                            BigDecimal::add
                                    )
                            ));

                    // Calcul des pourcentages par type
                    Map<String, Object> percentageByType = new HashMap<>();
                    if (totalAssetsValue.compareTo(BigDecimal.ZERO) > 0) {
                        amountByType.forEach((type, amount) -> {
                            BigDecimal percentage = amount
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(totalAssetsValue, 2, RoundingMode.HALF_UP);
                            percentageByType.put(type.name(), percentage);
                        });
                    }

                    result.put("totalAssetsValue", totalAssetsValue);
                    result.put("amountByCategory", amountByCategory);
                    result.put("percentageByCategory", percentageByCategory);
                    result.put("amountByType", amountByType);
                    result.put("percentageByType", percentageByType);
                    result.put("calculationDate", LocalDateTime.now());

                    return result;
                });
    }

    /**
     * Calcule l'évolution de la valeur du patrimoine dans le temps
     */
    public CompletableFuture<Map<String, Object>> calculatePatrimonyEvolution(String userId,
                                                                              LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        if (startDate == null) {
            // Par défaut, 1 an en arrière
            startDate = endDate.minus(1, ChronoUnit.YEARS);
        }

        final LocalDateTime finalStartDate = startDate;
        final LocalDateTime finalEndDate = endDate;

        return assetService.getAssetsByUserId(userId)
                .thenApply(assets -> {
                    Map<String, Object> result = new HashMap<>();
                    List<Map<String, Object>> evolutionPoints = new ArrayList<>();

                    // Extraire toutes les dates de valorisation
                    Set<LocalDateTime> allValuationDates = new TreeSet<>();
                    for (Asset asset : assets) {
                        for (AssetValuation valuation : asset.getValuationHistory()) {
                            LocalDateTime valuationDate = valuation.getValuationDate();
                            if (valuationDate.isAfter(finalStartDate) &&
                                    (valuationDate.isBefore(finalEndDate) || valuationDate.isEqual(finalEndDate))) {
                                allValuationDates.add(valuationDate);
                            }
                        }
                    }

                    // Ajouter également la date d'acquisition si elle est dans la plage
                    for (Asset asset : assets) {
                        if (asset.getAcquisitionDate() != null &&
                                asset.getAcquisitionDate().isAfter(finalStartDate) &&
                                (asset.getAcquisitionDate().isBefore(finalEndDate) ||
                                        asset.getAcquisitionDate().isEqual(finalEndDate))) {
                            allValuationDates.add(asset.getAcquisitionDate());
                        }
                    }

                    // Ajouter la date de début et de fin si pas déjà présentes
                    allValuationDates.add(finalStartDate);
                    allValuationDates.add(finalEndDate);

                    // Pour chaque date, calculer la valeur totale du patrimoine
                    for (LocalDateTime date : allValuationDates) {
                        BigDecimal totalAssetsValue = BigDecimal.ZERO;
                        BigDecimal totalLiabilitiesValue = BigDecimal.ZERO;

                        for (Asset asset : assets) {
                            // Trouver la valorisation la plus récente à cette date
                            Optional<AssetValuation> latestValuation = asset.getValuationHistory().stream()
                                    .filter(v -> v.getValuationDate().isBefore(date) || v.getValuationDate().isEqual(date))
                                    .max(Comparator.comparing(AssetValuation::getValuationDate));

                            if (latestValuation.isPresent()) {
                                BigDecimal value = latestValuation.get().getValue();
                                if (asset.getCategory() == Asset.AssetCategory.LIABILITY) {
                                    totalLiabilitiesValue = totalLiabilitiesValue.add(value);
                                } else {
                                    totalAssetsValue = totalAssetsValue.add(value);
                                }
                            } else if (asset.getAcquisitionDate() != null &&
                                    (asset.getAcquisitionDate().isBefore(date) ||
                                            asset.getAcquisitionDate().isEqual(date)) &&
                                    asset.getAcquisitionValue() != null) {
                                // Si pas de valorisation mais acquis avant cette date, utiliser la valeur d'acquisition
                                BigDecimal value = asset.getAcquisitionValue();
                                if (asset.getCategory() == Asset.AssetCategory.LIABILITY) {
                                    totalLiabilitiesValue = totalLiabilitiesValue.add(value);
                                } else {
                                    totalAssetsValue = totalAssetsValue.add(value);
                                }
                            }
                        }

                        BigDecimal netWorth = totalAssetsValue.subtract(totalLiabilitiesValue);

                        Map<String, Object> point = new HashMap<>();
                        point.put("date", date);
                        point.put("totalAssetsValue", totalAssetsValue);
                        point.put("totalLiabilitiesValue", totalLiabilitiesValue);
                        point.put("netWorth", netWorth);

                        evolutionPoints.add(point);
                    }

                    // Trier par date
                    evolutionPoints.sort(Comparator.comparing(p -> (LocalDateTime) p.get("date")));

                    result.put("evolutionPoints", evolutionPoints);
                    result.put("startDate", finalStartDate);
                    result.put("endDate", finalEndDate);

                    // Calculer la variation totale
                    if (evolutionPoints.size() >= 2) {
                        BigDecimal initialNetWorth = (BigDecimal) evolutionPoints.get(0).get("netWorth");
                        BigDecimal finalNetWorth = (BigDecimal) evolutionPoints.get(evolutionPoints.size() - 1).get("netWorth");

                        if (initialNetWorth.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal totalChangePercent = finalNetWorth.subtract(initialNetWorth)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(initialNetWorth, 2, RoundingMode.HALF_UP);

                            result.put("totalChangeAmount", finalNetWorth.subtract(initialNetWorth));
                            result.put("totalChangePercent", totalChangePercent);
                        }
                    }

                    return result;
                });
    }
}