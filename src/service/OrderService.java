package service;

import dao.OrderDAO;
import dao.ProductDAO;
import model.Order;
import model.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Order Service Class
 * Responsibility: Handle order-related business logic and calculation
 */
public class OrderService {
    private OrderDAO orderDAO;
    private ProductDAO productDAO;

    public OrderService(OrderDAO orderDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    /**
     * Place an order
     * @param userId User ID
     * @param cartItems Shopping cart items (Product ID -> Quantity)
     * @param deliveryAddress Delivery address
     * @param paymentMethod Payment method
     * @param notes Notes
     * @return Order ID, -1 if failed
     */
    public int placeOrder(int userId, Map<Integer, Integer> cartItems, 
                          String deliveryAddress, String paymentMethod, String notes) {
        // Validate user ID
        if (userId <= 0) {
            return -1;
        }

        // Validate shopping cart
        if (cartItems == null || cartItems.isEmpty()) {
            return -1;
        }

        // Validate delivery address
        if (!validateDeliveryAddress(deliveryAddress)) {
            return -1;
        }

        // Validate payment method
        if (!validatePaymentMethod(paymentMethod)) {
            return -1;
        }

        // Validate stock
        if (!validateStock(cartItems)) {
            return -1;
        }

        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(cartItems);
        
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return -1;
        }

        // Create order object
        Order order = new Order(userId, totalAmount, deliveryAddress.trim(), paymentMethod.trim());
        order.setStatus("pending");
        order.setPaymentStatus("pending");
        
        if (notes != null && !notes.trim().isEmpty()) {
            order.setNotes(notes.trim());
        }

        // Create order record
        int orderId = orderDAO.createOrder(order);
        
        if (orderId > 0) {
            // Create order items
            boolean itemsCreated = createOrderItems(orderId, cartItems);
            
            if (itemsCreated) {
                // Update stock
                updateProductStock(cartItems);
                return orderId;
            } else {
                return -1;
            }
        }

        return -1;
    }

    /**
     * Calculate order total amount
     * @param cartItems Shopping cart items
     * @return Total amount
     */
    public BigDecimal calculateTotalAmount(Map<Integer, Integer> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            if (quantity <= 0) {
                continue;
            }

            Product product = productDAO.findById(productId);
            if (product != null && product.getPrice() != null) {
                BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(quantity));
                total = total.add(itemTotal);
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validate if stock is sufficient
     * @param cartItems Shopping cart items
     * @return true if stock is sufficient, false otherwise
     */
    public boolean validateStock(Map<Integer, Integer> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return false;
        }

        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            if (quantity <= 0) {
                return false;
            }

            Product product = productDAO.findById(productId);
            
            if (product == null) {
                return false;
            }

            if (!product.isAvailable()) {
                return false;
            }

            if (product.getStock() < quantity) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create order items
     * @param orderId Order ID
     * @param cartItems Shopping cart items
     * @return true if successful, false if failed
     */
    private boolean createOrderItems(int orderId, Map<Integer, Integer> cartItems) {
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            
            Product product = productDAO.findById(productId);
            if (product == null) {
                return false;
            }

            double price = product.getPrice().doubleValue();
            int result = orderDAO.createOrderItem(orderId, productId, quantity, price);
            
            if (result <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Update product stock
     * @param cartItems Shopping cart items
     */
    private void updateProductStock(Map<Integer, Integer> cartItems) {
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            
            Product product = productDAO.findById(productId);
            if (product != null) {
                product.decreaseStock(quantity);
                productDAO.update(product);
            }
        }
    }

    /**
     * Get order list by user ID
     * @param userId User ID
     * @return Order list
     */
    public List<Order> getOrdersByUserId(int userId) {
        if (userId <= 0) {
            return null;
        }
        return orderDAO.findByUserId(userId);
    }

    /**
     * Validate delivery address
     * @param address Delivery address
     * @return true if valid, false if invalid
     */
    public boolean validateDeliveryAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return address.trim().length() >= 5;
    }

    /**
     * Validate payment method
     * @param method Payment method
     * @return true if valid, false if invalid
     */
    public boolean validatePaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return false;
        }
        String normalized = method.toLowerCase().trim();
        return normalized.equals("cash") || 
               normalized.equals("card") || 
               normalized.equals("online");
    }

    /**
     * Check if can place order
     * @param userId User ID
     * @param cartItems Shopping cart items
     * @return true if can place order, false otherwise
     */
    public boolean canPlaceOrder(int userId, Map<Integer, Integer> cartItems) {
        if (userId <= 0 || cartItems == null || cartItems.isEmpty()) {
            return false;
        }
        return validateStock(cartItems);
    }

    /**
     * Calculate item subtotal
     * @param productId Product ID
     * @param quantity Quantity
     * @return Subtotal amount
     */
    public BigDecimal calculateItemSubtotal(int productId, int quantity) {
        if (quantity <= 0) {
            return BigDecimal.ZERO;
        }

        Product product = productDAO.findById(productId);
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }

        return product.getPrice().multiply(new BigDecimal(quantity))
                                  .setScale(2, RoundingMode.HALF_UP);
    }
}
