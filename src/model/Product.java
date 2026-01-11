package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Product Model Class
 * 
 * Purpose: Represent a product entity in the Food Ordering System
 * This class maps to the 'products' table in the database
 * 
 * Design Pattern: JavaBean / POJO (Plain Old Java Object)
 * 
 * @author Cookie
 * @version 1.0
 */
public class Product {
    
    // ========================================
    // Private Fields (match database columns)
    // ========================================
    
    /**
     * Unique product identifier (Primary Key)
     */
    private int productId;
    
    /**
     * Product name
     */
    private String productName;
    
    /**
     * Product description
     */
    private String description;
    
    /**
     * Product price (using BigDecimal for precise monetary calculations)
     */
    private BigDecimal price;
    
    /**
     * Stock quantity available
     */
    private int stock;
    
    /**
     * Product category: appetizer, main_course, dessert, beverage, other
     */
    private String category;
    
    /**
     * Image URL or path
     */
    private String imageUrl;
    
    /**
     * Product status: available, unavailable
     */
    private String status;
    
    /**
     * Timestamp when product was created
     */
    private Timestamp createdAt;
    
    /**
     * Timestamp when product was last updated
     */
    private Timestamp updatedAt;
    
    // ========================================
    // Constructors
    // ========================================
    
    /**
     * Default constructor
     * Required for JavaBean specification
     */
    public Product() {
        // Empty constructor
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param productName Product name
     * @param description Description
     * @param price Price
     * @param stock Stock quantity
     * @param category Category
     */
    public Product(String productName, String description, BigDecimal price, 
                   int stock, String category) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = "available";  // Default status
    }
    
    /**
     * Constructor with all fields
     * 
     * @param productId Product ID
     * @param productName Product name
     * @param description Description
     * @param price Price
     * @param stock Stock quantity
     * @param category Category
     * @param imageUrl Image URL
     * @param status Status
     */
    public Product(int productId, String productName, String description, 
                   BigDecimal price, int stock, String category, 
                   String imageUrl, String status) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status;
    }
    
    // ========================================
    // Getters and Setters
    // ========================================
    
    /**
     * Get product ID
     * @return Product ID
     */
    public int getProductId() {
        return productId;
    }
    
    /**
     * Set product ID
     * @param productId Product ID
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    /**
     * Get product name
     * @return Product name
     */
    public String getProductName() {
        return productName;
    }
    
    /**
     * Set product name
     * @param productName Product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    /**
     * Get description
     * @return Description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set description
     * @param description Description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get price
     * @return Price
     */
    public BigDecimal getPrice() {
        return price;
    }
    
    /**
     * Set price
     * @param price Price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    /**
     * Get stock quantity
     * @return Stock quantity
     */
    public int getStock() {
        return stock;
    }
    
    /**
     * Set stock quantity
     * @param stock Stock quantity
     */
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    /**
     * Get category
     * @return Category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Set category
     * @param category Category
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Get image URL
     * @return Image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }
    
    /**
     * Set image URL
     * @param imageUrl Image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * Get status
     * @return Status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Set status
     * @param status Status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Get creation timestamp
     * @return Creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set creation timestamp
     * @param createdAt Creation timestamp
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get last update timestamp
     * @return Update timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Set last update timestamp
     * @param updatedAt Update timestamp
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ========================================
    // Utility Methods
    // ========================================
    
    /**
     * Check if product is available
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return "available".equalsIgnoreCase(status) && stock > 0;
    }
    
    /**
     * Check if product is in stock
     * @return true if in stock, false otherwise
     */
    public boolean isInStock() {
        return stock > 0;
    }
    
    /**
     * Check if product is low stock (less than 10)
     * @return true if low stock, false otherwise
     */
    public boolean isLowStock() {
        return stock > 0 && stock < 10;
    }
    
    /**
     * Get formatted price string
     * @return Price formatted as currency (e.g., "$12.99")
     */
    public String getFormattedPrice() {
        if (price != null) {
            return "$" + price.toString();
        }
        return "$0.00";
    }
    
    /**
     * Decrease stock by quantity
     * @param quantity Quantity to decrease
     * @return true if successful, false if insufficient stock
     */
    public boolean decreaseStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
            return true;
        }
        return false;
    }
    
    /**
     * Increase stock by quantity
     * @param quantity Quantity to increase
     */
    public void increaseStock(int quantity) {
        stock += quantity;
    }
    
    // ========================================
    // Override Methods
    // ========================================
    
    /**
     * String representation of Product object
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
    /**
     * Check if two Product objects are equal
     * 
     * @param obj Object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Product product = (Product) obj;
        return productId == product.productId;
    }
    
    /**
     * Generate hash code for Product object
     * 
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(productId);
    }
}
