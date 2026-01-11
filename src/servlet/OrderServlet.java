package servlet;

import model.Order;
import model.OrderItem;
import model.User;
import service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderServlet - 处理订单相关的前端请求与订单流程
 * 
 * @author Your Name
 * @version 1.0
 */
@WebServlet("/OrderServlet")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private OrderService orderService;
    
    @Override
    public void init() throws ServletException {
        // 初始化 Service
        orderService = new OrderService();
    }
    
    /**
     * Handle GET requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list";
        }
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login to view orders");
            return;
        }
        
        switch (action) {
            case "list":
                listUserOrders(request, response);
                break;
            case "details":
                getOrderDetails(request, response);
                break;
            case "adminList":
                // ② 确认 Admin 判断
                User user = (User) session.getAttribute("user");
                if (user == null || !user.isAdmin()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                    return;
                }
                listAllOrders(request, response);
                break;
            default:
                listUserOrders(request, response);
                break;
        }
    }
    
    /**
     * Handle POST requests
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null || action.trim().isEmpty()) {
            sendJsonResponse(response, false, "No action specified");
            return;
        }
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJsonResponse(response, false, "Please login to perform this action");
            return;
        }
        
        switch (action) {
            case "create":
                createOrder(request, response);
                break;
            case "cancel":
                cancelOrder(request, response);
                break;
            case "updateStatus":
                // ② 确认 Admin 判断
                User user = (User) session.getAttribute("user");
                if (user == null || !user.isAdmin()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                    return;
                }
                updateOrderStatus(request, response);
                break;
            default:
                sendJsonResponse(response, false, "Invalid action");
                break;
        }
    }
    
    /**
     * Create new order
     */
    private void createOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        String deliveryAddress = request.getParameter("deliveryAddress");
        String paymentMethod = request.getParameter("paymentMethod");
        String notes = request.getParameter("notes");
        String itemsJson = request.getParameter("items");
        
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            sendJsonResponse(response, false, "Delivery address is required");
            return;
        }
        
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            sendJsonResponse(response, false, "Payment method is required");
            return;
        }
        
        if (itemsJson == null || itemsJson.trim().isEmpty()) {
            sendJsonResponse(response, false, "Order items are required");
            return;
        }
        
        try {
            // Parse items
            List<OrderItem> items = parseOrderItems(itemsJson);
            
            if (items.isEmpty()) {
                sendJsonResponse(response, false, "No valid items in order");
                return;
            }
            
            // 调用 OrderService 创建订单
            int orderId = orderService.createOrder(userId, deliveryAddress, paymentMethod, notes, items);
            
            if (orderId > 0) {
                sendJsonResponse(response, true, "Order created successfully (Order ID: " + orderId + ")");
            } else {
                sendJsonResponse(response, false, "Failed to create order");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error creating order: " + e.getMessage());
        }
    }
    
    /**
     * List user's orders
     */
    private void listUserOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            sendOrderListResponse(response, true, "Orders retrieved successfully", orders);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error retrieving orders: " + e.getMessage());
        }
    }
    
    /**
     * List all orders (Admin only)
     */
    private void listAllOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            List<Order> orders = orderService.getAllOrders();
            sendOrderListResponse(response, true, "All orders retrieved successfully", orders);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error retrieving orders: " + e.getMessage());
        }
    }
    
    /**
     * Get order details with items
     */
    private void getOrderDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String orderIdStr = request.getParameter("orderId");
        
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            sendJsonResponse(response, false, "Order ID is required");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(orderIdStr);
            HttpSession session = request.getSession();
            Integer userId = (Integer) session.getAttribute("userId");
            User user = (User) session.getAttribute("user");
            boolean isAdmin = (user != null && user.isAdmin());
            
            // 调用 OrderService 获取订单
            Order order = orderService.getOrderWithItems(orderId, userId, isAdmin);
            
            if (order == null) {
                sendJsonResponse(response, false, "Order not found or unauthorized");
                return;
            }
            
            // 获取订单项
            List<OrderItem> items = orderService.getOrderItems(orderId);
            
            sendOrderDetailsResponse(response, true, "Order details retrieved", order, items);
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid order ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error retrieving order: " + e.getMessage());
        }
    }
    
    /**
     * Cancel order
     */
    private void cancelOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String orderIdStr = request.getParameter("orderId");
        
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            sendJsonResponse(response, false, "Order ID is required");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(orderIdStr);
            HttpSession session = request.getSession();
            Integer userId = (Integer) session.getAttribute("userId");
            
            // 调用 OrderService 取消订单
            boolean cancelled = orderService.cancelOrder(orderId, userId);
            
            if (cancelled) {
                sendJsonResponse(response, true, "Order cancelled successfully");
            } else {
                sendJsonResponse(response, false, "Failed to cancel order");
            }
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid order ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error cancelling order: " + e.getMessage());
        }
    }
    
    /**
     * Update order status (Admin only)
     */
    private void updateOrderStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String orderIdStr = request.getParameter("orderId");
        String newStatus = request.getParameter("status");
        
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            sendJsonResponse(response, false, "Order ID is required");
            return;
        }
        
        if (newStatus == null || newStatus.trim().isEmpty()) {
            sendJsonResponse(response, false, "New status is required");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(orderIdStr);
            
            // 调用 OrderService 更新状态
            boolean updated = orderService.updateOrderStatus(orderId, newStatus);
            
            if (updated) {
                sendJsonResponse(response, true, "Order status updated successfully");
            } else {
                sendJsonResponse(response, false, "Failed to update order status");
            }
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid order ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error updating order status: " + e.getMessage());
        }
    }
    
    /**
     * Parse order items from JSON string (simplified)
     */
    private List<OrderItem> parseOrderItems(String itemsJson) {
        List<OrderItem> items = new ArrayList<>();
        
        itemsJson = itemsJson.trim();
        if (itemsJson.startsWith("[") && itemsJson.endsWith("]")) {
            itemsJson = itemsJson.substring(1, itemsJson.length() - 1);
            
            String[] itemStrings = itemsJson.split("\\},\\{");
            
            for (String itemStr : itemStrings) {
                itemStr = itemStr.replace("{", "").replace("}", "").trim();
                
                try {
                    int productId = 0;
                    int quantity = 0;
                    
                    String[] pairs = itemStr.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            String value = keyValue[1].trim().replace("\"", "");
                            
                            if ("productId".equals(key)) {
                                productId = Integer.parseInt(value);
                            } else if ("quantity".equals(key)) {
                                quantity = Integer.parseInt(value);
                            }
                        }
                    }
                    
                    if (productId > 0 && quantity > 0) {
                        OrderItem item = new OrderItem();
                        item.setProductId(productId);
                        item.setQuantity(quantity);
                        items.add(item);
                    }
                    
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        return items;
    }
    
    /**
     * Send JSON response for order list
     */
    private void sendOrderListResponse(HttpServletResponse response, boolean success, String message, List<Order> orders)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\",");
        json.append("\"count\":").append(orders.size()).append(",");
        json.append("\"orders\":[");
        
        for (int i = 0; i < orders.size(); i++) {
            json.append(orderToJson(orders.get(i)));
            if (i < orders.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        
        out.print(json.toString());
        out.flush();
    }
    
    /**
     * Send JSON response for order details with items
     */
    private void sendOrderDetailsResponse(HttpServletResponse response, boolean success, String message, 
                                         Order order, List<OrderItem> items) throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\",");
        json.append("\"order\":").append(orderToJson(order)).append(",");
        json.append("\"items\":[");
        
        for (int i = 0; i < items.size(); i++) {
            json.append(orderItemToJson(items.get(i)));
            if (i < items.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        
        out.print(json.toString());
        out.flush();
    }
    
    /**
     * Convert Order object to JSON string
     */
    private String orderToJson(Order order) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"orderId\":").append(order.getOrderId()).append(",");
        json.append("\"userId\":").append(order.getUserId()).append(",");
        json.append("\"orderDate\":\"").append(order.getOrderDate()).append("\",");
        json.append("\"totalAmount\":").append(order.getTotalAmount()).append(",");
        json.append("\"status\":\"").append(escapeJson(order.getStatus())).append("\",");
        json.append("\"statusDisplay\":\"").append(escapeJson(order.getStatusDisplay())).append("\",");
        json.append("\"deliveryAddress\":\"").append(escapeJson(order.getDeliveryAddress())).append("\",");
        json.append("\"paymentMethod\":\"").append(escapeJson(order.getPaymentMethod())).append("\",");
        json.append("\"paymentStatus\":\"").append(escapeJson(order.getPaymentStatus())).append("\",");
        json.append("\"notes\":\"").append(escapeJson(order.getNotes() != null ? order.getNotes() : "")).append("\",");
        json.append("\"canBeCancelled\":").append(order.canBeCancelled());
        json.append("}");
        return json.toString();
    }
    
    /**
     * Convert OrderItem object to JSON string
     */
    private String orderItemToJson(OrderItem item) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"orderItemId\":").append(item.getOrderItemId()).append(",");
        json.append("\"orderId\":").append(item.getOrderId()).append(",");
        json.append("\"productId\":").append(item.getProductId()).append(",");
        json.append("\"quantity\":").append(item.getQuantity()).append(",");
        json.append("\"unitPrice\":").append(item.getUnitPrice()).append(",");
        json.append("\"subtotal\":").append(item.getSubtotal());
        json.append("}");
        return json.toString();
    }
    
    /**
     * Send simple JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        out.print("{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}");
        out.flush();
    }
    
    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
