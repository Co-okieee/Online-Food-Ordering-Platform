package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.User;
import util.DBConnection;

/**
 * User Data Access Object (DAO) - Implementation
 * Responsibility: Handle database operations for User entity
 * 
 */
public class UserDAO {
    
    // ========================================
    // SQL Query Constants
    // ========================================
    
    private static final String SQL_SELECT_BY_USERNAME =
            "SELECT user_id, username, password, email, full_name, phone, role, status, created_at " +
            "FROM users WHERE username = ?";
    
    private static final String SQL_SELECT_BY_USERNAME_PASSWORD =
            "SELECT user_id, username, email, full_name, phone, role, status, created_at " +
            "FROM users WHERE username = ? AND password = ? AND status = 'active'";
    
    private static final String SQL_SELECT_BY_ID =
            "SELECT user_id, username, email, full_name, phone, role, status, created_at " +
            "FROM users WHERE user_id = ?";
    
    private static final String SQL_SELECT_BY_EMAIL =
            "SELECT user_id, username, email, full_name, phone, role, status, created_at " +
            "FROM users WHERE email = ?";
    
    private static final String SQL_INSERT_USER =
            "INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) " +
            "VALUES (users_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, 'active')";
    
    private static final String SQL_UPDATE_USER =
            "UPDATE users SET email = ?, full_name = ?, phone = ? WHERE user_id = ?";
    
    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE users SET password = ? WHERE user_id = ?";
    
    private static final String SQL_DELETE_USER =
            "DELETE FROM users WHERE user_id = ?";
    
    // ========================================
    // Constructor
    // ========================================
    
    public UserDAO() {
        System.out.println("[UserDAO] Instance created");
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    /**
     * Map ResultSet to User object
     * 
     * @param rs ResultSet containing user data
     * @return User object
     * @throws SQLException if column access fails
     */
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
    
    /**
     * Close database resources safely
     * 
     * @param rs ResultSet to close
     * @param stmt PreparedStatement to close
     * @param conn Connection to close
     */
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing ResultSet: " + e.getMessage());
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing Statement: " + e.getMessage());
            }
        }
        
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[UserDAO] Error closing Connection: " + e.getMessage());
            }
        }
    }
    
    // ========================================
    // CRUD Operations
    // ========================================
    
    /**
     * Find user by username
     * 
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    public User findByUserName(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[UserDAO] findByUserName: Username is null or empty");
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_USERNAME);
            stmt.setString(1, username.trim());
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                conn.commit();
                System.out.println("[UserDAO] User found: " + username);
                return user;
            }
            
            conn.commit();
            System.out.println("[UserDAO] User not found: " + username);
            return null;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in findByUserName: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
            
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find user by username and password (for authentication)
     * 
     * @param username Username
     * @param password Hashed password
     * @return User object if credentials match, null otherwise
     */
    public User findByUserNameAndPassword(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.err.println("[UserDAO] findByUserNameAndPassword: Invalid parameters");
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_USERNAME_PASSWORD);
            stmt.setString(1, username.trim());
            stmt.setString(2, password.trim());
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                conn.commit();
                System.out.println("[UserDAO] Login successful: " + username);
                return user;
            }
            
            conn.commit();
            System.out.println("[UserDAO] Login failed: Invalid credentials for " + username);
            return null;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in findByUserNameAndPassword: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
            
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Create a new user
     * 
     * @param user User object to create
     * @param hashedPassword Hashed password
     * @return Generated user ID, or -1 if failed
     */
    public int create(User user, String hashedPassword) {
        if (user == null || hashedPassword == null) {
            System.err.println("[UserDAO] create: User or password is null");
            return -1;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_INSERT_USER, new String[]{"user_id"});
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getRole());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get generated user ID
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    conn.commit();
                    System.out.println("[UserDAO] User created successfully: " + user.getUsername() + " (ID: " + userId + ")");
                    return userId;
                }
            }
            
            conn.rollback();
            System.err.println("[UserDAO] Failed to create user: " + user.getUsername());
            return -1;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in create: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return -1;
            
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Update existing user
     * 
     * @param user User object with updated information
     * @return Number of rows affected
     */
    public int update(User user) {
        if (user == null || user.getUserId() <= 0) {
            System.err.println("[UserDAO] update: Invalid user");
            return 0;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_USER);
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getPhone());
            stmt.setInt(4, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                conn.commit();
                System.out.println("[UserDAO] User updated successfully: ID=" + user.getUserId());
            } else {
                conn.rollback();
                System.err.println("[UserDAO] Failed to update user: ID=" + user.getUserId());
            }
            
            return rowsAffected;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in update: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
            
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * Delete user by ID
     * 
     * @param userId User ID to delete
     * @return Number of rows affected
     */
    public int deleteById(int userId) {
        if (userId <= 0) {
            System.err.println("[UserDAO] deleteById: Invalid user ID");
            return 0;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_DELETE_USER);
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                conn.commit();
                System.out.println("[UserDAO] User deleted successfully: ID=" + userId);
            } else {
                conn.rollback();
                System.err.println("[UserDAO] Failed to delete user: ID=" + userId);
            }
            
            return rowsAffected;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in deleteById: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
            
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * Find user by ID
     * 
     * @param userId User ID to search for
     * @return User object if found, null otherwise
     */
    public User findById(int userId) {
        if (userId <= 0) {
            System.err.println("[UserDAO] findById: Invalid user ID");
            return null;
        }
        
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
                System.out.println("[UserDAO] User found: ID=" + userId);
                return user;
            }
            
            conn.commit();
            System.out.println("[UserDAO] User not found: ID=" + userId);
            return null;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in findById: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
            
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find user by email
     * 
     * @param email Email address to search for
     * @return User object if found, null otherwise
     */
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("[UserDAO] findByEmail: Email is null or empty");
            return null;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_EMAIL);
            stmt.setString(1, email.trim());
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                conn.commit();
                System.out.println("[UserDAO] User found by email: " + email);
                return user;
            }
            
            conn.commit();
            System.out.println("[UserDAO] User not found by email: " + email);
            return null;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in findByEmail: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
            
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Update user password
     * 
     * @param userId User ID
     * @param newHashedPassword New hashed password
     * @return Number of rows affected
     */
    public int updatePassword(int userId, String newHashedPassword) {
        if (userId <= 0 || newHashedPassword == null || newHashedPassword.trim().isEmpty()) {
            System.err.println("[UserDAO] updatePassword: Invalid parameters");
            return 0;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_PASSWORD);
            
            stmt.setString(1, newHashedPassword.trim());
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                conn.commit();
                System.out.println("[UserDAO] Password updated successfully: ID=" + userId);
            } else {
                conn.rollback();
                System.err.println("[UserDAO] Failed to update password: ID=" + userId);
            }
            
            return rowsAffected;
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in updatePassword: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
            
        } finally {
            closeResources(null, stmt, conn);
        }
    }
}