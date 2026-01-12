package service;

import dao.UserDAO;
import model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Authentication Service Class
 * Responsibility: Handle user authentication and registration business logic
 * 
 * @author Cookie
 * @version 1.0
 */
public class AuthService {
    
    private UserDAO userDAO;
    
    // ========================================
    // Constructor
    // ========================================
    
    /**
     * Default constructor
     * Creates UserDAO instance automatically
     */
    public AuthService() {
        this.userDAO = new UserDAO();
        System.out.println("[AuthService] Instance created with default UserDAO");
    }
    
    /**
     * Constructor with UserDAO injection
     * 
     * @param userDAO UserDAO instance
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
        System.out.println("[AuthService] Instance created with injected UserDAO");
    }
    
    // ========================================
    // Authentication Methods
    // ========================================
    
    /**
     * Authenticate user (used by LoginServlet)
     * 
     * @param username Username
     * @param password Plain text password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        System.out.println("[AuthService] Authenticating user: " + username);
        
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[AuthService] Authentication failed: Username is empty");
            return null;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.err.println("[AuthService] Authentication failed: Password is empty");
            return null;
        }
        
        // Hash the password
        String hashedPassword = hashPassword(password);
        
        // Query database
        User user = userDAO.findByUserNameAndPassword(username.trim(), hashedPassword);
        
        // Check if user is active
        if (user != null && user.isActive()) {
            System.out.println("[AuthService] Authentication successful: " + username);
            return user;
        }
        
        System.out.println("[AuthService] Authentication failed: " + username);
        return null;
    }
    
    /**
     * User login (alternative method name)
     * Same as authenticateUser but different name
     * 
     * @param username Username
     * @param password Plain text password
     * @return User object if login successful, null otherwise
     */
    public User login(String username, String password) {
        return authenticateUser(username, password);
    }
    
    // ========================================
    // Registration Methods
    // ========================================
    
    /**
     * Register new user (used by LoginServlet)
     * 
     * @param username Username
     * @param password Plain text password
     * @param email Email address
     * @param fullName Full name
     * @param phone Phone number (can be null or empty)
     * @return User object if registration successful, null otherwise
     */
    public User registerUser(String username, String password, String email, 
                           String fullName, String phone) {
        System.out.println("[AuthService] Registering new user: " + username);
        
        // Validate username
        if (!validateUsername(username)) {
            System.err.println("[AuthService] Registration failed: Invalid username");
            return null;
        }
        
        // Validate password
        if (!validatePassword(password)) {
            System.err.println("[AuthService] Registration failed: Invalid password");
            return null;
        }
        
        // Validate email
        if (!validateEmail(email)) {
            System.err.println("[AuthService] Registration failed: Invalid email");
            return null;
        }
        
        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            System.err.println("[AuthService] Registration failed: Full name is empty");
            return null;
        }
        
        // Check if username already exists
        User existingUser = userDAO.findByUserName(username.trim());
        if (existingUser != null) {
            System.err.println("[AuthService] Registration failed: Username already exists");
            return null;
        }
        
        // Check if email already exists
        User existingEmail = userDAO.findByEmail(email.trim());
        if (existingEmail != null) {
            System.err.println("[AuthService] Registration failed: Email already exists");
            return null;
        }
        
        // Create new user object
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setEmail(email.trim());
        newUser.setFullName(fullName.trim());
        newUser.setPhone(phone != null ? phone.trim() : "");
        newUser.setRole("user");  // Default role
        newUser.setStatus("active");  // Default status
        
        // Hash password
        String hashedPassword = hashPassword(password);
        
        // Save to database
        int userId = userDAO.create(newUser, hashedPassword);
        
        if (userId > 0) {
            newUser.setUserId(userId);
            System.out.println("[AuthService] Registration successful: " + username + " (ID: " + userId + ")");
            return newUser;
        }
        
        System.err.println("[AuthService] Registration failed: Database error");
        return null;
    }
    
    /**
     * Register user (alternative method signature)
     * 
     * @param user User object
     * @param password Plain text password
     * @return true if registration successful, false otherwise
     */
    public boolean register(User user, String password) {
        if (user == null || password == null) {
            return false;
        }
        
        User result = registerUser(
            user.getUsername(),
            password,
            user.getEmail(),
            user.getFullName(),
            user.getPhone()
        );
        
        return result != null;
    }
    
    // ========================================
    // Validation Methods
    // ========================================
    
    /**
     * Validate username format
     * 
     * @param username Username to validate
     * @return true if valid, false otherwise
     */
    public boolean validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        
        // Check length (3-20 characters)
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            System.err.println("[AuthService] Username validation failed: Length must be 3-20 characters");
            return false;
        }
        
        // Check format (alphanumeric and underscore only)
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            System.err.println("[AuthService] Username validation failed: Only letters, numbers, and underscore allowed");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate email format
     * 
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email format validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            System.err.println("[AuthService] Email validation failed: Invalid format");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        
        // Check minimum length (6 characters)
        if (password.length() < 6) {
            System.err.println("[AuthService] Password validation failed: Must be at least 6 characters");
            return false;
        }
        
        return true;
    }
    
    // ========================================
    // Utility Methods
    // ========================================
    
    /**
     * Check if user is active
     * 
     * @param user User object
     * @return true if active, false otherwise
     */
    public boolean isUserActive(User user) {
        return user != null && user.isActive();
    }
    
    /**
     * Check if user is administrator
     * 
     * @param user User object
     * @return true if admin, false otherwise
     */
    public boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }
    
    /**
     * Check if user has specified permission
     * 
     * @param user User object
     * @param requiredRole Required role
     * @return true if has permission, false otherwise
     */
    public boolean hasPermission(User user, String requiredRole) {
        if (user == null || requiredRole == null) {
            return false;
        }
        
        // Admins have all permissions
        if (user.isAdmin()) {
            return true;
        }
        
        // Check specific role
        return user.getRole().equalsIgnoreCase(requiredRole);
    }
    
    /**
     * Get user by username
     * 
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userDAO.findByUserName(username.trim());
    }
    
    // ========================================
    // Password Hashing
    // ========================================
    
    /**
     * Hash password using SHA-256
     * 
     * @param password Plain text password
     * @return Hashed password (hexadecimal string)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[AuthService] Error hashing password: " + e.getMessage());
            e.printStackTrace();
            // Fallback: return plain password (not recommended for production)
            return password;
        }
    }
}