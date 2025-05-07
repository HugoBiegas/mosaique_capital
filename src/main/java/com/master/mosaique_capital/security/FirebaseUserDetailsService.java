package com.master.mosaique_capital.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.master.mosaique_capital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseUserDetailsService implements UserDetailsService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    @Override
    public FirebaseUserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        try {
            // Récupérer d'abord les informations d'authentification
            UserRecord userRecord = firebaseAuth.getUser(uid);

            // Ensuite récupérer les données complémentaires (rôles, etc.)
            return userRepository.findById(uid)
                    .map(user -> {
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                        // Ajouter les rôles comme autorités
                        Set<String> roles = user.getRoles();
                        if (roles != null) {
                            authorities.addAll(roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                    .collect(Collectors.toList()));
                        }

                        return new FirebaseUserDetails(
                                userRecord.getUid(),
                                userRecord.getEmail(),
                                userRecord.getDisplayName(),
                                userRecord.isEmailVerified(),
                                !userRecord.isDisabled(),
                                user.isTwoFactorEnabled(),
                                authorities
                        );
                    })
                    .orElseGet(() -> {
                        // Si l'utilisateur existe dans Firebase Auth mais pas dans notre DB
                        log.warn("User {} exists in Firebase Auth but not in our database. Creating with default role.", uid);
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                        return new FirebaseUserDetails(
                                userRecord.getUid(),
                                userRecord.getEmail(),
                                userRecord.getDisplayName(),
                                userRecord.isEmailVerified(),
                                !userRecord.isDisabled(),
                                false,
                                authorities
                        );
                    });

        } catch (FirebaseAuthException e) {
            log.error("Error getting user details from Firebase: {}", e.getMessage());
            throw new UsernameNotFoundException("User not found in Firebase: " + uid);
        }
    }
}