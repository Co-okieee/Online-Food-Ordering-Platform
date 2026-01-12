package model;

import java.sql.Timestamp;

/**
 * Product Model Class
 * Represents a food product in the system
 * Corresponds to PRODUCTS table in database
 */
public class Product {
    
    // Primary Key
    private int productId;
    
    // Product Information
    private String productName;
    private String description;
    
    // Pricing and Inventory
    private double price;
    private int stock;
    
    // Classification
    private String category;  // 'appetizer', 'main_course', 'dessert', 'beverage', 'other'
    
    // Media
    private String imageUrl;
    
    // Availability
    private String status;    // 'available', 'unavailable', 'discontinued'
    
    // Timestamps
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    
    /**
     * Default constructor
     */
    public Product() {
    }
    
    /**
     * Constructor for creating new product (without ID)
     */
    public Product(String productName, String description, double price, int stock,
                   String category, String imageUrl, String status) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status;
    }
    
    /**
     * Full constructor (with ID - for database retrieval)
     */
    public Product(int productId, String productName, String description, double price,
                   int stock, String category, String imageUrl, String status,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility Methods
    
    /**
     * Check if product is available for order
     */
    public boolean isAvailable() {
        return "available".equalsIgnoreCase(this.status) && this.stock > 0;
    }
    
    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return this.stock > 0;
    }
    
    /**
     * Decrease stock by quantity
     */
    public boolean decreaseStock(int quantity) {
        if (this.stock >= quantity) {
            this.stock -= quantity;
            return true;
        }
        return false;
    }
    
    /**
     * Increase stock by quantity
     */
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
    
    /**
     * Get category emoji (for frontend display)
     */
    public String getCategoryEmoji() {
        switch (this.category.toLowerCase()) {
            case "appetizer":
                return "🥗";
            case "main_course":
                return "🍔";
            case "dessert":
                return "🍰";
            case "beverage":
                return "🥤";
            default:
                return "🍽️";
        }
    }
    
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
}