package org.dooit;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {
    private static Firestore db;

    // Initialize Firebase connection
    public static void initializeFirebase() throws IOException {
        if (db == null) {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://dooitsystem.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
        }
    }

    // Get Firestore instance
    public static Firestore getFirestore() throws IOException {
        if (db == null) {
            initializeFirebase();
        }
        return db;
    }
}

// File name: FirebaseConfig.java

// Usage example:
// Firestore db = FirebaseConfig.getFirestore();
