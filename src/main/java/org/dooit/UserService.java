package org.dooit;

import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

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
            // Display menu in tabular format
            System.out.println("+---------------------------------------------+");
            System.out.println("|              LOGIN OR REGISTER              |");
            System.out.println("+-----+---------------------------------------+");
            System.out.format("| %-3s | %-37s |%n", "No.", "Option");
            System.out.println("+-----+---------------------------------------+");
            System.out.format("| %-3s | %-37s |%n", "1", "Register");
            System.out.format("| %-3s | %-37s |%n", "2", "Login");
            System.out.println("+-----+---------------------------------------+");

            // Get user choice
            System.out.print("Enter your choice (or 0 to exit): ");
            int choice = InputUtil.getNumericInput(0, 2);

            // Handle the user choice
            if (choice == 0) {
                System.out.println("Exiting to main menu...");
                return null;
            }

            switch (choice) {
                case 1 -> currentUser = registerUser();
                case 2 -> currentUser = loginUser();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        return currentUser;
    }
    private static User registerUser() {
        System.out.println("[+---------------------------------------------+");
        System.out.println("|                REGISTER USER                |");
        System.out.println("+-----+---------------------------------------+");
        System.out.format("| %-3s | %-37s |%n", "No.", "Field");
        System.out.println("+-----+---------------------------------------+");
        System.out.format("| %-3s | %-37s |%n", "1", "Username");
        System.out.format("| %-3s | %-37s |%n", "2", "Email");
        System.out.format("| %-3s | %-37s |%n", "3", "Password");
        System.out.format("| %-3s | %-37s |%n", "0", "Exit to Menu");
        System.out.println("+-----+---------------------------------------+");
        System.out.println("Enter the details below (enter '0' anytime to return to main menu).\033[0m");

        // Username Input and Validation
        String username = null;
        while (username == null) {
            System.out.print("\033[32m\nEnter your username: \033[0m");
            username = scanner.nextLine().trim();
            if (username.equals("0")) {
                System.out.println("\033[32mReturning to main menu...\033[0m");
                return null;
            }
            if (username.isEmpty()) {
                System.out.println("\033[32mUsername cannot be empty. Try again.\033[0m");
                username = null;
            } else if (userManager.doesUserExistByUsername(username)) {
                System.out.println("\033[32mUsername already taken: " + username + "\033[0m");
                username = null;
            }
        }

        // Email Input and Validation
        String email = null;
        while (email == null) {
            System.out.print("\033[32mEnter your email: \033[0m");
            email = scanner.nextLine().trim().toLowerCase();
            if (email.equals("0")) {
                System.out.println("\033[32mReturning to main menu...\033[0m");
                return null;
            }
            if (!isValidEmail(email)) {
                System.out.println("\033[32mInvalid email format. Try again.\033[0m");
                email = null;
            } else if (userManager.doesUserExistByEmail(email)) {
                System.out.println("\033[32mUser already exists with email: " + email + "\033[0m");
                email = null;
            }
        }

        // Password Input
        String password = null;
        while (password == null) {
            System.out.print("\033[32mEnter your password: \033[0m");
            password = scanner.nextLine().trim();
            if (password.equals("0")) {
                System.out.println("\033[32mReturning to main menu...\033[0m");
                return null;
            }
            if (password.isEmpty()) {
                System.out.println("\033[32mPassword cannot be empty. Try again.\033[0m");
                password = null;
            }
        }

        // Create a new user object
        boolean isAdmin = false;
        User user = new User(username, email, password, isAdmin);

        // Save to Firestore
        boolean firestoreSuccess = userManager.registerUser(user);
        if (firestoreSuccess) {
            System.out.println("\033[32mUser saved to Firestore successfully.\033[0m");
        } else {
            System.err.println("\033[32mFailed to save user to Firestore.\033[0m");
        }

        // Save to CSV
        saveUserToCSV(user);

        if (firestoreSuccess) {
            System.out.println("\033[32mRegistered and logged in successfully!\033[0m");
        } else {
            System.out.println("\033[32mRegistered locally but failed to save online.\033[0m");
        }
        return user;
    }
    private static User loginUser() {
        System.out.println("\033[34m+---------------------------------------------+");
        System.out.println("|                 LOGIN USER                  |");
        System.out.println("+-----+---------------------------------------+");
        System.out.format("| %-3s | %-37s |%n", "No.", "Field");
        System.out.println("+-----+---------------------------------------+");
        System.out.format("| %-3s | %-37s |%n", "1", "Email");
        System.out.format("| %-3s | %-37s |%n", "2", "Password");
        System.out.println("+-----+---------------------------------------+");
        System.out.println("Enter the details below (enter '0' anytime to return to main menu).\033[0m");

        System.out.print("\033[34m\nEnter your email: \033[0m");
        String email = scanner.nextLine();
        if (email.equals("0")) {
            System.out.println("\033[34mReturning to main menu...\033[0m");
            return null;
        }

        System.out.print("\033[34mEnter your password: \033[0m");
        String password = scanner.nextLine();
        if (password.equals("0")) {
            System.out.println("\033[34mReturning to main menu...\033[0m");
            return null;
        }

        User user = userManager.loginUser(email, password);
        if (user != null) {
            System.out.println("\033[34mLogged in successfully!\033[0m");
            return user;
        } else {
            System.out.println("\033[34mLogin failed. Try again.\033[0m");
            return null;
        }
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    private static void saveUserToCSV(User user) {
        String filePath = "users.csv";

        try (FileWriter writer = new FileWriter(filePath, true)) {
            // Write header if the file is empty
            if (new java.io.File(filePath).length() == 0) {
                writer.write("Username,Email,Password,Admin\n");
            }

            // Write user details
            writer.write(String.format(
                    "\"%s\",\"%s\",\"%s\",%b\n",
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(), // Already hashed
                    user.isAdmin()
            ));
            System.out.println("User details saved to CSV successfully.");
        } catch (IOException e) {
            System.err.println("Error saving user to CSV: " + e.getMessage());
        }
    }

    public static UserManager getUserManager() {
        return userManager;
    }


}