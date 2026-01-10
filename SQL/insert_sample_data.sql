-- ========================================
-- Food Ordering System - Sample Data
-- Description: Insert test data for development and demo
-- Version: 1.0 - Initial sample data
-- ========================================

-- IMPORTANT: Run create_tables.sql before running this file

-- ========================================
-- Clear Existing Data (Development Only)
-- ========================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE products;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- Insert Sample Users
-- Password: All passwords are 'password123' hashed with BCrypt
-- BCrypt hash: $2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2
-- ========================================

INSERT INTO users (username, password, email, full_name, phone, role, status) VALUES
-- Admin Users
('admin', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2', 
 'admin@foodhub.com', 'Admin User', '555-0001', 'admin', 'active'),

('manager', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'manager@foodhub.com', 'Manager User', '555-0002', 'admin', 'active'),

-- Customer Users
('john_doe', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'john.doe@email.com', 'John Doe', '555-1001', 'user', 'active'),

('jane_smith', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'jane.smith@email.com', 'Jane Smith', '555-1002', 'user', 'active'),

('bob_wilson', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'bob.wilson@email.com', 'Bob Wilson', '555-1003', 'user', 'active'),

('alice_brown', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'alice.brown@email.com', 'Alice Brown', '555-1004', 'user', 'active'),

('charlie_davis', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'charlie.davis@email.com', 'Charlie Davis', '555-1005', 'user', 'active'),

('emma_taylor', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'emma.taylor@email.com', 'Emma Taylor', '555-1006', 'user', 'active'),

-- Test Users
('test_user', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'test@test.com', 'Test User', '555-9999', 'user', 'active'),

('demo_user', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
 'demo@demo.com', 'Demo User', '555-9998', 'user', 'active');

-- ========================================
-- Insert Sample Products
-- Organized by category with realistic prices
-- ========================================

-- Appetizers
INSERT INTO products (product_name, description, price, stock, category, image_url, status) VALUES
('Spring Rolls', 'Crispy vegetable spring rolls served with sweet chili sauce', 5.99, 100, 'appetizer', 
 '/images/products/spring-rolls.jpg', 'available'),

('Chicken Wings', 'Buffalo-style chicken wings with blue cheese dip', 8.99, 80, 'appetizer',
 '/images/products/chicken-wings.jpg', 'available'),

('Mozzarella Sticks', 'Golden fried mozzarella with marinara sauce', 6.99, 75, 'appetizer',
 '/images/products/mozzarella-sticks.jpg', 'available'),

('Garlic Bread', 'Toasted bread with garlic butter and herbs', 4.99, 120, 'appetizer',
 '/images/products/garlic-bread.jpg', 'available'),

('Bruschetta', 'Fresh tomatoes, basil, and olive oil on toasted bread', 7.99, 60, 'appetizer',
 '/images/products/bruschetta.jpg', 'available');

-- Main Courses
INSERT INTO products (product_name, description, price, stock, category, image_url, status) VALUES
('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 50, 'main_course',
 '/images/products/margherita-pizza.jpg', 'available'),

('Pepperoni Pizza', 'Traditional pizza topped with pepperoni and cheese', 14.99, 45, 'main_course',
 '/images/products/pepperoni-pizza.jpg', 'available'),

('Cheeseburger', 'Juicy beef burger with cheese, lettuce, tomato, and pickles', 10.99, 70, 'main_course',
 '/images/products/cheeseburger.jpg', 'available'),

('Grilled Chicken Sandwich', 'Tender grilled chicken breast with avocado and aioli', 11.99, 55, 'main_course',
 '/images/products/chicken-sandwich.jpg', 'available'),

('Spaghetti Carbonara', 'Creamy pasta with bacon, eggs, and parmesan cheese', 13.99, 40, 'main_course',
 '/images/products/spaghetti-carbonara.jpg', 'available'),

('Caesar Salad', 'Fresh romaine lettuce with Caesar dressing and croutons', 9.99, 65, 'main_course',
 '/images/products/caesar-salad.jpg', 'available'),

('Beef Tacos', 'Three soft tacos filled with seasoned beef and toppings', 11.99, 60, 'main_course',
 '/images/products/beef-tacos.jpg', 'available'),

('Pad Thai', 'Traditional Thai rice noodles with shrimp and peanuts', 13.99, 35, 'main_course',
 '/images/products/pad-thai.jpg', 'available'),

('Fish and Chips', 'Beer-battered fish with crispy fries and tartar sauce', 15.99, 30, 'main_course',
 '/images/products/fish-and-chips.jpg', 'available'),

('BBQ Ribs', 'Slow-cooked pork ribs with BBQ sauce and coleslaw', 18.99, 25, 'main_course',
 '/images/products/bbq-ribs.jpg', 'available');

-- Desserts
INSERT INTO products (product_name, description, price, stock, category, image_url, status) VALUES
('Chocolate Cake', 'Rich chocolate layer cake with chocolate frosting', 6.99, 40, 'dessert',
 '/images/products/chocolate-cake.jpg', 'available'),

('Cheesecake', 'Classic New York style cheesecake with berry compote', 7.99, 35, 'dessert',
 '/images/products/cheesecake.jpg', 'available'),

('Ice Cream Sundae', 'Vanilla ice cream with chocolate sauce and whipped cream', 5.99, 50, 'dessert',
 '/images/products/ice-cream-sundae.jpg', 'available'),

('Tiramisu', 'Italian coffee-flavored dessert with mascarpone', 8.99, 30, 'dessert',
 '/images/products/tiramisu.jpg', 'available'),

('Apple Pie', 'Warm apple pie with cinnamon and vanilla ice cream', 6.99, 45, 'dessert',
 '/images/products/apple-pie.jpg', 'available');

-- Beverages
INSERT INTO products (product_name, description, price, stock, category, image_url, status) VALUES
('Coca-Cola', 'Classic Coca-Cola soft drink (330ml)', 2.99, 200, 'beverage',
 '/images/products/coca-cola.jpg', 'available'),

('Orange Juice', 'Freshly squeezed orange juice (350ml)', 3.99, 150, 'beverage',
 '/images/products/orange-juice.jpg', 'available'),

('Iced Tea', 'Refreshing lemon iced tea (500ml)', 3.49, 180, 'beverage',
 '/images/products/iced-tea.jpg', 'available'),

('Cappuccino', 'Italian espresso with steamed milk foam', 4.99, 100, 'beverage',
 '/images/products/cappuccino.jpg', 'available'),

('Mineral Water', 'Sparkling mineral water (500ml)', 2.49, 250, 'beverage',
 '/images/products/mineral-water.jpg', 'available'),

('Smoothie', 'Mixed berry smoothie with yogurt', 5.99, 80, 'beverage',
 '/images/products/smoothie.jpg', 'available'),

('Lemonade', 'Homemade fresh lemonade (350ml)', 3.99, 120, 'beverage',
 '/images/products/lemonade.jpg', 'available');

-- ========================================
-- Insert Sample Orders
-- Create realistic order history
-- ========================================

-- Order 1: John Doe - Completed order
INSERT INTO orders (user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (3, DATE_SUB(NOW(), INTERVAL 7 DAY), 0, 'delivered', 
        '123 Main St, Apt 4B, New York, NY 10001', 'card', 'paid', 'Please ring doorbell');

-- Order 1 Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES 
(1, 1, 2, 5.99),   -- 2x Spring Rolls
(1, 6, 1, 12.99),  -- 1x Margherita Pizza
(1, 21, 2, 2.99);  -- 2x Coca-Cola

-- Order 2: Jane Smith - In progress
INSERT INTO orders (user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (4, DATE_SUB(NOW(), INTERVAL 2 DAY), 0, 'preparing',
        '456 Oak Avenue, Los Angeles, CA 90001', 'online', 'paid', NULL);

-- Order 2 Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(2, 8, 1, 10.99),  -- 1x Cheeseburger
(2, 17, 1, 6.99),  -- 1x Chocolate Cake
(2, 22, 1, 3.99);  -- 1x Orange Juice

-- Order 3: Bob Wilson - Pending
INSERT INTO orders (user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (5, DATE_SUB(NOW(), INTERVAL 1 HOUR), 0, 'pending',
        '789 Pine Street, Chicago, IL 60601', 'cash', 'pending', 'Call on arrival');

-- Order 3 Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(3, 7, 2, 14.99),  -- 2x Pepperoni Pizza
(3, 4, 1, 4.99),   -- 1x Garlic Bread
(3, 23, 3, 3.49);  -- 3x Iced Tea

-- Order 4: Alice Brown - Delivered
INSERT INTO orders (user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (6, DATE_SUB(NOW(), INTERVAL 5 DAY), 0, 'delivered',
        '321 Elm Road, Houston, TX 77001', 'card', 'paid', NULL);

-- Order 4 Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(4, 10, 1, 13.99),  -- 1x Spaghetti Carbonara
(4, 11, 1, 9.99),   -- 1x Caesar Salad
(4, 18, 1, 7.99),   -- 1x Cheesecake
(4, 24, 2, 4.99);   -- 2x Cappuccino

-- Order 5: Charlie Davis - Cancelled
INSERT INTO orders (user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (7, DATE_SUB(NOW(), INTERVAL 3 DAY), 0, 'cancelled',
        '555 Maple Drive, Phoenix, AZ 85001', 'online', 'paid', 'Changed my mind');

-- Order 5 Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(5, 15, 1, 18.99),  -- 1x BBQ Ribs
(5, 21, 1, 2.99);   -- 1x Coca-Cola

-- ========================================
-- Verification Queries
-- ========================================

-- Show all users
SELECT user_id, username, full_name, role, status FROM users ORDER BY role, username;

-- Show all products by category
SELECT category, product_name, price, stock, status 
FROM products 
ORDER BY category, product_name;

-- Show all orders with customer info
SELECT 
    o.order_id,
    u.username,
    u.full_name,
    o.order_date,
    o.total_amount,
    o.status,
    o.payment_status
FROM orders o
JOIN users u ON o.user_id = u.user_id
ORDER BY o.order_date DESC;

-- Show order details
SELECT 
    o.order_id,
    u.full_name AS customer,
    p.product_name,
    oi.quantity,
    oi.unit_price,
    oi.subtotal,
    o.total_amount AS order_total
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
ORDER BY o.order_id, p.product_name;

-- Show product sales statistics
SELECT 
    p.product_name,
    p.category,
    COUNT(oi.order_item_id) AS times_ordered,
    SUM(oi.quantity) AS total_sold,
    SUM(oi.subtotal) AS revenue
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
GROUP BY p.product_id
ORDER BY total_sold DESC;

-- Show customer order statistics
SELECT 
    u.username,
    u.full_name,
    COUNT(o.order_id) AS total_orders,
    SUM(o.total_amount) AS total_spent
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
WHERE u.role = 'user'
GROUP BY u.user_id
ORDER BY total_spent DESC;

-- ========================================
-- Data Summary
-- ========================================

SELECT 'Users' AS table_name, COUNT(*) AS record_count FROM users
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items;

-- ========================================
-- Test Credentials Summary
-- ========================================

SELECT '=== TEST ACCOUNTS ===' AS info;
SELECT 
    username,
    role,
    email,
    'password123' AS password_plaintext,
    status
FROM users
ORDER BY role, username;

-- ========================================
-- Notes
-- ========================================
--
-- TEST ACCOUNTS:
-- Admin accounts:
--   Username: admin    | Password: password123 | Role: admin
--   Username: manager  | Password: password123 | Role: admin
--
-- Customer accounts:
--   Username: john_doe | Password: password123 | Role: user
--   Username: jane_smith | Password: password123 | Role: user
--   ... (8 customer accounts total)
--
-- SAMPLE DATA INCLUDES:
-- - 10 users (2 admins, 8 customers)
-- - 27 products (5 appetizers, 10 main courses, 5 desserts, 7 beverages)
-- - 5 sample orders with different statuses
-- - Realistic order history spanning 7 days
--
-- PASSWORD SECURITY:
-- - All passwords are BCrypt hashed
-- - Plain text password for testing: 'password123'
-- - In production, use proper password hashing
--
-- IMAGE PATHS:
-- - Image URLs are placeholders
-- - Create actual image files in /images/products/ directory
--
-- ========================================