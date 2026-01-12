package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Utility Class
 * Manages Oracle database connections
 * Provides connection pooling and resource management
 *
 * Usage:
 * Connection conn = DBConnection.getConnection();
 * // Use connection...
 * DBConnection.closeConnection(conn);
 */
public class DBConnection {

    // ================================
    // Database Configuration
    // ================================

    // Oracle JDBC Driver
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";

    // Database URL (Modify according to your Oracle setup)
    // Format: jdbc:oracle:thin:@hostname:port:SID
    // For Oracle XE (Express Edition): jdbc:oracle:thin:@localhost:1521:XE
    // For Oracle with Service Name: jdbc:oracle:thin:@//hostname:port/servicename
    private static final String DB_URL = "jdbc:oracle:thin:@//localhost:1521/FREE";
    private static final String DB_USERNAME = "C##COOKIE";
    private static final String DB_PASSWORD = "123456";

    // Connection timeout (30 seconds)
    private static final int CONNECTION_TIMEOUT = 30;

    // Static initialization block to load JDBC driver
    static {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Oracle JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Oracle JDBC Driver not found!");
            System.err.println("Make sure ojdbc8.jar or ojdbc11.jar is in your classpath");
            e.printStackTrace();
        }
    }

    // ================================
    // Connection Methods
    // ================================

    /**
     * Get a database connection
     * @return Connection object, or null if connection fails
     */
    public static Connection getConnection() {
        Connection connection = null;

        try {
            // Set connection timeout
            DriverManager.setLoginTimeout(CONNECTION_TIMEOUT);

            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Set auto-commit to false for transaction control
            connection.setAutoCommit(false);

            System.out.println("Database connection established successfully");

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to establish database connection");
            System.err.println("Database URL: " + DB_URL);
            System.err.println("Username: " + DB_USERNAME);
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Close a database connection
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                // Commit any pending transactions before closing
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }

                connection.close();
                System.out.println("Database connection closed successfully");

            } catch (SQLException e) {
                System.err.println("ERROR: Failed to close database connection");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void closeResources(Connection connection, java.sql.PreparedStatement statement, java.sql.ResultSet resultSet) {
        // Close ResultSet
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to close ResultSet");
                e.printStackTrace();
            }
        }

        // Close PreparedStatement
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to close PreparedStatement");
                e.printStackTrace();
            }
        }

        // Close Connection
        closeConnection(connection);
    }

    /**
     * Close database resources (Connection and PreparedStatement only)
     * Overloaded method when there's no ResultSet
     * @param connection Connection to close
     * @param statement PreparedStatement to close
     */
    public static void closeResources(Connection connection, java.sql.PreparedStatement statement) {
        // Close PreparedStatement
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to close PreparedStatement");
                e.printStackTrace();
            }
        }

        // Close Connection
        closeConnection(connection);
    }

    /**
     * Rollback transaction and close connection
     * Use this when an error occurs during transaction
     * @param connection Connection to rollback and close
     */
    public static void rollbackAndClose(Connection connection) {
        if (connection != null) {
            try {
                // Rollback any pending transactions
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    System.out.println("Transaction rolled back");
                }

                connection.close();
                System.out.println("Database connection closed after rollback");

            } catch (SQLException e) {
                System.err.println("ERROR: Failed to rollback and close connection");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Commit transaction
     * @param connection Connection to commit
     * @return true if commit successful, false otherwise
     */
    public static boolean commit(Connection connection) {
        if (connection != null) {
            try {
                connection.commit();
                System.out.println("Transaction committed successfully");
                return true;
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to commit transaction");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Rollback transaction
     * @param connection Connection to rollback
     * @return true if rollback successful, false otherwise
     */
    public static boolean rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                System.out.println("Transaction rolled back successfully");
                return true;
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to rollback transaction");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    // ================================
    // Test Connection Method
    // ================================

    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        System.out.println("===================================");
        System.out.println("Testing Database Connection...");
        System.out.println("===================================");
        System.out.println("Driver: " + JDBC_DRIVER);
        System.out.println("URL: " + DB_URL);
        System.out.println("Username: " + DB_USERNAME);
        System.out.println("-----------------------------------");

        Connection connection = null;

        try {
            connection = getConnection();

            if (connection != null && !connection.isClosed()) {
                System.out.println("✓ Connection successful!");
                System.out.println("✓ Database product: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("✓ Database version: " + connection.getMetaData().getDatabaseProductVersion());
                System.out.println("===================================");
                return true;
            } else {
                System.out.println("✗ Connection failed!");
                System.out.println("===================================");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("✗ Connection test failed!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("===================================");
            e.printStackTrace();
            return false;

        } finally {
            closeConnection(connection);
        }
    }

    // ================================
    // Getters for Configuration (for debugging)
    // ================================

    /**
     * Get database URL (for debugging purposes)
     */
    public static String getDatabaseURL() {
        return DB_URL;
    }

    /**
     * Get database username (for debugging purposes)
     */
    public static String getDatabaseUsername() {
        return DB_USERNAME;
    }

    // ================================
    // Main Method for Testing
    // ================================

    /**
     * Main method for testing database connection
     * Run this to verify your database configuration
     */
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║   Oracle Database Connection Test    ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println();

        // Test connection
        boolean success = testConnection();

        if (success) {
            System.out.println("\n✓ Database is ready to use!");
            System.out.println("You can now run your application.\n");
        } else {
            System.out.println("\n✗ Database connection failed!");
            System.out.println("\nTroubleshooting steps:");
            System.out.println("1. Check if Oracle Database is running");
            System.out.println("2. Verify database URL, username, and password in DBConnection.java");
            System.out.println("3. Ensure ojdbc jar file is in your classpath");
            System.out.println("4. Check if port 1521 is not blocked by firewall");
            System.out.println("5. Verify Oracle service/SID name (default: XE for Express Edition)\n");
        }
    }
}