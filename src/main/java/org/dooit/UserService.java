package org.dooit;

import java.util.Scanner;

public class UserService {

    private static final Scanner scanner = new Scanner(System.in);
    private static UserManager userManager;

    static {
        try {
            userManager = new UserManager();
        } catch (Exception e) {
            System.err.println("Error initializing UserManager: " + e.getMessage());
            System.exit(1); // Exit if initialization fails
        }
    }

    public static User showLoginRegisterMenu() {
        User currentUser = null;
        while (currentUser == null) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            int choice = InputUtil.getNumericInput(1, 2);

            switch (choice) {
                case 1 -> currentUser = registerUser();
                case 2 -> currentUser = loginUser();
            }
        }
        return currentUser;
    }

    private static User registerUser() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();
        while (username.isEmpty()) {
            System.out.println("Username cannot be empty. Try again.");
            System.out.print("Enter your username: ");
            username = scanner.nextLine().trim();
        }

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();
        while (!isValidEmail(email)) {
            System.out.println("Invalid email format. Try again.");
            System.out.print("Enter your email: ");
            email = scanner.nextLine().trim();
        }

        System.out.print("Enter your password: ");
        String password = scanner.nextLine().trim();

        // For admin registration, prompt for an admin code
        System.out.print("Enter admin code (leave blank if not an admin): ");
        String adminCode = scanner.nextLine().trim();
        boolean isAdmin = "secretAdminCode".equals(adminCode); // Replace with your admin code

        // Create a new user
        User user = new User(username, email, password, isAdmin);

        // Register the user
        if (userManager.registerUser(user)) {
            System.out.println("Registered and logged in successfully!");
            return user;
        } else {
            System.out.println("Registration failed. Username or email may already be taken. Try again.");
            return null;
        }
    }

    private static User loginUser() {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        User user = userManager.loginUser(email, password);
        if (user != null) {
            System.out.println("Logged in successfully!");
            return user;
        } else {
            System.out.println("Login failed. Try again.");
            return null;
        }
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    public static UserManager getUserManager() {
        return userManager;
    }
}