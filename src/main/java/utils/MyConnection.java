package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private final String URL = "jdbc:mysql://localhost:3306/eventuras";
    private final String USER = "root";
    private final String PASS = "";
    private Connection connection;
    private static MyConnection instance;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private MyConnection() {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Connection established successfully");
                return;
            } catch (SQLException e) {
                retries++;
                System.err.println("Connection attempt " + retries + " failed: " + e.getMessage());
                if (retries < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        if (connection == null) {
            System.err.println("Failed to establish database connection after " + MAX_RETRIES + " attempts");
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            synchronized (MyConnection.class) {
                if (instance == null) {
                    instance = new MyConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new RuntimeException("Database connection is not available");
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
