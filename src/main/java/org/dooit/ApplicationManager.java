package org.dooit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ApplicationManager {
    private static final String COLLECTION_NAME = "applications";
    private Firestore db;

    // Constructor to initialize Firestore
    public ApplicationManager() throws IOException {
        this.db = FirebaseConfig.getFirestore();
    }

    public boolean createApplication(Application application) {
        if (application != null) {
            CollectionReference applications = db.collection(COLLECTION_NAME);
            DocumentReference appDoc = applications.document(application.getApplicationId());
            ApiFuture<WriteResult> result = appDoc.set(application);
            try {
                result.get(); // Ensure the write completes successfully
                System.out.println("Application created: " + application);
                return true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error creating application: " + e.getMessage());
                return false;
            }
        }
        return false; // Invalid application object
    }

    public List<Application> getApplicationsByUsername(String username) {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.whereEqualTo("username", username).get();
        List<Application> applicationList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Application application = document.toObject(Application.class);
                applicationList.add(application);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving applications: " + e.getMessage());
        }
        return applicationList;
    }
}