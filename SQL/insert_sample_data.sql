-- ========================================
-- Food Ordering System - Sample Data (FIXED v2)
-- Author: Cookie
-- Description: Insert test data for Oracle Database
-- Version: Use sequence.NEXTVAL directly instead of NULL
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
-- Insert Sample Products
-- Using sequence.NEXTVAL to get next ID value
-- ========================================

-- Appetizers
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Spring Rolls', 'Crispy vegetable spring rolls served with sweet chili sauce', 
        5.99, 100, 'appetizer', '/images/products/spring-rolls.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Chicken Wings', 'Buffalo-style chicken wings with blue cheese dip', 
        8.99, 80, 'appetizer', '/images/products/chicken-wings.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Mozzarella Sticks', 'Golden fried mozzarella with marinara sauce', 
        6.99, 75, 'appetizer', '/images/products/mozzarella-sticks.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Garlic Bread', 'Toasted bread with garlic butter and herbs', 
        4.99, 120, 'appetizer', '/images/products/garlic-bread.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Bruschetta', 'Fresh tomatoes, basil, and olive oil on toasted bread', 
        7.99, 60, 'appetizer', '/images/products/bruschetta.jpg', 'available');

-- Main Courses
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 
        12.99, 50, 'main_course', '/images/products/margherita-pizza.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Pepperoni Pizza', 'Traditional pizza topped with pepperoni and cheese', 
        14.99, 45, 'main_course', '/images/products/pepperoni-pizza.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Cheeseburger', 'Juicy beef burger with cheese, lettuce, tomato, and pickles', 
        10.99, 70, 'main_course', '/images/products/cheeseburger.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Grilled Chicken Sandwich', 'Tender grilled chicken breast with avocado and aioli', 
        11.99, 55, 'main_course', '/images/products/chicken-sandwich.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Spaghetti Carbonara', 'Creamy pasta with bacon, eggs, and parmesan cheese', 
        13.99, 40, 'main_course', '/images/products/spaghetti-carbonara.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Caesar Salad', 'Fresh romaine lettuce with Caesar dressing and croutons', 
        9.99, 65, 'main_course', '/images/products/caesar-salad.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Beef Tacos', 'Three soft tacos filled with seasoned beef and toppings', 
        11.99, 60, 'main_course', '/images/products/beef-tacos.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Pad Thai', 'Traditional Thai rice noodles with shrimp and peanuts', 
        13.99, 35, 'main_course', '/images/products/pad-thai.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Fish and Chips', 'Beer-battered fish with crispy fries and tartar sauce', 
        15.99, 30, 'main_course', '/images/products/fish-and-chips.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'BBQ Ribs', 'Slow-cooked pork ribs with BBQ sauce and coleslaw', 
        18.99, 25, 'main_course', '/images/products/bbq-ribs.jpg', 'available');

-- Desserts
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Chocolate Cake', 'Rich chocolate layer cake with chocolate frosting', 
        6.99, 40, 'dessert', '/images/products/chocolate-cake.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Cheesecake', 'Classic New York style cheesecake with berry compote', 
        7.99, 35, 'dessert', '/images/products/cheesecake.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Ice Cream Sundae', 'Vanilla ice cream with chocolate sauce and whipped cream', 
        5.99, 50, 'dessert', '/images/products/ice-cream-sundae.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Tiramisu', 'Italian coffee-flavored dessert with mascarpone', 
        8.99, 30, 'dessert', '/images/products/tiramisu.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Apple Pie', 'Warm apple pie with cinnamon and vanilla ice cream', 
        6.99, 45, 'dessert', '/images/products/apple-pie.jpg', 'available');

-- Beverages
INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Coca-Cola', 'Classic Coca-Cola soft drink (330ml)', 
        2.99, 200, 'beverage', '/images/products/coca-cola.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Orange Juice', 'Freshly squeezed orange juice (350ml)', 
        3.99, 150, 'beverage', '/images/products/orange-juice.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Iced Tea', 'Refreshing lemon iced tea (500ml)', 
        3.49, 180, 'beverage', '/images/products/iced-tea.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Cappuccino', 'Italian espresso with steamed milk foam', 
        4.99, 100, 'beverage', '/images/products/cappuccino.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Mineral Water', 'Sparkling mineral water (500ml)', 
        2.49, 250, 'beverage', '/images/products/mineral-water.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Smoothie', 'Mixed berry smoothie with yogurt', 
        5.99, 80, 'beverage', '/images/products/smoothie.jpg', 'available');

INSERT INTO products (product_id, product_name, description, price, stock, category, image_url, status) 
VALUES (products_seq.NEXTVAL, 'Lemonade', 'Homemade fresh lemonade (350ml)', 
        3.99, 120, 'beverage', '/images/products/lemonade.jpg', 'available');

COMMIT;

-- ========================================
-- Insert Sample Orders
-- Using sequence.NEXTVAL to get next ID value
-- ========================================

-- Order 1: John Doe - Completed order (7 days ago)
INSERT INTO orders (order_id, user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (orders_seq.NEXTVAL, 3, SYSDATE - INTERVAL '7' DAY, 30.95, 'delivered', 
        '123 Main St, Apt 4B, New York, NY 10001', 'card', 'paid', 'Please ring doorbell');

-- Order 1 Items
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 1, 1, 2, 5.99, 11.98);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 1, 6, 1, 12.99, 12.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 1, 21, 2, 2.99, 5.98);

-- Order 2: Jane Smith - In progress (2 days ago)
INSERT INTO orders (order_id, user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (orders_seq.NEXTVAL, 4, SYSDATE - INTERVAL '2' DAY, 21.97, 'preparing',
        '456 Oak Avenue, Los Angeles, CA 90001', 'online', 'paid', NULL);

-- Order 2 Items
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 2, 8, 1, 10.99, 10.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 2, 17, 1, 6.99, 6.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 2, 22, 1, 3.99, 3.99);

-- Order 3: Bob Wilson - Pending (1 hour ago)
INSERT INTO orders (order_id, user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (orders_seq.NEXTVAL, 5, SYSDATE - INTERVAL '1' HOUR, 45.44, 'pending',
        '789 Pine Street, Chicago, IL 60601', 'cash', 'pending', 'Call on arrival');

-- Order 3 Items
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 3, 7, 2, 14.99, 29.98);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 3, 4, 1, 4.99, 4.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 3, 23, 3, 3.49, 10.47);

-- Order 4: Alice Brown - Delivered (5 days ago)
INSERT INTO orders (order_id, user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (orders_seq.NEXTVAL, 6, SYSDATE - INTERVAL '5' DAY, 41.95, 'delivered',
        '321 Elm Road, Houston, TX 77001', 'card', 'paid', NULL);

-- Order 4 Items
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 4, 10, 1, 13.99, 13.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 4, 11, 1, 9.99, 9.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 4, 18, 1, 7.99, 7.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 4, 24, 2, 4.99, 9.98);

-- Order 5: Charlie Davis - Cancelled (3 days ago)
INSERT INTO orders (order_id, user_id, order_date, total_amount, status, delivery_address, payment_method, payment_status, notes)
VALUES (orders_seq.NEXTVAL, 7, SYSDATE - INTERVAL '3' DAY, 21.98, 'cancelled',
        '555 Maple Drive, Phoenix, AZ 85001', 'online', 'paid', 'Changed my mind');

-- Order 5 Items
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 5, 15, 1, 18.99, 18.99);

INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal)
VALUES (order_items_seq.NEXTVAL, 5, 21, 1, 2.99, 2.99);

COMMIT;

-- ========================================
-- Verification Queries
-- ========================================

-- Show all users
SELECT user_id, username, full_name, role, status 
FROM users 
ORDER BY user_id;

-- Show all products
SELECT product_id, product_name, category, price, stock 
FROM products 
ORDER BY product_id;

-- Show all orders
SELECT order_id, user_id, order_date, total_amount, status 
FROM orders 
ORDER BY order_id;

-- Data summary
SELECT 'Users' AS table_name, COUNT(*) AS count FROM users
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items;
