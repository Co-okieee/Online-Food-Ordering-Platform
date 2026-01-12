package service;

import dao.ProductDAO;
import model.Product;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service Class
 * Handles business logic for product operations
 * Including CRUD operations, filtering, searching, stock management
 */
public class ProductService {
    
    private ProductDAO productDAO;
    
    /**
     * Constructor
     */
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    // ================================
    // Product Creation
    // ================================
    
    /**
     * Add a new product
     * @param product Product object to add
     * @return Created product with ID, or null if failed
     */
    public Product addProduct(Product product) {
        try {
            // Validate product data
            if (!validateProduct(product)) {
                return null;
            }
            
            // Insert product
            int productId = productDAO.insertProduct(product);
            
            if (productId > 0) {
                product.setProductId(productId);
                System.out.println("Product added successfully: " + product.getProductName());
                return product;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Add a new product with parameters
     */
    public Product addProduct(String name, String description, double price, 
                             int stock, String category, String imageUrl, String status) {
        Product product = new Product(name, description, price, stock, category, imageUrl, status);
        return addProduct(product);
    }
    
    /**
     * Validate product data
     */
    private boolean validateProduct(Product product) {
        // Name validation
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            System.out.println("Product name cannot be empty");
            return false;
        }
        
        // Price validation
        if (product.getPrice() < 0) {
            System.out.println("Product price cannot be negative");
            return false;
        }
        
        // Stock validation
        if (product.getStock() < 0) {
            System.out.println("Product stock cannot be negative");
            return false;
        }
        
        // Category validation
        String category = product.getCategory();
        if (category == null || (!category.equals("appetizer") && 
            !category.equals("main_course") && !category.equals("dessert") && 
            !category.equals("beverage") && !category.equals("other"))) {
            System.out.println("Invalid product category: " + category);
            return false;
        }
        
        // Status validation
        String status = product.getStatus();
        if (status == null || (!status.equals("available") && 
            !status.equals("unavailable") && !status.equals("discontinued"))) {
            System.out.println("Invalid product status: " + status);
            return false;
        }
        
        return true;
    }
    
    // ================================
    // Product Retrieval
    // ================================
    
    /**
     * Get product by ID
     */
    public Product getProductById(int productId) {
        try {
            return productDAO.getProductById(productId);
        } catch (SQLException e) {
            System.err.println("Error getting product by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        try {
            return productDAO.getAllProducts();
        } catch (SQLException e) {
            System.err.println("Error getting all products: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) {
        try {
            return productDAO.getProductsByCategory(category);
        } catch (SQLException e) {
            System.err.println("Error getting products by category: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get products by status
     */
    public List<Product> getProductsByStatus(String status) {
        try {
            return productDAO.getProductsByStatus(status);
        } catch (SQLException e) {
            System.err.println("Error getting products by status: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get available products (status = available and stock > 0)
     */
    public List<Product> getAvailableProducts() {
        try {
            List<Product> allProducts = productDAO.getAllProducts();
            return allProducts.stream()
                    .filter(Product::isAvailable)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Error getting available products: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Search products by name
     */
    public List<Product> searchProducts(String keyword) {
        try {
            return productDAO.searchProducts(keyword);
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get products with pagination
     */
    public List<Product> getProductsWithPagination(int offset, int limit) {
        try {
            return productDAO.getProductsWithPagination(offset, limit);
        } catch (SQLException e) {
            System.err.println("Error getting products with pagination: " + e.getMessage());
            return null;
        }
    }
    
    // ================================
    // Product Update
    // ================================
    
    /**
     * Update product
     */
    public boolean updateProduct(Product product) {
        try {
            // Validate product data
            if (!validateProduct(product)) {
                return false;
            }
            
            boolean success = productDAO.updateProduct(product);
            
            if (success) {
                System.out.println("Product updated successfully: " + product.getProductName());
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update product stock
     */
    public boolean updateProductStock(int productId, int newStock) {
        try {
            if (newStock < 0) {
                System.out.println("Stock cannot be negative");
                return false;
            }
            
            return productDAO.updateProductStock(productId, newStock);
            
        } catch (SQLException e) {
            System.err.println("Error updating product stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update product status
     */
    public boolean updateProductStatus(int productId, String status) {
        try {
            // Validate status
            if (!status.equals("available") && !status.equals("unavailable") && 
                !status.equals("discontinued")) {
                System.out.println("Invalid status: " + status);
                return false;
            }
            
            return productDAO.updateProductStatus(productId, status);
            
        } catch (SQLException e) {
            System.err.println("Error updating product status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update product price
     */
    public boolean updateProductPrice(int productId, double newPrice) {
        try {
            if (newPrice < 0) {
                System.out.println("Price cannot be negative");
                return false;
            }
            
            return productDAO.updateProductPrice(productId, newPrice);
            
        } catch (SQLException e) {
            System.err.println("Error updating product price: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Product Deletion
    // ================================
    
    /**
     * Delete product by ID
     * Note: Will fail if product is referenced in orders
     */
    public boolean deleteProduct(int productId) {
        try {
            boolean success = productDAO.deleteProduct(productId);
            
            if (success) {
                System.out.println("Product deleted successfully: ID " + productId);
            } else {
                System.out.println("Cannot delete product (may be referenced in orders)");
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Stock Management
    // ================================
    
    /**
     * Decrease product stock (when order is placed)
     * @param productId Product ID
     * @param quantity Quantity to decrease
     * @return true if stock decreased successfully
     */
    public boolean decreaseStock(int productId, int quantity) {
        try {
            // Get current product
            Product product = productDAO.getProductById(productId);
            
            if (product == null) {
                System.out.println("Product not found: ID " + productId);
                return false;
            }
            
            // Check if enough stock
            if (product.getStock() < quantity) {
                System.out.println("Insufficient stock for product: " + product.getProductName() +
                                 " (available: " + product.getStock() + ", requested: " + quantity + ")");
                return false;
            }
            
            // Decrease stock
            int newStock = product.getStock() - quantity;
            return productDAO.updateProductStock(productId, newStock);
            
        } catch (SQLException e) {
            System.err.println("Error decreasing stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Increase product stock (when order is cancelled or restocked)
     */
    public boolean increaseStock(int productId, int quantity) {
        try {
            // Get current product
            Product product = productDAO.getProductById(productId);
            
            if (product == null) {
                System.out.println("Product not found: ID " + productId);
                return false;
            }
            
            // Increase stock
            int newStock = product.getStock() + quantity;
            return productDAO.updateProductStock(productId, newStock);
            
        } catch (SQLException e) {
            System.err.println("Error increasing stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if product has sufficient stock
     */
    public boolean hasStock(int productId, int quantity) {
        try {
            Product product = productDAO.getProductById(productId);
            return product != null && product.getStock() >= quantity;
        } catch (SQLException e) {
            System.err.println("Error checking stock: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Statistics
    // ================================
    
    /**
     * Get total product count
     */
    public int getTotalProductCount() {
        try {
            return productDAO.getTotalProductCount();
        } catch (SQLException e) {
            System.err.println("Error getting product count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get product count by category
     */
    public int getProductCountByCategory(String category) {
        try {
            return productDAO.getProductCountByCategory(category);
        } catch (SQLException e) {
            System.err.println("Error getting product count by category: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get product count by status
     */
    public int getProductCountByStatus(String status) {
        try {
            return productDAO.getProductCountByStatus(status);
        } catch (SQLException e) {
            System.err.println("Error getting product count by status: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get low stock products (stock <= threshold)
     */
    public List<Product> getLowStockProducts(int threshold) {
        try {
            return productDAO.getLowStockProducts(threshold);
        } catch (SQLException e) {
            System.err.println("Error getting low stock products: " + e.getMessage());
            return null;
        }
    }
}