package com.master.mosaique_capital.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;
    private String email;
    private String displayName;
    private String phoneNumber;
    private boolean emailVerified;
    private boolean twoFactorEnabled;

    @JsonIgnore
    private String totpSecret; // Secret pour l'authentification 2FA

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean disabled;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Map<String, String> preferences = new HashMap<>();

    // Pour la conversion en/depuis Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("displayName", displayName);
        map.put("phoneNumber", phoneNumber);
        map.put("emailVerified", emailVerified);
        map.put("twoFactorEnabled", twoFactorEnabled);
        map.put("totpSecret", totpSecret);
        map.put("createdAt", createdAt != null ? createdAt.toString() : null);
        map.put("updatedAt", updatedAt != null ? updatedAt.toString() : null);
        map.put("disabled", disabled);
        map.put("roles", roles);
        map.put("preferences", preferences);
        return map;
    }

    public static User fromMap(Map<String, Object> map) {
        User user = new User();
        user.setUid((String) map.get("uid"));
        user.setEmail((String) map.get("email"));
        user.setDisplayName((String) map.get("displayName"));
        user.setPhoneNumber((String) map.get("phoneNumber"));

        if (map.get("emailVerified") != null) {
            user.setEmailVerified((Boolean) map.get("emailVerified"));
        }

        if (map.get("twoFactorEnabled") != null) {
            user.setTwoFactorEnabled((Boolean) map.get("twoFactorEnabled"));
        }

        user.setTotpSecret((String) map.get("totpSecret"));

        String createdAtStr = (String) map.get("createdAt");
        if (createdAtStr != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAtStr));
        }

        String updatedAtStr = (String) map.get("updatedAt");
        if (updatedAtStr != null) {
            user.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
        }

        if (map.get("disabled") != null) {
            user.setDisabled((Boolean) map.get("disabled"));
        }

        if (map.get("roles") != null) {
            @SuppressWarnings("unchecked")
            Set<String> roles = new HashSet<>((Set<String>) map.get("roles"));
            user.setRoles(roles);
        }

        if (map.get("preferences") != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> preferences = new HashMap<>((Map<String, String>) map.get("preferences"));
            user.setPreferences(preferences);
        }

        return user;
    }
}