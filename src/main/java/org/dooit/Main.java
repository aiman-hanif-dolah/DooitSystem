package org.dooit;

public class Main {

    public static void main(String[] args) {
        User currentUser = null; // Initialize the current user

        while (true) { // Loop continuously until the user decides to exit
            if (currentUser == null) {
                // Show login or register menu
                currentUser = UserService.showLoginRegisterMenu();

                if (currentUser == null) {
                    // If the user chooses to exit in the login/register menu, terminate the program
                    System.out.println("Exiting the application. Goodbye!");
                    break;
                }
            } else {
                // Show the main menu for the logged-in user
                currentUser = Menu.showMainMenu(currentUser);

                if (currentUser == null) {
                    // If the user logs out or exits from the main menu
                    System.out.println("Returning to login/register menu...");
                }
            }
        }
    }
}
