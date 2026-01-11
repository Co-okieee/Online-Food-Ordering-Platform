package dao;

import model.User;

/**
 * User Data Access Object Interface
 * Responsibility: Define data access operations for User entity
 */
public interface UserDAO {
    
    /**
     * Find user by username
     * @param username Username
     * @return User object, null if not found
     */
    User findByUserName(String username);
    
    /**
     * Find user by username and password (for authentication)
     * @param username Username
     * @param password Password (hashed)
     * @return User object if credentials match, null otherwise
     */
    User findByUserNameAndPassword(String username, String password);
    
    /**
     * Create a new user
     * @param user User object to create
     * @param hashedPassword Hashed password (already encrypted)
     * @return Generated user ID, or -1 if failed
     */
    int create(User user, String hashedPassword);
    
    /**
     * Update existing user
     * @param user User object with updated information
     * @return Number of rows affected
     */
    int update(User user);
    
    /**
     * Delete user by ID
     * @param userId User ID
     * @return Number of rows affected
     */
    int deleteById(int userId);
    
    /**
     * Find user by ID
     * @param userId User ID
     * @return User object, null if not found
     */
    User findById(int userId);
    
    /**
     * Find user by email
     * @param email Email address
     * @return User object, null if not found
     */
    User findByEmail(String email);
    
    /**
     * Update user password
     * @param userId User ID
     * @param newHashedPassword New hashed password
     * @return Number of rows affected
     */
    int updatePassword(int userId, String newHashedPassword);
}
