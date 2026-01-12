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

-- ========================================
-- Food Ordering System - Sample Data Insert
-- Description: Insert sample data for testing
-- Version: Oracle compatible
-- ========================================

-- ========================================
-- Insert Sample Users
-- Password: All passwords are 'password123' (you should hash these in production)
-- ========================================

-- Insert Admin User
INSERT INTO users (username, password, email, full_name, phone, role, status)
VALUES ('admin', 'password123', 'admin@foodhub.com', 'System Administrator', '1234567890', 'admin', 'active');

-- Insert Regular Users (Customers)
INSERT INTO users (username, password, email, full_name, phone, role, status)
VALUES ('john', 'password123', 'john@example.com', 'John Smith', '1234567891', 'user', 'active');

INSERT INTO users (username, password, email, full_name, phone, role, status)
VALUES ('mary', 'password123', 'mary@example.com', 'Mary Johnson', '1234567892', 'user', 'active');

INSERT INTO users (username, password, email, full_name, phone, role, status)
VALUES ('david', 'password123', 'david@example.com', 'David Brown', '1234567893', 'user', 'active');

INSERT INTO users (username, password, email, full_name, phone, role, status)
VALUES ('cookie', 'password123', 'cookie@example.com', 'Cookie Monster', '1234567894', 'user', 'active');

COMMIT;

-- ========================================
-- Insert Sample Products
-- Categories: appetizer, main_course, dessert, beverage
-- ========================================

-- Appetizers
INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Caesar Salad', 'Fresh romaine lettuce with Caesar dressing, croutons, and parmesan cheese', 8.99, 50, 'appetizer', '/201Project/images/caesar-salad.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Chicken Wings', 'Crispy chicken wings with your choice of sauce: BBQ, Buffalo, or Honey Garlic', 12.99, 40, 'appetizer', '/201Project/images/wings.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Mozzarella Sticks', 'Golden fried mozzarella cheese sticks served with marinara sauce', 7.99, 60, 'appetizer', '/201Project/images/mozzarella.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Bruschetta', 'Toasted bread topped with fresh tomatoes, basil, garlic, and olive oil', 9.99, 35, 'appetizer', '/201Project/images/bruschetta.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Spring Rolls', 'Crispy vegetable spring rolls served with sweet chili sauce', 6.99, 45, 'appetizer', '/201Project/images/spring-rolls.jpg', 'available');

-- Main Courses
INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Margherita Pizza', 'Classic pizza with fresh mozzarella, tomatoes, and basil', 14.99, 30, 'main_course', '/201Project/images/margherita.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Pepperoni Pizza', 'Traditional pizza loaded with pepperoni and mozzarella cheese', 16.99, 28, 'main_course', '/201Project/images/pepperoni.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Grilled Salmon', 'Fresh Atlantic salmon grilled to perfection, served with vegetables and rice', 22.99, 20, 'main_course', '/201Project/images/salmon.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Beef Burger', 'Juicy beef patty with lettuce, tomato, cheese, and special sauce', 13.99, 40, 'main_course', '/201Project/images/burger.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Chicken Pasta', 'Creamy Alfredo pasta with grilled chicken and parmesan', 15.99, 35, 'main_course', '/201Project/images/pasta.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Spaghetti Carbonara', 'Traditional Italian pasta with bacon, eggs, and parmesan cheese', 14.99, 32, 'main_course', '/201Project/images/carbonara.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Ribeye Steak', 'Premium 12oz ribeye steak cooked to your preference with sides', 28.99, 15, 'main_course', '/201Project/images/steak.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Fish and Chips', 'Crispy battered fish with golden fries and tartar sauce', 16.99, 25, 'main_course', '/201Project/images/fish-chips.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Vegetable Stir Fry', 'Fresh mixed vegetables stir-fried in Asian sauce with rice', 12.99, 40, 'main_course', '/201Project/images/stirfry.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('BBQ Ribs', 'Slow-cooked pork ribs with BBQ sauce, served with coleslaw', 19.99, 18, 'main_course', '/201Project/images/ribs.jpg', 'available');

-- Desserts
INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Chocolate Cake', 'Rich chocolate layer cake with chocolate frosting', 6.99, 25, 'dessert', '/201Project/images/choco-cake.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Cheesecake', 'New York style cheesecake with berry compote', 7.99, 20, 'dessert', '/201Project/images/cheesecake.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Tiramisu', 'Classic Italian dessert with coffee-soaked ladyfingers and mascarpone', 8.99, 18, 'dessert', '/201Project/images/tiramisu.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Ice Cream Sundae', 'Three scoops of ice cream with chocolate sauce, whipped cream, and cherry', 5.99, 40, 'dessert', '/201Project/images/sundae.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Apple Pie', 'Warm apple pie with cinnamon, served with vanilla ice cream', 6.49, 22, 'dessert', '/201Project/images/apple-pie.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Brownie', 'Warm chocolate brownie with walnuts and vanilla ice cream', 5.49, 35, 'dessert', '/201Project/images/brownie.jpg', 'available');

-- Beverages
INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Coca Cola', 'Classic Coca Cola (330ml)', 2.99, 100, 'beverage', '/201Project/images/coke.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Fresh Orange Juice', 'Freshly squeezed orange juice', 4.99, 50, 'beverage', '/201Project/images/orange-juice.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Iced Coffee', 'Cold brew coffee served over ice', 4.49, 60, 'beverage', '/201Project/images/iced-coffee.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Lemonade', 'Homemade fresh lemonade', 3.99, 55, 'beverage', '/201Project/images/lemonade.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Green Tea', 'Hot or iced green tea', 3.49, 70, 'beverage', '/201Project/images/green-tea.jpg', 'available');

INSERT INTO products (product_name, description, price, stock, category, image_url, status)
VALUES ('Mineral Water', 'Still or sparkling mineral water (500ml)', 2.49, 120, 'beverage', '/201Project/images/water.jpg', 'available');

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