package org.dooit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GigManager {
    private static final String COLLECTION_NAME = "gigs";
    private Firestore db;

    // Constructor to initialize Firestore
    public GigManager() throws IOException {
        this.db = FirebaseConfig.getFirestore();
    }

    public boolean createGig(Gig gig) {
        DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(gig.getGigId());

        ApiFuture<WriteResult> result = gigDoc.set(gig);
        try {
            result.get(); // Ensure the write completes successfully
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error creating gig: " + e.getMessage());
            return false;
        }
    }

    public List<Gig> getAvailableGigs() {
        CollectionReference gigs = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = gigs.whereEqualTo("available", true).get();
        List<Gig> gigList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Gig gig = document.toObject(Gig.class);
                gigList.add(gig);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting available gigs: " + e.getMessage());
        }
        return gigList;
    }

    public List<Gig> getAllGigs() {
        CollectionReference gigs = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = gigs.get();
        List<Gig> gigList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Gig gig = document.toObject(Gig.class);
                gigList.add(gig);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting all gigs: " + e.getMessage());
        }
        return gigList;
    }

    public List<Gig> getGigsByUser(String username) {
        CollectionReference gigs = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = gigs.whereEqualTo("postedBy", username).get();
        List<Gig> gigList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Gig gig = document.toObject(Gig.class);
                gigList.add(gig);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting user's gigs: " + e.getMessage());
        }
        return gigList;
    }

    public Gig getGigById(String gigId) {
        DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(gigId);
        ApiFuture<DocumentSnapshot> future = gigDoc.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Gig.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting gig by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updateGig(Gig gig) {
        DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(gig.getGigId());
        ApiFuture<WriteResult> future = gigDoc.set(gig);
        try {
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating gig: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGig(String gigId) {
        DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(gigId);
        ApiFuture<WriteResult> future = gigDoc.delete();
        try {
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting gig: " + e.getMessage());
            return false;
        }
    }

    // Method to get the total number of gigs
    public int getGigCount() {
        CollectionReference gigs = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = gigs.get();
        try {
            QuerySnapshot querySnapshot = query.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting gig count: " + e.getMessage());
            return 0;
        }
    }

    public List<Gig> getGigsByUsername(String username) {
        CollectionReference gigs = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = gigs.whereEqualTo("postedBy", username).get();
        List<Gig> gigList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Gig gig = document.toObject(Gig.class);
                if (gig != null) {
                    gig.setGigId(document.getId());
                }
                gigList.add(gig);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving gigs by username: " + e.getMessage());
        }
        return gigList;
    }
}