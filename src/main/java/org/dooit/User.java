package org.dooit;

import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;
    private boolean admin;

    public User() {
        super();
    }

    public User(String username, String email, String password, boolean admin) {
        super();
        this.username = username;
        this.email = email;
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
            return password;
        }
    }

    @Exclude
    @Override
    public String getDetails() {
        return "Username: " + username + ", Email: " + email + ", Admin: " + admin;
    }

    // Add this stub setter to suppress Firestore warning
    @PropertyName("details")
    public void setDetails(String details) {
        // This setter does nothing, added to suppress Firestore warnings.
    }
}
