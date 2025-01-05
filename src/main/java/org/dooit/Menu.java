package org.dooit;

public class Menu {

    private static GigService gigService = new GigService();
    private static ApplicationService applicationService = new ApplicationService();
    private static UserManager userManager = UserService.getUserManager();

    public static User showMainMenu(User currentUser) {
        while (true) {
            // Print the menu header
            System.out.println("\033[35m+---------------------------------------------+");
            System.out.println("|                  MAIN MENU                  |");
            System.out.println("+-----+---------------------------------------+");
            System.out.format("| %-3s | %-37s |%n", "No.", "Option");
            System.out.println("+-----+---------------------------------------+");

            // Print the menu options
            System.out.format("| %-3s | %-37s |%n", "1", "Create Gig");
            System.out.format("| %-3s | %-37s |%n", "2", "View All Gigs");
            System.out.format("| %-3s | %-37s |%n", "3", "View My Applications");
            System.out.format("| %-3s | %-37s |%n", "4", "Update Gig");
            System.out.format("| %-3s | %-37s |%n", "5", "Delete Gig");

            if (currentUser.isAdmin()) {
                System.out.format("| %-3s | %-37s |%n", "6", "View Analytics");
                System.out.format("| %-3s | %-37s |%n", "7", "Manage All Gigs");
                System.out.format("| %-3s | %-37s |%n", "8", "Logout");
                System.out.format("| %-3s | %-37s |%n", "0", "Exit");
            } else {
                System.out.format("| %-3s | %-37s |%n", "6", "Manage Gigs");
                System.out.format("| %-3s | %-37s |%n", "7", "Logout");
                System.out.format("| %-3s | %-37s |%n", "8", "Exit");
            }

            // Print the menu footer
            System.out.println("+-----+---------------------------------------+");

            // Get user input
            int maxOption = currentUser.isAdmin() ? 8 : 8;
            int choice = InputUtil.getNumericInput(0, maxOption, "\033[35mEnter your choice: \033[0m");

            // Handle user choice
            switch (choice) {
                case 1 -> gigService.createGig(currentUser);
                case 2 -> {
                    boolean continueApplying = true;
                    while (continueApplying) {
                        gigService.viewAllGigs(currentUser);
                        System.out.println("\n\033[35mEnter the number of the gig you want to apply for (or 0 to cancel): \033[0m");
                        int selectedGig = InputUtil.getNumericInput(0, gigService.getGigCount());

                        if (selectedGig == 0) {
                            System.out.println("\033[35mCancelled application.\033[0m");
                            continueApplying = false; // Exit the loop and return to the main menu
                        } else {
                            continueApplying = gigService.applyForGig(currentUser, selectedGig);
                        }
                    }
                }
                case 3 -> applicationService.viewMyApplications(currentUser);
                case 4 -> gigService.updateGig(currentUser);
                case 5 -> gigService.deleteGig(currentUser);
                case 6 -> {
                    if (!currentUser.isAdmin()) {
                        gigService.manageGigs(currentUser); // Normal users manage their own gigs
                    } else {
                        viewAnalytics(); // Admins view analytics
                    }
                }
                case 7 -> {
                    if (currentUser.isAdmin()) {
                        gigService.manageGigs(currentUser); // Admins manage all gigs
                    } else {
                        System.out.println("\033[35mLogged out.\033[0m");
                        return null; // Logout for normal users
                    }
                }
                case 8 -> {
                    System.out.println("\033[35mLogged out.\033[0m");
                    return null; // Logout, return to login/register menu
                }
                case 0 -> {
                    System.out.println("\033[35mExiting the application. Goodbye!\033[0m");
                    System.exit(0); // Exit the program
                }
                default -> System.out.println("\033[35mInvalid choice. Please try again.\033[0m");
            }
        }
    }

    private static void viewAnalytics() {
        AnalyticsService analyticsService = new AnalyticsService(userManager, gigService.getGigManager(), applicationService.getApplicationManager());
        analyticsService.generateAnalyticsReport();
    }
}