package com.master.mosaique_capital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// DTO pour la vérification du code 2FA
@Data
public class VerifyTwoFactorDto {
    @NotBlank(message = "Le code est obligatoire")
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code doit être composé de 6 chiffres")
    private String code;
}
