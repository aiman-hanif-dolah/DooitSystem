package org.dooit;

import java.util.Scanner;

public class InputUtil {

    private static final Scanner scanner = new Scanner(System.in);

    // Existing method
    public static int getNumericInput(int min, int max) {
        int choice;
        while (true) {
            try {
                System.out.print("Respond (" + min + "-" + max + "): ");
                String input = scanner.nextLine().trim();
                choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    // Overloaded method to include custom prompts
    public static int getNumericInput(int min, int max, String prompt) {
        System.out.println(prompt);
        return getNumericInput(min, max); // Calls the original method
    }

    public static int getNumericInput(int min, int max, String prompt, int defaultValue) {
        System.out.println(prompt + " (Press Enter to select default: " + defaultValue + ")");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue; // Return default if no input is provided
        }
        try {
            int choice = Integer.parseInt(input);
            if (choice >= min && choice <= max) {
                return choice;
            } else {
                System.out.println("Invalid choice. Default value selected: " + defaultValue);
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Default value selected: " + defaultValue);
            return defaultValue;
        }
    }
}
