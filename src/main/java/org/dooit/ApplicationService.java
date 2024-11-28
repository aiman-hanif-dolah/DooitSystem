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

        try {
            String recommendation = getRecommendationFromOpenAI(selectedGig, applications);
            System.out.println("\nRecommendation from AI:");
            System.out.println(recommendation);
        } catch (Exception e) {
            System.err.println("Error getting recommendation: " + e.getMessage());
        }

        System.out.println("\nEnter the number of the applicant to approve (or 0 to cancel): ");
        int choice = InputUtil.getNumericInput(0, applications.size());

        if (choice == 0) {
            System.out.println("Cancelled applicant approval.");
            return;
        }

        Application selectedApplication = applications.get(choice - 1);

        // Approve the application
        if (selectedApplication.getStatus().equals("Approved")) {
            System.out.println("This application is already approved.");
            return;
        }

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

    // Updated method using Gson
    private String getRecommendationFromOpenAI(Gig gig, List<Application> applications) throws IOException, InterruptedException {
        // Hardcoded API Key
        String apiKey = "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"; // Replace with your actual API key

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