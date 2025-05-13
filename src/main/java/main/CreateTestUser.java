package main;

import java.io.IOException;
import java.sql.SQLException;

import entities.user;
import services.userService;
import utils.MyConnection;

public class CreateTestUser {
    public static void main(String[] args) {
        try {
            // Test database connection first
            MyConnection.getInstance().getConnection();
            System.out.println("Database connection successful!");
            
            userService userService = new userService();
            
            // Create a test user
            user testUser = new user(
                "testuser",           // username
                "test@example.com",   // email
                "password123",        // password
                "Test",              // firstname
                "User",              // lastname
                "2000-01-01",        // birthday
                "Male",              // gender
                "",                  // picture (empty for now)
                "1234567890",        // phonenumber
                1,                   // level
                2                    // role_id (2 for regular user)
            );
            
            userService.addUser(testUser);
            System.out.println("Test user created successfully!");
            System.out.println("Username: testuser");
            System.out.println("Password: password123");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 