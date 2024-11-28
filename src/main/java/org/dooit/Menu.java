package org.dooit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Menu {

    private static final Scanner scanner = new Scanner(System.in);
    private static GigService gigService = new GigService();
    private static ApplicationService applicationService = new ApplicationService();
    private static UserManager userManager = UserService.getUserManager();

    public static User showMainMenu(User currentUser) {
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Create Gig");
            System.out.println("2. View All Gigs");
            System.out.println("3. View My Applications");
            System.out.println("4. Update Gig");
            System.out.println("5. Delete Gig");

            if (currentUser.isAdmin()) {
                System.out.println("6. View Analytics");
                System.out.println("7. Manage All Gigs");
                System.out.println("8. Logout");
                System.out.println("9. Exit");
            } else {
                System.out.println("6. Manage Gigs");
                System.out.println("7. Logout");
                System.out.println("8. Exit");
            }

            int maxOption = currentUser.isAdmin() ? 9 : 8;
            int choice = InputUtil.getNumericInput(1, maxOption);

            switch (choice) {
                case 1 -> gigService.createGig(currentUser);
                case 2 -> gigService.viewAllGigs(currentUser);
                case 3 -> applicationService.viewMyApplications(currentUser);
                case 4 -> gigService.updateGig(currentUser);
                case 5 -> gigService.deleteGig(currentUser);
                case 6 -> {
                    if (currentUser.isAdmin()) {
                        viewAnalytics();
                    } else {
                        gigService.manageGigs(currentUser);
                    }
                }
                case 7 -> {
                    if (currentUser.isAdmin()) {
                        gigService.manageGigsAdmin(currentUser);
                    } else {
                        System.out.println("Logged out.");
                        return null; // Set currentUser to null
                    }
                }
                case 8 -> {
                    if (currentUser.isAdmin()) {
                        System.out.println("Logged out.");
                        return null; // Set currentUser to null
                    } else {
                        System.out.println("Exiting the application.");
                        System.exit(0);
                    }
                }
                case 9 -> {
                    System.out.println("Exiting the application.");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAnalytics() {
        System.out.println("\nAnalytics:");
        int userCount = userManager.getUserCount();
        int gigCount = gigService.getGigManager().getGigCount();
        int applicationCount = applicationService.getApplicationManager().getApplicationCount();
        int approvedApplications = applicationService.getApplicationManager().getApprovedApplicationCount();

        System.out.println("Total Users: " + userCount);
        System.out.println("Total Gigs: " + gigCount);
        System.out.println("Total Applications: " + applicationCount);
        System.out.println("Approved Applications: " + approvedApplications);

        // Write analytics to a text file
        try (FileWriter writer = new FileWriter("analytics.txt", true)) {
            writer.write("Analytics Report\n");
            writer.write("================\n");
            writer.write("Total Users: " + userCount + "\n");
            writer.write("Total Gigs: " + gigCount + "\n");
            writer.write("Total Applications: " + applicationCount + "\n");
            writer.write("Approved Applications: " + approvedApplications + "\n");
            writer.write("------------------------\n");
            System.out.println("Analytics data has been written to 'analytics.txt'.");
        } catch (IOException e) {
            System.err.println("Error writing analytics to file: " + e.getMessage());
        }
    }
}