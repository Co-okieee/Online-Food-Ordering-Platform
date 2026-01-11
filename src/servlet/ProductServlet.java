package servlet;

import dao.ProductDAO;
import model.Product;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

/**
 * ProductServlet - 首屏，处理商品相关的前端请求
 * 
 * @author Your Name
 * @version 1.0
 */
@WebServlet("/ProductServlet")
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProductDAO productDAO;
    
    @Override
    public void init() throws ServletException {
        // 初始化 DAO（通过接口）
        productDAO = new dao.impl.ProductDAOImpl();
    }
    
    /**
     * Handle GET requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list"; // Default action: show all products
        }
        
        switch (action) {
            case "list":
                listAllProducts(request, response);
                break;
            case "category":
                listProductsByCategory(request, response);
                break;
            case "details":
                getProductDetails(request, response);
                break;
            case "search":
                searchProducts(request, response);
                break;
            default:
                listAllProducts(request, response);
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
        
        // ② 确认 Admin 判断
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login first");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        switch (action) {
            case "add":
                addProduct(request, response);
                break;
            case "update":
                updateProduct(request, response);
                break;
            case "delete":
                deleteProduct(request, response);
                break;
            default:
                sendJsonResponse(response, false, "Invalid action");
                break;
        }
    }
    
    /**
     * List all available products
     */
    private void listAllProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            List<Product> products = productDAO.getAvailableProducts();
            sendProductListResponse(response, true, "Products retrieved successfully", products);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error retrieving products: " + e.getMessage());
        }
    }
    
    /**
     * List products by category
     */
    private void listProductsByCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String category = request.getParameter("category");
        
        if (category == null || category.trim().isEmpty()) {
            sendJsonResponse(response, false, "Category parameter is required");
            return;
        }
        
        try {
            List<Product> products = productDAO.getProductsByCategory(category);
            sendProductListResponse(response, true, "Products retrieved successfully", products);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error retrieving products: " + e.getMessage());
        }
    }
    
    /**
     * Get product details by ID
     */
    private void getProductDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String productIdStr = request.getParameter("productId");
        
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            sendJsonResponse(response, false, "Product ID is required");
            return;
        }
        
        try {
            int productId = Integer.parseInt(productIdStr);
            Product product = productDAO.getProductById(productId);
            
            if (product != null) {
                sendProductResponse(response, true, "Product found", product);
            } else {
                sendJsonResponse(response, false, "Product not found");
            }
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid product ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error retrieving product: " + e.getMessage());
        }
    }
    
    /**
     * Search products by keyword
     */
    private void searchProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        
        if (keyword == null || keyword.trim().isEmpty()) {
            sendJsonResponse(response, false, "Search keyword is required");
            return;
        }
        
        try {
            List<Product> products = productDAO.searchProducts(keyword);
            sendProductListResponse(response, true, products.size() + " products found", products);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error searching products: " + e.getMessage());
        }
    }
    
    /**
     * Add new product (Admin only)
     */
    private void addProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String productName = request.getParameter("productName");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String stockStr = request.getParameter("stock");
            String category = request.getParameter("category");
            String imageUrl = request.getParameter("imageUrl");
            
            if (productName == null || productName.trim().isEmpty() ||
                priceStr == null || stockStr == null || category == null) {
                sendJsonResponse(response, false, "All required fields must be filled");
                return;
            }
            
            BigDecimal price = new BigDecimal(priceStr);
            int stock = Integer.parseInt(stockStr);
            
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                sendJsonResponse(response, false, "Price must be greater than 0");
                return;
            }
            
            if (stock < 0) {
                sendJsonResponse(response, false, "Stock cannot be negative");
                return;
            }
            
            Product product = new Product(productName, description, price, stock, category);
            product.setImageUrl(imageUrl);
            product.setStatus("available");
            
            int productId = productDAO.insertProduct(product);
            
            if (productId > 0) {
                sendJsonResponse(response, true, "Product added successfully (ID: " + productId + ")");
            } else {
                sendJsonResponse(response, false, "Failed to add product");
            }
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid number format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error adding product: " + e.getMessage());
        }
    }
    
    /**
     * Update product (Admin only)
     */
    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String productIdStr = request.getParameter("productId");
            
            if (productIdStr == null) {
                sendJsonResponse(response, false, "Product ID is required");
                return;
            }
            
            int productId = Integer.parseInt(productIdStr);
            Product product = productDAO.getProductById(productId);
            
            if (product == null) {
                sendJsonResponse(response, false, "Product not found");
                return;
            }
            
            // Update fields if provided
            String productName = request.getParameter("productName");
            if (productName != null && !productName.trim().isEmpty()) {
                product.setProductName(productName);
            }
            
            String description = request.getParameter("description");
            if (description != null) {
                product.setDescription(description);
            }
            
            String priceStr = request.getParameter("price");
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                product.setPrice(new BigDecimal(priceStr));
            }
            
            String stockStr = request.getParameter("stock");
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                product.setStock(Integer.parseInt(stockStr));
            }
            
            String category = request.getParameter("category");
            if (category != null && !category.trim().isEmpty()) {
                product.setCategory(category);
            }
            
            String imageUrl = request.getParameter("imageUrl");
            if (imageUrl != null) {
                product.setImageUrl(imageUrl);
            }
            
            String status = request.getParameter("status");
            if (status != null && !status.trim().isEmpty()) {
                product.setStatus(status);
            }
            
            boolean updated = productDAO.updateProduct(product);
            
            if (updated) {
                sendJsonResponse(response, true, "Product updated successfully");
            } else {
                sendJsonResponse(response, false, "Failed to update product");
            }
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid number format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error updating product: " + e.getMessage());
        }
    }
    
    /**
     * Delete product (Admin only)
     */
    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String productIdStr = request.getParameter("productId");
        
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            sendJsonResponse(response, false, "Product ID is required");
            return;
        }
        
        try {
            int productId = Integer.parseInt(productIdStr);
            boolean deleted = productDAO.deleteProduct(productId);
            
            if (deleted) {
                sendJsonResponse(response, true, "Product deleted successfully");
            } else {
                sendJsonResponse(response, false, "Product not found");
            }
            
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "Invalid product ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "Error deleting product: " + e.getMessage());
        }
    }
    
    /**
     * Send JSON response for single product
     */
    private void sendProductResponse(HttpServletResponse response, boolean success, String message, Product product)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\"");
        
        if (product != null) {
            json.append(",\"product\":").append(productToJson(product));
        }
        
        json.append("}");
        
        out.print(json.toString());
        out.flush();
    }
    
    /**
     * Send JSON response for product list
     */
    private void sendProductListResponse(HttpServletResponse response, boolean success, String message, List<Product> products)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\",");
        json.append("\"count\":").append(products.size()).append(",");
        json.append("\"products\":[");
        
        for (int i = 0; i < products.size(); i++) {
            json.append(productToJson(products.get(i)));
            if (i < products.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        
        out.print(json.toString());
        out.flush();
    }
    
    /**
     * Convert Product object to JSON string
     */
    private String productToJson(Product product) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"productId\":").append(product.getProductId()).append(",");
        json.append("\"productName\":\"").append(escapeJson(product.getProductName())).append("\",");
        json.append("\"description\":\"").append(escapeJson(product.getDescription() != null ? product.getDescription() : "")).append("\",");
        json.append("\"price\":").append(product.getPrice()).append(",");
        json.append("\"stock\":").append(product.getStock()).append(",");
        json.append("\"category\":\"").append(escapeJson(product.getCategory())).append("\",");
        json.append("\"imageUrl\":\"").append(escapeJson(product.getImageUrl() != null ? product.getImageUrl() : "")).append("\",");
        json.append("\"status\":\"").append(escapeJson(product.getStatus())).append("\",");
        json.append("\"isAvailable\":").append(product.isAvailable()).append(",");
        json.append("\"isInStock\":").append(product.isInStock()).append(",");
        json.append("\"isLowStock\":").append(product.isLowStock());
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
