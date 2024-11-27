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
            for (Application app : applications) {
                Gig gig = gigManager.getGigById(app.getGigId());
                if (gig != null) {
                    System.out.printf("Gig Title: %s%nDescription: %s%nLocation: %s%nStatus: %s%n%n",
                            gig.getTitle(),
                            gig.getDescription(),
                            gig.getLocation(),
                            app.getStatus());
                } else {
                    System.out.printf("Gig not found for application. Status: %s%n%n", app.getStatus());
                }
            }
        }
    }

    public boolean hasUserApplied(User currentUser, String gigId) {
        List<Application> existingApplications = applicationManager.getApplicationsByUsername(currentUser.getUsername());
        return existingApplications.stream().anyMatch(app -> app.getGigId().equals(gigId));
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
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            System.out.printf("%d. Username: %s, Status: %s%n", i + 1, app.getUsername(), app.getStatus());
            System.out.println("Reason for Applying: " + app.getReason());
            System.out.println("Experience: " + app.getExperience());
            System.out.println("-----------------------------------");
        }

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

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }
}