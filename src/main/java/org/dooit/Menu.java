package org.dooit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import java.net.URI;
import java.net.http.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

        // Display analytics in a table format
        String format = "| %-25s | %-15s |%n";
        System.out.format("+---------------------------+-----------------+%n");
        System.out.format("| Metric                    | Value           |%n");
        System.out.format("+---------------------------+-----------------+%n");
        System.out.format(format, "Total Users", userCount);
        System.out.format(format, "Total Gigs", gigCount);
        System.out.format(format, "Total Applications", applicationCount);
        System.out.format(format, "Approved Applications", approvedApplications);
        System.out.format("+---------------------------+-----------------+%n");

        // Declare the AI insights variable outside the try block
        String aiInsights = "";

        // Use OpenAI API to get AI-generated insights
        try {
            aiInsights = getAnalyticsInsightsFromOpenAI(userCount, gigCount, applicationCount, approvedApplications);
            System.out.println("\nAI Insights:");
            System.out.println(aiInsights);
        } catch (Exception e) {
            System.err.println("Error getting AI insights: " + e.getMessage());
        }

        // Write analytics to a text file (including AI insights)
        String filePath = "analytics.txt";
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write("Analytics Report\n");
            writer.write("================\n");
            writer.write(String.format("%-25s: %d%n", "Total Users", userCount));
            writer.write(String.format("%-25s: %d%n", "Total Gigs", gigCount));
            writer.write(String.format("%-25s: %d%n", "Total Applications", applicationCount));
            writer.write(String.format("%-25s: %d%n", "Approved Applications", approvedApplications));
            System.out.println("Analytics data has been written to '" + new File(filePath).getAbsolutePath() + "'.");
            writer.write("\nAI Insights:\n");
            writer.write(aiInsights + "\n"); // Safely use aiInsights here
            writer.write("------------------------\n");
        } catch (IOException e) {
            System.err.println("Error writing analytics to file: " + e.getMessage());
        }
    }

    private static String getAnalyticsInsightsFromOpenAI(int userCount, int gigCount, int applicationCount, int approvedApplications) throws IOException, InterruptedException {
        // Construct the prompt
        String prompt = String.format(
                "Based on the following platform analytics data:\n" +
                        "- Total Users: %d\n" +
                        "- Total Gigs: %d\n" +
                        "- Total Applications: %d\n" +
                        "- Approved Applications: %d\n" +
                        "Provide an analysis of the platform's performance and suggest ways to improve user engagement and gig postings.",
                userCount, gigCount, applicationCount, approvedApplications);

        // Prepare the API request
        String apiKey = "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"; // Replace with your actual OpenAI API key
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key is missing. Please provide your API key.");
        }

        HttpClient client = HttpClient.newHttpClient();
        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("model", "gpt-3.5-turbo");

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are an expert data analyst providing insights based on platform analytics.");
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
}