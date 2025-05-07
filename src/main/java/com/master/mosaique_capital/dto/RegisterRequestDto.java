package com.master.mosaique_capital.dto;

import com.master.mosaique_capital.model.Asset;
import com.master.mosaique_capital.model.AssetValuation;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// DTO pour l'enregistrement d'un utilisateur
@Data
public class RegisterRequestDto {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le nom d'affichage est obligatoire")
    private String displayName;
}
