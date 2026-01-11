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
import model.Product;
import util.DBConnection;

/**
 * Product Data Access Object (DAO) - Complete Implementation
 *
 * Full CRUD operations for products with:
 * - Search and filtering
 * - Stock management
 * - Category-based queries
 * - Comprehensive error handling
 *
 * @author Cookie
 * @version 2.0 - Complete CRUD implementation
 */
public class ProductDAO {

    // Logger
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());
    private static final boolean DEBUG_MODE = true;

    // SQL Query Constants
    private static final String SQL_SELECT_BY_ID =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at FROM products WHERE product_id = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at FROM products ORDER BY product_id";

    private static final String SQL_SELECT_BY_CATEGORY =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at FROM products WHERE category = ? ORDER BY product_name";

    private static final String SQL_SELECT_AVAILABLE =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at FROM products WHERE status = 'available' AND stock > 0 " +
                    "ORDER BY category, product_name";

    private static final String SQL_SEARCH_BY_NAME =
            "SELECT product_id, product_name, description, price, stock, category, " +
                    "image_url, status, created_at, updated_at FROM products WHERE LOWER(product_name) LIKE LOWER(?) " +
                    "ORDER BY product_name";

    private static final String SQL_INSERT_PRODUCT =
            "INSERT INTO products (product_id, product_name, description, price, stock, " +
                    "category, image_url, status) VALUES (products_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, 'available')";

    private static final String SQL_UPDATE_PRODUCT =
            "UPDATE products SET product_name = ?, description = ?, price = ?, " +
                    "stock = ?, category = ?, image_url = ? WHERE product_id = ?";

    private static final String SQL_UPDATE_STOCK =
            "UPDATE products SET stock = ? WHERE product_id = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE products SET status = ? WHERE product_id = ?";

    private static final String SQL_DELETE_PRODUCT =
            "DELETE FROM products WHERE product_id = ?";

    private static final String SQL_COUNT_PRODUCTS =
            "SELECT COUNT(*) FROM products";

    private static final String SQL_COUNT_BY_CATEGORY =
            "SELECT COUNT(*) FROM products WHERE category = ?";

    // Constructor
    public ProductDAO() {
        LOGGER.log(Level.INFO, "ProductDAO instance created");
    }

    // Custom Exception
    public static class DAOException extends Exception {
        private static final long serialVersionUID = 1L;
        private final String operation;
        private final int errorCode;

        public DAOException(String operation, String message, SQLException cause) {
            super(message, cause);
            this.operation = operation;
            this.errorCode = cause != null ? cause.getErrorCode() : -1;
        }

        public String getOperation() { return operation; }
        public int getErrorCode() { return errorCode; }

        @Override
        public String toString() {
            return String.format("DAOException[operation=%s, errorCode=%d, message=%s]",
                    operation, errorCode, getMessage());
        }
    }

    // Helper Methods
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

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) try { rs.close(); LOGGER.log(Level.FINEST, "ResultSet closed"); }
        catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing ResultSet", e); }

        if (stmt != null) try { stmt.close(); LOGGER.log(Level.FINEST, "PreparedStatement closed"); }
        catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e); }

        if (conn != null) try { conn.close(); LOGGER.log(Level.FINE, "Connection closed"); }
        catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing Connection", e); }
    }

    private void validateProductData(Product product) throws DAOException {
        LOGGER.log(Level.FINE, "Validating product data");

        if (product == null) {
            LOGGER.log(Level.SEVERE, "Validation failed: Product object is null");
            throw new DAOException("Validation", "Product object cannot be null", null);
        }

        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation failed: Product name is empty");
            throw new DAOException("Validation", "Product name cannot be empty", null);
        }
        if (product.getProductName().length() > 100) {
            throw new DAOException("Validation", "Product name must be 100 characters or less", null);
        }

        if (product.getPrice() == null) {
            throw new DAOException("Validation", "Price cannot be null", null);
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new DAOException("Validation", "Price cannot be negative", null);
        }
        if (product.getPrice().compareTo(new BigDecimal("9999.99")) > 0) {
            throw new DAOException("Validation", "Price cannot exceed $9999.99", null);
        }

        if (product.getStock() < 0) {
            throw new DAOException("Validation", "Stock cannot be negative", null);
        }

        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
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
            throw new DAOException("Validation",
                    "Category must be: appetizer, main_course, dessert, beverage, or other", null);
        }

        LOGGER.log(Level.FINE, "Product data validation passed");
    }

    private void logSQLError(SQLException e, String operation, String sql) {
        LOGGER.log(Level.SEVERE, "SQL Error in {0}: ErrorCode={1}, SQLState={2}, Message={3}",
                new Object[]{operation, e.getErrorCode(), e.getSQLState(), e.getMessage()});
        if (sql != null && DEBUG_MODE) {
            LOGGER.log(Level.SEVERE, "SQL Query: {0}", sql);
        }
        LOGGER.log(Level.SEVERE, "Exception details", e);
    }

    private void handleRollback(Connection conn, String operation) {
        if (conn != null) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Transaction rolled back for: {0}", operation);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Rollback failed for: {0}", operation);
            }
        }
    }

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

    private void logOperationEnd(String operation, long startTime, boolean success) {
        long duration = System.currentTimeMillis() - startTime;
        if (success) {
            LOGGER.log(Level.INFO, "Completed {0} successfully in {1}ms", new Object[]{operation, duration});
        } else {
            LOGGER.log(Level.WARNING, "Failed {0} after {1}ms", new Object[]{operation, duration});
        }
    }

    // ========================================
    // READ Operations
    // ========================================

    /**
     * Get product by ID
     */
    public Product getProductById(int productId) throws DAOException {
        long startTime = logOperationStart("getProductById", productId);

        if (productId <= 0) {
            throw new DAOException("GetProductById", "Product ID must be positive", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            stmt.setInt(1, productId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                conn.commit();
                LOGGER.log(Level.INFO, "Product found: ID={0}, Name={1}",
                        new Object[]{productId, product.getProductName()});
                logOperationEnd("getProductById", startTime, true);
                return product;
            } else {
                LOGGER.log(Level.INFO, "No product found with ID: {0}", productId);
                handleRollback(conn, "getProductById");
                logOperationEnd("getProductById", startTime, false);
                return null;
            }
        } catch (SQLException e) {
            logSQLError(e, "getProductById", SQL_SELECT_BY_ID);
            handleRollback(conn, "getProductById");
            logOperationEnd("getProductById", startTime, false);
            throw new DAOException("GetProductById", "Database error retrieving product", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() throws DAOException {
        long startTime = logOperationStart("getAllProducts");
        List<Product> products = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Retrieved {0} products from database", products.size());
            logOperationEnd("getAllProducts", startTime, true);
            return products;
        } catch (SQLException e) {
            logSQLError(e, "getAllProducts", SQL_SELECT_ALL);
            handleRollback(conn, "getAllProducts");
            logOperationEnd("getAllProducts", startTime, false);
            throw new DAOException("GetAllProducts", "Database error retrieving products", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) throws DAOException {
        long startTime = logOperationStart("getProductsByCategory", category);

        if (category == null || category.trim().isEmpty()) {
            throw new DAOException("GetProductsByCategory", "Category cannot be empty", null);
        }

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_BY_CATEGORY);
            stmt.setString(1, category);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Retrieved {0} products in category: {1}",
                    new Object[]{products.size(), category});
            logOperationEnd("getProductsByCategory", startTime, true);
            return products;
        } catch (SQLException e) {
            logSQLError(e, "getProductsByCategory", SQL_SELECT_BY_CATEGORY);
            handleRollback(conn, "getProductsByCategory");
            logOperationEnd("getProductsByCategory", startTime, false);
            throw new DAOException("GetProductsByCategory", "Database error retrieving products", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Get available products (status='available' AND stock>0)
     */
    public List<Product> getAvailableProducts() throws DAOException {
        long startTime = logOperationStart("getAvailableProducts");
        List<Product> products = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_AVAILABLE);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Retrieved {0} available products", products.size());
            logOperationEnd("getAvailableProducts", startTime, true);
            return products;
        } catch (SQLException e) {
            logSQLError(e, "getAvailableProducts", SQL_SELECT_AVAILABLE);
            handleRollback(conn, "getAvailableProducts");
            logOperationEnd("getAvailableProducts", startTime, false);
            throw new DAOException("GetAvailableProducts", "Database error retrieving products", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Search products by name (case-insensitive, partial match)
     */
    public List<Product> searchProductsByName(String keyword) throws DAOException {
        long startTime = logOperationStart("searchProductsByName", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new DAOException("SearchProducts", "Search keyword cannot be empty", null);
        }

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SEARCH_BY_NAME);
            stmt.setString(1, "%" + keyword + "%");
            rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Found {0} products matching: {1}",
                    new Object[]{products.size(), keyword});
            logOperationEnd("searchProductsByName", startTime, true);
            return products;
        } catch (SQLException e) {
            logSQLError(e, "searchProductsByName", SQL_SEARCH_BY_NAME);
            handleRollback(conn, "searchProductsByName");
            logOperationEnd("searchProductsByName", startTime, false);
            throw new DAOException("SearchProducts", "Database error searching products", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    // ========================================
    // CREATE Operation
    // ========================================

    /**
     * Add new product
     */
    public boolean addProduct(Product product) throws DAOException {
        long startTime = logOperationStart("addProduct", product != null ? product.getProductName() : "null");

        validateProductData(product);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_INSERT_PRODUCT);

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Product added successfully: {0}", product.getProductName());
                logOperationEnd("addProduct", startTime, true);
                return true;
            } else {
                handleRollback(conn, "addProduct");
                logOperationEnd("addProduct", startTime, false);
                return false;
            }
        } catch (SQLException e) {
            logSQLError(e, "addProduct", SQL_INSERT_PRODUCT);
            handleRollback(conn, "addProduct");
            logOperationEnd("addProduct", startTime, false);
            throw new DAOException("AddProduct", "Database error adding product", e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    // ========================================
    // UPDATE Operations
    // ========================================

    /**
     * Update product information
     */
    public boolean updateProduct(Product product) throws DAOException {
        long startTime = logOperationStart("updateProduct", product != null ? product.getProductId() : "null");

        if (product == null || product.getProductId() <= 0) {
            throw new DAOException("UpdateProduct", "Invalid product object", null);
        }

        validateProductData(product);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_PRODUCT);

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setInt(7, product.getProductId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Product updated: ID={0}", product.getProductId());
                logOperationEnd("updateProduct", startTime, true);
                return true;
            } else {
                handleRollback(conn, "updateProduct");
                LOGGER.log(Level.WARNING, "Update failed: Product not found ID={0}", product.getProductId());
                logOperationEnd("updateProduct", startTime, false);
                return false;
            }
        } catch (SQLException e) {
            logSQLError(e, "updateProduct", SQL_UPDATE_PRODUCT);
            handleRollback(conn, "updateProduct");
            logOperationEnd("updateProduct", startTime, false);
            throw new DAOException("UpdateProduct", "Database error updating product", e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * Update product stock
     */
    public boolean updateStock(int productId, int newStock) throws DAOException {
        long startTime = logOperationStart("updateStock", productId, newStock);

        if (productId <= 0) {
            throw new DAOException("UpdateStock", "Product ID must be positive", null);
        }
        if (newStock < 0) {
            throw new DAOException("UpdateStock", "Stock cannot be negative", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_STOCK);
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Stock updated: ID={0}, New stock={1}",
                        new Object[]{productId, newStock});
                logOperationEnd("updateStock", startTime, true);
                return true;
            } else {
                handleRollback(conn, "updateStock");
                logOperationEnd("updateStock", startTime, false);
                return false;
            }
        } catch (SQLException e) {
            logSQLError(e, "updateStock", SQL_UPDATE_STOCK);
            handleRollback(conn, "updateStock");
            logOperationEnd("updateStock", startTime, false);
            throw new DAOException("UpdateStock", "Database error updating stock", e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * Update product status
     */
    public boolean updateStatus(int productId, String status) throws DAOException {
        long startTime = logOperationStart("updateStatus", productId, status);

        if (productId <= 0) {
            throw new DAOException("UpdateStatus", "Product ID must be positive", null);
        }
        if (!"available".equalsIgnoreCase(status) && !"unavailable".equalsIgnoreCase(status)) {
            throw new DAOException("UpdateStatus", "Status must be 'available' or 'unavailable'", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_STATUS);
            stmt.setString(1, status);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Status updated: ID={0}, Status={1}",
                        new Object[]{productId, status});
                logOperationEnd("updateStatus", startTime, true);
                return true;
            } else {
                handleRollback(conn, "updateStatus");
                logOperationEnd("updateStatus", startTime, false);
                return false;
            }
        } catch (SQLException e) {
            logSQLError(e, "updateStatus", SQL_UPDATE_STATUS);
            handleRollback(conn, "updateStatus");
            logOperationEnd("updateStatus", startTime, false);
            throw new DAOException("UpdateStatus", "Database error updating status", e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    // ========================================
    // DELETE Operation
    // ========================================

    /**
     * Delete product
     */
    public boolean deleteProduct(int productId) throws DAOException {
        long startTime = logOperationStart("deleteProduct", productId);

        if (productId <= 0) {
            throw new DAOException("DeleteProduct", "Product ID must be positive", null);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_DELETE_PRODUCT);
            stmt.setInt(1, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                LOGGER.log(Level.INFO, "Product deleted: ID={0}", productId);
                logOperationEnd("deleteProduct", startTime, true);
                return true;
            } else {
                handleRollback(conn, "deleteProduct");
                LOGGER.log(Level.WARNING, "Delete failed: Product not found ID={0}", productId);
                logOperationEnd("deleteProduct", startTime, false);
                return false;
            }
        } catch (SQLException e) {
            logSQLError(e, "deleteProduct", SQL_DELETE_PRODUCT);
            handleRollback(conn, "deleteProduct");
            logOperationEnd("deleteProduct", startTime, false);

            if (e.getErrorCode() == 2292) {
                throw new DAOException("DeleteProduct",
                        "Cannot delete product with existing orders", e);
            }
            throw new DAOException("DeleteProduct", "Database error deleting product", e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    // ========================================
    // COUNT Operations
    // ========================================

    /**
     * Get total product count
     */
    public int getProductCount() throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_COUNT_PRODUCTS);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                conn.commit();
                return count;
            }
            handleRollback(conn, "getProductCount");
            return 0;
        } catch (SQLException e) {
            logSQLError(e, "getProductCount", SQL_COUNT_PRODUCTS);
            handleRollback(conn, "getProductCount");
            throw new DAOException("GetProductCount", "Database error counting products", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Get product count by category
     */
    public int getProductCountByCategory(String category) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SQL_COUNT_BY_CATEGORY);
            stmt.setString(1, category);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                conn.commit();
                return count;
            }
            handleRollback(conn, "getProductCountByCategory");
            return 0;
        } catch (SQLException e) {
            logSQLError(e, "getProductCountByCategory", SQL_COUNT_BY_CATEGORY);
            handleRollback(conn, "getProductCountByCategory");
            throw new DAOException("GetProductCountByCategory", "Database error counting products", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    // ========================================
    // Main Method (for testing)
    // ========================================

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("ProductDAO Complete CRUD Test");
        System.out.println("========================================");

        ProductDAO productDAO = new ProductDAO();

        try {
            // Test 1: Get all products
            System.out.println("\n--- Test 1: Get All Products ---");
            List<Product> products = productDAO.getAllProducts();
            System.out.println("✓ Retrieved " + products.size() + " products");

            // Test 2: Get by category
            System.out.println("\n--- Test 2: Get Products by Category ---");
            List<Product> appetizers = productDAO.getProductsByCategory("appetizer");
            System.out.println("✓ Found " + appetizers.size() + " appetizers");

            // Test 3: Search products
            System.out.println("\n--- Test 3: Search Products ---");
            List<Product> searchResults = productDAO.searchProductsByName("pizza");
            System.out.println("✓ Found " + searchResults.size() + " products matching 'pizza'");

            // Test 4: Get available products
            System.out.println("\n--- Test 4: Get Available Products ---");
            List<Product> available = productDAO.getAvailableProducts();
            System.out.println("✓ Found " + available.size() + " available products");

            // Test 5: Product count
            System.out.println("\n--- Test 5: Product Count ---");
            int count = productDAO.getProductCount();
            System.out.println("✓ Total products: " + count);

            System.out.println("\n========================================");
            System.out.println("All ProductDAO tests passed!");
            System.out.println("========================================");

        } catch (DAOException e) {
            System.err.println("Test failed with exception:");
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
=======
import model.Product;
import java.util.List;

/**
 * Product Data Access Object Interface
 * Responsibility: Define data access operations for Product entity
 */
public interface ProductDAO {
    
    /**
     * Get all products
     * @return List of all products
     */
    List<Product> findAll();
    
    /**
     * Find product by ID
     * @param productId Product ID
     * @return Product object, null if not found
     */
    Product findById(int productId);
    
    /**
     * Create a new product
     * @param product Product object to create
     * @return Generated product ID, or -1 if failed
     */
    int create(Product product);
    
    /**
     * Update existing product
     * @param product Product object with updated information
     * @return Number of rows affected
     */
    int update(Product product);
    
    /**
     * Delete product by ID
     * @param productId Product ID
     * @return Number of rows affected
     */
    int deleteById(int productId);
    
    /**
     * Find products by category
     * @param category Category name
     * @return List of products in the category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find available products (in stock and available status)
     * @return List of available products
     */
    List<Product> findAvailable();
}
>>>>>>> origin/backend
