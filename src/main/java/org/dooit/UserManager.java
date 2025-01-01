package org.dooit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UserManager {
    private static final String COLLECTION_NAME = "users";
    private Firestore db;

    // Constructor to initialize Firestore
    public UserManager() throws IOException {
        this.db = FirebaseConfig.getFirestore();
    }

    // Register a user (ensuring email normalization)
    public boolean registerUser(User user) {
        // Normalize email to lowercase
        user.setEmail(user.getEmail().toLowerCase());

        if (userExists(user.getEmail())) {
            System.err.println("User already exists with email: " + user.getEmail());
            return false;
        }
        if (userExistsByUsername(user.getUsername())) {
            System.err.println("Username already taken: " + user.getUsername());
            return false;
        }

        DocumentReference userDoc = db.collection(COLLECTION_NAME).document(user.getUsername());
        ApiFuture<WriteResult> result = userDoc.set(user);
        try {
            result.get(); // Ensure the write completes successfully
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Login a user by email and password
    public User loginUser(String email, String password) {
        email = email.toLowerCase(); // Normalize email for case-insensitive checks
        CollectionReference users = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = users.whereEqualTo("email", email).get();

        try {
            QuerySnapshot querySnapshot = query.get();
            if (!querySnapshot.isEmpty()) {
                User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                if (user != null && User.hashPassword(password).equals(user.getPassword())) {
                    return user; // Login successful
                } else {
                    System.err.println("Invalid password for email: " + email);
                }
            } else {
                System.err.println("No user found with email: " + email);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null; // Login failed
    }

    // Private method to check if a user exists by email
    private boolean userExists(String email) {
        CollectionReference users = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = users.whereEqualTo("email", email).get();
        try {
            QuerySnapshot querySnapshot = query.get();
            return !querySnapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }

    // Private method to check if a user exists by username
    private boolean userExistsByUsername(String username) {
        DocumentReference userDoc = db.collection(COLLECTION_NAME).document(username);
        ApiFuture<DocumentSnapshot> future = userDoc.get();
        try {
            DocumentSnapshot document = future.get();
            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error checking if username exists: " + e.getMessage());
            return false;
        }
    }

    // Method to get the total number of users
    public int getUserCount() {
        CollectionReference users = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = users.get();
        try {
            QuerySnapshot querySnapshot = query.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting user count: " + e.getMessage());
            return 0;
        }
    }

    // Public method to check if a user exists by email
    public boolean doesUserExistByEmail(String email) {
        return userExists(email.toLowerCase()); // Normalize email to lowercase
    }

    // Public method to check if a user exists by username
    public boolean doesUserExistByUsername(String username) {
        return userExistsByUsername(username);
    }
}
