package servlet;

import model.Order;
import model.OrderItem;
import model.User;
import service.OrderService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Servlet
 * Handles order operations (create, list, update status, cancel)
 *
 * Supported Actions:
 * - create: Create new order
 * - list: Get user's orders
 * - listAll: Get all orders (admin only)
 * - get: Get order by ID
 * - updateStatus: Update order status (admin only)
 * - cancel: Cancel order
 */
@WebServlet("/OrderServlet")
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        gson = new Gson();
    }

    // ================================
    // GET Request Handler
    // ================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            sendErrorResponse(response, "Action parameter is required");
            return;
        }

        if ("list".equals(action) || "getUserOrders".equals(action)) {
            getUserOrders(request, response);
            return;
        }

        if (!isLoggedIn(request)) {
            sendErrorResponse(response, "Unauthorized - Please login");
            return;
        }

        switch (action) {
            case "listAll":
                getAllOrders(request, response);
                break;
            case "get":
                handleGetOrder(request, response);
                break;
            default:
                sendErrorResponse(response, "Invalid action: " + action);
        }
    }

    // ================================
    // POST Request Handler
    // ================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            sendErrorResponse(response, "Action parameter is required");
            return;
        }

        // Check authentication
        if (!isLoggedIn(request)) {
            sendErrorResponse(response, "Unauthorized - Please login");
            return;
        }

        switch (action) {
            case "create":
                handleCreateOrder(request, response);
                break;
            case "updateStatus":
                handleUpdateOrderStatus(request, response);
                break;
            case "cancel":
                handleCancelOrder(request, response);
                break;
            default:
                sendErrorResponse(response, "Invalid action: " + action);
        }
    }

    // ================================
    // Create Order Handler
    // ================================

    private void handleCreateOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        // Read JSON from request body
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String jsonString = sb.toString();

        if (jsonString == null || jsonString.trim().isEmpty()) {
            sendErrorResponse(response, "Request body is empty");
            return;
        }

        try {
            // Parse JSON
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            String deliveryAddress = jsonObject.get("deliveryAddress").getAsString();
            String paymentMethod = jsonObject.get("paymentMethod").getAsString();
            double totalAmount = jsonObject.get("totalAmount").getAsDouble();
            String notes = jsonObject.has("notes") ? jsonObject.get("notes").getAsString() : "";

            JsonArray itemsArray = jsonObject.getAsJsonArray("items");

            // Validate
            if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
                sendErrorResponse(response, "Delivery address is required");
                return;
            }

            if (itemsArray == null || itemsArray.size() == 0) {
                sendErrorResponse(response, "Order must contain at least one item");
                return;
            }

            // Create Order object
            Order order = new Order(userId, totalAmount, deliveryAddress, paymentMethod, notes);

            // Create OrderItem objects
            List<OrderItem> orderItems = new ArrayList<>();

            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject itemObj = itemsArray.get(i).getAsJsonObject();

                int productId = itemObj.get("productId").getAsInt();
                int quantity = itemObj.get("quantity").getAsInt();
                double unitPrice = itemObj.get("unitPrice").getAsDouble();

                OrderItem item = new OrderItem(0, productId, quantity, unitPrice);
                orderItems.add(item);
            }

            // Create order
            Order createdOrder = orderService.createOrder(order, orderItems);

            if (createdOrder != null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Order placed successfully");
                jsonResponse.addProperty("orderId", createdOrder.getOrderId());
                jsonResponse.add("order", gson.toJsonTree(createdOrder));

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Failed to create order - Check stock availability");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "Error processing order: " + e.getMessage());
        }
    }

    // ================================
    // get User Orders
    // ================================
    private void getUserOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("getUserOrders called");
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                sendErrorResponse(response, "Please login to view orders");
                return;
            }

            User user = (User) session.getAttribute("user");
            int userId = user.getUserId();

            List<Order> orders = orderService.getOrdersByUserId(userId);

            for (Order order : orders) {
                List<OrderItem> items = orderService.getOrderItemsByOrderId(order.getOrderId());
                order.setOrderItems(items);
            }

            JsonArray ordersArray = new JsonArray();
            for (Order order : orders) {
                JsonObject orderJson = new JsonObject();
                orderJson.addProperty("orderId", order.getOrderId());
                orderJson.addProperty("userId", order.getUserId());
                orderJson.addProperty("orderDate", order.getOrderDate().toString());
                orderJson.addProperty("totalAmount", order.getTotalAmount());
                orderJson.addProperty("status", order.getStatus());
                orderJson.addProperty("deliveryAddress", order.getDeliveryAddress());
                orderJson.addProperty("paymentMethod", order.getPaymentMethod());
                orderJson.addProperty("paymentStatus", order.getPaymentStatus());
                orderJson.addProperty("notes", order.getNotes() != null ? order.getNotes() : "");

                JsonArray itemsArray = new JsonArray();
                for (OrderItem item : order.getOrderItems()) {
                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("orderItemId", item.getOrderItemId());
                    itemJson.addProperty("productId", item.getProductId());
                    itemJson.addProperty("productName", item.getProductName());
                    itemJson.addProperty("quantity", item.getQuantity());
                    itemJson.addProperty("unitPrice", item.getUnitPrice());
                    itemJson.addProperty("subtotal", item.getSubtotal());
                    itemJson.addProperty("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
                    itemJson.addProperty("category", item.getCategory() != null ? item.getCategory() : "");
                    itemsArray.add(itemJson);
                }
                orderJson.add("items", itemsArray);

                ordersArray.add(orderJson);
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("orders", ordersArray);

            sendJsonResponse(response, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "Error loading orders: " + e.getMessage());
        }
    }

    // ================================
    // get All Orders
    // ================================
    private void getAllOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                sendErrorResponse(response, "Please login to view orders");
                return;
            }

            User user = (User) session.getAttribute("user");
            if (!"admin".equals(user.getRole())) {
                sendErrorResponse(response, "Unauthorized - Admin access required");
                return;
            }

            List<Order> orders = orderService.getAllOrders();

            for (Order order : orders) {
                List<OrderItem> items = orderService.getOrderItemsByOrderId(order.getOrderId());
                order.setOrderItems(items);
            }

            JsonArray ordersArray = new JsonArray();
            for (Order order : orders) {
                JsonObject orderJson = new JsonObject();
                orderJson.addProperty("orderId", order.getOrderId());
                orderJson.addProperty("userId", order.getUserId());
                orderJson.addProperty("username", order.getUsername());
                orderJson.addProperty("orderDate", order.getOrderDate().toString());
                orderJson.addProperty("totalAmount", order.getTotalAmount());
                orderJson.addProperty("status", order.getStatus());
                orderJson.addProperty("deliveryAddress", order.getDeliveryAddress());
                orderJson.addProperty("paymentMethod", order.getPaymentMethod());
                orderJson.addProperty("paymentStatus", order.getPaymentStatus());
                orderJson.addProperty("notes", order.getNotes() != null ? order.getNotes() : "");

                JsonArray itemsArray = new JsonArray();
                for (OrderItem item : order.getOrderItems()) {
                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("orderItemId", item.getOrderItemId());
                    itemJson.addProperty("productId", item.getProductId());
                    itemJson.addProperty("productName", item.getProductName());
                    itemJson.addProperty("quantity", item.getQuantity());
                    itemJson.addProperty("unitPrice", item.getUnitPrice());
                    itemJson.addProperty("subtotal", item.getSubtotal());
                    itemJson.addProperty("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
                    itemJson.addProperty("category", item.getCategory() != null ? item.getCategory() : "");
                    itemsArray.add(itemJson);
                }
                orderJson.add("items", itemsArray);

                ordersArray.add(orderJson);
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("orders", ordersArray);

            sendJsonResponse(response, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "Error loading orders: " + e.getMessage());
        }
    }

    // ================================
    // Get Order Handler
    // ================================

    private void handleGetOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String orderIdStr = request.getParameter("orderId");

        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            sendErrorResponse(response, "Order ID is required");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            Order order = orderService.getOrderById(orderId);

            if (order != null) {
                // Check authorization - user can only view their own orders
                HttpSession session = request.getSession(false);
                int userId = (Integer) session.getAttribute("userId");

                if (order.getUserId() != userId && !isAdmin(request)) {
                    sendErrorResponse(response, "Unauthorized - Cannot view other user's orders");
                    return;
                }

                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("order", gson.toJsonTree(order));

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Order not found");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid order ID format");
        }
    }

    // ================================
    // Update Order Status Handler (Admin only)
    // ================================

    private void handleUpdateOrderStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Check admin permission
        if (!isAdmin(request)) {
            sendErrorResponse(response, "Unauthorized - Admin access required");
            return;
        }

        String orderIdStr = request.getParameter("orderId");
        String newStatus = request.getParameter("status");

        if (orderIdStr == null || orderIdStr.trim().isEmpty() ||
                newStatus == null || newStatus.trim().isEmpty()) {
            sendErrorResponse(response, "Order ID and status are required");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            boolean success = orderService.updateOrderStatus(orderId, newStatus);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Order status updated successfully");

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Failed to update order status");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid order ID format");
        }
    }

    // ================================
    // Cancel Order Handler
    // ================================

    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String orderIdStr = request.getParameter("orderId");

        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            sendErrorResponse(response, "Order ID is required");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);

            // Check authorization
            Order order = orderService.getOrderById(orderId);

            if (order == null) {
                sendErrorResponse(response, "Order not found");
                return;
            }

            HttpSession session = request.getSession(false);
            int userId = (Integer) session.getAttribute("userId");

            if (order.getUserId() != userId && !isAdmin(request)) {
                sendErrorResponse(response, "Unauthorized - Cannot cancel other user's orders");
                return;
            }

            // Cancel order
            boolean success = orderService.cancelOrder(orderId);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Order cancelled successfully");

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Failed to cancel order");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid order ID format");
        }
    }

    // ================================
    // Helper Methods
    // ================================

    /**
     * Check if user is logged in
     */
    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("role") != null) {
            String role = (String) session.getAttribute("role");
            return "admin".equalsIgnoreCase(role);
        }

        return false;
    }

    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, JsonObject jsonObject)
            throws IOException {
        PrintWriter out = response.getWriter();
        out.print(jsonObject.toString());
        out.flush();
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, String message)
            throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", message);

        sendJsonResponse(response, jsonResponse);
    }
}