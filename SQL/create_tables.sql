-- ========================================
-- Food Ordering System - Complete Database Schema
-- Description: Full database schema for Oracle Database
-- Version: Oracle compatible
-- ========================================

-- ========================================
-- IMPORTANT: Comment out in production!
-- Note: Drop in reverse order due to foreign keys
-- ========================================
DROP TABLE order_items CASCADE CONSTRAINTS;
DROP TABLE orders CASCADE CONSTRAINTS;
DROP TABLE products CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;

-- Drop sequences if they exist
DROP SEQUENCE users_seq;
DROP SEQUENCE products_seq;
DROP SEQUENCE orders_seq;
DROP SEQUENCE order_items_seq;

-- ========================================
-- Create Sequences for Auto-increment
-- ========================================

-- Sequence for users table
CREATE SEQUENCE users_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Sequence for products table
CREATE SEQUENCE products_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Sequence for orders table
CREATE SEQUENCE orders_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Sequence for order_items table
CREATE SEQUENCE order_items_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- ========================================
-- Table 1: Users
-- Purpose: Store user account information
-- Stores both customers and administrators
-- ========================================
CREATE TABLE users (
    -- Primary Key
    user_id NUMBER(10) PRIMARY KEY,
    
    -- Login Credentials
    username VARCHAR2(20) NOT NULL UNIQUE,
    password VARCHAR2(255) NOT NULL,
    
    -- Contact Information
    email VARCHAR2(100) NOT NULL UNIQUE,
    full_name VARCHAR2(50) NOT NULL,
    phone VARCHAR2(20),
    
    -- Account Settings
    role VARCHAR2(10) DEFAULT 'user' NOT NULL 
        CHECK (role IN ('user', 'admin')),
    status VARCHAR2(10) DEFAULT 'active' NOT NULL
        CHECK (status IN ('active', 'inactive', 'suspended')),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Add comments to users table
COMMENT ON TABLE users IS 'User accounts table - stores customer and admin accounts';
COMMENT ON COLUMN users.user_id IS 'Unique user identifier';
COMMENT ON COLUMN users.username IS 'Login username (3-20 chars)';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password';
COMMENT ON COLUMN users.email IS 'User email address';
COMMENT ON COLUMN users.full_name IS 'User full name';
COMMENT ON COLUMN users.phone IS 'Contact phone number (optional)';
COMMENT ON COLUMN users.role IS 'User role: user=customer, admin=administrator';
COMMENT ON COLUMN users.status IS 'Account status';
COMMENT ON COLUMN users.created_at IS 'Account creation time';
COMMENT ON COLUMN users.updated_at IS 'Last update time';

-- Create indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- ========================================
-- Table 2: Products
-- Purpose: Store food product catalog
-- Includes all menu items available for order
-- ========================================
CREATE TABLE products (
    -- Primary Key
    product_id NUMBER(10) PRIMARY KEY,
    
    -- Product Information
    product_name VARCHAR2(100) NOT NULL,
    description VARCHAR2(1000),
    
    -- Pricing and Inventory
    price NUMBER(10, 2) NOT NULL,
    stock NUMBER(10) DEFAULT 0 NOT NULL,
    
    -- Classification
    category VARCHAR2(20) NOT NULL
        CHECK (category IN ('appetizer', 'main_course', 'dessert', 'beverage', 'other')),
    
    -- Media
    image_url VARCHAR2(255),
    
    -- Availability
    status VARCHAR2(15) DEFAULT 'available' NOT NULL
        CHECK (status IN ('available', 'unavailable', 'discontinued')),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Add comments to products table
COMMENT ON TABLE products IS 'Product catalog table - stores all food items';
COMMENT ON COLUMN products.product_id IS 'Unique product identifier';
COMMENT ON COLUMN products.product_name IS 'Product name (e.g., "Margherita Pizza")';
COMMENT ON COLUMN products.description IS 'Detailed product description';
COMMENT ON COLUMN products.price IS 'Product price (e.g., 12.99)';
COMMENT ON COLUMN products.stock IS 'Available quantity in stock';
COMMENT ON COLUMN products.category IS 'Food category';
COMMENT ON COLUMN products.image_url IS 'Product image file path';
COMMENT ON COLUMN products.status IS 'Product availability status';
COMMENT ON COLUMN products.created_at IS 'Product creation time';
COMMENT ON COLUMN products.updated_at IS 'Last update time';

-- Create indexes for products table
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_name ON products(product_name);

-- ========================================
-- Table 3: Orders
-- Purpose: Store customer order information
-- Main order records with delivery and payment info
-- ========================================
CREATE TABLE orders (
    -- Primary Key
    order_id NUMBER(10) PRIMARY KEY,
    
    -- Customer Reference (Foreign Key)
    user_id NUMBER(10) NOT NULL,
    
    -- Order Information
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    total_amount NUMBER(10, 2) NOT NULL,
    
    -- Order Status Tracking
    status VARCHAR2(15) DEFAULT 'pending' NOT NULL
        CHECK (status IN ('pending', 'confirmed', 'preparing', 'ready', 'delivered', 'cancelled')),
    
    -- Delivery Information
    delivery_address VARCHAR2(255) NOT NULL,
    
    -- Payment Information
    payment_method VARCHAR2(10) NOT NULL
        CHECK (payment_method IN ('cash', 'card', 'online')),
    payment_status VARCHAR2(10) DEFAULT 'pending' NOT NULL
        CHECK (payment_status IN ('pending', 'paid', 'failed')),
    
    -- Additional Information
    notes VARCHAR2(1000),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Foreign Key Constraint
    CONSTRAINT fk_orders_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(user_id) 
        ON DELETE CASCADE
);

-- Add comments to orders table
COMMENT ON TABLE orders IS 'Orders table - stores customer order records';
COMMENT ON COLUMN orders.order_id IS 'Unique order identifier';
COMMENT ON COLUMN orders.user_id IS 'Customer who placed the order';
COMMENT ON COLUMN orders.order_date IS 'When order was placed';
COMMENT ON COLUMN orders.total_amount IS 'Total order amount';
COMMENT ON COLUMN orders.status IS 'Current order status';
COMMENT ON COLUMN orders.delivery_address IS 'Delivery address';
COMMENT ON COLUMN orders.payment_method IS 'Payment method chosen';
COMMENT ON COLUMN orders.payment_status IS 'Payment completion status';
COMMENT ON COLUMN orders.notes IS 'Special instructions from customer';
COMMENT ON COLUMN orders.created_at IS 'Order creation time';
COMMENT ON COLUMN orders.updated_at IS 'Last status update time';

-- Create indexes for orders table
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);

-- ========================================
-- Table 4: Order Items
-- Purpose: Store individual items in each order
-- Junction table connecting orders and products
-- ========================================
CREATE TABLE order_items (
    -- Primary Key
    order_item_id NUMBER(10) PRIMARY KEY,
    
    -- Foreign Keys
    order_id NUMBER(10) NOT NULL,
    product_id NUMBER(10) NOT NULL,
    
    -- Item Details
    quantity NUMBER(10) NOT NULL,
    unit_price NUMBER(10, 2) NOT NULL,
    subtotal NUMBER(10, 2) NOT NULL,
    
    -- Timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_order_items_order 
        FOREIGN KEY (order_id) 
        REFERENCES orders(order_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(product_id)
);

-- Add comments to order_items table
COMMENT ON TABLE order_items IS 'Order items table - stores individual items in each order';
COMMENT ON COLUMN order_items.order_item_id IS 'Unique order item identifier';
COMMENT ON COLUMN order_items.order_id IS 'Reference to parent order';
COMMENT ON COLUMN order_items.product_id IS 'Reference to ordered product';
COMMENT ON COLUMN order_items.quantity IS 'Number of items ordered';
COMMENT ON COLUMN order_items.unit_price IS 'Price per item (snapshot at order time)';
COMMENT ON COLUMN order_items.subtotal IS 'Total for this item (quantity × unit_price)';
COMMENT ON COLUMN order_items.created_at IS 'Item creation time';

-- Create indexes for order_items table
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- ========================================
-- Create Triggers for Auto-increment
-- Oracle requires triggers for auto-increment behavior
-- ========================================

-- Trigger for users table
CREATE OR REPLACE TRIGGER trg_users_bi
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF :NEW.user_id IS NULL THEN
        SELECT users_seq.NEXTVAL INTO :NEW.user_id FROM dual;
    END IF;
END;
/

-- Trigger for products table
CREATE OR REPLACE TRIGGER trg_products_bi
BEFORE INSERT ON products
FOR EACH ROW
BEGIN
    IF :NEW.product_id IS NULL THEN
        SELECT products_seq.NEXTVAL INTO :NEW.product_id FROM dual;
    END IF;
END;
/

-- Trigger for orders table
CREATE OR REPLACE TRIGGER trg_orders_bi
BEFORE INSERT ON orders
FOR EACH ROW
BEGIN
    IF :NEW.order_id IS NULL THEN
        SELECT orders_seq.NEXTVAL INTO :NEW.order_id FROM dual;
    END IF;
END;
/

-- Trigger for order_items table
CREATE OR REPLACE TRIGGER trg_order_items_bi
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    IF :NEW.order_item_id IS NULL THEN
        SELECT order_items_seq.NEXTVAL INTO :NEW.order_item_id FROM dual;
    END IF;
END;
/

-- ========================================
-- Create Triggers for Updated_at Timestamp
-- Oracle requires triggers to update timestamp automatically
-- ========================================

-- Trigger to update users.updated_at
CREATE OR REPLACE TRIGGER trg_users_bu
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger to update products.updated_at
CREATE OR REPLACE TRIGGER trg_products_bu
BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger to update orders.updated_at
CREATE OR REPLACE TRIGGER trg_orders_bu
BEFORE UPDATE ON orders
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- ========================================
-- Database Relationships Summary
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
--    - No CASCADE: Cannot delete product if used in orders
--
-- UNIQUE CONSTRAINTS:
-- 1. users.username - Each username must be unique
-- 2. users.email - Each email must be unique
--
-- CHECK CONSTRAINTS:
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
SELECT table_name FROM user_tables ORDER BY table_name;

-- Show all sequences
SELECT sequence_name FROM user_sequences ORDER BY sequence_name;

-- Show all triggers
SELECT trigger_name, table_name FROM user_triggers ORDER BY table_name, trigger_name;

-- Display table structures
DESCRIBE users;
DESCRIBE products;
DESCRIBE orders;
DESCRIBE order_items;

-- Show constraints
SELECT constraint_name, constraint_type, table_name 
FROM user_constraints 
WHERE table_name IN ('USERS', 'PRODUCTS', 'ORDERS', 'ORDER_ITEMS')
ORDER BY table_name, constraint_type;

-- Show foreign key relationships
SELECT 
    a.table_name AS child_table,
    a.constraint_name,
    a.column_name AS child_column,
    c_pk.table_name AS parent_table,
    b.column_name AS parent_column
FROM user_cons_columns a
JOIN user_constraints c ON a.constraint_name = c.constraint_name
JOIN user_constraints c_pk ON c.r_constraint_name = c_pk.constraint_name
JOIN user_cons_columns b ON c_pk.constraint_name = b.constraint_name
WHERE c.constraint_type = 'R'
ORDER BY a.table_name, a.constraint_name;