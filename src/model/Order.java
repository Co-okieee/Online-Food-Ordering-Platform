package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    // Fields
    private int orderId;
    private int userId;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String status;              // pending, preparing, delivered, cancelled
    private String deliveryAddress;
    private String paymentMethod;       // cash, card, online
    private String paymentStatus;       // pending, paid, refunded
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Order() {}

    public Order(int userId, BigDecimal totalAmount, String deliveryAddress,
                 String paymentMethod) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.status = "pending";
        this.paymentStatus = "pending";
    }

    // Getters and Setters (generate all)
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Business Methods
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isDelivered() {
        return "delivered".equalsIgnoreCase(status);
    }

    public boolean canBeCancelled() {
        return "pending".equalsIgnoreCase(status) || "preparing".equalsIgnoreCase(status);
    }

    public String getStatusDisplay() {
        switch (status.toLowerCase()) {
            case "pending": return "待处理";
            case "preparing": return "准备中";
            case "delivered": return "已送达";
            case "cancelled": return "已取消";
            default: return status;
        }
    }

    @Override
    public String toString() {
        return "Order{orderId=" + orderId + ", userId=" + userId +
                ", totalAmount=" + totalAmount + ", status='" + status + "'}";
    }
}