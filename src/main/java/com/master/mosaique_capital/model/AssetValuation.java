package com.master.mosaique_capital.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetValuation {
    private String id;
    private String assetId;
    private BigDecimal value;
    private LocalDateTime valuationDate;
    private String currency;
    private String source; // Manuel, API, etc.

    // Méthodes pour faciliter la conversion depuis/vers Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("assetId", assetId);
        map.put("value", value != null ? value.toString() : null);
        map.put("valuationDate", valuationDate != null ? valuationDate.toString() : null);
        map.put("currency", currency);
        map.put("source", source);
        return map;
    }

    public static AssetValuation fromMap(Map<String, Object> map) {
        AssetValuation valuation = new AssetValuation();
        valuation.setId((String) map.get("id"));
        valuation.setAssetId((String) map.get("assetId"));

        String valueStr = (String) map.get("value");
        if (valueStr != null) {
            valuation.setValue(new BigDecimal(valueStr));
        }

        String valuationDateStr = (String) map.get("valuationDate");
        if (valuationDateStr != null) {
            valuation.setValuationDate(LocalDateTime.parse(valuationDateStr));
        }

        valuation.setCurrency((String) map.get("currency"));
        valuation.setSource((String) map.get("source"));

        return valuation;
    }
}