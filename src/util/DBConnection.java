package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Connection Utility Class
 * Responsibility: Manage database connections using JDBC
 * 
 * IMPROVED VERSION:
 * - Reads configuration from db.properties file
 * - No hardcoded credentials
 * - Better security and flexibility
 */
public class DBConnection {
    
    // Database connection parameters (loaded from properties file)
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    // Singleton instance
    private static DBConnection instance;
    
    /**
     * Private constructor (Singleton pattern)
     * Loads database configuration from properties file
     */
    private DBConnection() {
        loadConfiguration();
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load database driver: " + e.getMessage());
        }
    }
    
    /**
     * Load database configuration from db.properties file
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        InputStream input = null;
        
        try {
            // Try to load from classpath
            input = getClass().getClassLoader().getResourceAsStream("db.properties");
            
            if (input == null) {
                // Fallback to default values if properties file not found
                System.err.println("Warning: db.properties not found, using default configuration");
                useDefaultConfiguration();
                return;
            }
            
            props.load(input);
            
            DB_URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/food_ordering_system");
            DB_USER = props.getProperty("db.user", "root");
            DB_PASSWORD = props.getProperty("db.password", "");
            DB_DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading db.properties: " + e.getMessage());
            useDefaultConfiguration();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Use default configuration (fallback)
     */
    private void useDefaultConfiguration() {
        DB_URL = "jdbc:mysql://localhost:3306/food_ordering_system";
        DB_USER = "root";
        DB_PASSWORD = "password";
        DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    }
    
    /**
     * Get singleton instance
     * @return DBConnection instance
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Close database connection
     * @param connection Connection to close
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get current database URL (for debugging)
     * @return Database URL
     */
    public String getDatabaseUrl() {
        return DB_URL;
    }
}
