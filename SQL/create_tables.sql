-- ========================================
-- Food Ordering System - Database Schema
-- Description: Complete database schema with all tables
-- Version: 2.0 - Added orders and order_items tables
-- ========================================

-- Drop existing tables if exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- ========================================
-- User Table
-- Purpose: Store user account information
-- ========================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    status ENUM('active', 'inactive', 'suspended') NOT NULL DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User account information table';

-- ========================================
-- Product Table
-- Purpose: Store food product information
-- ========================================
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Product name: Name of the food item
    product_name VARCHAR(100) NOT NULL,
    
    -- Description: Detailed description of the product
    description TEXT,
    
    -- Price: Product price in currency (e.g., USD)
    price DECIMAL(10, 2) NOT NULL,
    
    -- Stock: Available quantity
    stock INT NOT NULL DEFAULT 0,
    
    -- Category: Food category (appetizer, main_course, dessert, beverage)
    category ENUM('appetizer', 'main_course', 'dessert', 'beverage', 'other') NOT NULL,
    
    -- Image URL: Path to product image
    image_url VARCHAR(255),
    
    -- Status: Product availability status
    status ENUM('available', 'unavailable', 'discontinued') NOT NULL DEFAULT 'available',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Food product catalog table';

-- ========================================
-- Orders Table
-- Purpose: Store customer order information
-- ========================================
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign key: Reference to user who placed the order
    user_id INT NOT NULL,
    
    -- Order date: When the order was placed
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Total amount: Total price of the order
    total_amount DECIMAL(10, 2) NOT NULL,
    
    -- Order status: Current status of the order
    status ENUM('pending', 'confirmed', 'preparing', 'ready', 'delivered', 'cancelled') 
        NOT NULL DEFAULT 'pending',
    
    -- Delivery address: Where to deliver the order
    delivery_address VARCHAR(255) NOT NULL,
    
    -- Payment method: How the customer will pay
    payment_method ENUM('cash', 'card', 'online') NOT NULL,
    
    -- Payment status: Whether payment is completed
    payment_status ENUM('pending', 'paid', 'failed') NOT NULL DEFAULT 'pending',
    
    -- Notes: Special instructions from customer
    notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer orders table';

-- ========================================
-- Order Items Table
-- Purpose: Store individual items in each order
-- ========================================
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign key: Reference to the order
    order_id INT NOT NULL,
    
    -- Foreign key: Reference to the product
    product_id INT NOT NULL,
    
    -- Quantity: Number of items ordered
    quantity INT NOT NULL,
    
    -- Unit price: Price per item at time of order (snapshot)
    unit_price DECIMAL(10, 2) NOT NULL,
    
    -- Subtotal: Total price for this item (quantity * unit_price)
    subtotal DECIMAL(10, 2) NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Individual items in each order';

-- ========================================
-- Table Relationships Summary
-- ========================================
-- users (1) ----< orders (M): One user can have many orders
-- orders (1) ----< order_items (M): One order can have many items
-- products (1) ----< order_items (M): One product can be in many order items
--
-- Cascade Rules:
-- - DELETE user → CASCADE delete all their orders
-- - DELETE order → CASCADE delete all order_items
-- - DELETE product → RESTRICT (cannot delete if used in orders)
-- ========================================

-- Display all table structures
DESCRIBE users;
DESCRIBE products;
DESCRIBE orders;
DESCRIBE order_items;