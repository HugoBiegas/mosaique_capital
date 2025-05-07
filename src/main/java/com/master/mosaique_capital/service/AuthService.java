package com.master.mosaique_capital.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final FirebaseAuth firebaseAuth;

    public UserRecord registerUser(String email, String password, String displayName) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(displayName)
                .setEmailVerified(false)
                .setDisabled(false);

        return firebaseAuth.createUser(request);
    }

    public String verifyToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
        return decodedToken.getUid();
    }

    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
        return firebaseAuth.getUserByEmail(email);
    }

    public UserRecord getUserByUid(String uid) throws FirebaseAuthException {
        return firebaseAuth.getUser(uid);
    }

    public void setEmailVerified(String uid, boolean emailVerified) throws FirebaseAuthException {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setEmailVerified(emailVerified);
        firebaseAuth.updateUser(request);
    }

    public void resetPassword(String email) throws FirebaseAuthException {
        // Cette fonctionnalité nécessite généralement une intégration avec Firebase Admin SDK,
        // mais dans un environnement réel, cela se fait souvent via Firebase Client SDK côté frontend
        // Nous simulons ici la logique backend

        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);

            // Dans une vraie implémentation, vous devriez envoyer un email avec un lien de réinitialisation
            // via l'API Firebase ou votre propre système d'emails
            log.info("Envoi d'un email de réinitialisation pour l'utilisateur: {}", userRecord.getUid());

        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la réinitialisation du mot de passe: {}", e.getMessage());
            throw e;
        }
    }

    public void updatePassword(String uid, String newPassword) throws FirebaseAuthException {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setPassword(newPassword);
        firebaseAuth.updateUser(request);
    }

    public void revokeAllTokens(String uid) throws FirebaseAuthException {
        firebaseAuth.revokeRefreshTokens(uid);
    }
}