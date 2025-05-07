package com.master.mosaique_capital.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class FirebaseUserDetails implements UserDetails {

    private final String uid;
    private final String email;
    private final String displayName;
    private final boolean emailVerified;
    private final boolean enabled;
    private final boolean twoFactorEnabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public FirebaseUserDetails(
            String uid,
            String email,
            String displayName,
            boolean emailVerified,
            boolean enabled,
            boolean twoFactorEnabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.emailVerified = emailVerified;
        this.enabled = enabled;
        this.twoFactorEnabled = twoFactorEnabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Firebase gère les mots de passe, on retourne null
        return null;
    }

    @Override
    public String getUsername() {
        // On utilise l'uid comme identifiant principal
        return uid;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}