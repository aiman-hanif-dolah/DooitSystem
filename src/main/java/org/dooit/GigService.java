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
        // Display gigs in a table format
        String format = "| %-3s | %-20s | %-15s | %-10s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        | Pay Rate   | Posted By       |%n");
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format(format, i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    String.format("RM%.2f", gig.getPayRate()),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");

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
        // Display gigs in a table format
        String format = "| %-3s | %-20s | %-15s | %-10s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        | Pay Rate   | Posted By       |%n");
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format(format, i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    String.format("RM%.2f", gig.getPayRate()),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");

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
        // Display gigs in a table format
        String format = "| %-3s | %-20s | %-15s | %-10s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        | Pay Rate   | Posted By       |%n");
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format(format, i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    String.format("RM%.2f", gig.getPayRate()),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");

        System.out.println("\nEnter the number of the gig you want to delete (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled deleting a gig.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

        // Confirm deletion
        System.out.printf("Are you sure you want to delete the gig '%s'? (1 for Yes, 0 for No): ", selectedGig.getTitle());
        int confirmation = InputUtil.getNumericInput(0, 1);
        if (confirmation != 1) {
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
        // Display gigs in a table format
        String format = "| %-3s | %-20s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        |%n");
        System.out.format("+-----+----------------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format(format, i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15));
        }
        System.out.format("+-----+----------------------+-----------------+%n");

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
        // Display gigs in a table format
        String format = "| %-3s | %-20s | %-15s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        | Posted By       |%n");
        System.out.format("+-----+----------------------+-----------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format(format, i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.format("+-----+----------------------+-----------------+-----------------+%n");

        System.out.println("\nEnter the number of the gig you want to manage (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("Cancelled managing gigs.");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);
        applicationService.manageApplicationsForGig(selectedGig, currentUser);
    }

    // Utility method to truncate strings for table display
    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        if (value.length() <= length) {
            return value;
        } else {
            return value.substring(0, length - 3) + "...";
        }
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