package com.master.mosaique_capital.dto;

import com.master.mosaique_capital.model.Asset;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// DTO pour la création/mise à jour d'un actif
@Data
public class AssetDto {
    private String id;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String description;

    @NotNull(message = "Le type d'actif est obligatoire")
    private Asset.AssetType type;

    @NotNull(message = "La catégorie d'actif est obligatoire")
    private Asset.AssetCategory category;

    @NotBlank(message = "La devise est obligatoire")
    private String currency;

    @NotNull(message = "La valeur actuelle est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "La valeur doit être positive ou nulle")
    private BigDecimal currentValue;

    @DecimalMin(value = "0.0", inclusive = true, message = "La valeur d'acquisition doit être positive ou nulle")
    private BigDecimal acquisitionValue;

    private LocalDateTime acquisitionDate;

    private Map<String, String> attributes = new HashMap<>();

    // Méthode de conversion en entité
    public Asset toAsset() {
        return Asset.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .category(this.category)
                .currency(this.currency)
                .currentValue(this.currentValue)
                .acquisitionValue(this.acquisitionValue)
                .acquisitionDate(this.acquisitionDate)
                .attributes(this.attributes)
                .build();
    }
}
