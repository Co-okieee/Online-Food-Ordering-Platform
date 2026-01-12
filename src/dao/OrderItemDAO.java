package dao;

import model.OrderItem;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderItem Data Access Object
 * Handles all database operations for order_items table
 */
public class OrderItemDAO {

    // ================================
    // INSERT Operations
    // ================================

    /**
     * Insert a new order item into database
     * @param orderItem OrderItem object to insert
     * @return Generated order item ID, or -1 if failed
     */
    public int insertOrderItem(OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, new String[]{"order_item_id"});

            pstmt.setInt(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getProductId());
            pstmt.setInt(3, orderItem.getQuantity());
            pstmt.setDouble(4, orderItem.getUnitPrice());
            pstmt.setDouble(5, orderItem.getSubtotal());

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

    /**
     * Insert multiple order items in batch
     */
    public boolean insertOrderItemsBatch(List<OrderItem> orderItems) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            pstmt = conn.prepareStatement(sql);

            for (OrderItem item : orderItems) {
                pstmt.setInt(1, item.getOrderId());
                pstmt.setInt(2, item.getProductId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getUnitPrice());
                pstmt.setDouble(5, item.getSubtotal());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit(); // Commit transaction

            // Check if all inserts were successful
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }

            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e;

        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restore auto-commit
            }
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    // ================================
    // SELECT Operations
    // ================================

    /**
     * Get order item by ID
     */
    public OrderItem getOrderItemById(int orderItemId) throws SQLException {
        String sql = "SELECT oi.*, p.product_name, p.category, p.image_url " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE oi.order_item_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderItemId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractOrderItemFromResultSet(rs);
            }

            return null;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get all order items for a specific order
     */
    public List<OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        String sql = "SELECT oi.*, p.product_name, p.category, p.image_url " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE oi.order_id = ? " +
                "ORDER BY oi.order_item_id";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderItem> orderItems = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                orderItems.add(extractOrderItemFromResultSet(rs));
            }

            return orderItems;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get all order items for a specific product
     */
    public List<OrderItem> getOrderItemsByProductId(int productId) throws SQLException {
        String sql = "SELECT oi.*, p.product_name, p.category, p.image_url " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE oi.product_id = ? " +
                "ORDER BY oi.created_at DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderItem> orderItems = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                orderItems.add(extractOrderItemFromResultSet(rs));
            }

            return orderItems;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get all order items
     */
    public List<OrderItem> getAllOrderItems() throws SQLException {
        String sql = "SELECT oi.*, p.product_name, p.category, p.image_url " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "ORDER BY oi.created_at DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderItem> orderItems = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                orderItems.add(extractOrderItemFromResultSet(rs));
            }

            return orderItems;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================
    // UPDATE Operations
    // ================================

    /**
     * Update order item quantity and recalculate subtotal
     */
    public boolean updateOrderItemQuantity(int orderItemId, int newQuantity) throws SQLException {
        String sql = "UPDATE order_items SET quantity = ?, subtotal = quantity * unit_price " +
                "WHERE order_item_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, orderItemId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update order item unit price and recalculate subtotal
     */
    public boolean updateOrderItemPrice(int orderItemId, double newUnitPrice) throws SQLException {
        String sql = "UPDATE order_items SET unit_price = ?, subtotal = quantity * ? " +
                "WHERE order_item_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setDouble(1, newUnitPrice);
            pstmt.setDouble(2, newUnitPrice);
            pstmt.setInt(3, orderItemId);

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
     * Delete order item by ID
     */
    public boolean deleteOrderItem(int orderItemId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderItemId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Delete all order items for a specific order
     * Usually handled by CASCADE DELETE, but provided for manual cleanup
     */
    public boolean deleteOrderItemsByOrderId(int orderId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id = ?";

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
     * Get total quantity of items in an order
     */
    public int getTotalQuantityByOrderId(int orderId) throws SQLException {
        String sql = "SELECT SUM(quantity) FROM order_items WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);

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
     * Get total subtotal for an order
     */
    public double getTotalSubtotalByOrderId(int orderId) throws SQLException {
        String sql = "SELECT SUM(subtotal) FROM order_items WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);

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
     * Get total quantity sold for a product
     */
    public int getTotalQuantitySoldByProductId(int productId) throws SQLException {
        String sql = "SELECT SUM(quantity) FROM order_items WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);

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
     * Get most popular products (by quantity sold)
     */
    public List<OrderItem> getMostPopularProducts(int limit) throws SQLException {
        String sql = "SELECT oi.product_id, p.product_name, p.category, p.image_url, " +
                "SUM(oi.quantity) as total_quantity, SUM(oi.subtotal) as total_revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "GROUP BY oi.product_id, p.product_name, p.category, p.image_url " +
                "ORDER BY total_quantity DESC " +
                "FETCH FIRST ? ROWS ONLY";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderItem> popularProducts = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setCategory(rs.getString("category"));
                item.setImageUrl(rs.getString("image_url"));
                item.setQuantity(rs.getInt("total_quantity"));
                item.setSubtotal(rs.getDouble("total_revenue"));
                popularProducts.add(item);
            }

            return popularProducts;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================
    // Helper Methods
    // ================================

    /**
     * Extract OrderItem object from ResultSet
     */
    private OrderItem extractOrderItemFromResultSet(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(rs.getInt("order_item_id"));
        orderItem.setOrderId(rs.getInt("order_id"));
        orderItem.setProductId(rs.getInt("product_id"));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setUnitPrice(rs.getDouble("unit_price"));
        orderItem.setSubtotal(rs.getDouble("subtotal"));
        orderItem.setCreatedAt(rs.getTimestamp("created_at"));

        // Product details from JOIN
        orderItem.setProductName(rs.getString("product_name"));
        orderItem.setCategory(rs.getString("category"));
        orderItem.setImageUrl(rs.getString("image_url"));

        return orderItem;
    }
}