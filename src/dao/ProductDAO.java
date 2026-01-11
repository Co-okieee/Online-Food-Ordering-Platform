package dao;

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
