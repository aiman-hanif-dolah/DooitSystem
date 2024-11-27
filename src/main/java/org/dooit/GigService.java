package org.dooit;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GigService {

    private static final Scanner scanner = new Scanner(System.in);
    private GigManager gigManager;
    private ApplicationService applicationService;

    public GigService() {
        try {
            gigManager = new GigManager();
            applicationService = new ApplicationService();
        } catch (Exception e) {
            System.err.println("Error initializing GigManager: " + e.getMessage());
            System.exit(1);
        }
    }

    public void createGig(User currentUser) {
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
        Gig gig = new Gig(generateShortId(), title, description, location, payRate, postedBy);
        boolean success = gigManager.createGig(gig);
        System.out.println(success ? "Gig created successfully!" : "Failed to create gig.");
    }

    public void viewAllGigs(User currentUser) {
        List<Gig> gigs = gigManager.getAvailableGigs();
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
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled applying for a gig.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

        // Check if the gig is posted by the currentUser
        if (selectedGig.getPostedBy().equals(currentUser.getUsername())) {
            System.out.println("You cannot apply to your own gig.");
            return;
        }

        // Check if the user has already applied to this gig
        boolean alreadyApplied = applicationService.hasUserApplied(currentUser, selectedGig.getGigId());
        if (alreadyApplied) {
            System.out.println("You have already applied to this gig.");
            return;
        }

        // Prompt for reason and experience
        System.out.print("Enter your reason for applying: ");
        String reason = scanner.nextLine().trim();
        while (reason.isEmpty()) {
            System.out.println("Reason cannot be empty. Please provide a reason.");
            System.out.print("Enter your reason for applying: ");
            reason = scanner.nextLine().trim();
        }

        System.out.print("Enter your experience relevant to this gig: ");
        String experience = scanner.nextLine().trim();

        // Create a new application with reason and experience
        String applicationId = generateShortId();
        Application application = new Application(applicationId, selectedGig.getGigId(), currentUser.getUsername(), "Pending", reason, experience);

        boolean success = applicationService.createApplication(application);
        if (success) {
            System.out.println("You have successfully applied for the gig: " + selectedGig.getTitle());
        } else {
            System.out.println("Failed to apply for the gig. Please try again.");
        }
    }

    public void updateGig(User currentUser) {
        List<Gig> gigs = currentUser.isAdmin() ? gigManager.getAllGigs() : gigManager.getGigsByUser(currentUser.getUsername());

        if (gigs.isEmpty()) {
            System.out.println("No gigs available to update.");
            return;
        }

        System.out.println("\nGigs Available for Update:");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.printf("%d. %s at %s for RM%.2f per hour by %s%n",
                    i + 1,
                    gig.getTitle(),
                    gig.getLocation(),
                    gig.getPayRate(),
                    gig.getPostedBy());
        }

        System.out.println("\nEnter the number of the gig you want to update (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled updating a gig.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

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

    public void deleteGig(User currentUser) {
        List<Gig> gigs = currentUser.isAdmin() ? gigManager.getAllGigs() : gigManager.getGigsByUser(currentUser.getUsername());

        if (gigs.isEmpty()) {
            System.out.println("No gigs available to delete.");
            return;
        }

        System.out.println("\nGigs Available for Deletion:");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.printf("%d. %s at %s for RM%.2f per hour by %s%n",
                    i + 1,
                    gig.getTitle(),
                    gig.getLocation(),
                    gig.getPayRate(),
                    gig.getPostedBy());
        }

        System.out.println("\nEnter the number of the gig you want to delete (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled deleting a gig.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

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

    public void manageGigs(User currentUser) {
        List<Gig> gigs = gigManager.getGigsByUser(currentUser.getUsername());

        if (gigs.isEmpty()) {
            System.out.println("You have no gigs to manage.");
            return;
        }

        System.out.println("\nYour Gigs:");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.printf("%d. %s at %s%n", i + 1, gig.getTitle(), gig.getLocation());
        }

        System.out.println("\nEnter the number of the gig you want to manage (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled managing gigs.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);
        applicationService.manageApplicationsForGig(selectedGig, currentUser);
    }

    public void manageGigsAdmin(User currentUser) {
        List<Gig> gigs = gigManager.getAllGigs();

        if (gigs.isEmpty()) {
            System.out.println("No gigs available to manage.");
            return;
        }

        System.out.println("\nAll Gigs:");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.printf("%d. %s at %s by %s%n", i + 1, gig.getTitle(), gig.getLocation(), gig.getPostedBy());
        }

        System.out.println("\nEnter the number of the gig you want to manage (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled managing gigs.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);
        applicationService.manageApplicationsForGig(selectedGig, currentUser);
    }

    // Method to generate a short alphanumeric ID
    private String generateShortId() {
        int length = 6;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder id;
        Random random = new Random();
        do {
            id = new StringBuilder();
            for (int i = 0; i < length; i++) {
                id.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (applicationService.getApplicationManager().applicationIdExists(id.toString())); // Ensure uniqueness
        return id.toString();
    }

    public GigManager getGigManager() {
        return gigManager;
    }
}