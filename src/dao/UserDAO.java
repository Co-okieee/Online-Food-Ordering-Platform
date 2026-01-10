package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;
import util.DBConnection;

/**
 * User Data Access Object (DAO)
 *
 * Purpose: Handle all database operations related to users
 * Responsibilities:
 * - User authentication (login)
 * - User CRUD operations (Create, Read, Update, Delete)
 * - User data validation
 * - SQL injection prevention
 *
 * Design Pattern: DAO (Data Access Object)
 * This class separates business logic from database access logic
 *
 * @author Cookie
 * @version 1.0 - Basic structure
 */
public class UserDAO {

    // ========================================
    // SQL Query Constants
    // ========================================

    /**
     * SQL query to select user by username
     * Used for login validation
     */
    private static final String SQL_SELECT_BY_USERNAME =
            "SELECT user_id, username, password, email, full_name, phone, role, status, created_at " +
                    "FROM users " +
                    "WHERE username = ? AND status = 'active'";

    /**
     * SQL query to select user by ID
     * Used for getting user details
     */
    private static final String SQL_SELECT_BY_ID =
            "SELECT user_id, username, password, email, full_name, phone, role, status, created_at " +
                    "FROM users " +
                    "WHERE user_id = ?";

    /**
     * SQL query to select all users
     * Used for admin user management
     */
    private static final String SQL_SELECT_ALL =
            "SELECT user_id, username, email, full_name, phone, role, status, created_at " +
                    "FROM users " +
                    "ORDER BY user_id";

    /**
     * SQL query to insert new user
     * Used for user registration
     */
    private static final String SQL_INSERT_USER =
            "INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) " +
                    "VALUES (users_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, 'active')";

    /**
     * SQL query to update user information
     * Used for profile updates
     */
    private static final String SQL_UPDATE_USER =
            "UPDATE users " +
                    "SET email = ?, full_name = ?, phone = ? " +
                    "WHERE user_id = ?";

    /**
     * SQL query to update user password
     * Used for password change
     */
    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE users " +
                    "SET password = ? " +
                    "WHERE user_id = ?";

    /**
     * SQL query to update user status
     * Used for activating/deactivating users
     */
    private static final String SQL_UPDATE_STATUS =
            "UPDATE users " +
                    "SET status = ? " +
                    "WHERE user_id = ?";

    /**
     * SQL query to delete user
     * Used for user account deletion
     * Note: This will cascade delete to orders (as per foreign key constraint)
     */
    private static final String SQL_DELETE_USER =
            "DELETE FROM users WHERE user_id = ?";

    /**
     * SQL query to check if username exists
     * Used for registration validation
     */
    private static final String SQL_CHECK_USERNAME =
            "SELECT COUNT(*) FROM users WHERE username = ?";

    /**
     * SQL query to check if email exists
     * Used for registration validation
     */
    private static final String SQL_CHECK_EMAIL =
            "SELECT COUNT(*) FROM users WHERE email = ?";

    // ========================================
    // Constructor
    // ========================================

    /**
     * Default constructor
     * UserDAO is stateless - no instance variables needed
     */
    public UserDAO() {
        System.out.println("[UserDAO] UserDAO instance created");
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Convert ResultSet row to User object
     *
     * This helper method extracts user data from a ResultSet
     * and creates a User object.
     *
     * @param rs ResultSet containing user data
     * @return User object with data from ResultSet
     * @throws SQLException if column access fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        // Map database columns to User object properties
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));

        // Note: Password is not included in User object for security
        // Only password hash should be accessed when needed for validation

        return user;
    }

    /**
     * Close database resources safely
     *
     * This method closes ResultSet, PreparedStatement, and Connection
     * in the correct order and handles any errors.
     *
     * @param rs ResultSet to close (can be null)
     * @param stmt PreparedStatement to close (can be null)
     * @param conn Connection to close (can be null)
     */
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        // Close ResultSet
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing ResultSet: " + e.getMessage());
            }
        }

        // Close PreparedStatement
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing PreparedStatement: " + e.getMessage());
            }
        }

        // Close Connection
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing Connection: " + e.getMessage());
            }
        }
    }

    /**
     * Validate user input data
     *
     * Checks if user data meets basic requirements before database operations.
     *
     * @param user User object to validate
     * @return true if valid, false otherwise
     */
    private boolean validateUserData(User user) {
        // Check if user object is null
        if (user == null) {
            System.err.println("[UserDAO] Validation failed: User object is null");
            return false;
        }

        // Check username
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            System.err.println("[UserDAO] Validation failed: Username is empty");
            return false;
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            System.err.println("[UserDAO] Validation failed: Username must be 3-20 characters");
            return false;
        }

        // Check email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            System.err.println("[UserDAO] Validation failed: Email is empty");
            return false;
        }
        if (!user.getEmail().contains("@")) {
            System.err.println("[UserDAO] Validation failed: Invalid email format");
            return false;
        }

        // Check full name
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            System.err.println("[UserDAO] Validation failed: Full name is empty");
            return false;
        }

        // Validation passed
        return true;
    }

    /**
     * Print SQL error details
     *
     * Helper method for debugging SQL errors.
     *
     * @param e SQLException to print
     * @param operation Description of operation that failed
     */
    private void printSQLError(SQLException e, String operation) {
        System.err.println("[UserDAO ERROR] " + operation + " failed");
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
    }

    // ========================================
    // Public DAO Methods (to be implemented)
    // ========================================

    // Method stubs - will be implemented in next commits:
    // - public User login(String username, String password)
    // - public boolean register(User user, String password)
    // - public User getUserById(int userId)
    // - public List<User> getAllUsers()
    // - public boolean updateUser(User user)
    // - public boolean updatePassword(int userId, String newPassword)
    // - public boolean deleteUser(int userId)
    // - public boolean isUsernameExists(String username)
    // - public boolean isEmailExists(String email)

    // ========================================
    // Testing Method
    // ========================================

    /**
     * Test database connection for UserDAO
     *
     * This method verifies that UserDAO can connect to database
     * and access the users table.
     *
     * @return true if test passed, false otherwise
     */
    public boolean testDatabaseAccess() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DBConnection.getConnection();
            System.out.println("[UserDAO] Database connection obtained");

            // Test query: count users in database
            String testSQL = "SELECT COUNT(*) AS user_count FROM users";
            stmt = conn.prepareStatement(testSQL);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("user_count");
                System.out.println("[UserDAO] Users table accessible");
                System.out.println("[UserDAO] Total users in database: " + count);
                return true;
            }

            return false;

        } catch (SQLException e) {
            printSQLError(e, "Database access test");
            return false;

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    // ========================================
    // Main Method (for testing)
    // ========================================

    /**
     * Main method for testing UserDAO
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("UserDAO Test");
        System.out.println("========================================");

        // Create UserDAO instance
        UserDAO userDAO = new UserDAO();

        // Test database access
        if (userDAO.testDatabaseAccess()) {
            System.out.println("\n✓ UserDAO is configured correctly!");
            System.out.println("Ready to implement user operations.");
        } else {
            System.out.println("\n✗ UserDAO test failed!");
            System.out.println("Please check database connection.");
        }
    }
}