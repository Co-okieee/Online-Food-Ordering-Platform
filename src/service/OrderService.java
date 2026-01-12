package service;

import dao.OrderDAO;
import dao.OrderItemDAO;
import model.Order;
import model.OrderItem;
import java.sql.SQLException;
import java.util.List;

/**
 * Order Service Class
 * Handles business logic for order operations
 * Including order creation, status updates, order retrieval
 */
public class OrderService {
    
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private ProductService productService;
    
    /**
     * Constructor
     */
    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.productService = new ProductService();
    }
    
    // ================================
    // Order Creation
    // ================================
    
    /**
     * Create a new order with items
     * This is a transaction operation - all or nothing
     * @param order Order object (without items)
     * @param orderItems List of order items
     * @return Created order with ID and items, or null if failed
     */
    public Order createOrder(Order order, List<OrderItem> orderItems) {
        try {
            // Validate order data
            if (!validateOrder(order, orderItems)) {
                return null;
            }
            
            // Check stock availability for all items
            for (OrderItem item : orderItems) {
                if (!productService.hasStock(item.getProductId(), item.getQuantity())) {
                    System.out.println("Insufficient stock for product ID: " + item.getProductId());
                    return null;
                }
            }
            
            // Insert order
            int orderId = orderDAO.insertOrder(order);
            
            if (orderId <= 0) {
                System.out.println("Failed to create order");
                return null;
            }
            
            order.setOrderId(orderId);
            
            // Insert order items
            boolean allItemsInserted = true;
            for (OrderItem item : orderItems) {
                item.setOrderId(orderId);
                int itemId = orderItemDAO.insertOrderItem(item);
                
                if (itemId <= 0) {
                    allItemsInserted = false;
                    System.out.println("Failed to insert order item for product ID: " + item.getProductId());
                    break;
                }
                
                item.setOrderItemId(itemId);
                
                // Decrease product stock
                if (!productService.decreaseStock(item.getProductId(), item.getQuantity())) {
                    allItemsInserted = false;
                    System.out.println("Failed to decrease stock for product ID: " + item.getProductId());
                    break;
                }
            }
            
            // If any item failed, rollback by cancelling the order
            if (!allItemsInserted) {
                cancelOrder(orderId);
                return null;
            }
            
            order.setOrderItems(orderItems);
            System.out.println("Order created successfully: ID " + orderId);
            return order;
            
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validate order data
     */
    private boolean validateOrder(Order order, List<OrderItem> orderItems) {
        // Check user ID
        if (order.getUserId() <= 0) {
            System.out.println("Invalid user ID");
            return false;
        }
        
        // Check delivery address
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            System.out.println("Delivery address cannot be empty");
            return false;
        }
        
        // Check payment method
        String paymentMethod = order.getPaymentMethod();
        if (paymentMethod == null || (!paymentMethod.equals("cash") && 
            !paymentMethod.equals("card") && !paymentMethod.equals("online"))) {
            System.out.println("Invalid payment method: " + paymentMethod);
            return false;
        }
        
        // Check total amount
        if (order.getTotalAmount() <= 0) {
            System.out.println("Total amount must be greater than 0");
            return false;
        }
        
        // Check order items
        if (orderItems == null || orderItems.isEmpty()) {
            System.out.println("Order must contain at least one item");
            return false;
        }
        
        // Validate each order item
        for (OrderItem item : orderItems) {
            if (item.getProductId() <= 0) {
                System.out.println("Invalid product ID in order item");
                return false;
            }
            if (item.getQuantity() <= 0) {
                System.out.println("Item quantity must be greater than 0");
                return false;
            }
            if (item.getUnitPrice() < 0) {
                System.out.println("Item unit price cannot be negative");
                return false;
            }
        }
        
        return true;
    }
    
    // ================================
    // Order Retrieval
    // ================================
    
    /**
     * Get order by ID with items
     */
    public Order getOrderById(int orderId) {
        try {
            Order order = orderDAO.getOrderById(orderId);
            
            if (order != null) {
                // Load order items
                List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(orderId);
                order.setOrderItems(items);
            }
            
            return order;
            
        } catch (SQLException e) {
            System.err.println("Error getting order by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all orders for a user
     */
    public List<Order> getOrdersByUserId(int userId) {
        try {
            List<Order> orders = orderDAO.getOrdersByUserId(userId);
            
            // Load items for each order
            for (Order order : orders) {
                List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(order.getOrderId());
                order.setOrderItems(items);
            }
            
            return orders;
            
        } catch (SQLException e) {
            System.err.println("Error getting orders by user ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(String status) {
        try {
            List<Order> orders = orderDAO.getOrdersByStatus(status);
            
            // Load items for each order
            for (Order order : orders) {
                List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(order.getOrderId());
                order.setOrderItems(items);
            }
            
            return orders;
            
        } catch (SQLException e) {
            System.err.println("Error getting orders by status: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all orders (for admin)
     */
    public List<Order> getAllOrders() {
        try {
            List<Order> orders = orderDAO.getAllOrders();
            
            // Load items for each order
            for (Order order : orders) {
                List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(order.getOrderId());
                order.setOrderItems(items);
            }
            
            return orders;
            
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get recent orders for a user
     */
    public List<Order> getRecentOrders(int userId, int limit) {
        try {
            List<Order> orders = orderDAO.getRecentOrders(userId, limit);
            
            // Load items for each order
            for (Order order : orders) {
                List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(order.getOrderId());
                order.setOrderItems(items);
            }
            
            return orders;
            
        } catch (SQLException e) {
            System.err.println("Error getting recent orders: " + e.getMessage());
            return null;
        }
    }
    
    // ================================
    // Order Update
    // ================================
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            // Validate status
            if (!isValidOrderStatus(newStatus)) {
                System.out.println("Invalid order status: " + newStatus);
                return false;
            }
            
            boolean success = orderDAO.updateOrderStatus(orderId, newStatus);
            
            if (success) {
                System.out.println("Order status updated: ID " + orderId + " -> " + newStatus);
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(int orderId, String paymentStatus) {
        try {
            // Validate payment status
            if (!paymentStatus.equals("pending") && !paymentStatus.equals("paid") && 
                !paymentStatus.equals("failed")) {
                System.out.println("Invalid payment status: " + paymentStatus);
                return false;
            }
            
            return orderDAO.updatePaymentStatus(orderId, paymentStatus);
            
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update order delivery address
     */
    public boolean updateDeliveryAddress(int orderId, String newAddress) {
        try {
            if (newAddress == null || newAddress.trim().isEmpty()) {
                System.out.println("Delivery address cannot be empty");
                return false;
            }
            
            return orderDAO.updateDeliveryAddress(orderId, newAddress);
            
        } catch (SQLException e) {
            System.err.println("Error updating delivery address: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Order Cancellation
    // ================================
    
    /**
     * Cancel an order and restore stock
     */
    public boolean cancelOrder(int orderId) {
        try {
            // Get order
            Order order = getOrderById(orderId);
            
            if (order == null) {
                System.out.println("Order not found: ID " + orderId);
                return false;
            }
            
            // Check if order can be cancelled
            if (order.isCompleted() || order.isCancelled()) {
                System.out.println("Cannot cancel order in status: " + order.getStatus());
                return false;
            }
            
            // Restore stock for all items
            for (OrderItem item : order.getOrderItems()) {
                productService.increaseStock(item.getProductId(), item.getQuantity());
            }
            
            // Update order status to cancelled
            boolean success = orderDAO.updateOrderStatus(orderId, "cancelled");
            
            if (success) {
                System.out.println("Order cancelled successfully: ID " + orderId);
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error cancelling order: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Order Deletion
    // ================================
    
    /**
     * Delete order (admin only, cascades to order items)
     */
    public boolean deleteOrder(int orderId) {
        try {
            boolean success = orderDAO.deleteOrder(orderId);
            
            if (success) {
                System.out.println("Order deleted successfully: ID " + orderId);
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            return false;
        }
    }
    
    // ================================
    // Statistics
    // ================================
    
    /**
     * Get total order count
     */
    public int getTotalOrderCount() {
        try {
            return orderDAO.getTotalOrderCount();
        } catch (SQLException e) {
            System.err.println("Error getting order count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get order count by user
     */
    public int getOrderCountByUser(int userId) {
        try {
            return orderDAO.getOrderCountByUser(userId);
        } catch (SQLException e) {
            System.err.println("Error getting order count by user: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get order count by status
     */
    public int getOrderCountByStatus(String status) {
        try {
            return orderDAO.getOrderCountByStatus(status);
        } catch (SQLException e) {
            System.err.println("Error getting order count by status: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get total revenue
     */
    public double getTotalRevenue() {
        try {
            return orderDAO.getTotalRevenue();
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Get revenue by user
     */
    public double getRevenueByUser(int userId) {
        try {
            return orderDAO.getRevenueByUser(userId);
        } catch (SQLException e) {
            System.err.println("Error getting revenue by user: " + e.getMessage());
            return 0.0;
        }
    }
    
    // ================================
    // Validation Methods
    // ================================
    
    /**
     * Check if order status is valid
     */
    private boolean isValidOrderStatus(String status) {
        return status.equals("pending") || status.equals("confirmed") || 
               status.equals("preparing") || status.equals("ready") || 
               status.equals("delivered") || status.equals("cancelled");
    }
}