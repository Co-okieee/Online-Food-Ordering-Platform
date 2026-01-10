package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Utility Class
 *
 * Purpose: Manage Oracle database connections for the Food Ordering System
 * Features:
 * - Load JDBC driver automatically
 * - Provide connection objects to DAO classes
 * - Handle connection errors
 *
 * @author Cookie
 */
public class DBConnection {

    // ========================================
    // Database Configuration Constants
    // ========================================

    /**
     * Oracle JDBC Driver class name
     * For Oracle 11g and above
     */
    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/FREE";
    private static final String USERNAME = "SYS";
    private static final String PASSWORD = "123456";

    // ========================================
    // Static Initialization Block
    // ========================================

    /**
     * Load the Oracle JDBC driver when class is loaded
     * This only needs to happen once
     */
    static {
        try {
            // Load the Oracle JDBC driver class
            Class.forName(DRIVER);
            System.out.println("[DBConnection] Oracle JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            // Driver not found - check if ojdbc jar is in classpath
            System.err.println("[DBConnection ERROR] Oracle JDBC Driver not found!");
            System.err.println("Please add ojdbc.jar to your project classpath");
            e.printStackTrace();
        }
    }

    // ========================================
    // Connection Methods
    // ========================================

    /**
     * Get a database connection
     *
     * This method creates a new connection to the Oracle database
     * using the configured URL, username, and password.
     *
     * Usage example:
     * <pre>
     * Connection conn = DBConnection.getConnection();
     * // Use connection...
     * conn.close();
     * </pre>
     *
     * @return Connection object to the database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Attempt to establish connection
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            // Set auto-commit to false for transaction control
            conn.setAutoCommit(false);

            System.out.println("[DBConnection] Database connection established");
            return conn;

        } catch (SQLException e) {
            // Connection failed - provide detailed error message
            System.err.println("[DBConnection ERROR] Failed to connect to database");
            System.err.println("URL: " + URL);
            System.err.println("Username: " + USERNAME);
            System.err.println("Error: " + e.getMessage());

            // Re-throw exception to caller
            throw e;
        }
    }

    /**
     * Close a database connection
     *
     * This method safely closes a connection and handles any errors.
     * It's safe to call even if connection is null or already closed.
     *
     * Usage example:
     * <pre>
     * Connection conn = null;
     * try {
     *     conn = DBConnection.getConnection();
     *     // Use connection...
     * } finally {
     *     DBConnection.closeConnection(conn);
     * }
     * </pre>
     *
     * @param conn Connection to close (can be null)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                // Close the connection
                conn.close();
                System.out.println("[DBConnection] Connection closed successfully");
            } catch (SQLException e) {
                // Error closing connection (usually not critical)
                System.err.println("[DBConnection WARNING] Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test the database connection
     *
     * This method attempts to connect to the database and prints
     * success or failure message. Useful for testing configuration.
     *
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            // Try to get connection
            conn = getConnection();

            // If we get here, connection was successful
            System.out.println("[DBConnection TEST] Connection test PASSED");
            System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
            return true;

        } catch (SQLException e) {
            // Connection test failed
            System.err.println("[DBConnection TEST] Connection test FAILED");
            System.err.println("Error: " + e.getMessage());
            return false;

        } finally {
            // Always close connection
            closeConnection(conn);
        }
    }

    // ========================================
    // Main Method (for testing)
    // ========================================

    /**
     * Main method for testing the database connection
     *
     * Run this class directly to test if your database
     * connection is configured correctly.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Database Connection Test");
        System.out.println("========================================");
        System.out.println("Driver: " + DRIVER);
        System.out.println("URL: " + URL);
        System.out.println("Username: " + USERNAME);
        System.out.println("========================================");

        // Test connection
        if (testConnection()) {
            System.out.println("\n✓ Database connection is working!");
        } else {
            System.out.println("\n✗ Database connection failed!");
            System.out.println("\nPlease check:");
            System.out.println("1. Oracle database is running");
            System.out.println("2. Connection details (URL, username, password) are correct");
            System.out.println("3. ojdbc.jar is in classpath");
        }
    }
}