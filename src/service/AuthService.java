package service;

import dao.UserDAO;
import model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Authentication Service Class
 * Responsibility: Handle user login validation and authentication business logic
 */
public class AuthService {
    private UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * User login
     * @param username Username
     * @param password Password
     * @return User object if login successful, null if failed
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        String hashedPassword = hashPassword(password);
        User user = userDAO.findByUserNameAndPassword(username.trim(), hashedPassword);
        
        if (user != null && user.isActive()) {
            return user;
        }
        
        return null;
    }

    /**
     * User registration
     * @param user User object
     * @param password Password
     * @return true if registration successful, false if failed
     */
    public boolean register(User user, String password) {
        if (user == null || password == null) {
            return false;
        }

        if (!validateUsername(user.getUsername())) {
            return false;
        }

        if (!validateEmail(user.getEmail())) {
            return false;
        }

        if (!validatePassword(password)) {
            return false;
        }

        User existingUser = userDAO.findByUserName(user.getUsername());
        if (existingUser != null) {
            return false;
        }

        user.setRole("user");
        user.setStatus("active");

        String hashedPassword = hashPassword(password);
        int result = userDAO.create(user, hashedPassword);
        return result > 0;
    }

    /**
     * Validate username format
     * @param username Username
     * @return true if valid, false if invalid
     */
    public boolean validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String trimmed = username.trim();
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return false;
        }
        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Validate email format
     * @param email Email address
     * @return true if valid, false if invalid
     */
    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate password strength
     * @param password Password
     * @return true if valid, false if invalid
     */
    public boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 6;
    }

    /**
     * Check if user is active
     * @param user User object
     * @return true if active, false if inactive
     */
    public boolean isUserActive(User user) {
        return user != null && user.isActive();
    }

    /**
     * Check if user is administrator
     * @param user User object
     * @return true if admin, false otherwise
     */
    public boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }

    /**
     * Check if user has specified permission
     * @param user User object
     * @param requiredRole Required role
     * @return true if has permission, false otherwise
     */
    public boolean hasPermission(User user, String requiredRole) {
        if (user == null || requiredRole == null) {
            return false;
        }
        return user.getRole().equalsIgnoreCase(requiredRole) || user.isAdmin();
    }

    /**
     * Password hashing (SHA-256)
     * @param password Original password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    /**
     * Get user by username
     * @param username Username
     * @return User object, null if not exists
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userDAO.findByUserName(username.trim());
    }

    /**
     * Authenticate user
     * @param username Username
     * @param password Password
     * @return true if authentication successful, false if failed
     */
    public boolean authenticateUser(String username, String password) {
        User user = login(username, password);
        return user != null;
    }
}
