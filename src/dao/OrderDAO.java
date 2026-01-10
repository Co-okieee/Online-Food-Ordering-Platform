package dao;

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
}