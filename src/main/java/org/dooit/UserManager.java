package org.dooit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UserManager {
    private static final String COLLECTION_NAME = "users";
    private final Firestore db;

    // Constructor to initialize Firestore
    public UserManager() throws IOException {
        this.db = FirebaseConfig.getFirestore();
    }

    // Register a new user
    public boolean registerUser(User user) {
        if (userExists(user.getEmail())) {
            System.err.println("User already exists with email: " + user.getEmail());
            return false;
        }
        if (userExistsByUsername(user.getUsername())) {
            System.err.println("Username already taken: " + user.getUsername());
            return false;
        }

        // Use the username as the document name
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

    // Check if a user exists by email
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

    // Check if a user exists by username
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

    // Login user by verifying email and password
    public User loginUser(String email, String password) {
        try {
            CollectionReference users = db.collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> query = users.whereEqualTo("email", email).get();
            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                User user = querySnapshot.getDocuments().get(0).toObject(User.class);

                // Hash the entered password
                String hashedPassword = User.hashPassword(password.trim());

                // Compare the hashed input with the stored hashed password
                if (user.getPassword().equals(hashedPassword)) {
                    return user; // Login successful
                } else {
                    System.err.println("Invalid password for email: " + email);
                }
            } else {
                System.err.println("No user found with email: " + email);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error logging in user: " + e.getMessage());
        }
        return null; // Login failed
    }

    // Other methods (getUserByUsername, updateUser, deleteUser) can be added as needed
}