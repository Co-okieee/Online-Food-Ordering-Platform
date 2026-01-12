package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Model Class
 * Represents a customer order in the system
 * Corresponds to ORDERS table in database
 */
public class Order {
    
    // Primary Key
    private int orderId;
    
    // Customer Reference
    private int userId;
    
    // Order Information
    private Timestamp orderDate;
    private double totalAmount;
    
    // Order Status Tracking
    private String status;  // 'pending', 'confirmed', 'preparing', 'ready', 'delivered', 'cancelled'
    
    // Delivery Information
    private String deliveryAddress;
    
    // Payment Information
    private String paymentMethod;  // 'cash', 'card', 'online'
    private String paymentStatus;  // 'pending', 'paid', 'failed'
    
    // Additional Information
    private String notes;
    
    // Timestamps
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Related Data (not in database, populated via JOIN)
    private String username;  // Customer username
    private List<OrderItem> orderItems;  // Order items list
    
    // Constructors
    
    /**
     * Default constructor
     */
    public Order() {
        this.orderItems = new ArrayList<>();
    }
    
    /**
     * Constructor for creating new order (without ID)
     */
    public Order(int userId, double totalAmount, String deliveryAddress,
                 String paymentMethod, String notes) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.status = "pending";
        this.paymentStatus = "pending";
        this.orderItems = new ArrayList<>();
    }
    
    /**
     * Full constructor (with ID - for database retrieval)
     */
    public Order(int orderId, int userId, Timestamp orderDate, double totalAmount,
                 String status, String deliveryAddress, String paymentMethod,
                 String paymentStatus, String notes, Timestamp createdAt, Timestamp updatedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.orderItems = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Timestamp getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    // Utility Methods
    
    /**
     * Add an order item to this order
     */
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
    }
    
    /**
     * Check if order is pending
     */
    public boolean isPending() {
        return "pending".equalsIgnoreCase(this.status);
    }
    
    /**
     * Check if order is completed/delivered
     */
    public boolean isCompleted() {
        return "delivered".equalsIgnoreCase(this.status);
    }
    
    /**
     * Check if order is cancelled
     */
    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(this.status);
    }
    
    /**
     * Check if payment is completed
     */
    public boolean isPaid() {
        return "paid".equalsIgnoreCase(this.paymentStatus);
    }
    
    /**
     * Get total number of items in order
     */
    public int getTotalItems() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
    
    /**
     * Recalculate total amount from order items
     */
    public void recalculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", itemsCount=" + orderItems.size() +
                '}';
    }
}