package service;

import dao.UserDAO;
import model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

/**
 * User Service Class
 * Handles business logic for user operations
 * Including registration, login, password hashing, etc.
 */
public class UserService {
    
    private UserDAO userDAO;
    
    /**
     * Constructor
     */
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    // ================================
    // Registration
    // ================================
    
    /**
     * Register a new user
     * @param username Username (3-20 chars)
     * @param password Plain text password (will be hashed)
     * @param email User email
     * @param fullName User full name
     * @param phone User phone (optional)
     * @param role User role ('user' or 'admin')
     * @return Created user object with ID, or null if failed
     */
    public User registerUser(String username, String password, String email, 
                            String fullName, String phone, String role) {
        try {
            // Validate input
            if (!validateRegistrationInput(username, password, email)) {
                return null;
            }
            
            // Check if username already exists
            if (userDAO.usernameExists(username)) {
                System.out.println("Username already exists: " + username);
                return null;
            }
            
            // Check if email already exists
            if (userDAO.emailExists(email)) {
                System.out.println("Email already exists: " + email);
                return null;
            }
            
            // Hash the password
            String hashedPassword = hashPassword(password);
            
            // Create user object
            User user = new User(username, hashedPassword, email, fullName, phone, role);
            
            // Insert into database
            int userId = userDAO.insertUser(user);
            
            if (userId > 0) {
                user.setUserId(userId);
                System.out.println("User registered successfully: " + username);
                return user;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validate registration input
     */
    private boolean validateRegistrationInput(String username, String password, String email) {
        // Username validation
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty");
            return false;
        }
        if (username.length() < 3 || username.length() > 20) {
            System.out.println("Username must be 3-20 characters");
            return false;
        }
        
        // Password validation
        if (password == null || password.length() < 6) {
            System.out.println("Password must be at least 6 characters");
            return false;
        }
        
        // Email validation
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            System.out.println("Invalid email format");
            return false;
        }
        
        return true;
    }
    
    // ================================
    // Login
    // ================================
    
    /**
     * Authenticate user login
     * @param username Username
     * @param password Plain text password
     * @param role Expected role ('user' or 'admin')
     * @return User object if login successful, null otherwise
     */
    public User loginUser(String username, String password, String role) {
        try {
            // Get user by username
            User user = userDAO.getUserByUsername(username);
            
            if (user == null) {
                System.out.println("User not found: " + username);
                return null;
            }
            
            // Check if account is active
            if (!user.isActive()) {
                System.out.println("Account is not active: " + username);
                return null;
            }
            
            // Verify password
            if (!verifyPassword(password, user.getPassword())) {
                System.out.println("Invalid password for user: " + username);
                return null;
            }
            
            // Check role if specified
            if (role != null && !role.isEmpty() && !user.getRole().equalsIgnoreCase(role)) {
                System.out.println("Role mismatch for user: " + username + 
                                 " (expected: " + role + ", actual: " + user.getRole() + ")");
                return null;
            }
            
            System.out.println("User logged in successfully: " + username);
            return user;
            
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // ================================
    // User Retrieval
    // ================================
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        try {
            return userDAO.getUserById(userId);
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        try {
            return userDAO.getUserByUsername(username);
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        try {
            return userDAO.getUserByEmail(email);
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all users (for admin)
     */
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        try {
            return userDAO.getUsersByRole(role);
        } catch (SQLException e) {
            System.err.println("Error getting users by role: " + e.getMessage());
            return null;
        }
    }
    
    // ================================
    // User Update
    // ================================
    
    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        try {
            return userDAO.updateUser(user);
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update user password
     */
    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        try {
            // Get current user
            User user = userDAO.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // Verify old password
            if (!verifyPassword(oldPassword, user.getPassword())) {
                System.out.println("Old password is incorrect");
                return false;
            }
            
            // Validate new password
            if (newPassword == null || newPassword.length() < 6) {
                System.out.println("New password must be at least 6 characters");
                return false;
            }
            
            // Hash new password
            String hashedPassword = hashPassword(newPassword);
            
            // Update password
            return userDAO.updatePassword(userId, hashedPassword);
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update user status
     */
    public boolean updateUserStatus(int userId, String status) {
        try {
            return userDAO.updateUserStatus(userId, status);
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // User Deletion
    // ================================
    
    /**
     * Delete user by ID
     */
    public boolean deleteUser(int userId) {
        try {
            return userDAO.deleteUser(userId);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Password Hashing and Verification
    // ================================
    
    /**
     * Hash password using SHA-256
     * @param password Plain text password
     * @return Hashed password in hexadecimal format
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            
            // Convert bytes to hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify password against hashed password
     * @param plainPassword Plain text password
     * @param hashedPassword Hashed password from database
     * @return true if passwords match
     */
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput != null && hashedInput.equals(hashedPassword);
    }
    
    // ================================
    // Validation Methods
    // ================================
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        try {
            return userDAO.usernameExists(username);
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        try {
            return userDAO.emailExists(email);
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Statistics
    // ================================
    
    /**
     * Get total user count
     */
    public int getTotalUserCount() {
        try {
            return userDAO.getTotalUserCount();
        } catch (SQLException e) {
            System.err.println("Error getting user count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get user count by role
     */
    public int getUserCountByRole(String role) {
        try {
            return userDAO.getUserCountByRole(role);
        } catch (SQLException e) {
            System.err.println("Error getting user count by role: " + e.getMessage());
            return 0;
        }
    }
}