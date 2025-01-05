package org.dooit;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ApplicationService {

    private static final Scanner scanner = new Scanner(System.in);
    private ApplicationManager applicationManager;
    private GigManager gigManager;

    public ApplicationService() {
        try {
            applicationManager = new ApplicationManager();
            gigManager = new GigManager(); // Ensure this is properly initialized
        } catch (Exception e) {
            System.err.println("Error initializing managers: " + e.getMessage());
            System.exit(1);
        }
    }

    public boolean createApplication(Application application) {
        return applicationManager.createApplication(application);
    }

    public void viewMyApplications(User currentUser) {
        List<Application> applications = applicationManager.getApplicationsByUsername(currentUser.getUsername());

        if (applications == null || applications.isEmpty()) {
            // Display a boxed message
            System.out.println("\033[35m+---------------------------------------------+");
            System.out.println("|       You have not applied for anything     |");
            System.out.println("|                  yet.                       |");
            System.out.println("+---------------------------------------------+\033[0m");
            return; // Exit the method to return to the main menu
        }

        // Display the list of applications
        System.out.println("\033[35m\nYour Applications:");
        String format = "| %-3s | %-20s | %-20s | %-15s | %-10s |%n";
        System.out.format("+-----+----------------------+----------------------+-----------------+------------+%n");
        System.out.format("| No. | Gig Title            | Description          | Location        | Status     |%n");
        System.out.format("+-----+----------------------+----------------------+-----------------+------------+%n");

        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            Gig gig = null;

            // Attempt to retrieve gig details
            try {
                gig = gigManager.getGigById(app.getGigId());
            } catch (Exception e) {
                System.err.println("\033[35mError retrieving gig details: " + e.getMessage() + "\033[0m");
            }

            if (gig != null) {
                System.out.format("\033[35m" + format + "\033[0m", i + 1,
                        truncate(gig.getTitle(), 20),
                        truncate(gig.getDescription(), 20),
                        truncate(gig.getLocation(), 15),
                        truncate(app.getStatus(), 10));
            } else {
                System.out.format("\033[35m" + format + "\033[0m", i + 1, "N/A", "N/A", "N/A", app.getStatus());
            }
        }
        System.out.format("\033[35m+-----+----------------------+----------------------+-----------------+------------+\033[0m%n");

        // Allow user to select an application for details
        System.out.println("\033[35m\nEnter the number of the application to view details (or 0 to cancel): \033[0m");
        int choice = InputUtil.getNumericInput(0, applications.size());

        if (choice == 0) {
            System.out.println("\033[35mReturning to main menu...\033[0m");
            return;
        }

        Application selectedApplication = applications.get(choice - 1);

        // Check if the application is approved
        if ("Approved".equalsIgnoreCase(selectedApplication.getStatus())) {
            System.out.println("\033[35m+---------------------------------------------+");
            System.out.println("|          APPLICATION APPROVED              |");
            System.out.println("+---------------------------------------------+");
            System.out.println("Congratulations, your application is successful.");
            System.out.println("Please contact us by clicking this link: " + selectedApplication.getContactInfo() + "\033[0m");
        } else {
            System.out.println("\033[35mThis application is not yet approved.\033[0m");
        }
    }

    public void manageApplicationsForGig(Gig selectedGig, User currentUser) {
        List<Application> applications = applicationManager.getApplicationsByGigId(selectedGig.getGigId());

        if (applications.isEmpty()) {
            System.out.println("No applications for this gig.");
            return;
        }

        // Display list of applications
        System.out.println("\nApplicants:");
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

        // AI Recommendation
        try {
            String recommendation = getRecommendationFromOpenAI(selectedGig, applications);
            System.out.println("\nRecommendation from AI:");
            System.out.println(recommendation);
        } catch (Exception e) {
            System.err.println("Error getting recommendation: " + e.getMessage());
        }

        // Prompt to approve an applicant
        System.out.println("\nEnter the number of the applicant to approve (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, applications.size());

        if (choice == 0) {
            System.out.println("Cancelled applicant approval.");
            return;
        }

        Application selectedApplication = applications.get(choice - 1);

        // Check if the application is already approved
        if ("Approved".equalsIgnoreCase(selectedApplication.getStatus())) {
            System.out.println("This application is already approved.");
            return;
        }

        // Enter phone number for WhatsApp
        System.out.println("\nEnter your phone number for WhatsApp contact (format: 601XXXXXXXX): ");
        String phoneNumber = scanner.nextLine().trim();

        // Validate phone number format
        while (!phoneNumber.matches("^601\\d{8,9}$")) {
            System.out.println("Invalid phone number format. Please enter a valid Malaysian phone number (format: 601XXXXXXXX): ");
            phoneNumber = scanner.nextLine().trim();
        }

        // Approve the application and update contact info
        selectedApplication.setStatus("Approved");
        selectedApplication.setContactInfo("https://wa.me/" + phoneNumber);

        boolean success = applicationManager.updateApplication(selectedApplication);

        if (success) {
            selectedGig.setAvailable(false); // Set gig as unavailable
            gigManager.updateGig(selectedGig);
            System.out.println("\nApplication approved successfully!");
            System.out.println("WhatsApp contact link has been shared: " + selectedApplication.getContactInfo());
        } else {
            System.out.println("Failed to approve application.");
        }
    }

    // Updated method using Gson
    private String getRecommendationFromOpenAI(Gig gig, List<Application> applications) throws IOException, InterruptedException {
        // Hardcoded API Key
        String apiKey = "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"; // Replace with your actual OpenAI API key

        // Construct the prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("As an expert recruiter, recommend the best applicant for the following gig:\n");
        promptBuilder.append("Gig Title: ").append(gig.getTitle()).append("\n");
        promptBuilder.append("Gig Description: ").append(gig.getDescription()).append("\n\n");
        promptBuilder.append("Applicants:\n");
        for (Application app : applications) {
            promptBuilder.append("- Username: ").append(app.getUsername()).append("\n");
            promptBuilder.append("  Reason: ").append(app.getReason()).append("\n");
            promptBuilder.append("  Experience: ").append(app.getExperience()).append("\n\n");
        }
        promptBuilder.append("Provide a recommendation on which applicant is the best fit for the gig and explain why.");

        String prompt = promptBuilder.toString();

        // Prepare the API request
        HttpClient client = HttpClient.newHttpClient();
        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("model", "gpt-3.5-turbo");

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are an expert recruiter helping to select the best applicant for a job.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        jsonRequest.add("messages", messages);
        jsonRequest.addProperty("max_tokens", 250);
        jsonRequest.addProperty("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            String aiInsights = jsonResponse
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString()
                    .trim();
            return aiInsights;
        } else {
            throw new IOException("OpenAI API request failed with status code " + response.statusCode() + ": " + response.body());
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