package org.dooit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AnalyticsService {
    private final UserManager userManager;
    private final GigManager gigManager;
    private final ApplicationManager applicationManager;

    public AnalyticsService(UserManager userManager, GigManager gigManager, ApplicationManager applicationManager) {
        this.userManager = userManager;
        this.gigManager = gigManager;
        this.applicationManager = applicationManager;
    }

    public void generateAnalyticsReport() {
        int userCount = userManager.getUserCount();
        int gigCount = gigManager.getGigCount();
        int applicationCount = applicationManager.getApplicationCount();
        int approvedApplications = applicationManager.getApprovedApplicationCount();

        // Display analytics in tabular form
        displayAnalytics(userCount, gigCount, applicationCount, approvedApplications);

        String aiInsights = "";
        try {
            aiInsights = getAnalyticsInsightsFromOpenAI(userCount, gigCount, applicationCount, approvedApplications);
            System.out.println("\n+-------------------------------+");
            System.out.println("|          AI Insights          |");
            System.out.println("+-------------------------------+");
            System.out.println(aiInsights);
        } catch (Exception e) {
            System.err.println("Error getting AI insights: " + e.getMessage());
        }

        writeAnalyticsToFile(userCount, gigCount, applicationCount, approvedApplications, aiInsights);
    }

    private void displayAnalytics(int userCount, int gigCount, int applicationCount, int approvedApplications) {
        String format = "| %-25s | %-15s |%n";
        System.out.println("\033[96m\n+---------------------------+-----------------+");
        System.out.println("| Metric                    | Value           |");
        System.out.println("+---------------------------+-----------------+");
        System.out.format("\033[96m" + format + "\033[0m", "Total Users", userCount);
        System.out.format("\033[96m" + format + "\033[0m", "Total Gigs", gigCount);
        System.out.format("\033[96m" + format + "\033[0m", "Total Applications", applicationCount);
        System.out.format("\033[96m" + format + "\033[0m", "Approved Applications", approvedApplications);
        System.out.println("\033[96m+---------------------------+-----------------+\033[0m");
    }

    private void writeAnalyticsToFile(int userCount, int gigCount, int applicationCount, int approvedApplications, String aiInsights) {
        String filePath = "analytics.txt";
        try (FileWriter writer = new FileWriter(filePath, false)) { // Overwrite file
            writer.write("+---------------------------+-----------------+\n");
            writer.write("| Metric                    | Value           |\n");
            writer.write("+---------------------------+-----------------+\n");
            writer.write(String.format("| %-25s | %-15d |\n", "Total Users", userCount));
            writer.write(String.format("| %-25s | %-15d |\n", "Total Gigs", gigCount));
            writer.write(String.format("| %-25s | %-15d |\n", "Total Applications", applicationCount));
            writer.write(String.format("| %-25s | %-15d |\n", "Approved Applications", approvedApplications));
            writer.write("+---------------------------+-----------------+\n");
            writer.write("\n+-------------------------------+\n");
            writer.write("|          AI Insights          |\n");
            writer.write("+-------------------------------+\n");
            writer.write(aiInsights + "\n");
            writer.write("+-------------------------------+\n");
            System.out.println("Analytics data has been written to '" + new File(filePath).getAbsolutePath() + "'.");
        } catch (IOException e) {
            System.err.println("Error writing analytics to file: " + e.getMessage());
        }
    }

    private String getAnalyticsInsightsFromOpenAI(int userCount, int gigCount, int applicationCount, int approvedApplications) throws IOException, InterruptedException {
        String prompt = String.format(
                """
                        Based on the following platform analytics data:
                        - Total Users: %d
                        - Total Gigs: %d
                        - Total Applications: %d
                        - Approved Applications: %d
                        Provide an analysis of the platform's performance and suggest ways to improve user engagement and gig postings.""",
                userCount, gigCount, applicationCount, approvedApplications);

        String apiKey = "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"; // Replace with your actual OpenAI API key
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
            return jsonResponse
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString()
                    .trim();
        } else {
            throw new IOException("OpenAI API request failed with status code " + response.statusCode() + ": " + response.body());
        }
    }
}
