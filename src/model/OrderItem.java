package model;

import java.sql.Timestamp;

/**
 * OrderItem Model Class
 * Represents an individual item in an order
 * Corresponds to ORDER_ITEMS table in database
 */
public class OrderItem {
    
    // Primary Key
    private int orderItemId;
    
    // Foreign Keys
    private int orderId;
    private int productId;
    
    // Item Details
    private int quantity;
    private double unitPrice;
    private double subtotal;
    
    // Timestamp
    private Timestamp createdAt;
    
    // Related Data (not in database, populated via JOIN)
    private String productName;   // Product name
    private String category;       // Product category
    private String imageUrl;       // Product image
    
    // Constructors
    
    /**
     * Default constructor
     */
    public OrderItem() {
    }
    
    /**
     * Constructor for creating new order item (without ID)
     */
    public OrderItem(int orderId, int productId, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }
    
    /**
     * Full constructor (with ID - for database retrieval)
     */
    public OrderItem(int orderItemId, int orderId, int productId, int quantity,
                     double unitPrice, double subtotal, Timestamp createdAt) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
    }
    
    /**
     * Constructor with product details (for frontend display)
     */
    public OrderItem(int orderItemId, int orderId, int productId, int quantity,
                     double unitPrice, double subtotal, String productName,
                     String category, String imageUrl, Timestamp createdAt) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.productName = productName;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    public int getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.quantity * this.unitPrice;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = this.quantity * this.unitPrice;
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
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
    
    // Utility Methods
    
    /**
     * Recalculate subtotal from quantity and unit price
     */
    public void recalculateSubtotal() {
        this.subtotal = this.quantity * this.unitPrice;
    }
    
    /**
     * Increase quantity by amount
     */
    public void increaseQuantity(int amount) {
        this.quantity += amount;
        recalculateSubtotal();
    }
    
    /**
     * Decrease quantity by amount
     */
    public boolean decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            recalculateSubtotal();
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}