package org.dooit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
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
        System.out.println("\033[33m+---------------------------------------------+");
        System.out.println("|               CREATE A NEW GIG              |");
        System.out.println("+-----+---------------------------------------+");
        System.out.format("| %-3s | %-37s |%n", "No.", "Field");
        System.out.println("+-----+---------------------------------------+");

        System.out.format("| %-3s | %-37s |%n", "1", "Gig Title");
        System.out.format("| %-3s | %-37s |%n", "2", "Description");
        System.out.format("| %-3s | %-37s |%n", "3", "Location");
        System.out.format("| %-3s | %-37s |%n", "4", "Pay Rate");
        System.out.println("+-----+---------------------------------------+");
        System.out.println("Fill in the details below (enter '0' anytime to return to menu).\033[0m");

        String title;
        while (true) {
            System.out.print("\033[33m\nEnter gig title (at least 3 characters and must include alphabets): \033[0m");
            title = scanner.nextLine().trim();
            if (title.equals("0")) {
                System.out.println("\033[33mReturning to menu...\033[0m");
                return;
            }
            if (title.length() >= 3 && title.matches(".*[a-zA-Z]+.*")) {
                break;
            } else {
                System.out.println("\033[33mInvalid input. Title must be at least 3 characters long and include at least one alphabetic character.\033[0m");
            }
        }

        String description;
        while (true) {
            System.out.print("\033[33mEnter gig description (at least 5 characters and must include alphabets): \033[0m");
            description = scanner.nextLine().trim();
            if (description.equals("0")) {
                System.out.println("\033[33mReturning to menu...\033[0m");
                return;
            }
            if (description.length() >= 5 && description.matches(".*[a-zA-Z]+.*")) {
                break;
            } else {
                System.out.println("\033[33mInvalid input. Description must be at least 5 characters long and include at least one alphabetic character.\033[0m");
            }
        }

        String location;
        while (true) {
            System.out.print("\033[33mEnter location (at least 3 characters and must include alphabets): \033[0m");
            location = scanner.nextLine().trim();
            if (location.equals("0")) {
                System.out.println("\033[33mReturning to menu...\033[0m");
                return;
            }
            if (location.length() >= 3 && location.matches(".*[a-zA-Z]+.*")) {
                break;
            } else {
                System.out.println("\033[33mInvalid input. Location must be at least 3 characters long and include at least one alphabetic character.\033[0m");
            }
        }

        double payRate;
        while (true) {
            System.out.print("\033[33mEnter pay rate per hour in RM (numeric value, minimum RM1): \033[0m");
            String payRateInput = scanner.nextLine().trim();
            try {
                payRate = Double.parseDouble(payRateInput);
                if (payRate >= 1) {
                    break;
                } else {
                    System.out.println("\033[33mPay rate must be at least RM1. Please try again.\033[0m");
                }
            } catch (NumberFormatException e) {
                if (payRateInput.equals("0")) {
                    System.out.println("\033[33mReturning to menu...\033[0m");
                    return;
                }
                System.out.println("\033[33mInvalid input. Please enter a valid numeric value for the pay rate.\033[0m");
            }
        }

        String postedBy = currentUser.getUsername();
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String documentName = title + " by " + postedBy + " at " + location + " on " + timestamp;

        Gig gig = new Gig(documentName, title, description, location, payRate, postedBy);
        boolean firestoreSuccess = gigManager.createGig(gig, documentName);

        if (firestoreSuccess) {
            System.out.println("\033[33mGig created successfully!\033[0m");
            saveGigToCSV(gig);
        } else {
            System.out.println("\033[33mFailed to create gig in Firestore.\033[0m");
        }
    }
    private void saveGigToCSV(Gig gig) {
        String filePath = "gigs.csv";
        boolean headerWritten = false;

        try (FileWriter writer = new FileWriter(filePath, true)) {
            // Write header if the file is empty
            if (new java.io.File(filePath).length() == 0) {
                writer.write("Gig ID,Title,Description,Location,Pay Rate,Posted By,Available\n");
                headerWritten = true;
            }

            // Write gig details
            writer.write(String.format(
                    "\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%b\n",
                    gig.getGigId(),
                    gig.getTitle(),
                    gig.getDescription(),
                    gig.getLocation(),
                    gig.getPayRate(),
                    gig.getPostedBy(),
                    gig.isAvailable()
            ));
            System.out.println("Gig details saved to CSV successfully.");
        } catch (IOException e) {
            System.err.println("Error saving gig to CSV: " + e.getMessage());
        }
    }

    public void viewAllGigs(User currentUser) {
        List<Gig> gigs = gigManager.getAvailableGigs();
        if (gigs.isEmpty()) {
            System.out.println("\033[95mNo gigs available.\033[0m");
            return;
        }

        // Display sorting options
        System.out.println("\033[33mChoose sorting order:");
        System.out.println("0. Back to Menu");
        System.out.println("1. By Pay Rate (Descending)");
        System.out.println("2. By Title (Alphabetical)");
        System.out.println("3. By Location (Alphabetical)");
        System.out.println("4. By Posted By (Alphabetical)");
        System.out.println("5. Default Order (No Sorting)");
        System.out.print("Enter your choice: \033[0m");
        int sortOption = InputUtil.getNumericInput(0, 5, "Choose sorting order (0 to 5):");

        // Apply sorting or return to menu
        switch (sortOption) {
            case 0 -> {
                System.out.println("\033[33mReturning to menu...\033[0m");
                return; // Exit the method to return to the menu
            }
            case 1 -> gigs.sort((g1, g2) -> Double.compare(g2.getPayRate(), g1.getPayRate()));
            case 2 -> gigs.sort(Comparator.comparing(Gig::getTitle));
            case 3 -> gigs.sort(Comparator.comparing(Gig::getLocation));
            case 4 -> gigs.sort(Comparator.comparing(Gig::getPostedBy));
            case 5 -> System.out.println("\033[33mDisplaying gigs in the default order.\033[0m");
            default -> System.out.println("\033[33mInvalid option. Displaying unsorted gigs.\033[0m");
        }

        // Display sorted gigs
        System.out.println("\033[95m+---------------------------------------------+");
        System.out.println("|              AVAILABLE GIGS                |");
        System.out.println("+-----+----------------------+-----------------+------------+-----------------+");
        System.out.format("| %-3s | %-20s | %-15s | %-10s | %-15s |%n", "No.", "Title", "Location", "Pay Rate", "Posted By");
        System.out.println("+-----+----------------------+-----------------+------------+-----------------+");

        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format("\033[95m| %-3d | %-20s | %-15s | RM%-8.2f | %-15s |\033[0m%n",
                    i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    gig.getPayRate(),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.println("\033[95m+-----+----------------------+-----------------+------------+-----------------+\033[0m");

        // Prompt user to select a gig
        System.out.println("\033[33mEnter the number of the gig you want to apply for (or 0 to cancel): \033[0m");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("\033[33mCancelled application. Returning to the main menu...\033[0m");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1); // Select from the sorted list

        // Pass the selected gig and sorted list to applyForGig
        boolean continueApplying = applyForGig(currentUser, selectedGig, gigs);
        if (!continueApplying) {
            System.out.println("\033[33mApplication process completed. Returning to main menu...\033[0m");
        }
    }

    public void updateGig(User currentUser) {
        List<Gig> gigs = currentUser.isAdmin() ? gigManager.getAllGigs() : gigManager.getGigsByUser(currentUser.getUsername());

        if (gigs.isEmpty()) {
            System.out.println("\033[93mNo gigs available to update.\033[0m");
            return;
        }

        System.out.println("\033[93m\nGigs Available for Update:");
        String format = "| %-3s | %-20s | %-15s | %-10s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        | Pay Rate   | Posted By       |%n");
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format("\033[93m" + format + "\033[0m",
                    i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    String.format("RM%.2f", gig.getPayRate()),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.println("\033[93m+-----+----------------------+-----------------+------------+-----------------+\033[0m");

        System.out.println("\n\033[93mEnter the number of the gig you want to update (or 0 to cancel): \033[0m");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("\033[93mCancelled updating a gig. Returning to main menu...\033[0m");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

        // Updating title
        System.out.print("\033[93mEnter new gig title (leave blank to keep unchanged, or enter '0' to cancel): \033[0m");
        String title = scanner.nextLine().trim();
        if (title.equals("0")) {
            System.out.println("\033[93mCancelled updating a gig. Returning to main menu...\033[0m");
            return;
        }
        if (!title.isEmpty() && title.matches("^[a-zA-Z0-9\s,.!?']+$")) {
            selectedGig.setTitle(title);
        } else if (!title.isEmpty()) {
            System.out.println("\033[93mInvalid input. Title not updated.\033[0m");
        }

        // Updating description
        System.out.print("\033[93mEnter new gig description (leave blank to keep unchanged, or enter '0' to cancel): \033[0m");
        String description = scanner.nextLine().trim();
        if (description.equals("0")) {
            System.out.println("\033[93mCancelled updating a gig. Returning to main menu...\033[0m");
            return;
        }
        if (!description.isEmpty() && description.matches("^[a-zA-Z0-9\s,.!?']+$")) {
            selectedGig.setDescription(description);
        } else if (!description.isEmpty()) {
            System.out.println("\033[93mInvalid input. Description not updated.\033[0m");
        }

        // Updating location
        System.out.print("\033[93mEnter new location (leave blank to keep unchanged, or enter '0' to cancel): \033[0m");
        String location = scanner.nextLine().trim();
        if (location.equals("0")) {
            System.out.println("\033[93mCancelled updating a gig. Returning to main menu...\033[0m");
            return;
        }
        if (!location.isEmpty() && location.matches("^[a-zA-Z0-9\s,]+$")) {
            selectedGig.setLocation(location);
        } else if (!location.isEmpty()) {
            System.out.println("\033[93mInvalid input. Location not updated.\033[0m");
        }

        // Updating pay rate
        System.out.print("\033[93mEnter new pay rate (leave blank to keep unchanged, or enter '0' to cancel): \033[0m");
        String payRateInput = scanner.nextLine().trim();
        if (payRateInput.equals("0")) {
            System.out.println("\033[93mCancelled updating a gig. Returning to main menu...\033[0m");
            return;
        }
        if (!payRateInput.isEmpty()) {
            try {
                double payRate = Double.parseDouble(payRateInput);
                if (payRate >= 1) {
                    selectedGig.setPayRate(payRate);
                } else {
                    System.out.println("\033[93mPay rate must be at least RM1. Pay rate not updated.\033[0m");
                }
            } catch (NumberFormatException e) {
                System.out.println("\033[93mInvalid input. Pay rate not updated.\033[0m");
            }
        }

        boolean success = gigManager.updateGig(selectedGig);
        System.out.println(success ? "\033[93mGig updated successfully!\033[0m" : "\033[93mFailed to update gig.\033[0m");
    }
    public void deleteGig(User currentUser) {
        List<Gig> gigs = currentUser.isAdmin() ? gigManager.getAllGigs() : gigManager.getGigsByUser(currentUser.getUsername());

        if (gigs.isEmpty()) {
            System.out.println("\033[31mNo gigs available to delete.\033[0m");
            return;
        }

        System.out.println("\033[31m\nGigs Available for Deletion:");
        // Display gigs in a table format
        String format = "| %-3s | %-20s | %-15s | %-10s | %-15s |%n";
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        System.out.format("| No. | Title                | Location        | Pay Rate   | Posted By       |%n");
        System.out.format("+-----+----------------------+-----------------+------------+-----------------+%n");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format("\033[31m" + format + "\033[0m",
                    i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    String.format("RM%.2f", gig.getPayRate()),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.println("\033[31m+-----+----------------------+-----------------+------------+-----------------+\033[0m");

        System.out.println("\n\033[31mEnter the number of the gig you want to delete (or 0 to cancel): \033[0m");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("\033[31mCancelled deletion. Returning to main menu...\033[0m");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

        // Display selected gig details
        System.out.println("\033[31m+---------------------------------------------+");
        System.out.println("|               SELECTED GIG DETAILS         |");
        System.out.println("+---------------------------------------------+");
        System.out.format("| %-15s: %-37s |%n", "Title", selectedGig.getTitle());
        System.out.format("| %-15s: %-37s |%n", "Location", selectedGig.getLocation());
        System.out.format("| %-15s: RM%-35.2f |%n", "Pay Rate", selectedGig.getPayRate());
        System.out.format("| %-15s: %-37s |%n", "Posted By", selectedGig.getPostedBy());
        System.out.format("| %-15s: %-37s |%n", "Description", selectedGig.getDescription());
        System.out.println("+---------------------------------------------+\033[0m");

        System.out.printf("\033[31mAre you sure you want to delete the gig '%s'? (1 for Yes, 0 for No): \033[0m", selectedGig.getTitle());
        int confirmation = InputUtil.getNumericInput(0, 1);

        if (confirmation == 0) {
            System.out.println("\033[31mDeletion canceled. Returning to main menu...\033[0m");
            return;
        }

        boolean success = gigManager.deleteGig(selectedGig.getGigId());
        System.out.println(success ? "\033[31mGig deleted successfully!\033[0m" : "\033[31mFailed to delete gig.\033[0m");
    }
    public void manageGigs(User currentUser) {
        // Fetch gigs: Admins see all, normal users see only their own
        List<Gig> gigs = currentUser.isAdmin()
                ? gigManager.getAllGigs()
                : gigManager.getGigsByUser(currentUser.getUsername());

        // Check if there are no gigs to manage
        if (gigs.isEmpty()) {
            System.out.println("\033[93m" + (currentUser.isAdmin() ? "No gigs available to manage." : "You have no gigs to manage.") + "\033[0m");
            return;
        }

        // Display available gigs
        System.out.println("\nYour Gigs:");
        System.out.println("+-----+----------------------+-----------------+------------+-----------------+");
        System.out.println("| No. | Title                | Location        | Pay Rate   | Posted By       |");
        System.out.println("+-----+----------------------+-----------------+------------+-----------------+");
        for (int i = 0; i < gigs.size(); i++) {
            Gig gig = gigs.get(i);
            System.out.format("| %-3d | %-20s | %-15s | RM%-8.2f | %-15s |%n",
                    i + 1,
                    truncate(gig.getTitle(), 20),
                    truncate(gig.getLocation(), 15),
                    gig.getPayRate(),
                    truncate(gig.getPostedBy(), 15));
        }
        System.out.println("+-----+----------------------+-----------------+------------+-----------------+");

        // Prompt user to select a gig
        System.out.println("\033[93mEnter the number of the gig you want to manage (or 0 to cancel): \033[0m");
        int choice = InputUtil.getNumericInput(0, gigs.size());

        if (choice == 0) {
            System.out.println("\033[93mCancelled managing gigs. Returning to main menu...\033[0m");
            return;
        }

        Gig selectedGig = gigs.get(choice - 1);

        // Display selected gig details
        System.out.println("\033[93m+---------------------------------------------+");
        System.out.println("|               SELECTED GIG DETAILS         |");
        System.out.println("+---------------------------------------------+");
        System.out.format("| %-15s: %-37s |%n", "Title", selectedGig.getTitle());
        System.out.format("| %-15s: %-37s |%n", "Location", selectedGig.getLocation());
        System.out.format("| %-15s: RM%-35.2f |%n", "Pay Rate", selectedGig.getPayRate());
        System.out.format("| %-15s: %-37s |%n", "Posted By", selectedGig.getPostedBy());
        System.out.format("| %-15s: %-37s |%n", "Description", selectedGig.getDescription());
        System.out.println("+---------------------------------------------+\033[0m");

        // Manage applications for the selected gig
        System.out.println("\033[93mManaging applications for this gig...\033[0m");
        applicationService.manageApplicationsForGig(selectedGig, currentUser);
    }

    public boolean applyForGig(User currentUser, Gig selectedGig, List<Gig> sortedGigs) {
        // Display selected gig details
        System.out.println("\033[36m+---------------------------------------------+");
        System.out.println("|               SELECTED GIG DETAILS         |");
        System.out.println("+---------------------------------------------+");
        System.out.format("| %-15s: %-37s |%n", "Title", selectedGig.getTitle());
        System.out.format("| %-15s: %-37s |%n", "Location", selectedGig.getLocation());
        System.out.format("| %-15s: RM%-35.2f |%n", "Pay Rate", selectedGig.getPayRate());
        System.out.format("| %-15s: %-37s |%n", "Posted By", selectedGig.getPostedBy());
        System.out.format("| %-15s: %-37s |%n", "Description", selectedGig.getDescription());
        System.out.println("+---------------------------------------------+\033[0m");

        // Confirm with the user
        System.out.println("\033[36mDo you want to apply for this gig?");
        System.out.println("1. Yes");
        System.out.println("0. Cancel\033[0m");
        int confirmation = InputUtil.getNumericInput(0, 1);

        if (confirmation == 0) {
            System.out.println("\033[36mApplication canceled. Returning to the gig list.\033[0m");
            return true; // Allow the user to try again
        }

        // Check if the user is the poster of the gig
        if (selectedGig.getPostedBy().equals(currentUser.getUsername())) {
            System.out.println("\033[36mYou cannot apply for your own gig. Please select another.\033[0m");
            return true; // Allow the user to try again
        }

        // Check if the user has already applied for the gig
        if (applicationService.hasUserApplied(currentUser, selectedGig.getGigId())) {
            System.out.println("\033[36mYou have already applied for this gig.\033[0m");
            return true; // Allow the user to try again
        }

        // Prompt user for application details
        System.out.print("\033[36mEnter your reason for applying: \033[0m");
        String reason = scanner.nextLine();

        System.out.print("\033[36mEnter your experience: \033[0m");
        String experience = scanner.nextLine();

        // Create and save the application
        Application application = new Application(
                generateShortId(), // Generates a unique application ID
                selectedGig.getGigId(),
                currentUser.getUsername(),
                "Pending",
                reason,
                experience,
                null // No contact info at the time of application
        );

        boolean success = applicationService.createApplication(application);
        if (success) {
            System.out.println("\033[36mApplication submitted successfully!\033[0m");
        } else {
            System.out.println("\033[36mFailed to submit application.\033[0m");
        }

        return false; // Return to the main menu after successful application
    }


    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 3) + "...";
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