package org.dooit;

public class Main {

    private static User currentUser = null; // Track the logged-in user

    public static void main(String[] args) {
        while (true) { // Loop continuously until the user decides to exit
            if (currentUser == null) {
                currentUser = UserService.showLoginRegisterMenu();
            } else {
                currentUser = Menu.showMainMenu(currentUser);
            }
        }
    }
}

//HELLO