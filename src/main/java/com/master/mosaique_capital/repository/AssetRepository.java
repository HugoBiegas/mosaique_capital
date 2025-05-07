package com.master.mosaique_capital.repository;

import com.google.firebase.database.*;
import com.master.mosaique_capital.model.Asset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AssetRepository {

    private final FirebaseDatabase firebaseDatabase;
    private static final String ASSETS_REF = "assets";

    public CompletableFuture<Asset> save(Asset asset) {
        CompletableFuture<Asset> future = new CompletableFuture<>();
        DatabaseReference assetRef;

        if (asset.getId() == null) {
            assetRef = firebaseDatabase.getReference(ASSETS_REF).push();
            asset.setId(assetRef.getKey());
        } else {
            assetRef = firebaseDatabase.getReference(ASSETS_REF).child(asset.getId());
        }

        asset.setLastUpdateDate(LocalDateTime.now());

        Map<String, Object> assetValues = asset.toMap();
        assetRef.setValueAsync(assetValues)
                .addOnSuccessListener(aVoid -> future.complete(asset))
                .addOnFailureListener(e -> {
                    log.error("Error saving asset to Firebase: {}", e.getMessage());
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<Optional<Asset>> findById(String id) {
        CompletableFuture<Optional<Asset>> future = new CompletableFuture<>();

        firebaseDatabase.getReference(ASSETS_REF).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> assetMap = (Map<String, Object>) dataSnapshot.getValue();
                        Asset asset = Asset.fromMap(assetMap);
                        future.complete(Optional.of(asset));
                    } catch (Exception e) {
                        log.error("Error parsing asset data: {}", e.getMessage());
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

    public CompletableFuture<List<Asset>> findByUserId(String userId) {
        CompletableFuture<List<Asset>> future = new CompletableFuture<>();

        firebaseDatabase.getReference(ASSETS_REF)
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Asset> assets = new ArrayList<>();

                        if (dataSnapshot.exists()) {
                            try {
                                for (DataSnapshot assetSnapshot : dataSnapshot.getChildren()) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> assetMap = (Map<String, Object>) assetSnapshot.getValue();
                                    Asset asset = Asset.fromMap(assetMap);
                                    assets.add(asset);
                                }
                            } catch (Exception e) {
                                log.error("Error parsing assets data: {}", e.getMessage());
                                future.completeExceptionally(e);
                                return;
                            }
                        }

                        future.complete(assets);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        log.error("Firebase database error: {}", databaseError.getMessage());
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<List<Asset>> findByUserIdAndCategory(String userId, Asset.AssetCategory category) {
        CompletableFuture<List<Asset>> future = new CompletableFuture<>();

        findByUserId(userId).thenApply(assets -> {
                    List<Asset> filteredAssets = new ArrayList<>();
                    for (Asset asset : assets) {
                        if (asset.getCategory() == category) {
                            filteredAssets.add(asset);
                        }
                    }
                    return filteredAssets;
                }).thenAccept(future::complete)
                .exceptionally(e -> {
                    future.completeExceptionally(e);
                    return null;
                });

        return future;
    }

    public CompletableFuture<List<Asset>> findByUserIdAndType(String userId, Asset.AssetType type) {
        CompletableFuture<List<Asset>> future = new CompletableFuture<>();

        findByUserId(userId).thenApply(assets -> {
                    List<Asset> filteredAssets = new ArrayList<>();
                    for (Asset asset : assets) {
                        if (asset.getType() == type) {
                            filteredAssets.add(asset);
                        }
                    }
                    return filteredAssets;
                }).thenAccept(future::complete)
                .exceptionally(e -> {
                    future.completeExceptionally(e);
                    return null;
                });

        return future;
    }

    public CompletableFuture<Void> deleteById(String id) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        firebaseDatabase.getReference(ASSETS_REF).child(id).removeValueAsync()
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> {
                    log.error("Error deleting asset from Firebase: {}", e.getMessage());
                    future.completeExceptionally(e);
                });

        return future;
    }
}