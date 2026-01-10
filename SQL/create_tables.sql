-- ========================================
-- Food Ordering System - Advanced Database Features
-- Description: Triggers, views, and stored procedures
-- Version: 4.0 - Enhanced with automation
-- ========================================

-- This file should be run AFTER create_tables.sql

-- ========================================
-- TRIGGERS
-- Purpose: Automate data integrity and calculations
-- ========================================

-- ========================================
-- Trigger 1: Calculate Order Item Subtotal
-- Purpose: Automatically calculate subtotal when order item is inserted
-- Trigger: BEFORE INSERT on order_items
-- ========================================
DELIMITER //

CREATE TRIGGER trg_calculate_order_item_subtotal
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    -- Calculate subtotal = quantity × unit_price
    SET NEW.subtotal = NEW.quantity * NEW.unit_price;
END//

DELIMITER ;

-- ========================================
-- Trigger 2: Update Order Total Amount
-- Purpose: Recalculate order total when items are added/updated/deleted
-- Trigger: AFTER INSERT/UPDATE/DELETE on order_items
-- ========================================

-- Trigger: After inserting new order item
DELIMITER //

CREATE TRIGGER trg_update_order_total_after_insert
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    -- Recalculate total amount for the order
    UPDATE orders 
    SET total_amount = (
        SELECT SUM(subtotal) 
        FROM order_items 
        WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END//

DELIMITER ;

-- Trigger: After updating order item
DELIMITER //

CREATE TRIGGER trg_update_order_total_after_update
AFTER UPDATE ON order_items
FOR EACH ROW
BEGIN
    -- Recalculate total amount for the order
    UPDATE orders 
    SET total_amount = (
        SELECT SUM(subtotal) 
        FROM order_items 
        WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END//

DELIMITER ;

-- Trigger: After deleting order item
DELIMITER //

CREATE TRIGGER trg_update_order_total_after_delete
AFTER DELETE ON order_items
FOR EACH ROW
BEGIN
    -- Recalculate total amount for the order
    UPDATE orders 
    SET total_amount = COALESCE((
        SELECT SUM(subtotal) 
        FROM order_items 
        WHERE order_id = OLD.order_id
    ), 0)
    WHERE order_id = OLD.order_id;
END//

DELIMITER ;

-- ========================================
-- Trigger 3: Update Product Stock
-- Purpose: Decrease stock when order is placed
-- Trigger: AFTER INSERT on order_items
-- ========================================
DELIMITER //

CREATE TRIGGER trg_decrease_product_stock
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    -- Decrease product stock by ordered quantity
    UPDATE products 
    SET stock = stock - NEW.quantity
    WHERE product_id = NEW.product_id;
    
    -- Optional: Check if stock is low and update status
    UPDATE products
    SET status = 'unavailable'
    WHERE product_id = NEW.product_id AND stock <= 0;
END//

DELIMITER ;

-- ========================================
-- VIEWS
-- Purpose: Simplify complex queries
-- ========================================

-- ========================================
-- View 1: Order Summary
-- Purpose: Complete order information with customer details
-- ========================================
CREATE OR REPLACE VIEW vw_order_summary AS
SELECT 
    o.order_id,
    o.order_date,
    u.username,
    u.full_name AS customer_name,
    u.email,
    u.phone,
    o.total_amount,
    o.status AS order_status,
    o.payment_method,
    o.payment_status,
    o.delivery_address,
    o.notes,
    COUNT(oi.order_item_id) AS total_items
FROM orders o
JOIN users u ON o.user_id = u.user_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id
ORDER BY o.order_date DESC;

-- ========================================
-- View 2: Order Details
-- Purpose: Detailed information about each order item
-- ========================================
CREATE OR REPLACE VIEW vw_order_details AS
SELECT 
    o.order_id,
    o.order_date,
    u.username,
    u.full_name AS customer_name,
    p.product_name,
    p.category,
    oi.quantity,
    oi.unit_price,
    oi.subtotal,
    o.total_amount AS order_total,
    o.status AS order_status
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
ORDER BY o.order_date DESC, o.order_id, p.product_name;

-- ========================================
-- View 3: Product Inventory
-- Purpose: Product stock and availability status
-- ========================================
CREATE OR REPLACE VIEW vw_product_inventory AS
SELECT 
    product_id,
    product_name,
    category,
    price,
    stock,
    status,
    CASE 
        WHEN stock = 0 THEN 'Out of Stock'
        WHEN stock <= 10 THEN 'Low Stock'
        ELSE 'In Stock'
    END AS stock_status,
    created_at,
    updated_at
FROM products
ORDER BY category, product_name;

-- ========================================
-- View 4: User Order History
-- Purpose: Customer purchase history
-- ========================================
CREATE OR REPLACE VIEW vw_user_order_history AS
SELECT 
    u.user_id,
    u.username,
    u.full_name,
    COUNT(DISTINCT o.order_id) AS total_orders,
    COALESCE(SUM(o.total_amount), 0) AS total_spent,
    MAX(o.order_date) AS last_order_date
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
WHERE u.role = 'user'
GROUP BY u.user_id
ORDER BY total_spent DESC;

-- ========================================
-- View 5: Popular Products
-- Purpose: Best-selling products statistics
-- ========================================
CREATE OR REPLACE VIEW vw_popular_products AS
SELECT 
    p.product_id,
    p.product_name,
    p.category,
    p.price,
    COUNT(oi.order_item_id) AS times_ordered,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.subtotal) AS total_revenue
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
GROUP BY p.product_id
ORDER BY total_quantity_sold DESC;

-- ========================================
-- STORED PROCEDURES
-- Purpose: Encapsulate complex business logic
-- ========================================

-- ========================================
-- Procedure 1: Place New Order
-- Purpose: Create order with validation
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_place_order(
    IN p_user_id INT,
    IN p_delivery_address VARCHAR(255),
    IN p_payment_method VARCHAR(20),
    IN p_notes TEXT,
    OUT p_order_id INT,
    OUT p_result VARCHAR(100)
)
BEGIN
    -- Declare variables
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        -- Rollback on error
        ROLLBACK;
        SET p_result = 'Error: Order placement failed';
    END;
    
    -- Start transaction
    START TRANSACTION;
    
    -- Validate user exists and is active
    IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = p_user_id AND status = 'active') THEN
        SET p_result = 'Error: User not found or inactive';
        ROLLBACK;
    ELSE
        -- Create new order
        INSERT INTO orders (user_id, total_amount, delivery_address, payment_method, notes)
        VALUES (p_user_id, 0, p_delivery_address, p_payment_method, p_notes);
        
        -- Get the new order ID
        SET p_order_id = LAST_INSERT_ID();
        SET p_result = 'Success: Order created';
        
        -- Commit transaction
        COMMIT;
    END IF;
END//

DELIMITER ;

-- ========================================
-- Procedure 2: Add Item to Order
-- Purpose: Add product to existing order
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_add_order_item(
    IN p_order_id INT,
    IN p_product_id INT,
    IN p_quantity INT,
    OUT p_result VARCHAR(100)
)
BEGIN
    DECLARE v_product_price DECIMAL(10,2);
    DECLARE v_available_stock INT;
    
    -- Declare error handler
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'Error: Failed to add item';
    END;
    
    START TRANSACTION;
    
    -- Get product price and stock
    SELECT price, stock INTO v_product_price, v_available_stock
    FROM products
    WHERE product_id = p_product_id AND status = 'available';
    
    -- Check if product exists and is available
    IF v_product_price IS NULL THEN
        SET p_result = 'Error: Product not found or unavailable';
        ROLLBACK;
    -- Check if enough stock
    ELSEIF v_available_stock < p_quantity THEN
        SET p_result = CONCAT('Error: Insufficient stock (Available: ', v_available_stock, ')');
        ROLLBACK;
    ELSE
        -- Add item to order
        INSERT INTO order_items (order_id, product_id, quantity, unit_price)
        VALUES (p_order_id, p_product_id, p_quantity, v_product_price);
        
        SET p_result = 'Success: Item added to order';
        COMMIT;
    END IF;
END//

DELIMITER ;

-- ========================================
-- Procedure 3: Cancel Order
-- Purpose: Cancel order and restore stock
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_cancel_order(
    IN p_order_id INT,
    OUT p_result VARCHAR(100)
)
BEGIN
    -- Declare error handler
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'Error: Failed to cancel order';
    END;
    
    START TRANSACTION;
    
    -- Check if order exists and can be cancelled
    IF NOT EXISTS (SELECT 1 FROM orders WHERE order_id = p_order_id AND status IN ('pending', 'confirmed')) THEN
        SET p_result = 'Error: Order not found or cannot be cancelled';
        ROLLBACK;
    ELSE
        -- Restore product stock
        UPDATE products p
        JOIN order_items oi ON p.product_id = oi.product_id
        SET p.stock = p.stock + oi.quantity
        WHERE oi.order_id = p_order_id;
        
        -- Update order status
        UPDATE orders
        SET status = 'cancelled'
        WHERE order_id = p_order_id;
        
        SET p_result = 'Success: Order cancelled';
        COMMIT;
    END IF;
END//

DELIMITER ;

-- ========================================
-- Verification and Testing
-- ========================================

-- Show all triggers
SHOW TRIGGERS;

-- Show all views
SHOW FULL TABLES WHERE TABLE_TYPE = 'VIEW';

-- Show all stored procedures
SHOW PROCEDURE STATUS WHERE Db = DATABASE();

-- Test queries for views
SELECT * FROM vw_order_summary LIMIT 5;
SELECT * FROM vw_product_inventory LIMIT 10;
SELECT * FROM vw_user_order_history LIMIT 10;

-- ========================================
-- Usage Examples
-- ========================================

/*
-- Example 1: Place a new order
CALL sp_place_order(
    1,                              -- user_id
    '123 Main St, City, State',     -- delivery_address
    'card',                         -- payment_method
    'Please ring doorbell',         -- notes
    @order_id,                      -- output: order_id
    @result                         -- output: result message
);
SELECT @order_id AS order_id, @result AS result;

-- Example 2: Add items to order
CALL sp_add_order_item(@order_id, 1, 2, @result);  -- Add 2x product_id=1
SELECT @result;

CALL sp_add_order_item(@order_id, 3, 1, @result);  -- Add 1x product_id=3
SELECT @result;

-- Example 3: Cancel order
CALL sp_cancel_order(@order_id, @result);
SELECT @result;
*/

-- ========================================
-- Notes
-- ========================================
--
-- TRIGGERS:
-- - Automatically calculate order totals
-- - Update product stock on orders
-- - Maintain data integrity
--
-- VIEWS:
-- - Simplify complex queries
-- - Provide business intelligence
-- - Hide implementation details
--
-- STORED PROCEDURES:
-- - Encapsulate business logic
-- - Ensure data consistency
-- - Provide transaction safety
--
-- ========================================