package org.dooit;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null; // Track the logged-in user
    private static UserManager userManager;
    private static GigManager gigManager;
    private static ApplicationManager applicationManager; // ApplicationManager

    // Static block to initialize managers
    static {
        try {
            userManager = new UserManager();
            gigManager = new GigManager();
            applicationManager = new ApplicationManager(); // Initialize ApplicationManager
        } catch (Exception e) {
            System.err.println("Error initializing managers: " + e.getMessage());
            System.exit(1); // Exit if initialization fails
        }
    }

    public static void main(String[] args) {
        while (true) { // Loop continuously until the user decides to exit
            if (currentUser == null) {
                showLoginRegisterMenu(); // Show login/register menu
            } else {
                showMainMenu(); // Show the main menu if the user is logged in
            }
        }
    }

    private static void showLoginRegisterMenu() {
        while (currentUser == null) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            int choice = getNumericInput(1, 2);

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
            }
        }
    }

    private static void registerUser() {
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

        // Create a new user
        User user = new User(username, email, password);

        // Register the user
        if (userManager.registerUser(user)) {
            currentUser = user;
            System.out.println("Registered and logged in successfully!");
        } else {
            System.out.println("Registration failed. Username or email may already be taken. Try again.");
        }
    }

    private static void loginUser() {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        User user = userManager.loginUser(email, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Logged in successfully!");
        } else {
            System.out.println("Login failed. Try again.");
        }
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    private static int getNumericInput(int min, int max) {
        int choice;
        while (true) {
            try {
                System.out.print("Enter choice (" + min + "-" + max + "): ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Create Gig");
            System.out.println("2. View All Gigs");
            System.out.println("3. View My Applications");
            System.out.println("4. Update Gig");
            System.out.println("5. Delete Gig");
            System.out.println("6. Logout");
            System.out.println("7. Exit");

            int choice = getNumericInput(1, 7);

            switch (choice) {
                case 1 -> createGig();
                case 2 -> viewAllGigs();
                case 3 -> viewMyApplications();
                case 4 -> updateGig();
                case 5 -> deleteGig();
                case 6 -> {
                    currentUser = null; // Log out the user
                    System.out.println("Logged out.");
                    return; // Go back to the login/register menu
                }
                case 7 -> {
                    System.out.println("Exiting the application.");
                    System.exit(0); // Exit the application
                }
            }
        }
    }

    private static void createGig() {
        System.out.print("Enter gig title: ");
        String title = scanner.nextLine();

        System.out.print("Enter gig description: ");
        String description = scanner.nextLine();

        System.out.print("Enter location: ");
        String location = scanner.nextLine();

        double payRate;
        while (true) {
            System.out.print("Enter pay rate (numeric value): ");
            String payRateInput = scanner.nextLine();
            try {
                payRate = Double.parseDouble(payRateInput);
                break; // Exit loop if input is valid
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value for the pay rate.");
            }
        }

        String postedBy = currentUser.getUsername(); // Use username
        Gig gig = new Gig(UUID.randomUUID().toString(), title, description, location, payRate, postedBy);
        boolean success = gigManager.createGig(gig);
        System.out.println(success ? "Gig created successfully!" : "Failed to create gig.");
    }

    private static void viewAllGigs() {
        List<Gig> gigs = gigManager.getAllGigs();
        if (gigs.isEmpty()) {
            System.out.println("No gigs available.");
            return;
        }

        System.out.println("\nAvailable Gigs:");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.printf("%d. %s at %s for RM%.2f per hour by %s%n",
                    i + 1,
                    gig.getTitle(),
                    gig.getLocation(),
                    gig.getPayRate(),
                    gig.getPostedBy());
        }

        System.out.println("\nEnter the number of the gig you want to apply for (or 0 to cancel): ");
        int choice = getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled applying for a gig.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

        // Check if the gig is posted by the current user
        if (selectedGig.getPostedBy().equals(currentUser.getUsername())) {
            System.out.println("You cannot apply to your own gig.");
            return;
        }

        // Check if the user has already applied to this gig
        List<Application> existingApplications = applicationManager.getApplicationsByUsername(currentUser.getUsername());
        boolean alreadyApplied = existingApplications.stream()
                .anyMatch(app -> app.getGigId().equals(selectedGig.getGigId()));
        if (alreadyApplied) {
            System.out.println("You have already applied to this gig.");
            return;
        }

        // Create a new application
        String applicationId = UUID.randomUUID().toString();
        Application application = new Application(applicationId, selectedGig.getGigId(), currentUser.getUsername(), "Pending");

        boolean success = applicationManager.createApplication(application);
        if (success) {
            System.out.println("You have successfully applied for the gig: " + selectedGig.getTitle());
        } else {
            System.out.println("Failed to apply for the gig. Please try again.");
        }
    }

    private static void viewMyApplications() {
        List<Application> applications = applicationManager.getApplicationsByUsername(currentUser.getUsername());
        if (applications.isEmpty()) {
            System.out.println("No applications found.");
        } else {
            System.out.println("\nYour Applications:");
            for (Application app : applications) {
                Gig gig = gigManager.getGigById(app.getGigId());
                String gigTitle = (gig != null) ? gig.getTitle() : "Unknown Gig";
                System.out.printf("Application ID: %s, Gig: %s, Status: %s%n",
                        app.getApplicationId(),
                        gigTitle,
                        app.getStatus());
            }
        }
    }

    private static void updateGig() {
        List<Gig> gigs = gigManager.getAllGigs();
        if (gigs.isEmpty()) {
            System.out.println("No gigs available to update.");
            return;
        }

        // Display user's own gigs
        List<Gig> userGigs = gigs.stream()
                .filter(gig -> gig.getPostedBy().equals(currentUser.getUsername()))
                .toList();

        if (userGigs.isEmpty()) {
            System.out.println("You have no gigs to update.");
            return;
        }

        System.out.println("\nYour Gigs:");
        for (int i = 0; i < userGigs.size(); i++) {
            Gig gig = userGigs.get(i);
            System.out.printf("%d. %s at %s for RM%.2f per hour%n",
                    i + 1,
                    gig.getTitle(),
                    gig.getLocation(),
                    gig.getPayRate());
        }

        System.out.println("\nEnter the number of the gig you want to update (or 0 to cancel): ");
        int choice = getNumericInput(0, userGigs.size());

        if (choice == 0) {
            System.out.println("Cancelled updating a gig.");
            return;
        }

        Gig selectedGig = userGigs.get(choice - 1);

        System.out.print("Enter new gig title (leave blank to keep unchanged): ");
        String title = scanner.nextLine();
        if (!title.isEmpty()) {
            selectedGig.setTitle(title);
        }

        System.out.print("Enter new gig description (leave blank to keep unchanged): ");
        String description = scanner.nextLine();
        if (!description.isEmpty()) {
            selectedGig.setDescription(description);
        }

        System.out.print("Enter new location (leave blank to keep unchanged): ");
        String location = scanner.nextLine();
        if (!location.isEmpty()) {
            selectedGig.setLocation(location);
        }

        System.out.print("Enter new pay rate (leave blank to keep unchanged): ");
        String payRateInput = scanner.nextLine();
        if (!payRateInput.isEmpty()) {
            try {
                double payRate = Double.parseDouble(payRateInput);
                selectedGig.setPayRate(payRate);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Pay rate not updated.");
            }
        }

        boolean success = gigManager.updateGig(selectedGig);
        System.out.println(success ? "Gig updated successfully!" : "Failed to update gig.");
    }

    private static void deleteGig() {
        List<Gig> gigs = gigManager.getAllGigs();
        if (gigs.isEmpty()) {
            System.out.println("No gigs available to delete.");
            return;
        }

        // Display user's own gigs
        List<Gig> userGigs = gigs.stream()
                .filter(gig -> gig.getPostedBy().equals(currentUser.getUsername()))
                .toList();

        if (userGigs.isEmpty()) {
            System.out.println("You have no gigs to delete.");
            return;
        }

        System.out.println("\nYour Gigs:");
        for (int i = 0; i < userGigs.size(); i++) {
            Gig gig = userGigs.get(i);
            System.out.printf("%d. %s at %s for RM%.2f per hour%n",
                    i + 1,
                    gig.getTitle(),
                    gig.getLocation(),
                    gig.getPayRate());
        }

        System.out.println("\nEnter the number of the gig you want to delete (or 0 to cancel): ");
        int choice = getNumericInput(0, userGigs.size());

        if (choice == 0) {
            System.out.println("Cancelled deleting a gig.");
            return;
        }

        Gig selectedGig = userGigs.get(choice - 1);

        // Confirm deletion
        System.out.printf("Are you sure you want to delete the gig '%s'? (y/n): ", selectedGig.getTitle());
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (!confirmation.equals("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean success = gigManager.deleteGig(selectedGig.getGigId());
        System.out.println(success ? "Gig deleted successfully!" : "Failed to delete gig.");
    }
}