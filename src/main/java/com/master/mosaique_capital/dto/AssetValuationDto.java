package com.master.mosaique_capital.dto;

import com.master.mosaique_capital.model.AssetValuation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO pour l'ajout d'une valorisation
@Data
public class AssetValuationDto {
    @NotNull(message = "La valeur est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "La valeur doit être positive ou nulle")
    private BigDecimal value;

    private LocalDateTime valuationDate;

    @NotBlank(message = "La devise est obligatoire")
    private String currency;

    private String source;

    // Méthode de conversion en entité
    public AssetValuation toAssetValuation() {
        return AssetValuation.builder()
                .value(this.value)
                .valuationDate(this.valuationDate != null ? this.valuationDate : LocalDateTime.now())
                .currency(this.currency)
                .source(this.source != null ? this.source : "Manual")
                .build();
    }
}