package com.master.mosaique_capital.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Configuration Firebase pour l'application
 */
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.database-url}")
    private String databaseUrl;

    @Value("${app.firebase.project-id}")
    private String projectId;

    @Value("${app.firebase.storage-bucket}")
    private String storageBucket;

    @Value("${app.firebase.credentials-path}")
    private Resource credentialsResource;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsResource.getInputStream()))
                    .setDatabaseUrl(databaseUrl)
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .build();

            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }
}
