package dao;

import model.Order;
import java.util.List;

/**
 * Order Data Access Object Interface
 * Responsibility: Define data access operations for Order entity
 */
public interface OrderDAO {
    
    /**
     * Create a new order
     * @param order Order object to create
     * @return Generated order ID, or -1 if failed
     */
    int createOrder(Order order);
    
    /**
     * Create an order item
     * @param orderId Order ID
     * @param productId Product ID
     * @param quantity Quantity
     * @param price Unit price
     * @return Number of rows affected
     */
    int createOrderItem(int orderId, int productId, int quantity, double price);
    
    /**
     * Find orders by user ID
     * @param userId User ID
     * @return List of orders for the user
     */
    List<Order> findByUserId(int userId);
    
    /**
     * Find order by ID
     * @param orderId Order ID
     * @return Order object, null if not found
     */
    Order findById(int orderId);
    
    /**
     * Update order
     * @param order Order object with updated information
     * @return Number of rows affected
     */
    int update(Order order);
    
    /**
     * Find all orders
     * @return List of all orders
     */
    List<Order> findAll();
    
    /**
     * Update order status
     * @param orderId Order ID
     * @param status New status
     * @return Number of rows affected
     */
    int updateStatus(int orderId, String status);
}
