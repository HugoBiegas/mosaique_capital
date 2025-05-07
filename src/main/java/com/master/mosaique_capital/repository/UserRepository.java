package com.master.mosaique_capital.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.mosaique_capital.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {

    private final FirebaseDatabase firebaseDatabase;
    private static final String USERS_REF = "users";

    public CompletableFuture<User> save(User user) {
        CompletableFuture<User> future = new CompletableFuture<>();
        DatabaseReference userRef;

        if (user.getUid() == null) {
            userRef = firebaseDatabase.getReference(USERS_REF).push();
            user.setUid(userRef.getKey());
        } else {
            userRef = firebaseDatabase.getReference(USERS_REF).child(user.getUid());
        }

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        user.setUpdatedAt(LocalDateTime.now());

        Map<String, Object> userValues = user.toMap();
        userRef.setValueAsync(userValues)
                .addOnSuccessListener(aVoid -> future.complete(user))
                .addOnFailureListener(e -> {
                    log.error("Error saving user to Firebase: {}", e.getMessage());
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<Optional<User>> findById(String uid) {
        CompletableFuture<Optional<User>> future = new CompletableFuture<>();

        firebaseDatabase.getReference(USERS_REF).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userMap = (Map<String, Object>) dataSnapshot.getValue();
                        User user = User.fromMap(userMap);
                        future.complete(Optional.of(user));
                    } catch (Exception e) {
                        log.error("Error parsing user data: {}", e.getMessage());
                        future.completeExceptionally(e);
                    }
                } else {
                    future.complete(Optional.empty());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log.error("Firebase database error: {}", databaseError.getMessage());
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> deleteById(String uid) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        firebaseDatabase.getReference(USERS_REF).child(uid).removeValueAsync()
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> {
                    log.error("Error deleting user from Firebase: {}", e.getMessage());
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<Optional<User>> findByEmail(String email) {
        CompletableFuture<Optional<User>> future = new CompletableFuture<>();

        firebaseDatabase.getReference(USERS_REF)
                .orderByChild("email")
                .equalTo(email)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                            try {
                                DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                                @SuppressWarnings("unchecked")
                                Map<String, Object> userMap = (Map<String, Object>) userSnapshot.getValue();
                                User user = User.fromMap(userMap);
                                future.complete(Optional.of(user));
                            } catch (Exception e) {
                                log.error("Error parsing user data: {}", e.getMessage());
                                future.completeExceptionally(e);
                            }
                        } else {
                            future.complete(Optional.empty());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        log.error("Firebase database error: {}", databaseError.getMessage());
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }
}