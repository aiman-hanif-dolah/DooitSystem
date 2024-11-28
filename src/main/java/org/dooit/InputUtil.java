package org.dooit;

import java.util.Scanner;

public class InputUtil {

    private static final Scanner scanner = new Scanner(System.in);

    public static int getNumericInput(int min, int max) {
        int choice;
        while (true) {
            try {
                System.out.print("Enter choice (" + min + "-" + max + "): ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Additional utility methods for table formatting can be added here if needed
}