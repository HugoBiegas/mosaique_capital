package com.master.mosaique_capital.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class XssConfig {

    private final HandlerMappingIntrospector mvcHandlerMappingIntrospector;

    /**
     * Configure les en-têtes de sécurité pour prévenir les attaques XSS
     */
    @Bean
    public HeaderWriter securityHeadersWriter() {
        List<HeaderWriter> headerWriters = new ArrayList<>();

        // Content-Security-Policy
        headerWriters.add(new StaticHeadersWriter("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' https://storage.googleapis.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "img-src 'self' data: https://storage.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "connect-src 'self' https://*.googleapis.com https://*.firebaseio.com; " +
                        "frame-src 'self'; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self';"));

        // X-Content-Type-Options
        headerWriters.add(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"));

        // X-XSS-Protection
        headerWriters.add(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"));

        // X-Frame-Options
        headerWriters.add(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.DENY));

        // Referrer-Policy
        headerWriters.add(new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin"));

        // Permissions-Policy
        headerWriters.add(new StaticHeadersWriter("Permissions-Policy",
                "geolocation=(), microphone=(), camera=()"));

        // Strict-Transport-Security (HSTS)
        headerWriters.add(new DelegatingRequestMatcherHeaderWriter(
                new OrRequestMatcher(
                        new AntPathRequestMatcher("/**")
                ),
                new StaticHeadersWriter("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains")
        ));

        return (request, response) -> {
            for (HeaderWriter writer : headerWriters) {
                writer.writeHeaders(request, response);
            }
        };
    }

    /**
     * Configure un filtre de nettoyage XSS
     */
    @Bean
    public RequestMatcher xssProtectionMatcher() {
        List<RequestMatcher> matchers = new ArrayList<>();

        // Protéger toutes les requêtes POST, PUT, PATCH
        matchers.add(new MvcRequestMatcher(mvcHandlerMappingIntrospector, "/**"));

        return new OrRequestMatcher(matchers);
    }
}