package com.master.mosaique_capital.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.master.mosaique_capital.model.User;
import com.master.mosaique_capital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    public CompletableFuture<User> createUser(User user) {
        return userRepository.save(user);
    }

    public CompletableFuture<Optional<User>> getUserById(String uid) {
        return userRepository.findById(uid);
    }

    public CompletableFuture<User> updateUser(User user) {
        return userRepository.save(user);
    }

    public CompletableFuture<Void> deleteUser(String uid) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Supprimer de Firebase Auth
            firebaseAuth.deleteUser(uid);

            // Supprimer de notre base de données
            userRepository.deleteById(uid)
                    .thenAccept(v -> future.complete(null))
                    .exceptionally(e -> {
                        future.completeExceptionally(e);
                        return null;
                    });

        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la suppression de l'utilisateur dans Firebase Auth: {}", e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<Optional<User>> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public CompletableFuture<User> disableUser(String uid, boolean disabled) {
        CompletableFuture<User> future = new CompletableFuture<>();

        try {
            // Mettre à jour dans Firebase Auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setDisabled(disabled);
            firebaseAuth.updateUser(request);

            // Mettre à jour dans notre base de données
            userRepository.findById(uid)
                    .thenCompose(optionalUser -> {
                        if (optionalUser.isPresent()) {
                            User user = optionalUser.get();
                            user.setDisabled(disabled);
                            return userRepository.save(user);
                        } else {
                            CompletableFuture<User> errorFuture = new CompletableFuture<>();
                            errorFuture.completeExceptionally(new RuntimeException("Utilisateur non trouvé"));
                            return errorFuture;
                        }
                    })
                    .thenAccept(future::complete)
                    .exceptionally(e -> {
                        future.completeExceptionally(e);
                        return null;
                    });

        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la mise à jour du statut de l'utilisateur dans Firebase Auth: {}", e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<Boolean> checkEmailExists(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            firebaseAuth.getUserByEmail(email);
            future.complete(true);
        } catch (FirebaseAuthException e) {
            if (e.getErrorCode().equals("user-not-found")) {
                future.complete(false);
            } else {
                log.error("Erreur lors de la vérification de l'email: {}", e.getMessage());
                future.completeExceptionally(e);
            }
        }

        return future;
    }
}