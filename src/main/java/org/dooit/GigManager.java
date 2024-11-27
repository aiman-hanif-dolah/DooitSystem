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
        if (gig != null) {
            CollectionReference gigs = db.collection(COLLECTION_NAME);
            DocumentReference gigDoc = gigs.document(gig.getGigId());
            ApiFuture<WriteResult> result = gigDoc.set(gig);
            try {
                result.get(); // Ensure the write completes successfully
                System.out.println("Gig created: " + gig);
                return true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error creating gig: " + e.getMessage());
                return false;
            }
        }
        return false; // Invalid gig object
    }

    public List<Gig> getAllGigs() {
        CollectionReference gigs = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = gigs.get();
        List<Gig> gigList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Gig gig = document.toObject(Gig.class);
                if (gig != null) {
                    gig.setGigId(document.getId());
                }
                // Debug statement to print out the gig details
                System.out.println("Retrieved gig: " + gig);
                gigList.add(gig);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving gigs: " + e.getMessage());
        }
        return gigList;
    }

    public Gig getGigById(String gigId) {
        if (gigId == null || gigId.isEmpty()) {
            return null;
        }
        DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(gigId);
        ApiFuture<DocumentSnapshot> future = gigDoc.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Gig gig = document.toObject(Gig.class);
                if (gig != null) {
                    gig.setGigId(document.getId());
                }
                System.out.println("Retrieved gig by ID: " + gig);
                return gig;
            } else {
                System.err.println("No gig found with gigId: " + gigId);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving gig: " + e.getMessage());
        }
        return null;
    }

    public boolean updateGig(Gig updatedGig) {
        if (updatedGig != null) {
            DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(updatedGig.getGigId());
            ApiFuture<WriteResult> result = gigDoc.set(updatedGig);
            try {
                result.get();
                System.out.println("Gig updated: " + updatedGig);
                return true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error updating gig: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public boolean deleteGig(String gigId) {
        if (gigId != null && !gigId.isEmpty()) {
            DocumentReference gigDoc = db.collection(COLLECTION_NAME).document(gigId);
            ApiFuture<WriteResult> writeResult = gigDoc.delete();
            try {
                writeResult.get();
                System.out.println("Gig deleted with ID: " + gigId);
                return true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error deleting gig: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}