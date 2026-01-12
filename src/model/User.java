package model;

import java.sql.Timestamp;

/**
 * User Model Class
 * Represents a user account in the system
 * Corresponds to USERS table in database
 */
public class User {
    
    // Primary Key
    private int userId;
    
    // Login Credentials
    private String username;
    private String password;
    
    // Contact Information
    private String email;
    private String fullName;
    private String phone;
    
    // Account Settings
    private String role;      // 'user' or 'admin'
    private String status;    // 'active', 'inactive', 'suspended'
    
    // Timestamps
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor for registration (without ID)
     */
    public User(String username, String password, String email, String fullName, String phone, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.status = "active";
    }
    
    /**
     * Full constructor (with ID - for database retrieval)
     */
    public User(int userId, String username, String password, String email, 
                String fullName, String phone, String role, String status,
                Timestamp createdAt, Timestamp updatedAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility Methods
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if account is active
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(this.status);
    }
    
    /**
     * Get safe user object for JSON response (without password)
     */
    public UserDTO toDTO() {
        UserDTO dto = new UserDTO();
        dto.setUserId(this.userId);
        dto.setUsername(this.username);
        dto.setEmail(this.email);
        dto.setFullName(this.fullName);
        dto.setPhone(this.phone);
        dto.setRole(this.role);
        dto.setStatus(this.status);
        dto.setCreatedAt(this.createdAt);
        return dto;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
    /**
     * Data Transfer Object for User (without sensitive data)
     * Used for JSON responses to frontend
     */
    public static class UserDTO {
        private int userId;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String role;
        private String status;
        private Timestamp createdAt;
        
        // Getters and Setters
        
        public int getUserId() {
            return userId;
        }
        
        public void setUserId(int userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public Timestamp getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }
    }
}