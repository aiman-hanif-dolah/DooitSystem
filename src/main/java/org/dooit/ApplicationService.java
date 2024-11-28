package org.dooit;

import java.util.List;
import java.util.Scanner;

public class ApplicationService {

    private static final Scanner scanner = new Scanner(System.in);
    private ApplicationManager applicationManager;
    private GigManager gigManager;

    public ApplicationService() {
        try {
            applicationManager = new ApplicationManager();
            gigManager = new GigManager();
        } catch (Exception e) {
            System.err.println("Error initializing ApplicationManager: " + e.getMessage());
            System.exit(1);
        }
    }

    public boolean createApplication(Application application) {
        return applicationManager.createApplication(application);
    }

    public void viewMyApplications(User currentUser) {
        List<Application> applications = applicationManager.getApplicationsByUsername(currentUser.getUsername());
        if (applications.isEmpty()) {
            System.out.println("No applications found.");
        } else {
            System.out.println("\nYour Applications:");
            // Display applications in a table format
            String format = "| %-3s | %-20s | %-20s | %-15s | %-10s |%n";
            System.out.format("+-----+----------------------+----------------------+-----------------+------------+%n");
            System.out.format("| No. | Gig Title            | Description          | Location        | Status     |%n");
            System.out.format("+-----+----------------------+----------------------+-----------------+------------+%n");
            for (int i = 0; i < applications.size(); i++) {
                Application app = applications.get(i);
                Gig gig = gigManager.getGigById(app.getGigId());
                if (gig != null) {
                    System.out.format(format, i + 1,
                            truncate(gig.getTitle(), 20),
                            truncate(gig.getDescription(), 20),
                            truncate(gig.getLocation(), 15),
                            truncate(app.getStatus(), 10));
                } else {
                    System.out.format(format, i + 1, "N/A", "N/A", "N/A", app.getStatus());
                }
            }
            System.out.format("+-----+----------------------+----------------------+-----------------+------------+%n");
        }
    }

    public void manageApplicationsForGig(Gig selectedGig, User currentUser) {
        List<Application> applications = applicationManager.getApplicationsByGigId(selectedGig.getGigId());

        if (applications.isEmpty()) {
            System.out.println("No applications for this gig.");
            return;
        }

        // Check if an application for this gig is already approved
        boolean isApproved = applications.stream().anyMatch(app -> app.getStatus().equals("Approved"));
        if (isApproved) {
            System.out.println("An application for this gig has already been approved.");
            return;
        }

        System.out.println("\nApplicants:");
        // Display applications in a table format
        String format = "| %-3s | %-15s | %-10s | %-25s | %-25s |%n";
        System.out.format("+-----+-----------------+------------+---------------------------+---------------------------+%n");
        System.out.format("| No. | Username        | Status     | Reason for Applying       | Experience               |%n");
        System.out.format("+-----+-----------------+------------+---------------------------+---------------------------+%n");
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            System.out.format(format, i + 1,
                    truncate(app.getUsername(), 15),
                    truncate(app.getStatus(), 10),
                    truncate(app.getReason(), 25),
                    truncate(app.getExperience(), 25));
        }
        System.out.format("+-----+-----------------+------------+---------------------------+---------------------------+%n");

        System.out.println("\nEnter the number of the applicant to approve (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, applications.size());

        if (choice == 0) {
            System.out.println("Cancelled applicant approval.");
            return;
        }

        Application selectedApplication = applications.get(choice - 1);

        // Check if the application is already approved
        if (selectedApplication.getStatus().equals("Approved")) {
            System.out.println("This application is already approved.");
            return;
        }

        // Approve the application
        selectedApplication.setStatus("Approved");
        boolean success = applicationManager.updateApplication(selectedApplication);

        if (success) {
            // Set gig as unavailable
            selectedGig.setAvailable(false);
            gigManager.updateGig(selectedGig);
            System.out.println("Application approved and gig is now closed for applications.");
        } else {
            System.out.println("Failed to approve application.");
        }
    }

    public boolean hasUserApplied(User currentUser, String gigId) {
        List<Application> existingApplications = applicationManager.getApplicationsByUsername(currentUser.getUsername());
        return existingApplications.stream().anyMatch(app -> app.getGigId().equals(gigId));
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

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }
}