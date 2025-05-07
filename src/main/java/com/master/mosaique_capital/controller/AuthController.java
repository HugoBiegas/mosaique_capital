package com.master.mosaique_capital.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.master.mosaique_capital.dto.LoginRequestDto;
import com.master.mosaique_capital.dto.RegisterRequestDto;
import com.master.mosaique_capital.dto.VerifyTwoFactorDto;
import com.master.mosaique_capital.model.User;
import com.master.mosaique_capital.service.AuthService;
import com.master.mosaique_capital.service.TwoFactorService;
import com.master.mosaique_capital.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TwoFactorService twoFactorService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        try {
            UserRecord userRecord = authService.registerUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getDisplayName());

            // Créer l'utilisateur dans notre base aussi
            User user = User.builder()
                    .uid(userRecord.getUid())
                    .email(userRecord.getEmail())
                    .displayName(userRecord.getDisplayName())
                    .emailVerified(userRecord.isEmailVerified())
                    .disabled(userRecord.isDisabled())
                    .twoFactorEnabled(false)
                    .roles(Set.of("USER"))
                    .build();

            userService.createUser(user).get();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur enregistré avec succès");
            response.put("uid", userRecord.getUid());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FirebaseAuthException e) {
            log.error("Erreur d'enregistrement Firebase: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur d'enregistrement: " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la création du profil utilisateur: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur");
        }
    }

    @PostMapping("/setup-2fa")
    public ResponseEntity<Map<String, String>> setupTwoFactor(@RequestHeader("Authorization") String token) {
        try {
            String uid = authService.verifyToken(token.replace("Bearer ", ""));
            String secretKey = twoFactorService.generateSecretKey();
            String qrCodeUrl = twoFactorService.generateQrCodeUrl(secretKey, uid);

            User user = userService.getUserById(uid).get()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

            user.setTotpSecret(secretKey);
            userService.updateUser(user).get();

            Map<String, String> response = new HashMap<>();
            response.put("secretKey", secretKey);
            response.put("qrCodeUrl", qrCodeUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la configuration 2FA: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<Map<String, Object>> verifyAndEnableTwoFactor(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody VerifyTwoFactorDto verifyRequest) {
        try {
            String uid = authService.verifyToken(token.replace("Bearer ", ""));
            User user = userService.getUserById(uid).get()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

            boolean isCodeValid = twoFactorService.verifyCode(verifyRequest.getCode(), user.getTotpSecret());

            if (isCodeValid) {
                user.setTwoFactorEnabled(true);
                userService.updateUser(user).get();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Authentification à deux facteurs activée avec succès");

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Code invalide");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("Erreur lors de la vérification 2FA: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur: " + e.getMessage());
        }
    }
}