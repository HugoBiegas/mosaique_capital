package com.master.mosaique_capital.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    private String id;
    private String userId;
    private String name;
    private String description;
    private AssetType type;
    private AssetCategory category;
    private String currency;

    private BigDecimal currentValue;
    private BigDecimal acquisitionValue;
    private LocalDateTime acquisitionDate;
    private LocalDateTime lastUpdateDate;

    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @Builder.Default
    private List<AssetValuation> valuationHistory = new ArrayList<>();

    // Méthodes pour faciliter la conversion depuis/vers Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("name", name);
        map.put("description", description);
        map.put("type", type != null ? type.name() : null);
        map.put("category", category != null ? category.name() : null);
        map.put("currency", currency);
        map.put("currentValue", currentValue != null ? currentValue.toString() : null);
        map.put("acquisitionValue", acquisitionValue != null ? acquisitionValue.toString() : null);
        map.put("acquisitionDate", acquisitionDate != null ? acquisitionDate.toString() : null);
        map.put("lastUpdateDate", lastUpdateDate != null ? lastUpdateDate.toString() : null);
        map.put("attributes", attributes);

        List<Map<String, Object>> valuationHistoryMaps = new ArrayList<>();
        if (valuationHistory != null) {
            for (AssetValuation valuation : valuationHistory) {
                valuationHistoryMaps.add(valuation.toMap());
            }
        }
        map.put("valuationHistory", valuationHistoryMaps);

        return map;
    }

    public static Asset fromMap(Map<String, Object> map) {
        Asset asset = new Asset();
        asset.setId((String) map.get("id"));
        asset.setUserId((String) map.get("userId"));
        asset.setName((String) map.get("name"));
        asset.setDescription((String) map.get("description"));

        String typeStr = (String) map.get("type");
        if (typeStr != null) {
            asset.setType(AssetType.valueOf(typeStr));
        }

        String categoryStr = (String) map.get("category");
        if (categoryStr != null) {
            asset.setCategory(AssetCategory.valueOf(categoryStr));
        }

        asset.setCurrency((String) map.get("currency"));

        String currentValueStr = (String) map.get("currentValue");
        if (currentValueStr != null) {
            asset.setCurrentValue(new BigDecimal(currentValueStr));
        }

        String acquisitionValueStr = (String) map.get("acquisitionValue");
        if (acquisitionValueStr != null) {
            asset.setAcquisitionValue(new BigDecimal(acquisitionValueStr));
        }

        String acquisitionDateStr = (String) map.get("acquisitionDate");
        if (acquisitionDateStr != null) {
            asset.setAcquisitionDate(LocalDateTime.parse(acquisitionDateStr));
        }

        String lastUpdateDateStr = (String) map.get("lastUpdateDate");
        if (lastUpdateDateStr != null) {
            asset.setLastUpdateDate(LocalDateTime.parse(lastUpdateDateStr));
        }

        if (map.get("attributes") != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> attributes = new HashMap<>((Map<String, String>) map.get("attributes"));
            asset.setAttributes(attributes);
        }

        List<AssetValuation> valuationHistory = new ArrayList<>();
        if (map.get("valuationHistory") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> valuationHistoryMaps = (List<Map<String, Object>>) map.get("valuationHistory");
            for (Map<String, Object> valuationMap : valuationHistoryMaps) {
                valuationHistory.add(AssetValuation.fromMap(valuationMap));
            }
        }
        asset.setValuationHistory(valuationHistory);

        return asset;
    }

    public enum AssetType {
        REAL_ESTATE,        // Immobilier
        BANK_ACCOUNT,       // Compte bancaire
        STOCK,              // Actions
        BOND,               // Obligations
        MUTUAL_FUND,        // Fonds commun de placement
        ETF,                // ETF
        CRYPTO,             // Cryptomonnaie
        PRECIOUS_METAL,     // Métaux précieux
        VEHICLE,            // Véhicule
        ART,                // Art et collection
        INSURANCE,          // Assurance-vie
        RETIREMENT,         // Plan de retraite
        LOAN,               // Prêt (passif)
        OTHER               // Autre
    }

    public enum AssetCategory {
        LIQUID,             // Actifs liquides
        INVESTMENT,         // Investissements
        TANGIBLE,           // Actifs tangibles
        RETIREMENT,         // Retraite
        INSURANCE,          // Assurance
        LIABILITY,          // Passifs/Dettes
        OTHER               // Autre
    }
}