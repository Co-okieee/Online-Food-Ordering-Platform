package dao;

import model.Order;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Data Access Object
 * Handles all database operations for orders table
 */
public class OrderDAO {

    // ================================
    // INSERT Operations
    // ================================

    /**
     * Insert a new order into database
     * @param order Order object to insert
     * @return Generated order ID, or -1 if failed
     */
    public int insertOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id, total_amount, status, delivery_address, " +
                "payment_method, payment_status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, new String[]{"order_id"});

            pstmt.setInt(1, order.getUserId());
            pstmt.setDouble(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus() != null ? order.getStatus() : "pending");
            pstmt.setString(4, order.getDeliveryAddress());
            pstmt.setString(5, order.getPaymentMethod());
            pstmt.setString(6, order.getPaymentStatus() != null ? order.getPaymentStatus() : "pending");
            pstmt.setString(7, order.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            return -1;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================
    // SELECT Operations
    // ================================

    /**
     * Get order by ID (without items)
     */
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.*, u.username FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "WHERE o.order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractOrderFromResultSet(rs);
            }

            return null;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get all orders for a specific user
     */
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        String sql = "SELECT o.*, u.username FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "WHERE o.user_id = ? ORDER BY o.order_date DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

            return orders;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        String sql = "SELECT o.*, u.username FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "WHERE o.status = ? ORDER BY o.order_date DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

            return orders;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get all orders (for admin)
     */
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT o.*, u.username FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "ORDER BY o.order_date DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

            return orders;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get recent orders for a user
     */
    public List<Order> getRecentOrders(int userId, int limit) throws SQLException {
        String sql = "SELECT o.*, u.username FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "WHERE o.user_id = ? ORDER BY o.order_date DESC " +
                "FETCH FIRST ? ROWS ONLY";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

            return orders;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get orders by date range
     */
    public List<Order> getOrdersByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = "SELECT o.*, u.username FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "WHERE o.order_date BETWEEN ? AND ? ORDER BY o.order_date DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, startDate);
            pstmt.setTimestamp(2, endDate);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

            return orders;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================
    // UPDATE Operations
    // ================================

    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(int orderId, String paymentStatus) throws SQLException {
        String sql = "UPDATE orders SET payment_status = ? WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, paymentStatus);
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update delivery address
     */
    public boolean updateDeliveryAddress(int orderId, String newAddress) throws SQLException {
        String sql = "UPDATE orders SET delivery_address = ? WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newAddress);
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update order notes
     */
    public boolean updateOrderNotes(int orderId, String notes) throws SQLException {
        String sql = "UPDATE orders SET notes = ? WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, notes);
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    // ================================
    // DELETE Operations
    // ================================

    /**
     * Delete order by ID
     * Note: Will cascade delete all order items due to FK constraint
     */
    public boolean deleteOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    // ================================
    // Statistics Operations
    // ================================

    /**
     * Get total order count
     */
    public int getTotalOrderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get order count by user
     */
    public int getOrderCountByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get order count by status
     */
    public int getOrderCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get total revenue
     */
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status != 'cancelled'";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

            return 0.0;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get revenue by user
     */
    public double getRevenueByUser(int userId) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM orders " +
                "WHERE user_id = ? AND status != 'cancelled'";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

            return 0.0;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get revenue by date range
     */
    public double getRevenueByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM orders " +
                "WHERE order_date BETWEEN ? AND ? AND status != 'cancelled'";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, startDate);
            pstmt.setTimestamp(2, endDate);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

            return 0.0;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================
    // Helper Methods
    // ================================

    /**
     * Extract Order object from ResultSet
     */
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));
        order.setUsername(rs.getString("username")); // From JOIN
        return order;
    }
}