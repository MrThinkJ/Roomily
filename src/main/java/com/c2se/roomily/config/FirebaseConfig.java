package com.c2se.roomily.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Value("${app.resource.config-location}")
    private String configLocation;

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("roomily-b9645")
                .build();
        return FirebaseApp.initializeApp(options);
    }
    @Bean
    GoogleCredentials googleCredentials() throws IOException {
        FileInputStream serviceAccount;
        try {
            serviceAccount = new FileInputStream(configLocation+"/firebase_key.json");
        } catch (FileNotFoundException e) {
            serviceAccount = new FileInputStream(configLocation+"/firebase_key.json");
        }
        return GoogleCredentials.fromStream(serviceAccount);
    }
    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) throws IOException {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

//    @Bean
//    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) throws IOException {
//        return FirebaseDatabase.getInstance(firebaseApp);
//    }
}
