package model;

import java.sql.Timestamp;

/**
 * User Model Class
 *
 * Purpose: Represent a user entity in the Food Ordering System
 * This class maps to the 'users' table in the database
 *
 * Design Pattern: JavaBean / POJO (Plain Old Java Object)
 * - Private fields
 * - Public getters and setters
 * - Default constructor
 *
 * @author Cookie
 * @version 1.0
 */
public class User {

    // ========================================
    // Private Fields (match database columns)
    // ========================================

    /**
     * Unique user identifier (Primary Key)
     */
    private int userId;

    /**
     * Username for login (3-20 characters, unique)
     */
    private String username;

    /**
     * Email address (unique)
     */
    private String email;

    /**
     * User's full name
     */
    private String fullName;

    /**
     * Phone number (optional)
     */
    private String phone;

    /**
     * User role: 'user' or 'admin'
     */
    private String role;

    /**
     * Account status: 'active', 'inactive', or 'suspended'
     */
    private String status;

    /**
     * Timestamp when account was created
     */
    private Timestamp createdAt;

    /**
     * Timestamp when account was last updated
     */
    private Timestamp updatedAt;

    // Note: Password is NOT stored in this object for security
    // Password hashes are only accessed during authentication

    // ========================================
    // Constructors
    // ========================================

    /**
     * Default constructor
     * Required for JavaBean specification
     */
    public User() {
        // Empty constructor
    }

    /**
     * Constructor with essential fields
     *
     * @param username Username for login
     * @param email User's email address
     * @param fullName User's full name
     */
    public User(String username, String email, String fullName) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = "user";  // Default role
        this.status = "active";  // Default status
    }

    /**
     * Constructor with all fields except timestamps
     *
     * @param userId User ID
     * @param username Username
     * @param email Email address
     * @param fullName Full name
     * @param phone Phone number
     * @param role User role
     * @param status Account status
     */
    public User(int userId, String username, String email, String fullName,
                String phone, String role, String status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }

    // ========================================
    // Getters and Setters
    // ========================================

    /**
     * Get user ID
     * @return User ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set user ID
     * @param userId User ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Get username
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username
     * @param username Username (3-20 characters)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get email address
     * @return Email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email address
     * @param email Email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get full name
     * @return Full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Set full name
     * @param fullName Full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Get phone number
     * @return Phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set phone number
     * @param phone Phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get user role
     * @return Role ('user' or 'admin')
     */
    public String getRole() {
        return role;
    }

    /**
     * Set user role
     * @param role Role ('user' or 'admin')
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Get account status
     * @return Status ('active', 'inactive', or 'suspended')
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set account status
     * @param status Status ('active', 'inactive', or 'suspended')
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get creation timestamp
     * @return Timestamp when account was created
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Set creation timestamp
     * @param createdAt Creation timestamp
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get last update timestamp
     * @return Timestamp when account was last updated
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set last update timestamp
     * @param updatedAt Update timestamp
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Check if user is an administrator
     * @return true if user role is 'admin', false otherwise
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    /**
     * Check if account is active
     * @return true if status is 'active', false otherwise
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    /**
     * Get display name for UI
     * Returns full name if available, otherwise username
     * @return Display name
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return username;
    }

    // ========================================
    // Override Methods
    // ========================================

    /**
     * String representation of User object
     * Useful for debugging and logging
     * Note: Does not include sensitive information
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Check if two User objects are equal
     * Two users are equal if they have the same userId
     *
     * @param obj Object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;
        return userId == user.userId;
    }

    /**
     * Generate hash code for User object
     * Based on userId
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }
}