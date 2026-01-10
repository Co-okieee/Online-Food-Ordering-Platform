package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Product;
import util.DBConnection;

/**
 * Product Data Access Object (DAO)
 *
 * Purpose: Handle all database operations related to products
 * Responsibilities:
 * - Product CRUD operations (Create, Read, Update, Delete)
 * - Product search and filtering
 * - Stock management
 * - Category-based queries
 *
 * Design Pattern: DAO (Data Access Object)
 *
 * @author Cookie
 */
public class ProductDAO {

    // ========================================
    // Logger Configuration
    // ========================================

    /**
     * Logger instance for ProductDAO
     */
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    /**
     * Debug mode flag
     */
    private static final boolean DEBUG_MODE = true;

    // ========================================
    // SQL Query Constants
    // ========================================

    /**
     * Select product by ID
     */
    private static final String SQL_SELECT_BY_ID =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at " +
                    "FROM products WHERE product_id = ?";

    /**
     * Select all products
     */
    private static final String SQL_SELECT_ALL =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at " +
                    "FROM products ORDER BY product_id";

    /**
     * Select products by category
     */
    private static final String SQL_SELECT_BY_CATEGORY =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at " +
                    "FROM products WHERE category = ? ORDER BY product_name";

    /**
     * Select available products
     */
    private static final String SQL_SELECT_AVAILABLE =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at " +
                    "FROM products WHERE status = 'available' AND stock > 0 " +
                    "ORDER BY category, product_name";

    /**
     * Search products by name
     */
    private static final String SQL_SEARCH_BY_NAME =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at " +
                    "FROM products WHERE LOWER(product_name) LIKE LOWER(?) " +
                    "ORDER BY product_name";

    /**
     * Insert new product
     */
    private static final String SQL_INSERT_PRODUCT =
            "INSERT INTO products (product_id, product_name, description, price, stock, " +
                    "category, image_url, status) " +
                    "VALUES (products_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, 'available')";

    /**
     * Update product information
     */
    private static final String SQL_UPDATE_PRODUCT =
            "UPDATE products SET product_name = ?, description = ?, price = ?, " +
                    "stock = ?, category = ?, image_url = ? WHERE product_id = ?";

    /**
     * Update product stock
     */
    private static final String SQL_UPDATE_STOCK =
            "UPDATE products SET stock = ? WHERE product_id = ?";

    /**
     * Update product status
     */
    private static final String SQL_UPDATE_STATUS =
            "UPDATE products SET status = ? WHERE product_id = ?";

    /**
     * Delete product
     */
    private static final String SQL_DELETE_PRODUCT =
            "DELETE FROM products WHERE product_id = ?";

    /**
     * Count total products
     */
    private static final String SQL_COUNT_PRODUCTS =
            "SELECT COUNT(*) FROM products";

    /**
     * Count products by category
     */
    private static final String SQL_COUNT_BY_CATEGORY =
            "SELECT COUNT(*) FROM products WHERE category = ?";

    // ========================================
    // Constructor
    // ========================================

    /**
     * Default constructor
     */
    public ProductDAO() {
        LOGGER.log(Level.INFO, "ProductDAO instance created");
    }

    // ========================================
    // Custom Exception Class
    // ========================================

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

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Map ResultSet to Product object
     *
     * @param rs ResultSet containing product data
     * @return Product object
     * @throws SQLException if column access fails
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        try {
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

            if (DEBUG_MODE) {
                LOGGER.log(Level.FINE, "Mapped product: {0}", product.getProductName());
            }

            return product;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error mapping ResultSet to Product", e);
            throw e;
        }
    }

    /**
     * Close database resources
     *
     * @param rs   ResultSet to close
     * @param stmt PreparedStatement to close
     * @param conn Connection to close
     */
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
                LOGGER.log(Level.FINEST, "ResultSet closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
                LOGGER.log(Level.FINEST, "PreparedStatement closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
            }
        }

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
     * Validate product data
     *
     * @param product Product to validate
     * @throws DAOException if validation fails
     */
    private void validateProductData(Product product) throws DAOException {
        LOGGER.log(Level.FINE, "Validating product data");

        // Check null
        if (product == null) {
            LOGGER.log(Level.SEVERE, "Validation failed: Product object is null");
            throw new DAOException("Validation", "Product object cannot be null", null);
        }

        // Validate product name
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation failed: Product name is empty");
            throw new DAOException("Validation", "Product name cannot be empty", null);
        }
        if (product.getProductName().length() > 100) {
            LOGGER.log(Level.WARNING, "Validation failed: Product name too long");
            throw new DAOException("Validation", "Product name must be 100 characters or less", null);
        }

        // Validate price
        if (product.getPrice() == null) {
            LOGGER.log(Level.WARNING, "Validation failed: Price is null");
            throw new DAOException("Validation", "Price cannot be null", null);
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            LOGGER.log(Level.WARNING, "Validation failed: Price is negative");
            throw new DAOException("Validation", "Price cannot be negative", null);
        }
        if (product.getPrice().compareTo(new BigDecimal("9999.99")) > 0) {
            LOGGER.log(Level.WARNING, "Validation failed: Price too high");
            throw new DAOException("Validation", "Price cannot exceed $9999.99", null);
        }

        // Validate stock
        if (product.getStock() < 0) {
            LOGGER.log(Level.WARNING, "Validation failed: Stock is negative");
            throw new DAOException("Validation", "Stock cannot be negative", null);
        }

        // Validate category
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation failed: Category is empty");
            throw new DAOException("Validation", "Category cannot be empty", null);
        }

        String[] validCategories = {"appetizer", "main_course", "dessert", "beverage", "other"};
        boolean validCategory = false;
        for (String cat : validCategories) {
            if (cat.equalsIgnoreCase(product.getCategory())) {
                validCategory = true;
                break;
            }
        }
        if (!validCategory) {
            LOGGER.log(Level.WARNING, "Validation failed: Invalid category: {0}",
                    product.getCategory());
            throw new DAOException("Validation",
                    "Category must be: appetizer, main_course, dessert, beverage, or other", null);
        }

        LOGGER.log(Level.FINE, "Product data validation passed");
    }

    /**
     * Log SQL error
     *
     * @param e         SQLException
     * @param operation Operation name
     * @param sql       SQL query (optional)
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
     * Handle transaction rollback
     *
     * @param conn      Connection to rollback
     * @param operation Operation name
     */
    private void handleRollback(Connection conn, String operation) {
        if (conn != null) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Transaction rolled back for: {0}", operation);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Rollback failed for: {0}", operation);
                LOGGER.log(Level.SEVERE, "Rollback exception", rollbackEx);
            }
        }
    }

    /**
     * Log operation start
     *
     * @param operation Operation name
     * @param params    Parameters
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
     * Log operation end
     *
     * @param operation Operation name
     * @param startTime Start time
     * @param success   Success flag
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

    // ========================================
    // Public DAO Methods (to be implemented in next commit)
    // ========================================

    // Method stubs - will be implemented in commit 23:
    // - public Product getProductById(int productId)
    // - public List<Product> getAllProducts()
    // - public List<Product> getProductsByCategory(String category)
    // - public List<Product> getAvailableProducts()
    // - public List<Product> searchProductsByName(String keyword)
    // - public boolean addProduct(Product product)
    // - public boolean updateProduct(Product product)
    // - public boolean updateStock(int productId, int newStock)
    // - public boolean deleteProduct(int productId)

    // ========================================
    // Test Method
    // ========================================

    /**
     * Test database access for ProductDAO
     *
     * @return true if test passed, false otherwise
     */
    public boolean testDatabaseAccess() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            LOGGER.log(Level.INFO, "Database connection obtained");

            String testSQL = "SELECT COUNT(*) AS product_count FROM products";
            stmt = conn.prepareStatement(testSQL);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("product_count");
                LOGGER.log(Level.INFO, "Products table accessible");
                LOGGER.log(Level.INFO, "Total products in database: {0}", count);
                conn.commit();
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            logSQLError(e, "testDatabaseAccess", null);
            handleRollback(conn, "testDatabaseAccess");
            return false;

        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    // ========================================
    // Main Method (for testing)
    // ========================================

    /**
     * Main method for testing ProductDAO
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("ProductDAO Test");
        System.out.println("========================================");

        ProductDAO productDAO = new ProductDAO();

        if (productDAO.testDatabaseAccess()) {
            System.out.println("\n✓ ProductDAO is configured correctly!");
            System.out.println("Ready to implement product operations.");
        } else {
            System.out.println("\n✗ ProductDAO test failed!");
            System.out.println("Please check database connection.");
        }
    }
}