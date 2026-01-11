package dao.impl;

import dao.ProductDAO;
import model.Product;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Product DAO Implementation
 * Responsibility: Implement data access operations for Product entity using JDBC
 */
public class ProductDAOImpl implements ProductDAO {
    
    private DBConnection dbConnection;
    
    /**
     * Constructor
     */
    public ProductDAOImpl() {
        this.dbConnection = DBConnection.getInstance();
    }
    
    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products ORDER BY product_id";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return products;
    }
    
    @Override
    public Product findById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    @Override
    public int create(Product product) {
        String sql = "INSERT INTO products (product_name, description, price, stock, " +
                     "category, image_url, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setString(7, product.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return -1;
    }
    
    @Override
    public int update(Product product) {
        String sql = "UPDATE products SET product_name = ?, description = ?, price = ?, " +
                     "stock = ?, category = ?, image_url = ?, status = ?, updated_at = NOW() " +
                     "WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setString(7, product.getStatus());
            stmt.setInt(8, product.getProductId());
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return 0;
    }
    
    @Override
    public int deleteById(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return 0;
    }
    
    @Override
    public List<Product> findByCategory(String category) {
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY product_id";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return products;
    }
    
    @Override
    public List<Product> findAvailable() {
        String sql = "SELECT * FROM products WHERE status = 'available' AND stock > 0 " +
                     "ORDER BY product_id";
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return products;
    }
    
    /**
     * Extract Product object from ResultSet
     * @param rs ResultSet
     * @return Product object
     * @throws SQLException if SQL error occurs
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        product.setStatus(rs.getString("status"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }
    
    /**
     * Close database resources
     * @param conn Connection
     * @param stmt Statement
     * @param rs ResultSet
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
