package com.master.mosaique_capital.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Doit s'exécuter après le filtre d'authentification
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditFilter extends OncePerRequestFilter {

    private final SecurityAuditService securityAuditService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Chemins sensibles nécessitant un audit
    private static final String[] SENSITIVE_PATHS = {
            "/api/auth/**",
            "/api/assets/**",
            "/api/patrimony/**",
            "/api/users/**"
    };

    // Seuils de réponse HTTP à journaliser
    private static final int ERROR_THRESHOLD = 400;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Capturer l'heure de début pour calculer le temps de réponse
        long startTime = System.currentTimeMillis();

        // Vérifier si c'est un chemin sensible à auditer
        boolean isSensitivePath = isSensitivePath(request.getRequestURI());

        // Wrapper la réponse pour capturer le code de statut
        StatusCapturingResponseWrapper responseWrapper = new StatusCapturingResponseWrapper(response);

        try {
            // Continuer la chaîne de filtres
            filterChain.doFilter(request, responseWrapper);
        } finally {
            // Calculer le temps de réponse
            long responseTime = System.currentTimeMillis() - startTime;

            // Récupérer le statut HTTP
            int status = responseWrapper.getStatus();

            // Récupérer l'utilisateur authentifié, s'il existe
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;
            String email = null;

            if (authentication != null && authentication.getPrincipal() instanceof FirebaseUserDetails) {
                FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
                userId = userDetails.getUid();
                email = userDetails.getEmail();
            }

            // Journaliser les requêtes sensibles ou avec erreur
            if (isSensitivePath || status >= ERROR_THRESHOLD) {
                Map<String, Object> details = new HashMap<>();
                details.put("method", request.getMethod());
                details.put("uri", request.getRequestURI());
                details.put("query", request.getQueryString());
                details.put("status", status);
                details.put("responseTime", responseTime);

                if (status >= ERROR_THRESHOLD) {
                    // Journaliser les erreurs comme événements de sécurité
                    securityAuditService.logSecurityEvent(
                            "HTTP_ERROR",
                            "HTTP error response: " + status,
                            request,
                            details
                    );
                } else if (isSensitivePath && userId != null) {
                    // Journaliser les accès sensibles pour les utilisateurs authentifiés
                    securityAuditService.logSensitiveAction(
                            userId,
                            request.getMethod(),
                            request.getRequestURI(),
                            details,
                            request
                    );
                }
            }

            // Journaliser spécifiquement les tentatives d'authentification
            if (request.getRequestURI().contains("/api/auth/") && userId != null) {
                boolean isSuccess = status >= 200 && status < 300;
                securityAuditService.logAccessAttempt(
                        userId,
                        email,
                        isSuccess,
                        request,
                        isSuccess ? "Successful authentication" : "Failed authentication"
                );
            }
        }
    }

    private boolean isSensitivePath(String uri) {
        for (String pattern : SENSITIVE_PATHS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    // Classe interne pour capturer le statut HTTP
    private static class StatusCapturingResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private int status = 200;

        public StatusCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int status) {
            super.setStatus(status);
            this.status = status;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.status = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.status = sc;
        }

        public int getStatus() {
            return status;
        }
    }
}