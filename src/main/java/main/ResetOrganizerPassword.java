package main;

import java.sql.SQLException;

import services.userService;

public class ResetOrganizerPassword {
    public static void main(String[] args) {
        userService userService = new userService();
        try {
            // Reset password for the organizer account
            userService.resetOrganizerPassword("test@gmail.com", "Obukarhyv8@@");
            System.out.println("Password reset completed. Please try logging in again.");
        } catch (SQLException e) {
            System.out.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 