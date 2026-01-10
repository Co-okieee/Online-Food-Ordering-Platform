-- Drop existing user table if exists (for development)
DROP TABLE IF EXISTS users;

-- ========================================
-- User Table
-- Purpose: Store user account information
-- Fields: id, username, password, email, role, created_at
-- ========================================
CREATE TABLE users (
    -- Primary key: Auto-increment user ID
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Username: Unique identifier for login (3-20 characters)
    username VARCHAR(20) NOT NULL UNIQUE,
    
    -- Password: Hashed password storage (store BCrypt hash)
    password VARCHAR(255) NOT NULL,
    
    -- Email: User email address for communication
    email VARCHAR(100) NOT NULL UNIQUE,
    
    -- Full name: User's display name
    full_name VARCHAR(50) NOT NULL,
    
    -- Phone: Contact number (optional)
    phone VARCHAR(20),
    
    -- Role: User role (user/admin)
    -- Values: 'user' for customers, 'admin' for administrators
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    
    -- Status: Account status (active/inactive/suspended)
    status ENUM('active', 'inactive', 'suspended') NOT NULL DEFAULT 'active',
    
    -- Creation timestamp: When the account was created
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Last update timestamp: When the account was last modified
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for better query performance
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User account information table';

-- ========================================
-- Table Comments
-- ========================================
-- This table stores all user accounts including:
-- - Regular customers (role='user')
-- - System administrators (role='admin')
-- 
-- Security notes:
-- - Passwords should NEVER be stored in plain text
-- - Use BCrypt or similar hashing algorithm
-- - Minimum password length: 6 characters
-- - Username must be unique (enforced by UNIQUE constraint)
-- - Email must be unique (enforced by UNIQUE constraint)
--
-- Status values:
-- - 'active': Normal user account
-- - 'inactive': Temporarily disabled account
-- - 'suspended': Account suspended due to violations
-- ========================================

-- Display table structure
DESCRIBE users;

-- Show table creation statement
SHOW CREATE TABLE users;