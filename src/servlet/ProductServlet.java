package servlet;

import model.Product;
import service.ProductService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Product Servlet
 * Handles product operations (list, add, update, delete)
 *
 * Supported Actions:
 * - list: Get all products
 * - get: Get product by ID
 * - add: Add new product (admin only)
 * - update: Update product (admin only)
 * - delete: Delete product (admin only)
 * - search: Search products by keyword
 */
@WebServlet("/ProductServlet")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
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

        switch (action) {
            case "list":
                handleListProducts(request, response);
                break;
            case "get":
                handleGetProduct(request, response);
                break;
            case "search":
                handleSearchProducts(request, response);
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

        // Check admin authentication for write operations
        if (!action.equals("list") && !action.equals("get") && !action.equals("search")) {
            if (!isAdmin(request)) {
                sendErrorResponse(response, "Unauthorized - Admin access required");
                return;
            }
        }

        switch (action) {
            case "add":
                handleAddProduct(request, response);
                break;
            case "update":
                handleUpdateProduct(request, response);
                break;
            case "delete":
                handleDeleteProduct(request, response);
                break;
            default:
                sendErrorResponse(response, "Invalid action: " + action);
        }
    }

    // ================================
    // List Products Handler
    // ================================

    private void handleListProducts(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String category = request.getParameter("category");
        String status = request.getParameter("status");

        List<Product> products;

        if (category != null && !category.isEmpty() && !category.equals("all")) {
            // Filter by category
            products = productService.getProductsByCategory(category);
        } else if (status != null && !status.isEmpty()) {
            // Filter by status
            products = productService.getProductsByStatus(status);
        } else {
            // Get all products
            products = productService.getAllProducts();
        }

        if (products != null) {
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("products", gson.toJsonTree(products));

            sendJsonResponse(response, jsonResponse);
        } else {
            sendErrorResponse(response, "Failed to retrieve products");
        }
    }

    // ================================
    // Get Product Handler
    // ================================

    private void handleGetProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String productIdStr = request.getParameter("productId");

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            sendErrorResponse(response, "Product ID is required");
            return;
        }

        try {
            int productId = Integer.parseInt(productIdStr);
            Product product = productService.getProductById(productId);

            if (product != null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("product", gson.toJsonTree(product));

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Product not found");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid product ID format");
        }
    }

    // ================================
    // Search Products Handler
    // ================================

    private void handleSearchProducts(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String keyword = request.getParameter("keyword");

        if (keyword == null || keyword.trim().isEmpty()) {
            sendErrorResponse(response, "Search keyword is required");
            return;
        }

        List<Product> products = productService.searchProducts(keyword);

        if (products != null) {
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("products", gson.toJsonTree(products));

            sendJsonResponse(response, jsonResponse);
        } else {
            sendErrorResponse(response, "Search failed");
        }
    }

    // ================================
    // Add Product Handler (Admin only)
    // ================================

    private void handleAddProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stock");
        String category = request.getParameter("category");
        String imageUrl = request.getParameter("imageUrl");
        String status = request.getParameter("status");

        // Validate required fields
        if (name == null || name.trim().isEmpty() ||
                priceStr == null || stockStr == null ||
                category == null || category.trim().isEmpty()) {
            sendErrorResponse(response, "Name, price, stock, and category are required");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            // Default values
            if (description == null) description = "";
            if (imageUrl == null) imageUrl = "";
            if (status == null || status.trim().isEmpty()) status = "available";

            // Create product
            Product product = productService.addProduct(name, description, price, stock,
                    category, imageUrl, status);

            if (product != null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Product added successfully");
                jsonResponse.add("product", gson.toJsonTree(product));

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Failed to add product");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid price or stock format");
        }
    }

    // ================================
    // Update Product Handler (Admin only)
    // ================================

    private void handleUpdateProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String productIdStr = request.getParameter("productId");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stock");
        String category = request.getParameter("category");
        String imageUrl = request.getParameter("imageUrl");
        String status = request.getParameter("status");

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            sendErrorResponse(response, "Product ID is required");
            return;
        }

        try {
            int productId = Integer.parseInt(productIdStr);

            // Get existing product
            Product product = productService.getProductById(productId);

            if (product == null) {
                sendErrorResponse(response, "Product not found");
                return;
            }

            // Update fields if provided
            if (name != null && !name.trim().isEmpty()) {
                product.setProductName(name);
            }
            if (description != null) {
                product.setDescription(description);
            }
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                product.setPrice(Double.parseDouble(priceStr));
            }
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                product.setStock(Integer.parseInt(stockStr));
            }
            if (category != null && !category.trim().isEmpty()) {
                product.setCategory(category);
            }
            if (imageUrl != null) {
                product.setImageUrl(imageUrl);
            }
            if (status != null && !status.trim().isEmpty()) {
                product.setStatus(status);
            }

            // Update product
            boolean success = productService.updateProduct(product);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Product updated successfully");
                jsonResponse.add("product", gson.toJsonTree(product));

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Failed to update product");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid number format");
        }
    }

    // ================================
    // Delete Product Handler (Admin only)
    // ================================

    private void handleDeleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String productIdStr = request.getParameter("productId");

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            sendErrorResponse(response, "Product ID is required");
            return;
        }

        try {
            int productId = Integer.parseInt(productIdStr);
            boolean success = productService.deleteProduct(productId);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Product deleted successfully");

                sendJsonResponse(response, jsonResponse);
            } else {
                sendErrorResponse(response, "Failed to delete product (may be referenced in orders)");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid product ID format");
        }
    }

    // ================================
    // Helper Methods
    // ================================

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