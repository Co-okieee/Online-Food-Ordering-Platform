-- ========================================
-- Food Ordering System - Complete Database Schema
-- Description: Full database schema with all tables and constraints
-- Version: 3.0 - Production ready
-- Database: MySQL 8.0+
-- Charset: utf8mb4 (supports emoji and international characters)
-- ========================================

-- ========================================
-- Drop Existing Tables (Development Only)
-- IMPORTANT: Comment out in production!
-- ========================================
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- ========================================
-- Table 1: Users
-- Purpose: Store user account information
-- Stores both customers and administrators
-- ========================================
CREATE TABLE users (
    -- Primary Key
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique user identifier',
    
    -- Login Credentials
    username VARCHAR(20) NOT NULL UNIQUE COMMENT 'Login username (3-20 chars)',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    
    -- Contact Information
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'User email address',
    full_name VARCHAR(50) NOT NULL COMMENT 'User full name',
    phone VARCHAR(20) COMMENT 'Contact phone number (optional)',
    
    -- Account Settings
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user' 
        COMMENT 'User role: user=customer, admin=administrator',
    status ENUM('active', 'inactive', 'suspended') NOT NULL DEFAULT 'active'
        COMMENT 'Account status',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
        COMMENT 'Last update time',
    
    -- Indexes for Performance
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User accounts table - stores customer and admin accounts';

-- ========================================
-- Table 2: Products
-- Purpose: Store food product catalog
-- Includes all menu items available for order
-- ========================================
CREATE TABLE products (
    -- Primary Key
    product_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique product identifier',
    
    -- Product Information
    product_name VARCHAR(100) NOT NULL COMMENT 'Product name (e.g., "Margherita Pizza")',
    description TEXT COMMENT 'Detailed product description',
    
    -- Pricing and Inventory
    price DECIMAL(10, 2) NOT NULL COMMENT 'Product price (e.g., 12.99)',
    stock INT NOT NULL DEFAULT 0 COMMENT 'Available quantity in stock',
    
    -- Classification
    category ENUM('appetizer', 'main_course', 'dessert', 'beverage', 'other') 
        NOT NULL COMMENT 'Food category',
    
    -- Media
    image_url VARCHAR(255) COMMENT 'Product image file path',
    
    -- Availability
    status ENUM('available', 'unavailable', 'discontinued') NOT NULL DEFAULT 'available'
        COMMENT 'Product availability status',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Product creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        COMMENT 'Last update time',
    
    -- Indexes for Performance
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Product catalog table - stores all food items';

-- ========================================
-- Table 3: Orders
-- Purpose: Store customer order information
-- Main order records with delivery and payment info
-- ========================================
CREATE TABLE orders (
    -- Primary Key
    order_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique order identifier',
    
    -- Customer Reference
    user_id INT NOT NULL COMMENT 'Customer who placed the order',
    
    -- Order Information
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When order was placed',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT 'Total order amount',
    
    -- Order Status Tracking
    status ENUM('pending', 'confirmed', 'preparing', 'ready', 'delivered', 'cancelled') 
        NOT NULL DEFAULT 'pending'
        COMMENT 'Current order status',
    
    -- Delivery Information
    delivery_address VARCHAR(255) NOT NULL COMMENT 'Delivery address',
    
    -- Payment Information
    payment_method ENUM('cash', 'card', 'online') NOT NULL 
        COMMENT 'Payment method chosen',
    payment_status ENUM('pending', 'paid', 'failed') NOT NULL DEFAULT 'pending'
        COMMENT 'Payment completion status',
    
    -- Additional Information
    notes TEXT COMMENT 'Special instructions from customer',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Order creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        COMMENT 'Last status update time',
    
    -- Foreign Key Constraints
    FOREIGN KEY (user_id) REFERENCES users(user_id) 
        ON DELETE CASCADE 
        COMMENT 'Delete orders when user is deleted',
    
    -- Indexes for Performance
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Orders table - stores customer order records';

-- ========================================
-- Table 4: Order Items
-- Purpose: Store individual items in each order
-- Junction table connecting orders and products
-- ========================================
CREATE TABLE order_items (
    -- Primary Key
    order_item_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique order item identifier',
    
    -- Foreign Keys
    order_id INT NOT NULL COMMENT 'Reference to parent order',
    product_id INT NOT NULL COMMENT 'Reference to ordered product',
    
    -- Item Details
    quantity INT NOT NULL COMMENT 'Number of items ordered',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT 'Price per item (snapshot at order time)',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT 'Total for this item (quantity × unit_price)',
    
    -- Timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Item creation time',
    
    -- Foreign Key Constraints
    FOREIGN KEY (order_id) REFERENCES orders(order_id) 
        ON DELETE CASCADE
        COMMENT 'Delete items when order is deleted',
    FOREIGN KEY (product_id) REFERENCES products(product_id) 
        ON DELETE RESTRICT
        COMMENT 'Prevent deletion of products used in orders',
    
    -- Indexes for Performance
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Order items table - stores individual items in each order';

-- ========================================
-- Database Constraints Summary
-- ========================================
-- 
-- FOREIGN KEY RELATIONSHIPS:
-- 1. users → orders (1:M)
--    - One user can have multiple orders
--    - ON DELETE CASCADE: Delete all orders when user is deleted
--
-- 2. orders → order_items (1:M)
--    - One order contains multiple items
--    - ON DELETE CASCADE: Delete all items when order is deleted
--
-- 3. products → order_items (1:M)
--    - One product can appear in multiple orders
--    - ON DELETE RESTRICT: Cannot delete product if used in orders
--
-- UNIQUE CONSTRAINTS:
-- 1. users.username - Each username must be unique
-- 2. users.email - Each email must be unique
--
-- CHECK CONSTRAINTS (Enforced by ENUM):
-- 1. users.role - Must be 'user' or 'admin'
-- 2. users.status - Must be 'active', 'inactive', or 'suspended'
-- 3. products.category - Must be valid category
-- 4. products.status - Must be valid status
-- 5. orders.status - Must be valid order status
-- 6. orders.payment_method - Must be valid payment method
-- 7. orders.payment_status - Must be valid payment status
--
-- ========================================

-- ========================================
-- Verification Queries
-- ========================================

-- Show all tables
SHOW TABLES;

-- Display table structures
DESCRIBE users;
DESCRIBE products;
DESCRIBE orders;
DESCRIBE order_items;

-- Show table creation statements (includes all constraints)
SHOW CREATE TABLE users\G
SHOW CREATE TABLE products\G
SHOW CREATE TABLE orders\G
SHOW CREATE TABLE order_items\G

-- Verify foreign key relationships
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = DATABASE()
    AND TABLE_SCHEMA = DATABASE();

-- ========================================
-- Usage Notes
-- ========================================
-- 
-- SETUP INSTRUCTIONS:
-- 1. Create database: CREATE DATABASE food_ordering_db;
-- 2. Select database: USE food_ordering_db;
-- 3. Run this script: SOURCE create_tables.sql;
--
-- IMPORTANT NOTES:
-- - All tables use InnoDB engine for transaction support
-- - All tables use utf8mb4 charset for international support
-- - All foreign keys have proper ON DELETE actions
-- - All tables have appropriate indexes for performance
-- - Passwords should be hashed before storage (use BCrypt)
-- - Prices are stored as DECIMAL(10,2) for precision
--
-- MAINTENANCE:
-- - Regular backups recommended
-- - Monitor index performance
-- - Consider partitioning for large datasets
-- - Archive old orders periodically
--
-- ========================================