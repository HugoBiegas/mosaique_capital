package com.master.mosaique_capital.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwoFactorService {

    private static final String APP_NAME = "Mosaique Capital";
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    /**
     * Génère une clé secrète pour l'authentification TOTP
     * @return la clé secrète en format Base32
     */
    public String generateSecretKey() {
        return secretGenerator.generate();
    }

    /**
     * Génère l'URL de données QR pour l'authentification TOTP
     * @param secretKey la clé secrète TOTP
     * @param userIdentifier l'identifiant de l'utilisateur (email ou uid)
     * @return l'URL de l'image QR code en Base64
     */
    public String generateQrCodeUrl(String secretKey, String userIdentifier) {
        QrData data = new QrData.Builder()
                .label(userIdentifier)
                .secret(secretKey)
                .issuer(APP_NAME)
                .algorithm(QrData.Algorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            return Utils.getDataUriForImage(
                    qrGenerator.generate(data),
                    qrGenerator.getImageMimeType()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR code: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    /**
     * Vérifie un code TOTP par rapport à une clé secrète
     * @param code le code à 6 chiffres fourni par l'utilisateur
     * @param secretKey la clé secrète TOTP
     * @return true si le code est valide, false sinon
     */
    public boolean verifyCode(String code, String secretKey) {
        return codeVerifier.isValidCode(secretKey, code);
    }
}