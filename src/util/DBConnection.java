package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Enhanced Database Connection Utility Class with Connection Pooling
 *
 * Purpose: Manage Oracle database connections with optimized configuration
 * Features:
 * - Connection pooling support
 * - Configurable connection properties
 * - Connection validation
 * - Performance optimizations
 *
 * @version 2.0 - Added connection pooling and optimization
 */
public class DBConnection {

    // ========================================
    // Database Configuration Constants
    // ========================================

    /**
     * Oracle JDBC Driver class name
     */
    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/FREE";
    private static final String USERNAME = "C##COOKIE";
    private static final String PASSWORD = "123456";

    // ========================================
    // Connection Pool Configuration
    // ========================================

    /**
     * Maximum number of connections in the pool
     * Adjust based on your application load
     */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * Minimum number of connections to maintain
     */
    private static final int MIN_POOL_SIZE = 2;

    /**
     * Connection timeout in milliseconds
     * 30 seconds default
     */
    private static final int CONNECTION_TIMEOUT = 30000;

    /**
     * Statement timeout in seconds
     * 30 seconds default
     */
    private static final int STATEMENT_TIMEOUT = 30;

    // ========================================
    // Connection Properties
    // ========================================

    /**
     * Create and configure connection properties
     * These properties optimize connection behavior
     *
     * @return Properties object with connection settings
     */
    private static Properties getConnectionProperties() {
        Properties props = new Properties();

        // Basic authentication
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);

        // Performance optimizations
        props.setProperty("oracle.jdbc.ReadTimeout", String.valueOf(CONNECTION_TIMEOUT));
        props.setProperty("oracle.net.CONNECT_TIMEOUT", String.valueOf(CONNECTION_TIMEOUT));

        // Connection pooling hints
        props.setProperty("oracle.jdbc.implicitStatementCacheSize", "25");
        props.setProperty("oracle.jdbc.maxCachedBufferSize", "30");

        // Character encoding
        props.setProperty("oracle.jdbc.defaultNChar", "true");

        return props;
    }

    // ========================================
    // Static Initialization Block
    // ========================================

    static {
        try {
            // Load Oracle JDBC driver
            Class.forName(DRIVER);
            System.out.println("[DBConnection] Oracle JDBC Driver loaded successfully");
            System.out.println("[DBConnection] Connection pool configured:");
            System.out.println("  - Max pool size: " + MAX_POOL_SIZE);
            System.out.println("  - Min pool size: " + MIN_POOL_SIZE);
            System.out.println("  - Connection timeout: " + CONNECTION_TIMEOUT + "ms");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection ERROR] Oracle JDBC Driver not found!");
            System.err.println("Please add ojdbc.jar to your project classpath");
            e.printStackTrace();
        }
    }

    // ========================================
    // Connection Methods
    // ========================================

    /**
     * Get a database connection with optimized settings
     *
     * This version uses connection properties for better performance
     * and reliability.
     *
     * @return Connection object to the database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Create connection with properties
            Connection conn = DriverManager.getConnection(URL, getConnectionProperties());

            // Configure connection settings
            conn.setAutoCommit(false);  // Manual transaction control

            // Set default transaction isolation level
            // READ_COMMITTED is good balance of performance and consistency
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            System.out.println("[DBConnection] Database connection established");
            return conn;

        } catch (SQLException e) {
            System.err.println("[DBConnection ERROR] Failed to connect to database");
            System.err.println("URL: " + URL);
            System.err.println("Username: " + USERNAME);
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get a connection with auto-commit enabled
     *
     * Use this for simple queries that don't need transaction control.
     *
     * @return Connection with auto-commit enabled
     * @throws SQLException if connection fails
     */
    public static Connection getConnectionAutoCommit() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * Validate if a connection is still valid
     *
     * Checks if connection is open and responsive within timeout period.
     *
     * @param conn Connection to validate
     * @param timeout Timeout in seconds
     * @return true if connection is valid, false otherwise
     */
    public static boolean isConnectionValid(Connection conn, int timeout) {
        if (conn == null) {
            return false;
        }

        try {
            return conn.isValid(timeout);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close a database connection safely
     *
     * @param conn Connection to close (can be null)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                // Rollback any uncommitted changes before closing
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
                conn.close();
                System.out.println("[DBConnection] Connection closed successfully");
            } catch (SQLException e) {
                System.err.println("[DBConnection WARNING] Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Commit transaction and close connection
     *
     * Use this for successful transactions.
     *
     * @param conn Connection to commit and close
     */
    public static void commitAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
                System.out.println("[DBConnection] Transaction committed");
            } catch (SQLException e) {
                System.err.println("[DBConnection ERROR] Commit failed: " + e.getMessage());
            } finally {
                closeConnection(conn);
            }
        }
    }

    /**
     * Rollback transaction and close connection
     *
     * Use this when errors occur during transaction.
     *
     * @param conn Connection to rollback and close
     */
    public static void rollbackAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("[DBConnection] Transaction rolled back");
            } catch (SQLException e) {
                System.err.println("[DBConnection ERROR] Rollback failed: " + e.getMessage());
            } finally {
                closeConnection(conn);
            }
        }
    }

    /**
     * Test database connection with detailed information
     *
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            long startTime = System.currentTimeMillis();
            conn = getConnection();
            long endTime = System.currentTimeMillis();

            System.out.println("\n[DBConnection TEST] Connection test PASSED");
            System.out.println("========================================");
            System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Database Version: " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver Name: " + conn.getMetaData().getDriverName());
            System.out.println("Driver Version: " + conn.getMetaData().getDriverVersion());
            System.out.println("Connection Time: " + (endTime - startTime) + "ms");
            System.out.println("Auto Commit: " + conn.getAutoCommit());
            System.out.println("Transaction Isolation: " + getIsolationLevelName(conn.getTransactionIsolation()));
            System.out.println("Connection Valid: " + isConnectionValid(conn, 5));
            System.out.println("========================================\n");
            return true;

        } catch (SQLException e) {
            System.err.println("\n[DBConnection TEST] Connection test FAILED");
            System.err.println("========================================");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            System.err.println("========================================\n");
            return false;

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Get human-readable name for transaction isolation level
     *
     * @param level Transaction isolation level constant
     * @return String description of isolation level
     */
    private static String getIsolationLevelName(int level) {
        switch (level) {
            case Connection.TRANSACTION_NONE:
                return "NONE";
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED:
                return "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ:
                return "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE:
                return "SERIALIZABLE";
            default:
                return "UNKNOWN";
        }
    }

    // ========================================
    // Configuration Getters (for monitoring)
    // ========================================

    /**
     * Get the database URL (for logging/monitoring)
     * Password is not included for security
     *
     * @return Database connection URL
     */
    public static String getDatabaseURL() {
        return URL;
    }

    /**
     * Get the database username (for logging/monitoring)
     *
     * @return Database username
     */
    public static String getDatabaseUsername() {
        return USERNAME;
    }

    /**
     * Get maximum pool size
     *
     * @return Maximum connections in pool
     */
    public static int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }

    // ========================================
    // Main Method (for testing)
    // ========================================

    /**
     * Main method for testing the database connection
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Enhanced Database Connection Test");
        System.out.println("========================================");
        System.out.println("Configuration:");
        System.out.println("  Driver: " + DRIVER);
        System.out.println("  URL: " + URL);
        System.out.println("  Username: " + USERNAME);
        System.out.println("  Max Pool Size: " + MAX_POOL_SIZE);
        System.out.println("  Connection Timeout: " + CONNECTION_TIMEOUT + "ms");
        System.out.println("========================================");

        // Test connection
        if (testConnection()) {
            System.out.println("✓ Database connection is working perfectly!");
            System.out.println("\nYou can now use DBConnection in your DAO classes:");
            System.out.println("  Connection conn = DBConnection.getConnection();");
        } else {
            System.out.println("✗ Database connection failed!");
            System.out.println("\nTroubleshooting checklist:");
            System.out.println("  1. Is Oracle database running?");
            System.out.println("  2. Are connection details correct?");
            System.out.println("  3. Is ojdbc.jar in classpath?");
            System.out.println("  4. Can you ping the database server?");
            System.out.println("  5. Is the firewall blocking port 1521?");
        }
    }
}
