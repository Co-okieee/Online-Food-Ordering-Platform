package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;
import util.DBConnection;

/**
 * User Data Access Object (DAO) - With Login Functionality
 *
 * @author Cookie
 * @version 2.0 - Added login validation method
 */
public class UserDAO {

    // ========================================
    // SQL Query Constants
    // ========================================

    private static final String SQL_SELECT_BY_USERNAME =
            "SELECT user_id, username, password, email, full_name, phone, role, status, created_at " +
                    "FROM users " +
                    "WHERE username = ? AND status = 'active'";

    private static final String SQL_SELECT_BY_ID =
            "SELECT user_id, username, password, email, full_name, phone, role, status, created_at " +
                    "FROM users " +
                    "WHERE user_id = ?";

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) " +
                    "VALUES (users_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, 'active')";

    private static final String SQL_UPDATE_USER =
            "UPDATE users SET email = ?, full_name = ?, phone = ? WHERE user_id = ?";

    private static final String SQL_DELETE_USER =
            "DELETE FROM users WHERE user_id = ?";

    private static final String SQL_CHECK_USERNAME =
            "SELECT COUNT(*) FROM users WHERE username = ?";

    // ========================================
    // Constructor
    // ========================================

    public UserDAO() {
        System.out.println("[UserDAO] UserDAO instance created");
    }

    // ========================================
    // Helper Methods
    // ========================================

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing PreparedStatement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing Connection: " + e.getMessage());
            }
        }
    }

    private void printSQLError(SQLException e, String operation) {
        System.err.println("[UserDAO ERROR] " + operation + " failed");
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Message: " + e.getMessage());
    }

    // ========================================
    // Login Validation Method
    // ========================================

    /**
     * Validate user login credentials
     *
     * This method checks if the username exists and the password matches.
     * For security, passwords should be hashed using BCrypt before comparison.
     *
     * Process:
     * 1. Input validation
     * 2. Database query by username
     * 3. Password verification (plain text for now, BCrypt recommended)
     * 4. Return User object if valid, null otherwise
     *
     * @param username Username entered by user
     * @param password Password entered by user (plain text)
     * @return User object if login successful, null otherwise
     */
    public User login(String username, String password) {
        // ========================================
        // Step 1: Input Validation
        // ========================================

        // Check for null or empty username
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[UserDAO] Login failed: Username is empty");
            return null;
        }

        // Check for null or empty password
        if (password == null || password.trim().isEmpty()) {
            System.err.println("[UserDAO] Login failed: Password is empty");
            return null;
        }

        System.out.println("[UserDAO] Attempting login for username: " + username);

        // ========================================
        // Step 2: Database Resources
        // ========================================

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DBConnection.getConnection();
            System.out.println("[UserDAO] Database connection obtained");

            // ========================================
            // Step 3: Prepare SQL Statement
            // ========================================

            // Prepare statement with username parameter
            stmt = conn.prepareStatement(SQL_SELECT_BY_USERNAME);
            stmt.setString(1, username);  // Set username parameter (prevents SQL injection)

            System.out.println("[UserDAO] Executing query: " + SQL_SELECT_BY_USERNAME);

            // ========================================
            // Step 4: Execute Query
            // ========================================

            rs = stmt.executeQuery();

            // ========================================
            // Step 5: Process Results
            // ========================================

            if (rs.next()) {
                // User found - retrieve password hash from database
                String storedPassword = rs.getString("password");

                System.out.println("[UserDAO] User found in database");

                // ========================================
                // Step 6: Password Verification
                // ========================================

                // IMPORTANT: In production, use BCrypt for password comparison:
                // if (BCrypt.checkpw(password, storedPassword)) {

                // For now, using simple string comparison
                // TODO: Replace with BCrypt verification
                if (password.equals(storedPassword)) {
                    // Password matches - create User object
                    System.out.println("[UserDAO] Password verification successful");

                    // Map database result to User object
                    User user = mapResultSetToUser(rs);

                    // Commit transaction
                    conn.commit();

                    System.out.println("[UserDAO] Login successful for user: " + user.getUsername());
                    System.out.println("[UserDAO] User role: " + user.getRole());

                    return user;

                } else {
                    // Password doesn't match
                    System.err.println("[UserDAO] Login failed: Incorrect password");
                    conn.rollback();
                    return null;
                }

            } else {
                // No user found with this username
                System.err.println("[UserDAO] Login failed: Username not found");
                conn.rollback();
                return null;
            }

        } catch (SQLException e) {
            // Database error occurred
            printSQLError(e, "Login");

            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[UserDAO] Rollback failed: " + rollbackEx.getMessage());
                }
            }

            return null;

        } finally {
            // Always close resources
            closeResources(rs, stmt, conn);
            System.out.println("[UserDAO] Resources closed");
        }
    }

    /**
     * Validate user login with BCrypt password hashing
     *
     * This is the RECOMMENDED method for production use.
     * Requires BCrypt library (jbcrypt or spring-security-crypto)
     *
     * @param username Username entered by user
     * @param password Password entered by user (plain text)
     * @return User object if login successful, null otherwise
     */
    /*
    public User loginSecure(String username, String password) {
        // Input validation
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[UserDAO] Login failed: Username is empty");
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            System.err.println("[UserDAO] Login failed: Password is empty");
            return null;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_USERNAME);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");

                // Use BCrypt to verify password
                // Requires: import org.mindrot.jbcrypt.BCrypt;
                if (BCrypt.checkpw(password, storedPasswordHash)) {
                    User user = mapResultSetToUser(rs);
                    conn.commit();
                    System.out.println("[UserDAO] Secure login successful");
                    return user;
                } else {
                    System.err.println("[UserDAO] Login failed: Incorrect password");
                    conn.rollback();
                    return null;
                }
            } else {
                System.err.println("[UserDAO] Login failed: Username not found");
                conn.rollback();
                return null;
            }

        } catch (SQLException e) {
            printSQLError(e, "Secure Login");
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            return null;

        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    */

    // ========================================
    // Additional Query Methods
    // ========================================

    /**
     * Get user by user ID
     *
     * @param userId User ID to search for
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                conn.commit();
                System.out.println("[UserDAO] User found: " + user.getUsername());
                return user;
            } else {
                System.out.println("[UserDAO] User not found with ID: " + userId);
                conn.rollback();
                return null;
            }

        } catch (SQLException e) {
            printSQLError(e, "Get user by ID");
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            return null;

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Check if username already exists
     *
     * Useful for registration validation
     *
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_CHECK_USERNAME);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                conn.commit();
                return count > 0;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            printSQLError(e, "Check username exists");
            return false;

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    // ========================================
    // Testing Method
    // ========================================

    /**
     * Test login functionality
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("UserDAO Login Test");
        System.out.println("========================================");

        UserDAO userDAO = new UserDAO();

        // Test 1: Valid login (use test user from sample data)
        System.out.println("\n--- Test 1: Valid Login ---");
        User user = userDAO.login("admin", "password123");
        if (user != null) {
            System.out.println("✓ Login successful!");
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Status: " + user.getStatus());
        } else {
            System.out.println("✗ Login failed!");
        }

        // Test 2: Invalid password
        System.out.println("\n--- Test 2: Invalid Password ---");
        user = userDAO.login("admin", "wrongpassword");
        if (user == null) {
            System.out.println("✓ Correctly rejected invalid password");
        } else {
            System.out.println("✗ Should have rejected invalid password");
        }

        // Test 3: Invalid username
        System.out.println("\n--- Test 3: Invalid Username ---");
        user = userDAO.login("nonexistent", "password123");
        if (user == null) {
            System.out.println("✓ Correctly rejected invalid username");
        } else {
            System.out.println("✗ Should have rejected invalid username");
        }

        // Test 4: Check username exists
        System.out.println("\n--- Test 4: Check Username Exists ---");
        boolean exists = userDAO.isUsernameExists("admin");
        System.out.println("Username 'admin' exists: " + (exists ? "✓ Yes" : "✗ No"));

        System.out.println("\n========================================");
        System.out.println("Login tests complete!");
        System.out.println("========================================");
    }
}