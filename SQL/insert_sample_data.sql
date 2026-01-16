-- ========================================
-- Food Ordering System - Sample Data (FIXED v2)
-- Description: Insert test data for Oracle Database
-- ========================================

-- IMPORTANT: Run create_tables.sql before running this file

-- ========================================
-- Clear Existing Data (Development Only)
-- ========================================
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM users;
COMMIT;

BEGIN
    -- Try Oracle 12c+ syntax first
    BEGIN
        EXECUTE IMMEDIATE 'ALTER SEQUENCE users_seq RESTART START WITH 1';
        EXECUTE IMMEDIATE 'ALTER SEQUENCE products_seq RESTART START WITH 1';
        EXECUTE IMMEDIATE 'ALTER SEQUENCE orders_seq RESTART START WITH 1';
        EXECUTE IMMEDIATE 'ALTER SEQUENCE order_items_seq RESTART START WITH 1';
    EXCEPTION
        WHEN OTHERS THEN
            -- Fall back to Oracle 11g method
            EXECUTE IMMEDIATE 'DROP SEQUENCE users_seq';
            EXECUTE IMMEDIATE 'DROP SEQUENCE products_seq';
            EXECUTE IMMEDIATE 'DROP SEQUENCE orders_seq';
            EXECUTE IMMEDIATE 'DROP SEQUENCE order_items_seq';
            
            EXECUTE IMMEDIATE 'CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
            EXECUTE IMMEDIATE 'CREATE SEQUENCE products_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
            EXECUTE IMMEDIATE 'CREATE SEQUENCE orders_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
            EXECUTE IMMEDIATE 'CREATE SEQUENCE order_items_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
    END;
END;
/

COMMIT;

-- ========================================
-- Insert Sample Users
-- Password: All passwords are 'password123' hashed with BCrypt
-- Using sequence.NEXTVAL to get next ID value
-- ========================================

-- Admin Users
INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'admin', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2', 
        'admin@foodhub.com', 'Admin User', '555-0001', 'admin', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'manager', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'manager@foodhub.com', 'Manager User', '555-0002', 'admin', 'active');

-- Customer Users
INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'john_doe', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'john.doe@email.com', 'John Doe', '555-1001', 'user', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'jane_smith', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'jane.smith@email.com', 'Jane Smith', '555-1002', 'user', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'bob_wilson', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'bob.wilson@email.com', 'Bob Wilson', '555-1003', 'user', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'alice_brown', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'alice.brown@email.com', 'Alice Brown', '555-1004', 'user', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'charlie_davis', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'charlie.davis@email.com', 'Charlie Davis', '555-1005', 'user', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'emma_taylor', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'emma.taylor@email.com', 'Emma Taylor', '555-1006', 'user', 'active');

-- Test Users
INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'test_user', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'test@test.com', 'Test User', '555-9999', 'user', 'active');

INSERT INTO users (user_id, username, password, email, full_name, phone, role, status) 
VALUES (users_seq.NEXTVAL, 'demo_user', '$2a$10$YQ7l3KJvQnWLJQvYz2vKs.KJvFYZU7YQvYvFQ7l3KJvQnWLJQvYz2',
        'demo@demo.com', 'Demo User', '555-9998', 'user', 'active');

COMMIT;

-- ========================================
-- Insert Sample Products (Complete Product Catalog)
-- Using sequence.NEXTVAL to get next ID value
-- ========================================

-- Desserts
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Chocolate Lava Cake', 'Warm chocolate cake with a molten center, served with vanilla ice cream', 
        6.99, 20, 'dessert', 'images/products/lava.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Tiramisu', 'Classic Italian dessert with coffee-soaked ladyfingers and mascarpone', 
        7.99, 14, 'dessert', 'images/products/tiramisu.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'New York Cheesecake', 'Creamy cheesecake with graham cracker crust and berry compote', 
        7.99, 18, 'dessert', 'images/products/cheesecake.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Apple Pie', 'Homemade apple pie with cinnamon and a flaky crust, served warm', 
        5.99, 24, 'dessert', 'images/products/apple-pie.jpg', 'available');

-- Appetizers (Pizzas & Salads)
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Classic Margherita Pizza', 'Fresh mozzarella, tomato sauce, and basil on a crispy thin crust', 
        12.99, 50, 'appetizer', 'images/products/pizza1.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Pepperoni Pizza', 'Classic pepperoni with extra cheese and Italian spices', 
        14.99, 44, 'appetizer', 'images/products/pizza2.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'BBQ Chicken Pizza', 'Grilled chicken, BBQ sauce, red onions, and cilantro', 
        15.99, 7, 'appetizer', 'images/products/pizza3.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Vegetarian Supreme', 'Bell peppers, mushrooms, olives, onions, and fresh tomatoes', 
        13.99, 30, 'appetizer', 'images/products/pizza4.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Caesar Salad', 'Romaine lettuce, parmesan cheese, croutons, and Caesar dressing', 
        8.99, 50, 'appetizer', 'images/products/caesar.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Greek Salad', 'Fresh tomatoes, cucumber, feta cheese, olives, and olive oil', 
        9.99, 45, 'appetizer', 'images/products/greek.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Buffalo Wings', 'Crispy chicken wings tossed in spicy buffalo sauce', 
        11.99, 35, 'appetizer', 'images/products/wings.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Spring Rolls', 'Fresh vegetables wrapped in rice paper, served with peanut sauce', 
        7.99, 39, 'appetizer', 'images/products/springrolls.jpg', 'available');

-- Main Courses (Burgers)
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Classic Cheeseburger', 'Angus beef patty, cheddar cheese, lettuce, tomato, and special sauce', 
        10.99, 60, 'main_course', 'images/products/burger1.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Bacon Deluxe Burger', 'Double beef patty, crispy bacon, swiss cheese, and caramelized onions', 
        13.99, 35, 'main_course', 'images/products/burger2.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Veggie Burger', 'Plant-based patty, avocado, sprouts, and chipotle mayo', 
        11.99, 25, 'main_course', 'images/products/burger3.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Mushroom Swiss Burger', 'Beef patty topped with sauteed mushrooms and melted swiss cheese', 
        12.99, 40, 'main_course', 'images/products/burger4.jpg', 'available');

-- Main Courses (Asian Cuisine)
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Tonkotsu Ramen', 'Rich pork bone broth, chashu pork, soft-boiled egg, and noodles', 
        13.99, 40, 'main_course', 'images/products/ramen1.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Spicy Miso Ramen', 'Miso broth with chili oil, ground pork, and fresh vegetables', 
        14.99, 30, 'main_course', 'images/products/ramen2.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Pad Thai', 'Stir-fried rice noodles with shrimp, peanuts, and tamarind sauce', 
        12.99, 35, 'main_course', 'images/products/padthai.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Chicken Teriyaki Bowl', 'Grilled chicken with teriyaki glaze over steamed rice and vegetables', 
        11.99, 45, 'main_course', 'images/products/teriyaki.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Quinoa Power Bowl', 'Quinoa, roasted vegetables, chickpeas, and tahini dressing', 
        11.99, 6, 'main_course', 'images/products/quinoa.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Asian Chicken Salad', 'Mixed greens, grilled chicken, mandarin oranges, and sesame dressing', 
        10.99, 35, 'main_course', 'images/products/asian-salad.jpg', 'available');

-- Main Courses (Premium Items)
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Grilled Salmon', 'Fresh Atlantic salmon with lemon butter sauce and vegetables', 
        18.99, 20, 'main_course', 'images/products/salmon1.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Steak Frites', 'Grilled ribeye steak with french fries and garlic butter', 
        22.99, 15, 'main_course', 'images/products/steak1.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Lobster Roll', 'Fresh lobster meat in a buttered toasted bun with mayo', 
        19.99, 12, 'main_course', 'images/products/lobster.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'BBQ Ribs', 'Slow-cooked baby back ribs with BBQ sauce and coleslaw', 
        21.99, 18, 'main_course', 'images/products/ribs.jpg', 'available');

-- Beverages
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Coca Cola', 'Classic Coca Cola (330ml)', 
        2.99, 100, 'beverage', 'images/products/coke.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Fresh Orange Juice', 'Freshly squeezed orange juice', 
        4.99, 50, 'beverage', 'images/products/orange-juice.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Iced Coffee', 'Cold brew coffee served over ice', 
        4.49, 60, 'beverage', 'images/products/iced-coffee.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Lemonade', 'Homemade fresh lemonade', 
        3.99, 55, 'beverage', 'images/products/lemonade.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Green Tea', 'Hot or iced green tea', 
        3.49, 70, 'beverage', 'images/products/green-tea.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Mineral Water', 'Still or sparkling mineral water (500ml)', 
        2.49, 120, 'beverage', 'images/products/water.jpg', 'available');

COMMIT;


-- ========================================
-- Insert Sample Orders (Optional - for testing order history)
-- ========================================

-- Order 1: John's order
INSERT INTO orders (user_id, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (2, 45.96, 'delivered', '123 Main Street, Apt 4B, New York, NY 10001', 'card', 'paid', 'Please ring doorbell');

-- Order items for Order 1
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (1, 6, 1, 14.99, 14.99);  -- Margherita Pizza

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (1, 2, 1, 12.99, 12.99);  -- Chicken Wings

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (1, 16, 1, 6.99, 6.99);   -- Chocolate Cake

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (1, 22, 2, 2.99, 5.98);   -- Coca Cola x2

-- Order 2: Mary's order
INSERT INTO orders (user_id, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (3, 38.97, 'preparing', '456 Oak Avenue, Los Angeles, CA 90001', 'cash', 'pending', 'No onions please');

-- Order items for Order 2
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (2, 8, 1, 22.99, 22.99);  -- Grilled Salmon

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (2, 1, 1, 8.99, 8.99);    -- Caesar Salad

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (2, 23, 1, 4.99, 4.99);   -- Fresh Orange Juice

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (2, 19, 1, 5.99, 5.99);   -- Ice Cream Sundae

-- Order 3: Cookie's pending order
INSERT INTO orders (user_id, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (5, 29.97, 'pending', '789 Cookie Lane, Chicago, IL 60601', 'online', 'pending', 'Leave at door');

-- Order items for Order 3
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (3, 7, 1, 16.99, 16.99);  -- Pepperoni Pizza

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (3, 22, 1, 2.99, 2.99);   -- Coca Cola

INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
VALUES (3, 21, 1, 5.49, 5.49);   -- Brownie

COMMIT;

-- ========================================
-- Verification Queries
-- ========================================

-- Count records in each table
SELECT 'Users' AS table_name, COUNT(*) AS record_count FROM users
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items;

-- Show all users
SELECT user_id, username, email, full_name, role, status FROM users ORDER BY user_id;

-- Show all products by category
SELECT product_id, product_name, price, stock, category, status 
FROM products 
ORDER BY category, product_name;

-- Show all orders with user info
SELECT 
    o.order_id,
    u.username,
    o.order_date,
    o.total_amount,
    o.status,
    o.payment_status
FROM orders o
JOIN users u ON o.user_id = u.user_id
ORDER BY o.order_id;

-- Show order details
SELECT 
    o.order_id,
    u.username,
    p.product_name,
    oi.quantity,
    oi.unit_price,
    oi.subtotal
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
JOIN users u ON o.user_id = u.user_id
JOIN products p ON oi.product_id = p.product_id
ORDER BY o.order_id, oi.order_item_id;

-- ========================================
-- Success Message
-- ========================================
SELECT 'Sample data inserted successfully!' AS message FROM dual;
SELECT 'Total Users: ' || COUNT(*) AS info FROM users;
SELECT 'Total Products: ' || COUNT(*) AS info FROM products;
SELECT 'Total Orders: ' || COUNT(*) AS info FROM orders;
SELECT 'Total Order Items: ' || COUNT(*) AS info FROM order_items;
