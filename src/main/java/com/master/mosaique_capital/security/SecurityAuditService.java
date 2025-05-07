package com.master.mosaique_capital.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final FirebaseDatabase firebaseDatabase;
    private final ObjectMapper objectMapper;
    private static final String AUDIT_LOGS_REF = "audit_logs";

    /**
     * Enregistre une tentative d'accès à l'application
     */
    public void logAccessAttempt(String uid, String email, boolean success, HttpServletRequest request, String reason) {
        try {
            DatabaseReference logRef = firebaseDatabase.getReference(AUDIT_LOGS_REF).child("access").push();

            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("id", logRef.getKey());
            logEntry.put("timestamp", LocalDateTime.now().toString());
            logEntry.put("userId", uid);
            logEntry.put("email", email);
            logEntry.put("successful", success);
            logEntry.put("ipAddress", getClientIpAddress(request));
            logEntry.put("userAgent", request.getHeader("User-Agent"));
            logEntry.put("reason", reason);

            logRef.setValueAsync(logEntry);

            if (!success) {
                log.warn("Failed access attempt - User: {}, IP: {}, Reason: {}",
                        email, getClientIpAddress(request), reason);
            }
        } catch (Exception e) {
            log.error("Error logging access attempt: {}", e.getMessage());
        }
    }

    /**
     * Enregistre une action sensible dans le système
     */
    public void logSensitiveAction(String uid, String actionType, String resource, Map<String, Object> details,
                                   HttpServletRequest request) {
        try {
            DatabaseReference logRef = firebaseDatabase.getReference(AUDIT_LOGS_REF).child("actions").push();

            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("id", logRef.getKey());
            logEntry.put("timestamp", LocalDateTime.now().toString());
            logEntry.put("userId", uid);
            logEntry.put("actionType", actionType);
            logEntry.put("resource", resource);
            logEntry.put("ipAddress", getClientIpAddress(request));
            logEntry.put("userAgent", request.getHeader("User-Agent"));
            logEntry.put("details", details);

            logRef.setValueAsync(logEntry);

            log.info("Sensitive action logged - User: {}, Action: {}, Resource: {}",
                    uid, actionType, resource);
        } catch (Exception e) {
            log.error("Error logging sensitive action: {}", e.getMessage());
        }
    }

    /**
     * Enregistre une tentative d'attaque potentielle
     */
    public void logSecurityEvent(String eventType, String description, HttpServletRequest request,
                                 Map<String, Object> details) {
        try {
            DatabaseReference logRef = firebaseDatabase.getReference(AUDIT_LOGS_REF).child("security_events").push();

            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("id", logRef.getKey());
            logEntry.put("timestamp", LocalDateTime.now().toString());
            logEntry.put("eventType", eventType);
            logEntry.put("description", description);
            logEntry.put("ipAddress", getClientIpAddress(request));
            logEntry.put("userAgent", request.getHeader("User-Agent"));
            logEntry.put("requestUri", request.getRequestURI());
            logEntry.put("method", request.getMethod());
            logEntry.put("details", details);

            logRef.setValueAsync(logEntry);

            log.warn("Security event detected - Type: {}, IP: {}, Description: {}",
                    eventType, getClientIpAddress(request), description);
        } catch (Exception e) {
            log.error("Error logging security event: {}", e.getMessage());
        }
    }

    /**
     * Extrait l'adresse IP réelle du client, en tenant compte des proxys
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}