package dao;

<<<<<<< HEAD
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Order;
import model.OrderItem;
import model.Product;
import util.DBConnection;

public class OrderDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());
    private static final boolean DEBUG_MODE = true;
    // SQL Constants
    private static final String SQL_INSERT_ORDER =
            "INSERT INTO orders (order_id, user_id, order_date, total_amount, status, " +
                    "delivery_address, payment_method, payment_status, notes) " +
                    "VALUES (orders_seq.NEXTVAL, ?, SYSDATE, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_ORDER_ITEM =
            "INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal) " +
                    "VALUES (order_items_seq.NEXTVAL, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_ORDER_BY_ID =
            "SELECT order_id, user_id, order_date, total_amount, status, delivery_address, " +
                    "payment_method, payment_status, notes, created_at, updated_at " +
                    "FROM orders WHERE order_id = ?";

    private static final String SQL_SELECT_ORDERS_BY_USER =
            "SELECT order_id, user_id, order_date, total_amount, status, delivery_address, " +
                    "payment_method, payment_status, notes, created_at, updated_at " +
                    "FROM orders WHERE user_id = ? ORDER BY order_date DESC";

    private static final String SQL_SELECT_ORDER_ITEMS =
            "SELECT order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at " +
                    "FROM order_items WHERE order_id = ?";

    private static final String SQL_UPDATE_ORDER_STATUS =
            "UPDATE orders SET status = ? WHERE order_id = ?";

    private static final String SQL_UPDATE_PAYMENT_STATUS =
            "UPDATE orders SET payment_status = ? WHERE order_id = ?";

    /**
     * Custom exception for DAO operations
     */
    public static class DAOException extends Exception {
        private static final long serialVersionUID = 1L;
        private final String operation;
        private final int errorCode;

        public DAOException(String operation, String message, SQLException cause) {
            super(message, cause);
            this.operation = operation;
            this.errorCode = cause != null ? cause.getErrorCode() : -1;
        }

        public String getOperation() {
            return operation;
        }

        public int getErrorCode() {
            return errorCode;
        }

        @Override
        public String toString() {
            return String.format("DAOException[operation=%s, errorCode=%d, message=%s]",
                    operation, errorCode, getMessage());
        }
    }

    // Helper: Map ResultSet to Order
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));
        return order;
    }

    // Helper: Map ResultSet to OrderItem
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        return item;
    }







    /**
     * Create new order with order items
     *
     * TRANSACTION STEPS:
     * 1. Validate order data
     * 2. Check all products exist and have sufficient stock
     * 3. Insert order record
     * 4. Get generated order_id
     * 5. Insert order items
     * 6. Decrease product stock
     * 7. Commit transaction
     *
     * If ANY step fails, entire transaction is rolled back
     *
     * @param order Order object (without order_id)
     * @param orderItems List of order items
     * @return Generated order_id if successful, -1 if failed
     * @throws DAOException if error occurs
     */
    public int createOrder(Order order, List<OrderItem> orderItems) throws DAOException {
        long startTime = logOperationStart("createOrder");

        // Validation
        if (order == null) {
            throw new DAOException("CreateOrder", "Order object cannot be null", null);
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new DAOException("CreateOrder", "Order must have at least one item", null);
        }
        if (order.getUserId() <= 0) {
            throw new DAOException("CreateOrder", "Invalid user ID", null);
        }

        Connection conn = null;
        PreparedStatement stmtOrder = null;
        PreparedStatement stmtItem = null;
        PreparedStatement stmtStock = null;
        ResultSet rsOrderId = null;

        try {
            // Get connection and start transaction
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            LOGGER.log(Level.INFO, "Transaction started for order creation");

            // STEP 1: Validate products and stock
            ProductDAO productDAO = new ProductDAO();
            BigDecimal calculatedTotal = BigDecimal.ZERO;

            for (OrderItem item : orderItems) {
                // Get product
                Product product = productDAO.getProductById(item.getProductId());

                if (product == null) {
                    throw new DAOException("CreateOrder",
                            "Product not found: ID=" + item.getProductId(), null);
                }

                if (!product.isAvailable()) {
                    throw new DAOException("CreateOrder",
                            "Product not available: " + product.getProductName(), null);
                }

                if (product.getStock() < item.getQuantity()) {
                    throw new DAOException("CreateOrder",
                            "Insufficient stock for: " + product.getProductName() +
                                    " (Available: " + product.getStock() + ", Requested: " + item.getQuantity() + ")",
                            null);
                }

                // Set unit price from product (prevent price manipulation)
                item.setUnitPrice(product.getPrice());
                item.calculateSubtotal();
                calculatedTotal = calculatedTotal.add(item.getSubtotal());
            }

            // Set calculated total amount
            order.setTotalAmount(calculatedTotal);
            LOGGER.log(Level.INFO, "Order validation passed. Total: {0}", calculatedTotal);

            // STEP 2: Insert order record
            stmtOrder = conn.prepareStatement(SQL_INSERT_ORDER, new String[]{"order_id"});
            stmtOrder.setInt(1, order.getUserId());
            stmtOrder.setBigDecimal(2, order.getTotalAmount());
            stmtOrder.setString(3, order.getStatus() != null ? order.getStatus() : "pending");
            stmtOrder.setString(4, order.getDeliveryAddress());
            stmtOrder.setString(5, order.getPaymentMethod());
            stmtOrder.setString(6, order.getPaymentStatus() != null ? order.getPaymentStatus() : "pending");
            stmtOrder.setString(7, order.getNotes());

            int orderRows = stmtOrder.executeUpdate();
            if (orderRows == 0) {
                throw new DAOException("CreateOrder", "Failed to insert order", null);
            }

            // STEP 3: Get generated order_id
            rsOrderId = stmtOrder.getGeneratedKeys();
            if (!rsOrderId.next()) {
                throw new DAOException("CreateOrder", "Failed to retrieve generated order ID", null);
            }
            int orderId = rsOrderId.getInt(1);
            order.setOrderId(orderId);
            LOGGER.log(Level.INFO, "Order inserted with ID: {0}", orderId);

            // STEP 4: Insert order items and update stock
            stmtItem = conn.prepareStatement(SQL_INSERT_ORDER_ITEM);
            stmtStock = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE product_id = ?");

            for (OrderItem item : orderItems) {
                // Set order_id
                item.setOrderId(orderId);

                // Insert order item
                stmtItem.setInt(1, orderId);
                stmtItem.setInt(2, item.getProductId());
                stmtItem.setInt(3, item.getQuantity());
                stmtItem.setBigDecimal(4, item.getUnitPrice());
                stmtItem.setBigDecimal(5, item.getSubtotal());
                stmtItem.executeUpdate();

                // Decrease product stock
                stmtStock.setInt(1, item.getQuantity());
                stmtStock.setInt(2, item.getProductId());
                int stockRows = stmtStock.executeUpdate();

                if (stockRows == 0) {
                    throw new DAOException("CreateOrder",
                            "Failed to update stock for product ID: " + item.getProductId(), null);
                }

                LOGGER.log(Level.FINE, "Added item: ProductID={0}, Quantity={1}",
                        new Object[]{item.getProductId(), item.getQuantity()});
            }

            // STEP 5: Commit transaction
            conn.commit();
            LOGGER.log(Level.INFO, "Order created successfully: OrderID={0}, Items={1}, Total={2}",
                    new Object[]{orderId, orderItems.size(), calculatedTotal});
            logOperationEnd("createOrder", startTime, true);

            return orderId;

        } catch (DAOException e) {
            // Rollback on validation error
            handleRollback(conn, "createOrder");
            logOperationEnd("createOrder", startTime, false);
            throw e;

        } catch (SQLException e) {
            // Rollback on database error
            logSQLError(e, "createOrder", null);
            handleRollback(conn, "createOrder");
            logOperationEnd("createOrder", startTime, false);
            throw new DAOException("CreateOrder", "Database error creating order", e);

        } catch (ProductDAO.DAOException e) {
            // Rollback on ProductDAO error
            handleRollback(conn, "createOrder");
            logOperationEnd("createOrder", startTime, false);
            throw new DAOException("CreateOrder", "Product validation error: " + e.getMessage(), null);

        } finally {
            // Close resources
            if (rsOrderId != null) try { rsOrderId.close(); } catch (SQLException e) {}
            if (stmtOrder != null) try { stmtOrder.close(); } catch (SQLException e) {}
            if (stmtItem != null) try { stmtItem.close(); } catch (SQLException e) {}
            if (stmtStock != null) try { stmtStock.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Close database resources safely
     *
     * @param rs ResultSet to close
     * @param stmt PreparedStatement to close
     * @param conn Connection to close
     */
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        // Close ResultSet
        if (rs != null) {
            try {
                rs.close();
                LOGGER.log(Level.FINEST, "ResultSet closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }

        // Close PreparedStatement
        if (stmt != null) {
            try {
                stmt.close();
                LOGGER.log(Level.FINEST, "PreparedStatement closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
            }
        }

        // Close Connection
        if (conn != null) {
            try {
                conn.close();
                LOGGER.log(Level.FINE, "Connection closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing Connection", e);
            }
        }
    }

    /**
     * Log SQL error with detailed information
     *
     * @param e SQLException that occurred
     * @param operation Operation name
     * @param sql SQL query (optional)
     */
    private void logSQLError(SQLException e, String operation, String sql) {
        LOGGER.log(Level.SEVERE,
                "SQL Error in {0}: ErrorCode={1}, SQLState={2}, Message={3}",
                new Object[]{operation, e.getErrorCode(), e.getSQLState(), e.getMessage()});

        if (sql != null && DEBUG_MODE) {
            LOGGER.log(Level.SEVERE, "SQL Query: {0}", sql);
        }

        LOGGER.log(Level.SEVERE, "Exception details", e);
    }

    /**
     * Handle transaction rollback with logging
     *
     * @param conn Connection to rollback
     * @param operation Operation being rolled back
     */
    private void handleRollback(Connection conn, String operation) {
        if (conn != null) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Transaction rolled back for operation: {0}", operation);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Rollback failed for operation: {0}", operation);
                LOGGER.log(Level.SEVERE, "Rollback exception", rollbackEx);
            }
        }
    }

    /**
     * Log operation start with timestamp
     *
     * @param operation Operation name
     * @param params Operation parameters
     * @return Start time in milliseconds
     */
    private long logOperationStart(String operation, Object... params) {
        long startTime = System.currentTimeMillis();

        if (DEBUG_MODE && params.length > 0) {
            LOGGER.log(Level.INFO, "Starting {0} with params: {1}",
                    new Object[]{operation, java.util.Arrays.toString(params)});
        } else {
            LOGGER.log(Level.INFO, "Starting {0}", operation);
        }

        return startTime;
    }

    /**
     * Log operation completion with duration
     *
     * @param operation Operation name
     * @param startTime Start time from logOperationStart
     * @param success Whether operation succeeded
     */
    private void logOperationEnd(String operation, long startTime, boolean success) {
        long duration = System.currentTimeMillis() - startTime;

        if (success) {
            LOGGER.log(Level.INFO, "Completed {0} successfully in {1}ms",
                    new Object[]{operation, duration});
        } else {
            LOGGER.log(Level.WARNING, "Failed {0} after {1}ms",
                    new Object[]{operation, duration});
        }
    }

    /**
     * Validate order data
     *
     * @param order Order to validate
     * @throws DAOException if validation fails
     */
    private void validateOrder(Order order) throws DAOException {
        LOGGER.log(Level.FINE, "Validating order data");

        // Check null
        if (order == null) {
            LOGGER.log(Level.SEVERE, "Validation failed: Order object is null");
            throw new DAOException("Validation", "Order object cannot be null", null);
        }

        // Validate user ID
        if (order.getUserId() <= 0) {
            LOGGER.log(Level.WARNING, "Validation failed: Invalid user ID: {0}", order.getUserId());
            throw new DAOException("Validation", "User ID must be positive", null);
        }

        // Validate delivery address
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation failed: Delivery address is empty");
            throw new DAOException("Validation", "Delivery address cannot be empty", null);
        }
        if (order.getDeliveryAddress().length() > 200) {
            LOGGER.log(Level.WARNING, "Validation failed: Delivery address too long");
            throw new DAOException("Validation", "Delivery address must be 200 characters or less", null);
        }

        // Validate payment method
        if (order.getPaymentMethod() == null || order.getPaymentMethod().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation failed: Payment method is empty");
            throw new DAOException("Validation", "Payment method cannot be empty", null);
        }

        String[] validPaymentMethods = {"cash", "card", "online"};
        boolean validPayment = false;
        for (String method : validPaymentMethods) {
            if (method.equalsIgnoreCase(order.getPaymentMethod())) {
                validPayment = true;
                break;
            }
        }
        if (!validPayment) {
            LOGGER.log(Level.WARNING, "Validation failed: Invalid payment method: {0}",
                    order.getPaymentMethod());
            throw new DAOException("Validation",
                    "Payment method must be: cash, card, or online", null);
        }

        // Validate status (if provided)
        if (order.getStatus() != null) {
            String[] validStatuses = {"pending", "preparing", "delivered", "cancelled"};
            boolean validStatus = false;
            for (String status : validStatuses) {
                if (status.equalsIgnoreCase(order.getStatus())) {
                    validStatus = true;
                    break;
                }
            }
            if (!validStatus) {
                LOGGER.log(Level.WARNING, "Validation failed: Invalid status: {0}",
                        order.getStatus());
                throw new DAOException("Validation",
                        "Status must be: pending, preparing, delivered, or cancelled", null);
            }
        }

        LOGGER.log(Level.FINE, "Order data validation passed");
    }

    /**
     * Validate order item data
     *
     * @param item OrderItem to validate
     * @throws DAOException if validation fails
     */
    private void validateOrderItem(OrderItem item) throws DAOException {
        LOGGER.log(Level.FINE, "Validating order item data");

        // Check null
        if (item == null) {
            LOGGER.log(Level.SEVERE, "Validation failed: OrderItem object is null");
            throw new DAOException("Validation", "OrderItem object cannot be null", null);
        }

        // Validate product ID
        if (item.getProductId() <= 0) {
            LOGGER.log(Level.WARNING, "Validation failed: Invalid product ID: {0}",
                    item.getProductId());
            throw new DAOException("Validation", "Product ID must be positive", null);
        }

        // Validate quantity
        if (item.getQuantity() <= 0) {
            LOGGER.log(Level.WARNING, "Validation failed: Invalid quantity: {0}",
                    item.getQuantity());
            throw new DAOException("Validation", "Quantity must be positive", null);
        }
        if (item.getQuantity() > 100) {
            LOGGER.log(Level.WARNING, "Validation failed: Quantity too large: {0}",
                    item.getQuantity());
            throw new DAOException("Validation", "Quantity cannot exceed 100 per item", null);
        }

        LOGGER.log(Level.FINE, "Order item data validation passed");
    }

    /**
     * Calculate order total from items
     *
     * @param orderItems List of order items
     * @return Total amount
     */
    private BigDecimal calculateOrderTotal(List<OrderItem> orderItems) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : orderItems) {
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
        }

        LOGGER.log(Level.FINE, "Calculated order total: {0}", total);
        return total;
    }

    /**
     * Get order by ID
     *
     * Retrieves a single order from database by order ID
     *
     * @param orderId Order ID to search for
     * @return Order object if found, null otherwise
     * @throws DAOException if database error occurs
     */
    public Order getOrderById(int orderId) throws DAOException {
        long startTime = logOperationStart("getOrderById", orderId);

        // Validate input
        if (orderId <= 0) {
            LOGGER.log(Level.WARNING, "Invalid order ID: {0}", orderId);
            logOperationEnd("getOrderById", startTime, false);
            throw new DAOException("GetOrderById", "Order ID must be positive", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get connection
            conn = DBConnection.getConnection();

            // Prepare statement
            stmt = conn.prepareStatement(SQL_SELECT_ORDER_BY_ID);
            stmt.setInt(1, orderId);

            // Execute query
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Map result to Order object
                Order order = mapResultSetToOrder(rs);
                conn.commit();

                LOGGER.log(Level.INFO, "Order found: ID={0}, UserID={1}, Total={2}",
                        new Object[]{orderId, order.getUserId(), order.getTotalAmount()});
                logOperationEnd("getOrderById", startTime, true);
                return order;

            } else {
                // Order not found
                LOGGER.log(Level.INFO, "No order found with ID: {0}", orderId);
                handleRollback(conn, "getOrderById");
                logOperationEnd("getOrderById", startTime, false);
                return null;
            }

        } catch (SQLException e) {
            logSQLError(e, "getOrderById", SQL_SELECT_ORDER_BY_ID);
            handleRollback(conn, "getOrderById");
            logOperationEnd("getOrderById", startTime, false);
            throw new DAOException("GetOrderById", "Database error retrieving order", e);

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

// ========================================
// 2. 获取用户的所有订单 - getOrdersByUserId()
// ========================================

    /**
     * Get all orders for a specific user
     *
     * Retrieves all orders placed by a user, ordered by date (newest first)
     *
     * @param userId User ID to search for
     * @return List of orders (may be empty if user has no orders)
     * @throws DAOException if database error occurs
     */
    public List<Order> getOrdersByUserId(int userId) throws DAOException {
        long startTime = logOperationStart("getOrdersByUserId", userId);

        // Validate input
        if (userId <= 0) {
            LOGGER.log(Level.WARNING, "Invalid user ID: {0}", userId);
            logOperationEnd("getOrdersByUserId", startTime, false);
            throw new DAOException("GetOrdersByUserId", "User ID must be positive", null);
        }

        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get connection
            conn = DBConnection.getConnection();

            // Prepare statement
            stmt = conn.prepareStatement(SQL_SELECT_ORDERS_BY_USER);
            stmt.setInt(1, userId);

            // Execute query
            rs = stmt.executeQuery();

            // Process results
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }

            conn.commit();

            LOGGER.log(Level.INFO, "Retrieved {0} orders for user ID: {1}",
                    new Object[]{orders.size(), userId});
            logOperationEnd("getOrdersByUserId", startTime, true);
            return orders;

        } catch (SQLException e) {
            logSQLError(e, "getOrdersByUserId", SQL_SELECT_ORDERS_BY_USER);
            handleRollback(conn, "getOrdersByUserId");
            logOperationEnd("getOrdersByUserId", startTime, false);
            throw new DAOException("GetOrdersByUserId", "Database error retrieving user orders", e);

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

// ========================================
// 3. 获取订单的所有项目 - getOrderItems()
// ========================================

    /**
     * Get all items for a specific order
     *
     * Retrieves all order items (products) for a given order
     *
     * @param orderId Order ID to get items for
     * @return List of order items (may be empty if order has no items)
     * @throws DAOException if database error occurs
     */
    public List<OrderItem> getOrderItems(int orderId) throws DAOException {
        long startTime = logOperationStart("getOrderItems", orderId);

        // Validate input
        if (orderId <= 0) {
            LOGGER.log(Level.WARNING, "Invalid order ID: {0}", orderId);
            logOperationEnd("getOrderItems", startTime, false);
            throw new DAOException("GetOrderItems", "Order ID must be positive", null);
        }

        List<OrderItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get connection
            conn = DBConnection.getConnection();

            // Prepare statement
            stmt = conn.prepareStatement(SQL_SELECT_ORDER_ITEMS);
            stmt.setInt(1, orderId);

            // Execute query
            rs = stmt.executeQuery();

            // Process results
            while (rs.next()) {
                OrderItem item = mapResultSetToOrderItem(rs);
                items.add(item);
            }

            conn.commit();

            LOGGER.log(Level.INFO, "Retrieved {0} items for order ID: {1}",
                    new Object[]{items.size(), orderId});
            logOperationEnd("getOrderItems", startTime, true);
            return items;

        } catch (SQLException e) {
            logSQLError(e, "getOrderItems", SQL_SELECT_ORDER_ITEMS);
            handleRollback(conn, "getOrderItems");
            logOperationEnd("getOrderItems", startTime, false);
            throw new DAOException("GetOrderItems", "Database error retrieving order items", e);

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

// ========================================
// 4. 更新订单状态 - updateOrderStatus()
// ========================================

    /**
     * Update order status
     *
     * Changes the status of an order (e.g., from pending to preparing)
     *
     * Valid statuses:
     * - pending: Order placed, waiting to be processed
     * - preparing: Order is being prepared
     * - delivered: Order has been delivered to customer
     * - cancelled: Order has been cancelled
     *
     * @param orderId Order ID to update
     * @param status New status
     * @return true if update successful, false if order not found
     * @throws DAOException if validation fails or database error occurs
     */
    public boolean updateOrderStatus(int orderId, String status) throws DAOException {
        long startTime = logOperationStart("updateOrderStatus", orderId, status);

        // Validate order ID
        if (orderId <= 0) {
            LOGGER.log(Level.WARNING, "Invalid order ID: {0}", orderId);
            logOperationEnd("updateOrderStatus", startTime, false);
            throw new DAOException("UpdateOrderStatus", "Order ID must be positive", null);
        }

        // Validate status
        if (status == null || status.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Status is empty");
            logOperationEnd("updateOrderStatus", startTime, false);
            throw new DAOException("UpdateOrderStatus", "Status cannot be empty", null);
        }

        // Check valid status values
        String[] validStatuses = {"pending", "preparing", "delivered", "cancelled"};
        boolean validStatus = false;
        for (String validValue : validStatuses) {
            if (validValue.equalsIgnoreCase(status)) {
                validStatus = true;
                status = validValue; // Normalize to lowercase
                break;
            }
        }

        if (!validStatus) {
            LOGGER.log(Level.WARNING, "Invalid status: {0}", status);
            logOperationEnd("updateOrderStatus", startTime, false);
            throw new DAOException("UpdateOrderStatus",
                    "Status must be: pending, preparing, delivered, or cancelled", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Get connection
            conn = DBConnection.getConnection();

            // Prepare statement
            stmt = conn.prepareStatement(SQL_UPDATE_ORDER_STATUS);
            stmt.setString(1, status);
            stmt.setInt(2, orderId);

            // Execute update
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Order status updated: ID={0}, New status={1}",
                        new Object[]{orderId, status});
                logOperationEnd("updateOrderStatus", startTime, true);
                return true;

            } else {
                handleRollback(conn, "updateOrderStatus");
                LOGGER.log(Level.WARNING, "Update failed: Order not found ID={0}", orderId);
                logOperationEnd("updateOrderStatus", startTime, false);
                return false;
            }

        } catch (SQLException e) {
            logSQLError(e, "updateOrderStatus", SQL_UPDATE_ORDER_STATUS);
            handleRollback(conn, "updateOrderStatus");
            logOperationEnd("updateOrderStatus", startTime, false);
            throw new DAOException("UpdateOrderStatus", "Database error updating order status", e);

        } finally {
            closeResources(null, stmt, conn);
        }
    }

// ========================================
// 使用示例
// ========================================

    /**
     * Example usage of the above methods
     */
    public static void exampleUsage() throws DAOException {
        OrderDAO orderDAO = new OrderDAO();

        // Example 1: Get order by ID
        Order order = orderDAO.getOrderById(1);
        if (order != null) {
            System.out.println("Found order: " + order.getOrderId());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total: " + order.getTotalAmount());
        }

        // Example 2: Get all orders for user
        int userId = 3; // john_doe
        List<Order> userOrders = orderDAO.getOrdersByUserId(userId);
        System.out.println("User has " + userOrders.size() + " orders");

        for (Order o : userOrders) {
            System.out.println("Order #" + o.getOrderId() +
                    " - " + o.getStatus() +
                    " - $" + o.getTotalAmount());
        }

        // Example 3: Get items for an order
        int orderId = 1;
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        System.out.println("Order #" + orderId + " has " + items.size() + " items:");

        for (OrderItem item : items) {
            System.out.println("  Product ID: " + item.getProductId() +
                    ", Quantity: " + item.getQuantity() +
                    ", Subtotal: $" + item.getSubtotal());
        }

        // Example 4: Update order status
        boolean updated = orderDAO.updateOrderStatus(1, "preparing");
        if (updated) {
            System.out.println("Order status updated to 'preparing'");
        }
    }

// ========================================
// 在Servlet中的使用示例
// ========================================

/**
 * Example: Display user's order history in a servlet
 */
/*
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    // Get current user from session
    HttpSession session = request.getSession();
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect("login.html");
        return;
    }

    OrderDAO orderDAO = new OrderDAO();

    try {
        // Get all orders for this user
        List<Order> orders = orderDAO.getOrdersByUserId(user.getUserId());

        // For each order, get its items
        Map<Integer, List<OrderItem>> orderItemsMap = new HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderDAO.getOrderItems(order.getOrderId());
            orderItemsMap.put(order.getOrderId(), items);
        }

        // Set attributes for JSP
        request.setAttribute("orders", orders);
        request.setAttribute("orderItemsMap", orderItemsMap);

        // Forward to order history page
        request.getRequestDispatcher("order-history.jsp").forward(request, response);

    } catch (OrderDAO.DAOException e) {
        LOGGER.log(Level.SEVERE, "Error loading order history", e);
        request.setAttribute("error", "Failed to load order history");
        request.getRequestDispatcher("error.jsp").forward(request, response);
    }
}
*/

    /**
     * Example: Admin updates order status
     */
/*
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    // Check if user is admin
    HttpSession session = request.getSession();
    User user = (User) session.getAttribute("user");

    if (user == null || !user.isAdmin()) {
        response.sendError(403, "Access denied");
        return;
    }

    // Get parameters
    int orderId = Integer.parseInt(request.getParameter("orderId"));
    String newStatus = request.getParameter("status");

    OrderDAO orderDAO = new OrderDAO();

    try {
        // Update status
        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);

        if (updated) {
            response.sendRedirect("admin-orders.jsp?message=Status updated");
        } else {
            response.sendRedirect("admin-orders.jsp?error=Order not found");
        }

    } catch (OrderDAO.DAOException e) {
        LOGGER.log(Level.SEVERE, "Error updating order status", e);
        response.sendRedirect("admin-orders.jsp?error=" + e.getMessage());
    }
}
*/

    public boolean cancelOrder(int orderId) throws DAOException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. 获取订单项
            List<OrderItem> items = getOrderItems(orderId);

            // 2. 恢复每个商品的库存
            PreparedStatement stmtStock = conn.prepareStatement(
                    "UPDATE products SET stock = stock + ? WHERE product_id = ?");

            for (OrderItem item : items) {
                stmtStock.setInt(1, item.getQuantity());
                stmtStock.setInt(2, item.getProductId());
                stmtStock.executeUpdate();
            }

            // 3. 更新订单状态为cancelled
            PreparedStatement stmtOrder = conn.prepareStatement(SQL_UPDATE_ORDER_STATUS);
            stmtOrder.setString(1, "cancelled");
            stmtOrder.setInt(2, orderId);
            stmtOrder.executeUpdate();

            // 4. 提交事务
            conn.commit();
            LOGGER.log(Level.INFO, "Order cancelled and stock restored: OrderID={0}", orderId);
            return true;

        } catch (SQLException e) {
            handleRollback(conn, "cancelOrder");
            throw new DAOException("CancelOrder", "Failed to cancel order", e);
        } finally {
            closeResources(null, null, conn);
        }
    }

    /**
     * Get all orders (Admin function)
     *
     * @return List of all orders
     * @throws DAOException if database error occurs
     */
    public List<Order> getAllOrders() throws DAOException {
        long startTime = logOperationStart("getAllOrders");
        List<Order> orders = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String SQL_SELECT_ALL_ORDERS =
                    "SELECT order_id, user_id, order_date, total_amount, status, delivery_address, " +
                            "payment_method, payment_status, notes, created_at, updated_at " +
                            "FROM orders ORDER BY order_date DESC";

            stmt = conn.prepareStatement(SQL_SELECT_ALL_ORDERS);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Retrieved {0} total orders", orders.size());
            logOperationEnd("getAllOrders", startTime, true);
            return orders;

        } catch (SQLException e) {
            logSQLError(e, "getAllOrders", null);
            handleRollback(conn, "getAllOrders");
            logOperationEnd("getAllOrders", startTime, false);
            throw new DAOException("GetAllOrders", "Database error retrieving all orders", e);

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Update payment status
     *
     * @param orderId Order ID
     * @param paymentStatus New payment status (pending, paid, refunded)
     * @return true if successful
     * @throws DAOException if error occurs
     */
    public boolean updatePaymentStatus(int orderId, String paymentStatus) throws DAOException {
        long startTime = logOperationStart("updatePaymentStatus", orderId, paymentStatus);

        if (orderId <= 0) {
            throw new DAOException("UpdatePaymentStatus", "Order ID must be positive", null);
        }

        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new DAOException("UpdatePaymentStatus", "Payment status cannot be empty", null);
        }

        String[] validStatuses = {"pending", "paid", "refunded"};
        boolean valid = false;
        for (String s : validStatuses) {
            if (s.equalsIgnoreCase(paymentStatus)) {
                valid = true;
                paymentStatus = s;
                break;
            }
        }

        if (!valid) {
            throw new DAOException("UpdatePaymentStatus",
                    "Payment status must be: pending, paid, or refunded", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_PAYMENT_STATUS);
            stmt.setString(1, paymentStatus);
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Payment status updated: OrderID={0}, Status={1}",
                        new Object[]{orderId, paymentStatus});
                logOperationEnd("updatePaymentStatus", startTime, true);
                return true;
            } else {
                handleRollback(conn, "updatePaymentStatus");
                logOperationEnd("updatePaymentStatus", startTime, false);
                return false;
            }

        } catch (SQLException e) {
            logSQLError(e, "updatePaymentStatus", SQL_UPDATE_PAYMENT_STATUS);
            handleRollback(conn, "updatePaymentStatus");
            logOperationEnd("updatePaymentStatus", startTime, false);
            throw new DAOException("UpdatePaymentStatus", "Database error updating payment status", e);

        } finally {
            closeResources(null, stmt, conn);
        }
    }

}

=======
import model.Order;
import java.util.List;

/**
 * Order Data Access Object Interface
 * Responsibility: Define data access operations for Order entity
 */
public interface OrderDAO {
    
    /**
     * Create a new order
     * @param order Order object to create
     * @return Generated order ID, or -1 if failed
     */
    int createOrder(Order order);
    
    /**
     * Create an order item
     * @param orderId Order ID
     * @param productId Product ID
     * @param quantity Quantity
     * @param price Unit price
     * @return Number of rows affected
     */
    int createOrderItem(int orderId, int productId, int quantity, double price);
    
    /**
     * Find orders by user ID
     * @param userId User ID
     * @return List of orders for the user
     */
    List<Order> findByUserId(int userId);
    
    /**
     * Find order by ID
     * @param orderId Order ID
     * @return Order object, null if not found
     */
    Order findById(int orderId);
    
    /**
     * Update order
     * @param order Order object with updated information
     * @return Number of rows affected
     */
    int update(Order order);
    
    /**
     * Find all orders
     * @return List of all orders
     */
    List<Order> findAll();
    
    /**
     * Update order status
     * @param orderId Order ID
     * @param status New status
     * @return Number of rows affected
     */
    int updateStatus(int orderId, String status);
}
>>>>>>> origin/backend
