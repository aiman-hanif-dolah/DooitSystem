package org.dooit;

import java.util.Scanner;

public class Menu {

    private static final Scanner scanner = new Scanner(System.in);
    private static GigService gigService = new GigService();
    private static ApplicationService applicationService = new ApplicationService();
    private static UserManager userManager = UserService.getUserManager();

    public static void showMainMenu(User currentUser) {
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
                        return;
                    }
                }
                case 8 -> {
                    if (currentUser.isAdmin()) {
                        System.out.println("Logged out.");
                        return;
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
    }
}