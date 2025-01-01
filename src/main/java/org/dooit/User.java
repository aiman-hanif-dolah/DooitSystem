package org.dooit;

import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User extends BaseEntity {
    private String username;
    private String email;
    private String password; // Store hashed password
    private boolean admin;

    /**
     * No-argument constructor required by Firestore.
     * This allows Firestore to instantiate the class via reflection.
     */
    public User() {
        super();
        // no-argument constructor body can be empty
    }

    /**
     * Parameterized constructor.
     * Note: This hashes the 'password' argument before storing it in 'this.password'.
     */
    public User(String username, String email, String password, boolean admin) {
        super();
        this.username = username;
        this.email = email;
        // We hash here so it gets stored as a hashed string in Firestore.
        this.password = hashPassword(password);
        this.admin = admin;
    }

    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("password")
    public String getPassword() {
        return password;
    }

    @PropertyName("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @PropertyName("admin")
    public boolean isAdmin() {
        return admin;
    }

    @PropertyName("admin")
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Hashes the input password using SHA-256.
     * Marked @Exclude so Firestore won't try to serialize or deserialize it.
     */
    @Exclude
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to returning the original password (not recommended for security)
            return password;
        }
    }

    @Override
    public String getDetails() {
        return "Username: " + username + ", Email: " + email + ", Admin: " + admin;
    }
}
