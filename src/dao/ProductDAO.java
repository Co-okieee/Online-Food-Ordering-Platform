package dao;

import model.Product;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Data Access Object
 * Handles all database operations for products table
 */
public class ProductDAO {

    // ================================
    // INSERT Operations
    // ================================

    /**
     * Insert a new product into database
     * @param product Product object to insert
     * @return Generated product ID, or -1 if failed
     */
    public int insertProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_name, description, price, stock, " +
                "category, image_url, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, new String[]{"product_id"});

            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatus() != null ? product.getStatus() : "available");

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
     * Get product by ID
     */
    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }

            return null;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY created_at DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            return products;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY product_name";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            return products;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get products by status
     */
    public List<Product> getProductsByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM products WHERE status = ? ORDER BY product_name";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            return products;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Search products by name (case-insensitive)
     */
    public List<Product> searchProducts(String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE LOWER(product_name) LIKE LOWER(?) " +
                "OR LOWER(description) LIKE LOWER(?) ORDER BY product_name";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            return products;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get products with pagination
     */
    public List<Product> getProductsWithPagination(int offset, int limit) throws SQLException {
        String sql = "SELECT * FROM products ORDER BY created_at DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            return products;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts(int threshold) throws SQLException {
        String sql = "SELECT * FROM products WHERE stock <= ? AND status = 'available' " +
                "ORDER BY stock ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, threshold);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            return products;

        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================
    // UPDATE Operations
    // ================================

    /**
     * Update product information
     */
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET product_name = ?, description = ?, price = ?, " +
                "stock = ?, category = ?, image_url = ?, status = ? " +
                "WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatus());
            pstmt.setInt(8, product.getProductId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update product stock
     */
    public boolean updateProductStock(int productId, int newStock) throws SQLException {
        String sql = "UPDATE products SET stock = ? WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, newStock);
            pstmt.setInt(2, productId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update product status
     */
    public boolean updateProductStatus(int productId, String status) throws SQLException {
        String sql = "UPDATE products SET status = ? WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, status);
            pstmt.setInt(2, productId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Update product price
     */
    public boolean updateProductPrice(int productId, double newPrice) throws SQLException {
        String sql = "UPDATE products SET price = ? WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, productId);

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
     * Delete product by ID
     * Note: Will fail if product is referenced in order_items
     */
    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Check if error is due to foreign key constraint
            if (e.getErrorCode() == 2292) { // Oracle FK constraint violation
                System.out.println("Cannot delete product: referenced in orders");
                return false;
            }
            throw e;

        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    // ================================
    // Statistics Operations
    // ================================

    /**
     * Get total product count
     */
    public int getTotalProductCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";

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
     * Get product count by category
     */
    public int getProductCountByCategory(String category) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);

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
     * Get product count by status
     */
    public int getProductCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE status = ?";

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

    // ================================
    // Helper Methods
    // ================================

    /**
     * Extract Product object from ResultSet
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        product.setStatus(rs.getString("status"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }
}